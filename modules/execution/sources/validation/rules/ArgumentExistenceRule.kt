package io.fluidsonic.graphql


internal object ArgumentExistenceRule : ValidationRule.Singleton() {

	override fun onArgument(argument: GArgument, data: ValidationContext, visit: Visit) {
		val argumentDefinition = data.relatedArgumentDefinition
		if (argumentDefinition !== null)
			return // Exists.

		when (data.parentNode) {
			is GDirective -> {
				val directiveDefinition = data.relatedDirectiveDefinition ?: return

				data.reportError(
					message = "Unknown argument '${argument.name}' for directive '${directiveDefinition.name}'.",
					nodes = listOf(argument.nameNode)
				)

				return
			}

			is GFieldSelection -> {
				val fieldDefinition = data.relatedFieldDefinition ?: return
				val parentType = data.relatedParentType ?: return

				data.reportError(
					message = "Unknown argument '${argument.name}' for field '${parentType.name}.${fieldDefinition.name}'.",
					nodes = listOf(argument.nameNode)
				)
			}

			is GObjectValue -> {
				val fieldDefinition = data.relatedFieldDefinition ?: return
				val parentType = data.relatedParentType as? GInputObjectType ?: return

				data.reportError(
					message = "Unknown argument '${argument.name}' for field '${parentType.name}.${fieldDefinition.name}'.",
					nodes = listOf(argument.nameNode)
				)
			}

			else ->
				Unit
		}
	}
}
