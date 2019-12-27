package io.fluidsonic.graphql


internal object ObjectFieldExistenceRule : ValidationRule {

	override fun validateObjectValueField(field: GObjectValueField, context: ValidationContext) {
		val parentType = context.relatedParentType as? GInputObjectType
			?: return // Cannot validate unknown or incorrect type.

		val argumentDefinition = context.relatedArgumentDefinition
		if (argumentDefinition !== null)
			return // Field exists.


		context.reportError(
			message = "Unknown field '${field.name}' for Input type '${parentType.name}'.",
			nodes = listOf(field.nameNode, parentType.nameNode)
		)
	}
}
