package io.fluidsonic.graphql


internal object DirectiveExistenceRule : ValidationRule {

	override fun validateDirective(directive: GDirective, context: ValidationContext) {
		val definition = context.relatedDirectiveDefinition
		if (definition !== null)
			return // Exists.

		context.reportError(
			message = "Unknown directive '@${directive.name}'.",
			nodes = listOf(directive.nameNode)
		)
	}
}
