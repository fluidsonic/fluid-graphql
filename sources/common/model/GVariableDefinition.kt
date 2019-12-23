package io.fluidsonic.graphql


class GVariableDefinition(
	val name: String,
	val type: GTypeRef,
	val defaultValue: Any? = null,
	val directives: List<GDirective> = emptyList() // FIXME new, add to builder
) {

	companion object {

		fun from(ast: GAst.VariableDefinition) =
			GVariableDefinition(
				defaultValue = ast.defaultValue, // FIXME
				directives = ast.directives.map { GDirective.from(it) },
				name = ast.variable.name.value,
				type = GTypeRef.from(ast.type)
			)
	}
}
