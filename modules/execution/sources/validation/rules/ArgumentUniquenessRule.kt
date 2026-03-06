package io.fluidsonic.graphql


// https://graphql.github.io/graphql-spec/draft/#sec-Argument-Uniqueness
internal object ArgumentUniquenessRule : ValidationRule.Singleton() {

	override fun onFieldSelection(selection: GFieldSelection, data: ValidationContext, visit: Visit) =
		checkArgUniqueness(selection.arguments, data)

	override fun onDirective(directive: GDirective, data: ValidationContext, visit: Visit) =
		checkArgUniqueness(directive.arguments, data)

	private fun checkArgUniqueness(arguments: List<GArgument>, data: ValidationContext) {
		arguments.groupBy { it.name }
			.filterValues { it.size > 1 }
			.forEach { (name, nodes) ->
				data.reportError(
					message = "Argument '$name' must not occur multiple times.",
					nodes = nodes.map { it.nameNode }
				)
			}
	}
}
