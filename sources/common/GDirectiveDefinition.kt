package io.fluidsonic.graphql


// https://graphql.github.io/graphql-spec/June2018/#sec-Type-System.Directives
// https://graphql.github.io/graphql-spec/June2018/#sec-The-__Directive-Type
class GDirectiveDefinition internal constructor(
	typeFactory: TypeFactory,
	input: GQLInput.DirectiveDefinition
) {

	val arguments = input.arguments.map { GParameter(typeFactory, it) }
	val description = input.description
	val locations = input.locations
	val name = input.name


	companion object
}
