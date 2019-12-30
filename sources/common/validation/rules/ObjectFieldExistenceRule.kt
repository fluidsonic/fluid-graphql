package io.fluidsonic.graphql


internal object ObjectFieldExistenceRule : ValidationRule.Singleton() {

	override fun onObjectValueField(field: GObjectValueField, data: ValidationContext, visit: Visit) {
		val parentType = data.relatedParentType as? GInputObjectType
			?: return // Cannot validate unknown or incorrect type.

		val argumentDefinition = data.relatedArgumentDefinition
		if (argumentDefinition !== null)
			return // Field exists.


		data.reportError(
			message = "Unknown field '${field.name}' for Input type '${parentType.name}'.",
			nodes = listOf(field.nameNode, parentType.nameNode)
		)
	}
}
