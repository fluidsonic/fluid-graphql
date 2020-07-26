package io.fluidsonic.graphql


// https://graphql.github.io/graphql-spec/draft/#sec-Fragment-spread-target-defined
@Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")
internal object FragmentSelectionPossibilityRule : ValidationRule.Singleton() {

	override fun onFragmentSelection(selection: GFragmentSelection, data: ValidationContext, visit: Visit) {
		val definition = data.relatedFragmentDefinition
			?: return // Cannot validate nonexistent fragment.

		val parentType = data.relatedParentType as? GCompositeType
			?: return // Cannot validate nonexistent or invalid type.

		val type = data.relatedType as? GCompositeType
			?: return // Cannot validate nonexistent or invalid type.

		val possibleParentTypes = data.schema.getPossibleTypes(parentType)
		val possibleTypes = data.schema.getPossibleTypes(type)
		val intersectionTypes = possibleTypes.intersect(possibleParentTypes)
		if (intersectionTypes.isNotEmpty())
			return // Fragment type condition can match.

		data.reportError(
			message = "Fragment '${selection.name}' on '${type.name}' will never match the unrelated type '${parentType.name}'.",
			nodes = listOf(selection.nameNode, definition.typeCondition, type.nameNode, parentType.nameNode)
		)
	}


	override fun onInlineFragmentSelection(selection: GInlineFragmentSelection, data: ValidationContext, visit: Visit) {
		val typeCondition = selection.typeCondition
			?: return // Fragment always matches.

		val parentType = data.relatedParentType as? GCompositeType
			?: return // Cannot validate nonexistent or invalid type.

		val type = data.relatedType as? GCompositeType
			?: return // Cannot validate nonexistent or invalid type.

		val possibleParentTypes = data.schema.getPossibleTypes(parentType)
		val possibleTypes = data.schema.getPossibleTypes(type)
		val intersectionTypes = possibleTypes.intersect(possibleParentTypes)
		if (intersectionTypes.isNotEmpty())
			return // Fragment type condition can match.

		val relatedNodes = mutableListOf<GNode>(typeCondition)

		val relatedFieldSelection = data.relatedFieldSelection
		val relatedFragmentDefinition = data.relatedFragmentDefinition

		if (relatedFieldSelection !== null) {
			relatedNodes += relatedFieldSelection.nameNode
			data.relatedFieldDefinition?.let { relatedNodes += it.type }
		}
		else if (relatedFragmentDefinition !== null) {
			relatedNodes += relatedFragmentDefinition.typeCondition
		}

		relatedNodes += parentType.nameNode
		relatedNodes += type.nameNode

		data.reportError(
			message = "Inline fragment on '${type.name}' will never match the unrelated type '${parentType.name}'.",
			nodes = relatedNodes
		)
	}
}
