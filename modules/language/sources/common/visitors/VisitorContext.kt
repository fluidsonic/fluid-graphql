package io.fluidsonic.graphql


internal open class VisitorContext(
	val document: GDocument,
	val schema: GSchema
) {

	private var visitingNode: GNode? = null

	var parentNode: GNode? = null
		private set

	var relatedArgumentDefinition: GArgumentDefinition? = null
		private set

	var relatedDirective: GDirective? = null
		private set

	var relatedDirectiveDefinition: GDirectiveDefinition? = null
		private set

	var relatedFieldDefinition: GFieldDefinition? = null
		private set

	var relatedFieldSelection: GFieldSelection? = null
		private set

	var relatedFragmentDefinition: GFragmentDefinition? = null
		private set

	var relatedOperationDefinition: GOperationDefinition? = null
		private set

	var relatedParentSelectionSet: GSelectionSet? = null
		private set

	var relatedParentType: GType? = null
		private set

	var relatedSelection: GSelection? = null
		private set

	var relatedSelectionSet: GSelectionSet? = null
		private set

	var relatedType: GType? = null
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
				relatedArgumentDefinition = when {
					relatedDirective !== null -> relatedDirectiveDefinition?.argumentDefinition(node.name)
					relatedType is GInputObjectType -> (relatedType as GInputObjectType).argumentDefinition(node.name)
					relatedSelection is GFieldSelection -> relatedFieldDefinition?.argumentDefinition(node.name)
					else -> null
				}
				relatedParentType = relatedType
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
			}

			is GDirectiveDefinition ->
				relatedDirectiveDefinition = node

			is GFieldDefinition -> {
				relatedFieldDefinition = node
				relatedType = relatedFieldDefinition?.type?.let { schema.resolveType(it) }
			}

			is GFieldSelection -> {
				relatedFieldDefinition = _relatedType?.let { (relatedType as? GNode.WithFieldDefinitions)?.field(node.name) }
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
				relatedParentType = relatedType
				relatedSelection = node
				relatedType = relatedFragmentDefinition?.let { schema.resolveType(it.typeCondition) }
			}

			is GInlineFragmentSelection -> {
				relatedFragmentDefinition = null
				relatedSelection = node

				val typeCondition = node.typeCondition
				if (typeCondition !== null) {
					relatedParentType = relatedType
					relatedType = schema.resolveType(typeCondition)
				}
			}

			is GNamedType -> {
				relatedParentType = null
				relatedType = node
			}

			is GOperationDefinition -> {
				relatedOperationDefinition = node
				relatedType = schema.rootTypeForOperationType(node.type)
				relatedParentType = relatedType
			}

			is GSelectionSet -> {
				relatedParentSelectionSet = relatedSelectionSet
				relatedSelectionSet = node
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
