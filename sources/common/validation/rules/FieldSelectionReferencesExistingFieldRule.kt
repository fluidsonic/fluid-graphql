package io.fluidsonic.graphql


// https://graphql.github.io/graphql-spec/draft/#sec-Field-Selections-on-Objects-Interfaces-and-Unions-Types
internal object FieldSelectionReferencesExistingFieldRule : ValidationRule {

	override fun validateFieldSelection(selection: GFieldSelection, context: ValidationContext) {
		val parentType = context.relatedParentType ?: return
		val fieldDefinition = context.relatedFieldDefinition
		if (fieldDefinition != null)
			return

		context.reportError(
			message = "Cannot select non-existent field '${selection.name}' on type '${parentType.name}'.",
			nodes = listOf(selection)
		)
	}
}
