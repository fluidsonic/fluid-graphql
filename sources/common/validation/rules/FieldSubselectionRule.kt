package io.fluidsonic.graphql


// https://graphql.github.io/graphql-spec/draft/#sec-Field-Selections-on-Objects-Interfaces-and-Unions-Types
internal object FieldSubselectionRule : ValidationRule {

	override fun validateFieldSelection(selection: GFieldSelection, context: ValidationContext) {
		// Cannot validate a selection for an unknown type.
		val parentType = context.relatedParentType // FIXME add to err msg
			?: return

		// Cannot validate a selection for an unknown field.
		val fieldDefinition = context.relatedFieldDefinition
			?: return

		// Cannot validate a selection for an unknown type.
		val fieldType = context.relatedType
			?: return

		when (fieldType) {
			is GLeafType ->
				if (selection.selectionSet !== null)
					context.reportError(
						message = "Cannot select children of '${fieldType.name}' field '${selection.name}'.",
						nodes = listOf(selection.selectionSet, fieldDefinition.nameNode)
					)

			is GCompositeType ->
				if (selection.selectionSet === null)
					context.reportError(
						message = "Must select children of '${fieldType.name}' field '${selection.name}'.",
						nodes = listOf(selection.nameNode, fieldDefinition.nameNode)
					)
		}
	}
}
