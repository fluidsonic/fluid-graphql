package io.fluidsonic.graphql


// https://graphql.github.io/graphql-spec/draft/#sec-Directives-Are-Unique-Per-Location
@Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")
internal object DirectiveExclusivityRule : ValidationRule.Singleton() {

	override fun onAny(node: GNode, data: ValidationContext, visit: Visit) {
		if (node !is GNode.WithDirectives)
			return // No directives to check.

		node.directives
			.mapNotNull { directive ->
				// Unknown.
				data.schema.directiveDefinition(directive.name)?.let { directive to it }
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
				data.reportError(
					message = "Directive '@${name}' must not occur multiple times.",
					nodes = directives.map { it.nameNode }
				)
			}
	}
}
