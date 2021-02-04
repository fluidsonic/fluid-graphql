package io.fluidsonic.graphql


internal object ObjectFieldExistenceRule : ValidationRule.Singleton() {

	override fun onArgument(argument: GArgument, data: ValidationContext, visit: Visit) {
		val parentType = data.relatedParentType as? GInputObjectType
			?: return // Cannot validate unknown or incorrect type.

		val argumentDefinition = data.relatedArgumentDefinition
		if (argumentDefinition !== null)
			return // Field exists.

		data.reportError(
			message = "Unknown field '${argument.name}' for ${parentType.kind} type '${parentType.name}'.",
			nodes = listOf(argument.nameNode, parentType.nameNode)
		)
	}
}
