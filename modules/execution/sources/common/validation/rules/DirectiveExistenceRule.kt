package io.fluidsonic.graphql


@Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")
internal object DirectiveExistenceRule : ValidationRule.Singleton() {

	override fun onDirective(directive: GDirective, data: ValidationContext, visit: Visit) {
		val definition = data.relatedDirectiveDefinition
		if (definition !== null)
			return // Exists.

		data.reportError(
			message = "Unknown directive '@${directive.name}'.",
			nodes = listOf(directive.nameNode)
		)
	}
}
