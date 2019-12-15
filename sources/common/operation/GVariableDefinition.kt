package io.fluidsonic.graphql


class GVariableDefinition(
	val name: String,
	val type: GTypeRef,
	val defaultValue: Any? = null,
	val directives: List<GDirective> = emptyList() // FIXME new, add to builder
) {

	companion object {

		internal fun build(ast: AstNode.VariableDefinition) =
			GVariableDefinition(
				defaultValue = ast.defaultValue, // FIXME
				directives = ast.directives.map { GDirective.build(it) },
				name = ast.variable.name.value,
				type = GTypeRef.build(ast.type)
			)
	}
}
