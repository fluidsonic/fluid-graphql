package io.fluidsonic.graphql


// https://graphql.github.io/graphql-spec/June2018/#Argument
class GArgument(
	val name: String,
	val value: Any?
) {

	override fun toString() =
		GWriter { writeArgument(this@GArgument) }


	companion object
}
