package io.fluidsonic.graphql


// https://graphql.github.io/graphql-spec/draft/#sec-Field-Selections-on-Objects-Interfaces-and-Unions-Types
internal object FieldSelectionExistenceRule : ValidationRule.Singleton() {

	override fun onFieldSelection(selection: GFieldSelection, data: ValidationContext, visit: Visit) {
		val parentType = data.relatedParentType ?: return
		val fieldDefinition = data.relatedFieldDefinition
		if (fieldDefinition != null)
			return

		data.reportError(
			message = "Cannot select nonexistent field '${selection.name}' on type '${parentType.name}'.",
			nodes = listOf(selection.nameNode)
		)
	}
}
