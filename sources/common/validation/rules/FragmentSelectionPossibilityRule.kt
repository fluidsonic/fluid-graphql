package io.fluidsonic.graphql


// https://graphql.github.io/graphql-spec/draft/#sec-Fragment-spread-target-defined
internal object FragmentSelectionPossibilityRule : ValidationRule {

	override fun validateFragmentSelection(selection: GFragmentSelection, context: ValidationContext) {
		val definition = context.relatedFragmentDefinition
			?: return // Cannot validate nonexistent fragment.

		val parentType = context.relatedParentType as? GCompositeType
			?: return // Cannot validate nonexistent or invalid type.

		val type = context.relatedType as? GCompositeType
			?: return // Cannot validate nonexistent or invalid type.

		val possibleParentTypes = context.schema.getPossibleTypes(parentType)
		val possibleTypes = context.schema.getPossibleTypes(type)
		val intersectionTypes = possibleTypes.intersect(possibleParentTypes)
		if (intersectionTypes.isNotEmpty())
			return // Fragment type condition can match.

		context.reportError(
			message = "Fragment '${selection.name}' on '${type.name}' will never match the unrelated type '${parentType.name}'.",
			nodes = listOf(selection.nameNode, definition.typeCondition, type.nameNode, parentType.nameNode)
		)
	}


	override fun validateInlineFragmentSelection(selection: GInlineFragmentSelection, context: ValidationContext) {
		val typeCondition = selection.typeCondition
			?: return // Fragment always matches.

		val parentType = context.relatedParentType as? GCompositeType
			?: return // Cannot validate nonexistent or invalid type.

		val type = context.relatedType as? GCompositeType
			?: return // Cannot validate nonexistent or invalid type.

		val possibleParentTypes = context.schema.getPossibleTypes(parentType)
		val possibleTypes = context.schema.getPossibleTypes(type)
		val intersectionTypes = possibleTypes.intersect(possibleParentTypes)
		if (intersectionTypes.isNotEmpty())
			return // Fragment type condition can match.

		val relatedNodes = mutableListOf<GAst>(typeCondition)

		val relatedFieldSelection = context.relatedFieldSelection
		val relatedFragmentDefinition = context.relatedFragmentDefinition

		if (relatedFieldSelection !== null) {
			relatedNodes += relatedFieldSelection.nameNode
			context.relatedFieldDefinition?.let { relatedNodes += it.type }
		}
		else if (relatedFragmentDefinition !== null) {
			relatedNodes += relatedFragmentDefinition.typeCondition
		}

		relatedNodes += parentType.nameNode
		relatedNodes += type.nameNode

		context.reportError(
			message = "Inline fragment on '${type.name}' will never match the unrelated type '${parentType.name}'.",
			nodes = relatedNodes
		)
	}
}
