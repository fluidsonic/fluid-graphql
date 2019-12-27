package io.fluidsonic.graphql


// https://graphql.github.io/graphql-spec/draft/#sec-Directives-Are-Unique-Per-Location
internal object DirectiveExclusivityRule : ValidationRule {

	override fun validateNode(node: GAst, context: ValidationContext) {
		if (node !is GAst.WithDirectives)
			return // No directives to check.

		node.directives
			.mapNotNull { directive ->
				// Unknown.
				context.schema.directiveDefinition(directive.name)?.let { directive to it }
			}
			.filterNot { (_, definition) ->
				// Repeatable.
				definition.isRepeatable
			}
			.map { (directive, _) -> directive }
			.groupBy { directive -> directive.name }
			.filterNot { (_, directives) ->
				// Unique.
				directives.size == 1
			}
			.forEach { (name, directives) ->
				context.reportError(
					message = "Directive '@${name}' must not occur multiple times.",
					nodes = directives.map { it.nameNode }
				)
			}
	}
}
