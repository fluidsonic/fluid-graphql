package io.fluidsonic.graphql


internal object DirectiveLocationValidityRule : ValidationRule.Singleton() {

	override fun onDirective(directive: GDirective, data: ValidationContext, visit: Visit) {
		val location = data.parentNode?.let { GDirectiveLocation.forNode(it) }
			?: return // Unknown location.

		val definition = data.relatedDirectiveDefinition
		if (definition === null)
			return // Unknown directive.

		if (definition.locations.isEmpty())
			return // Invalid directive definition.

		if (definition.locations.contains(location))
			return // Valid location.

		val allowedLocationsText = definition.locations
			.sortedBy { it.name }
			.joinToString(separator = ", ", lastSeparator = " or ")

		data.reportError(
			message = "Directive '@${directive.name}' is not valid on $location but only on $allowedLocationsText.",
			nodes = listOf(directive.nameNode, definition.locationNodes.first())
		)
	}
}
