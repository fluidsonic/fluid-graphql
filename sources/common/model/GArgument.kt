package io.fluidsonic.graphql


// https://graphql.github.io/graphql-spec/June2018/#Argument
class GArgument(
	val name: String,
	val value: GAst.Value // FIXME full AST or no AST - don't mix!
) {

	override fun toString() =
		GWriter { writeArgument(this@GArgument) }


	companion object {

		fun from(ast: GAst.Argument) =
			GArgument(
				name = ast.name.value,
				value = ast.value
			)
	}
}
