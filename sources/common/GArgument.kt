package io.fluidsonic.graphql


// https://graphql.github.io/graphql-spec/June2018/#Argument
class GArgument internal constructor(
	input: GQLInput.Argument
) {

	val name = input.name
	val value = input.value


	companion object
}
