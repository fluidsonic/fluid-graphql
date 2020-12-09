package io.fluidsonic.graphql


@InternalGraphqlApi
public open class VisitorContext(
	public val document: GDocument,
	public val schema: GSchema,
	private val fieldDefinition: (name: String, parentType: GNamedType) -> GFieldDefinition? =
		{ name, parentType -> (parentType as? GNode.WithFieldDefinitions)?.fieldDefinition(name) },
) {

	private var visitingNode: GNode? = null

	public var parentNode: GNode? = null
		private set

	public var relatedArgumentDefinition: GArgumentDefinition? = null
		private set

	public var relatedDirective: GDirective? = null
		private set

	public var relatedDirectiveDefinition: GDirectiveDefinition? = null
		private set

	public var relatedFieldDefinition: GFieldDefinition? = null
		private set

	public var relatedFieldSelection: GFieldSelection? = null
		private set

	public var relatedFragmentDefinition: GFragmentDefinition? = null
		private set

	public var relatedOperationDefinition: GOperationDefinition? = null
		private set

	public var relatedParentSelectionSet: GSelectionSet? = null
		private set

	public var relatedParentType: GType? = null
		private set

	public var relatedSelection: GSelection? = null
		private set

	public var relatedSelectionSet: GSelectionSet? = null
		private set

	public var relatedType: GType? = null
		private set


	internal inline fun <Result> with(node: GNode, block: () -> Result): Result {
		val _parentNode = parentNode
		parentNode = visitingNode

		val _visitingNode = visitingNode
		visitingNode = node

		val _relatedArgumentDefinition = relatedArgumentDefinition
		val _relatedDirective = relatedDirective
		val _relatedDirectiveDefinition = relatedDirectiveDefinition
		val _relatedFieldDefinition = relatedFieldDefinition
		val _relatedFieldSelection = relatedFieldSelection
		val _relatedFragmentDefinition = relatedFragmentDefinition
		val _relatedOperationDefinition = relatedOperationDefinition
		val _relatedParentSelectionSet = relatedParentSelectionSet
		val _relatedParentType = relatedParentType
		val _relatedSelection = relatedSelection
		val _relatedType = relatedType

		when (node) {
			is GArgument -> {
				val underlyingRelatedType = relatedType?.underlyingNamedType

				relatedArgumentDefinition = when {
					relatedDirective != null -> relatedDirectiveDefinition?.argumentDefinition(node.name)
					underlyingRelatedType is GInputObjectType -> underlyingRelatedType.argumentDefinition(node.name)
					parentNode is GFieldSelection -> relatedFieldDefinition?.argumentDefinition(node.name)
					else -> null
				}
				relatedFieldDefinition
				relatedParentType = when {
					relatedDirective != null -> null
					underlyingRelatedType is GInputObjectType -> underlyingRelatedType
					parentNode is GFieldSelection -> relatedParentType
					else -> relatedType
				}
				relatedType = relatedArgumentDefinition?.let { schema.resolveType(it.type) }
			}

			is GArgumentDefinition -> {
				relatedArgumentDefinition = node
				relatedParentType = relatedType
				relatedType = schema.resolveType(node.type)
			}

			is GDirective -> {
				relatedDirective = node
				relatedDirectiveDefinition = schema.directiveDefinition(node.name)
				relatedType = null
			}

			is GDirectiveDefinition ->
				relatedDirectiveDefinition = node

			is GFieldDefinition -> {
				relatedFieldDefinition = node
				relatedType = relatedFieldDefinition?.type?.let { schema.resolveType(it) }
			}

			is GFieldSelection -> {
				relatedFieldDefinition = (_relatedParentType as? GNamedType)?.let { fieldDefinition(node.name, it) }
				relatedFieldSelection = node
				relatedSelection = node
				relatedType = relatedFieldDefinition
					?.type
					?.let { schema.resolveType(it) }
			}

			is GFragmentDefinition -> {
				relatedFragmentDefinition = node
				relatedType = schema.resolveType(node.typeCondition)
				relatedParentType = relatedType
			}

			is GFragmentSelection -> {
				relatedFragmentDefinition = document.fragment(node.name)
				relatedSelection = node
				relatedType = relatedFragmentDefinition?.let { schema.resolveType(it.typeCondition) }
			}

			is GInlineFragmentSelection -> {
				relatedFragmentDefinition = null
				relatedSelection = node
				relatedType = when (val typeCondition = node.typeCondition) {
					null -> relatedParentType
					else -> schema.resolveType(typeCondition)
				}
			}

			is GNamedType -> {
				relatedParentType = null
				relatedType = node
			}

			is GOperationDefinition -> {
				relatedOperationDefinition = node
				relatedType = schema.rootTypeForOperationType(node.type)
			}

			is GSelectionSet -> {
				relatedParentType = _relatedType?.underlyingNamedType
				relatedParentSelectionSet = relatedSelectionSet
				relatedSelectionSet = node
				relatedType = null
			}

			is GVariableDefinition ->
				relatedType = schema.resolveType(node.type)

			else ->
				Unit
		}

		try {
			return block()
		}
		finally {
			parentNode = _parentNode
			visitingNode = _visitingNode

			relatedArgumentDefinition = _relatedArgumentDefinition
			relatedDirective = _relatedDirective
			relatedDirectiveDefinition = _relatedDirectiveDefinition
			relatedFieldDefinition = _relatedFieldDefinition
			relatedFieldSelection = _relatedFieldSelection
			relatedFragmentDefinition = _relatedFragmentDefinition
			relatedOperationDefinition = _relatedOperationDefinition
			relatedParentSelectionSet = _relatedParentSelectionSet
			relatedParentType = _relatedParentType
			relatedSelection = _relatedSelection
			relatedType = _relatedType
		}
	}
}
