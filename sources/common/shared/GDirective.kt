package io.fluidsonic.graphql


// https://graphql.github.io/graphql-spec/June2018/#sec-Language.Directives
class GDirective(
	val name: String,
	arguments: List<GArgument> = emptyList()
) {

	val arguments: Map<String, GArgument>


	init {
		require(arguments.size <= 1 || arguments.mapTo(hashSetOf()) { it.name }.size == arguments.size) {
			"'arguments' must not contain multiple elements with the same name: $arguments"
		}

		this.arguments = arguments.associateBy { it.name }
	}


	override fun toString() =
		GWriter { writeDirective(this@GDirective) }


	companion object {

		fun from(ast: GAst.Directive) =
			GDirective(
				arguments = ast.arguments.map { GArgument.from(it) },
				name = ast.name.value
			)
	}
}
