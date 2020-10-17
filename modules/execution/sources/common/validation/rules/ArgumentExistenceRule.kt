package io.fluidsonic.graphql


internal object ArgumentExistenceRule : ValidationRule.Singleton() {

	override fun onArgument(argument: GArgument, data: ValidationContext, visit: Visit) {
		val argumentDefinition = data.relatedArgumentDefinition
		if (argumentDefinition !== null)
			return // Exists.

		val directive = data.relatedDirective
		if (directive !== null) {
			val directiveDefinition = data.relatedDirectiveDefinition ?: return

			data.reportError(
				message = "Unknown argument '${argument.name}' for directive '${directive.name}'.",
				nodes = listOf(argument.nameNode, directiveDefinition.nameNode)
			)

			return
		}

		when (val parentType = data.relatedParentType ?: return) {
			is GInputObjectType ->
				return

			else -> {
				val fieldDefinition = data.relatedFieldDefinition ?: return

				data.reportError(
					message = "Unknown argument '${argument.name}' for field '${parentType.name}.${fieldDefinition.name}'.",
					nodes = listOf(argument.nameNode, fieldDefinition.nameNode)
				)
			}
		}
	}
}
