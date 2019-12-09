package io.fluidsonic.graphql


// https://graphql.github.io/graphql-spec/June2018/#sec-Type-System.Directives
// https://graphql.github.io/graphql-spec/June2018/#sec-The-__Directive-Type
class GDirectiveDefinition internal constructor(
	typeFactory: TypeFactory,
	input: GQLInput.DirectiveDefinition
) {

	val arguments = input.arguments.map { GArgumentDefinition(typeFactory, it) } // FIXME parameters
	val description = input.description
	val locations = input.locations
	val name = input.name


	override fun toString() =
		GWriter { writeDirectiveDefinition(this@GDirectiveDefinition) }


	companion object
}
