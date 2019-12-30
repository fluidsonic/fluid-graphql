package io.fluidsonic.graphql


// https://graphql.github.io/graphql-spec/draft/#sec-Field-Selections-on-Objects-Interfaces-and-Unions-Types
internal object FieldSubselectionRule : ValidationRule.Singleton() {

	override fun onFieldSelection(selection: GFieldSelection, data: ValidationContext, visit: Visit) {
		// Cannot validate a selection for an unknown type.
		val parentType = data.relatedParentType // FIXME add to err msg
			?: return

		// Cannot validate a selection for an unknown field.
		val fieldDefinition = data.relatedFieldDefinition
			?: return

		// Cannot validate a selection for an unknown type.
		val fieldType = data.relatedType
			?: return

		when (fieldType) {
			is GLeafType ->
				if (selection.selectionSet !== null)
					data.reportError(
						message = "Cannot select children of '${fieldType.name}' field '${selection.name}'.",
						nodes = listOf(selection.selectionSet, fieldDefinition.nameNode)
					)

			is GCompositeType ->
				if (selection.selectionSet === null)
					data.reportError(
						message = "Must select children of '${fieldType.name}' field '${selection.name}'.",
						nodes = listOf(selection.nameNode, fieldDefinition.nameNode)
					)
		}
	}
}
