package io.fluidsonic.graphql


// https://graphql.github.io/graphql-spec/June2018/#Argument
class GArgument(
	val name: String,
	val value: Any?
) {

	override fun toString() =
		GWriter { writeArgument(this@GArgument) }


	companion object {

		internal fun build(ast: AstNode.Argument) =
			GArgument(
				name = ast.name.value,
				value = ast.value // FIXME
			)
	}
}
