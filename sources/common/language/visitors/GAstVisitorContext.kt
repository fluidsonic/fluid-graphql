package io.fluidsonic.graphql


open class GAstVisitorContext(
	val document: GDocument,
	val schema: GSchema
) {

	private var visitingNode: GAst? = null

	var relatedFieldDefinition: GFieldDefinition? = null
		private set

	var relatedFragmentDefinition: GFragmentDefinition? = null
		private set

	var relatedOperationDefinition: GOperationDefinition? = null
		private set

	var relatedParentSelectionSet: GSelectionSet? = null
		private set

	var relatedParentType: GType? = null
		private set

	var relatedSelectionSet: GSelectionSet? = null
		private set

	var relatedType: GType? = null
		private set


	internal inline fun <Result> with(node: GAst, block: () -> Result): Result {
		val _visitingNode = this.visitingNode
		visitingNode = node

		val _relatedFieldDefinition = relatedFieldDefinition
		val _relatedFragmentDefinition = relatedFragmentDefinition
		val _relatedOperationDefinition = relatedOperationDefinition
		val _relatedParentSelectionSet = relatedParentSelectionSet
		val _relatedParentType = relatedParentType
		val _relatedType = relatedType

		when (node) {
			is GFieldDefinition -> {
				relatedFieldDefinition = node
				relatedType = relatedFieldDefinition?.type?.let { schema.resolveType(it) }
			}

			is GFieldSelection -> {
				relatedFieldDefinition = _relatedType?.let { schema.getFieldDefinition(type = it, name = node.name) }
				relatedType = relatedFieldDefinition
					?.type
					?.let { schema.resolveType(it) }
			}

			is GFragmentDefinition -> {
				relatedFragmentDefinition = node
				relatedType = schema.resolveType(node.typeCondition)
				relatedParentType = relatedType
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
		}

		try {
			return block()
		}
		finally {
			visitingNode = _visitingNode

			relatedFieldDefinition = _relatedFieldDefinition
			relatedFragmentDefinition = _relatedFragmentDefinition
			relatedOperationDefinition = _relatedOperationDefinition
			relatedParentSelectionSet = _relatedParentSelectionSet
			relatedParentType = _relatedParentType
			relatedType = _relatedType
		}
	}
}
