package io.fluidsonic.graphql


@Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")
internal object ArgumentExistenceRule : ValidationRule.Singleton() {

	override fun onArgument(argument: GArgument, data: ValidationContext, visit: Visit) {
		val argumentDefinition = data.relatedArgumentDefinition
		if (argumentDefinition !== null)
			return // Exists.

		val directive = data.relatedDirective
		if (directive !== null) {
			// Cannot validate an argument name of a nonexistent directive.
			val directiveDefinition = data.relatedDirectiveDefinition
				?: return

			data.reportError(
				message = "Unknown argument '${argument.name}' for directive '${directive.name}'.",
				nodes = listOf(argument.nameNode, directiveDefinition.nameNode)
			)

			return
		}

		val fieldSelection = data.relatedSelection as? GFieldSelection
		if (fieldSelection !== null) {
			// Cannot validate an argument name of a nonexistent field.
			val fieldDefinition = data.relatedFieldDefinition
				?: return

			// Cannot validate a selection for an unknown type.
			val parentType = data.relatedParentType // FIXME add to err msg?
				?: return

			data.reportError(
				message = "Unknown argument '${argument.name}' for field '${fieldSelection.name}'.",
				nodes = listOf(argument.nameNode, fieldDefinition.nameNode)
			)

			return
		}
	}
}
