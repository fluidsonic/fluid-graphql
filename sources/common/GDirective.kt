package io.fluidsonic.graphql


// https://graphql.github.io/graphql-spec/June2018/#sec-Language.Directives
class GDirective internal constructor(
	input: GQLInput.Directive
) {

	val arguments = input.arguments.map(::GArgument)
	val name = input.name


	override fun toString() =
		GWriter { writeDirective(this@GDirective) }


	companion object
}
