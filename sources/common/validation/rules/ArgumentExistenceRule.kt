package io.fluidsonic.graphql


internal object ArgumentExistenceRule : ValidationRule {

	override fun validateArgument(argument: GArgument, context: ValidationContext) {
		val argumentDefinition = context.relatedArgumentDefinition
		if (argumentDefinition !== null)
			return // Exists.

		val directive = context.relatedDirective
		if (directive !== null) {
			// Cannot validate an argument name of a nonexistent directive.
			val directiveDefinition = context.relatedDirectiveDefinition
				?: return

			context.reportError(
				message = "Unknown argument '${argument.name}' for directive '${directive.name}'.",
				nodes = listOf(argument.nameNode, directiveDefinition.nameNode)
			)

			return
		}

		val fieldSelection = context.relatedSelection as? GFieldSelection
		if (fieldSelection !== null) {
			// Cannot validate an argument name of a nonexistent field.
			val fieldDefinition = context.relatedFieldDefinition
				?: return

			// Cannot validate a selection for an unknown type.
			val parentType = context.relatedParentType // FIXME add to err msg?
				?: return

			context.reportError(
				message = "Unknown argument '${argument.name}' for field '${fieldSelection.name}'.",
				nodes = listOf(argument.nameNode, fieldDefinition.nameNode)
			)

			return
		}
	}
}
