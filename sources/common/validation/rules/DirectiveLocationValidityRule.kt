package io.fluidsonic.graphql


internal object DirectiveLocationValidityRule : ValidationRule {

	override fun validateDirective(directive: GDirective, context: ValidationContext) {
		val location = context.parentNode?.let { GDirectiveLocation.forAstNode(it) }
			?: return // Unknown location.

		val definition = context.relatedDirectiveDefinition
		if (definition === null)
			return // Unknown directive.

		if (definition.locations.isEmpty())
			return // Invalid directive definition.

		if (definition.locations.contains(location))
			return // Valid location.

		val allowedLocationsText = definition.locations
			.sortedBy { it.name }
			.joinToString(separator = ", ", lastSeparator = " or ")

		context.reportError(
			message = "Directive '@${directive.name}' is not valid on $location but only on $allowedLocationsText.",
			nodes = listOf(directive.nameNode, definition.locationNodes.first())
		)
	}
}
