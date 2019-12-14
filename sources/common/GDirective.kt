package io.fluidsonic.graphql


// https://graphql.github.io/graphql-spec/June2018/#sec-Language.Directives
class GDirective(
	val name: String,
	val arguments: List<GArgument> = emptyList()
) {

	override fun toString() =
		GWriter { writeDirective(this@GDirective) }


	companion object
}
