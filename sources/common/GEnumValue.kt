package io.fluidsonic.graphql


// https://graphql.github.io/graphql-spec/June2018/#EnumValue
// https://graphql.github.io/graphql-spec/June2018/#sec-The-__EnumValue-Type
class GEnumValue internal constructor(
	input: GQLInput.EnumValue
) {

	val description = input.description
	val deprecationReason = input.deprecationReason
	val directives = input.directives.map(::GDirective)
	val isDeprecated = input.isDeprecated
	val name = input.name


//		init {
//			require(Specification.isValidEnumValue(name)) { "'name' is not a valid name: $name" }
//		}


	companion object
}
