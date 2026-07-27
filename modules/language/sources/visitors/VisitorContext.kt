package io.fluidsonic.graphql

@InternalGraphqlApi
public open class VisitorContext(
	public val document: GDocument,
	public val schema: GSchema,
	private val fieldDefinition: (name: String, parentType: GNamedType) -> GFieldDefinition? =
		{ name, parentType -> (parentType as? GNode.WithFieldDefinitions)?.fieldDefinition(name) },
) {

	// All traversal state lives in one holder so that [leave] can restore it by reassigning a single reference.
	// It used to save and restore each field individually, and `relatedSelectionSet` was left out of both lists —
	// it leaked out of the first selection set visited and every later sibling saw it as its parent.
	//
	// Internal rather than private only because [with] is an internal inline function, which cannot reference a
	// private type. Treat both this and [State] as private to this class.
	internal var state: State = State()

	// The state of every node the traversal has entered but not yet left, outermost first. It lives on the heap so
	// that a traversal can be arbitrarily deep without holding one stack frame per level.
	private val stateStack = mutableListOf<State>()

	public val parentNode: GNode? get() = state.parentNode
	public val relatedArgumentDefinition: GArgumentDefinition? get() = state.relatedArgumentDefinition
	public val relatedDirective: GDirective? get() = state.relatedDirective
	public val relatedDirectiveDefinition: GDirectiveDefinition? get() = state.relatedDirectiveDefinition
	public val relatedFieldDefinition: GFieldDefinition? get() = state.relatedFieldDefinition
	public val relatedFieldSelection: GFieldSelection? get() = state.relatedFieldSelection
	public val relatedFragmentDefinition: GFragmentDefinition? get() = state.relatedFragmentDefinition
	public val relatedOperationDefinition: GOperationDefinition? get() = state.relatedOperationDefinition
	public val relatedParentSelectionSet: GSelectionSet? get() = state.relatedParentSelectionSet
	public val relatedParentType: GType? get() = state.relatedParentType
	public val relatedSelection: GSelection? get() = state.relatedSelection
	public val relatedSelectionSet: GSelectionSet? get() = state.relatedSelectionSet
	public val relatedType: GType? get() = state.relatedType

	/**
	 * Advances the context to [node], which must be a child of the node last entered, or the root if none was.
	 *
	 * Every [enter] must be paired with a [leave], which restores the state of the enclosing node.
	 */
	internal fun enter(node: GNode) {
		val previousState = state
		stateStack += previousState

		// Everything the three `apply…` calls below read is still the enclosing node's state until that field is
		// assigned, which is what lets them derive the new state from the old one.
		val state = previousState.copy(parentNode = previousState.visitingNode, visitingNode = node)
		this.state = state

		applyTypeState(state, node)
		applyDirectiveState(state, node)
		applySelectionState(state, node)
	}

	/** Restores the state of the node enclosing the one last entered. Must be paired with an [enter]. */
	internal fun leave() {
		check(stateStack.isNotEmpty()) { "Cannot leave a node that was never entered." }

		state = stateStack.removeAt(stateStack.size - 1)
	}

	internal inline fun <Result> with(node: GNode, block: () -> Result): Result {
		enter(node)

		try {
			return block()
		} finally {
			leave()
		}
	}

	// The three `apply…` functions below are one dispatch over every node type that contributes state, split over
	// three disjoint sets of node types so that no single `when` grows unreadable. Since the sets are disjoint, at
	// most one of the three matches a given node and their order does not matter.
	private fun applyTypeState(state: State, node: GNode) {
		when (node) {
			is GArgument -> {
				val underlyingRelatedType = state.relatedType?.underlyingNamedType

				state.relatedArgumentDefinition = when {
					state.parentNode is GDirective -> state.relatedDirectiveDefinition?.argumentDefinition(node.name)
					underlyingRelatedType is GInputObjectType -> underlyingRelatedType.argumentDefinition(node.name)
					state.parentNode is GFieldSelection -> state.relatedFieldDefinition?.argumentDefinition(node.name)
					else -> null
				}
				state.relatedParentType = when {
					state.parentNode is GDirective -> null
					underlyingRelatedType is GInputObjectType -> underlyingRelatedType
					state.parentNode is GFieldSelection -> state.relatedParentType
					else -> state.relatedType
				}
				state.relatedType = state.relatedArgumentDefinition?.let { schema.resolveType(it.type) }
			}

			is GArgumentDefinition -> {
				state.relatedArgumentDefinition = node
				state.relatedParentType = state.relatedType
				state.relatedType = schema.resolveType(node.type)
			}

			is GFieldDefinition -> {
				state.relatedFieldDefinition = node
				state.relatedType = node.type.let { schema.resolveType(it) }
			}

			is GNamedType -> {
				state.relatedParentType = null
				state.relatedType = node
			}

			is GVariableDefinition ->
				state.relatedType = schema.resolveType(node.type)

			else ->
				Unit
		}
	}

	private fun applyDirectiveState(state: State, node: GNode) {
		when (node) {
			is GDirective -> {
				state.relatedDirective = node
				state.relatedDirectiveDefinition = schema.directiveDefinition(node.name)
				state.relatedType = null
			}

			is GDirectiveDefinition ->
				state.relatedDirectiveDefinition = node

			else ->
				Unit
		}
	}

	private fun applySelectionState(state: State, node: GNode) {
		when (node) {
			is GFieldSelection -> {
				state.relatedFieldDefinition = (state.relatedParentType as? GNamedType)?.let { fieldDefinition(node.name, it) }
				state.relatedFieldSelection = node
				state.relatedSelection = node
				state.relatedType = state.relatedFieldDefinition
					?.type
					?.let { schema.resolveType(it) }
			}

			is GFragmentDefinition -> {
				state.relatedFragmentDefinition = node
				state.relatedType = schema.resolveType(node.typeCondition)
				state.relatedParentType = state.relatedType
			}

			is GFragmentSelection -> {
				state.relatedFragmentDefinition = document.fragment(node.name)
				state.relatedSelection = node
				state.relatedType = state.relatedFragmentDefinition?.let { schema.resolveType(it.typeCondition) }
			}

			is GInlineFragmentSelection -> {
				state.relatedFragmentDefinition = null
				state.relatedSelection = node
				state.relatedType = when (val typeCondition = node.typeCondition) {
					null -> state.relatedParentType
					else -> schema.resolveType(typeCondition)
				}
			}

			is GOperationDefinition -> {
				state.relatedOperationDefinition = node
				state.relatedType = schema.rootTypeForOperationType(node.type)
			}

			is GSelectionSet -> {
				state.relatedParentType = state.relatedType?.underlyingNamedType
				state.relatedParentSelectionSet = state.relatedSelectionSet
				state.relatedSelectionSet = node
				state.relatedType = null
			}

			else ->
				Unit
		}
	}

	/**
	 * The traversal state for one node.
	 *
	 * Every mutable field of the context lives here so that entering a node is a [copy] and leaving it is a single
	 * reassignment. Adding a field here therefore cannot go on to be forgotten by a restore.
	 *
	 * Internal rather than private only so that the internal inline [with] can reference it.
	 */
	internal data class State(
		var parentNode: GNode? = null,
		var relatedArgumentDefinition: GArgumentDefinition? = null,
		var relatedDirective: GDirective? = null,
		var relatedDirectiveDefinition: GDirectiveDefinition? = null,
		var relatedFieldDefinition: GFieldDefinition? = null,
		var relatedFieldSelection: GFieldSelection? = null,
		var relatedFragmentDefinition: GFragmentDefinition? = null,
		var relatedOperationDefinition: GOperationDefinition? = null,
		var relatedParentSelectionSet: GSelectionSet? = null,
		var relatedParentType: GType? = null,
		var relatedSelection: GSelection? = null,
		var relatedSelectionSet: GSelectionSet? = null,
		var relatedType: GType? = null,
		var visitingNode: GNode? = null,
	)
}
