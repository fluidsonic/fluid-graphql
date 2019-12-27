package io.fluidsonic.graphql


// https://graphql.github.io/graphql-spec/draft/#sec-Field-Selections-on-Objects-Interfaces-and-Unions-Types
internal object FieldSelectionExistenceRule : ValidationRule {

	override fun validateFieldSelection(selection: GFieldSelection, context: ValidationContext) {
		val parentType = context.relatedParentType ?: return
		val fieldDefinition = context.relatedFieldDefinition
		if (fieldDefinition != null)
			return

		context.reportError(
			message = "Cannot select nonexistent field '${selection.name}' on type '${parentType.name}'.",
			nodes = listOf(selection.nameNode)
		)
	}
}
