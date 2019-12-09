package io.fluidsonic.graphql


// https://graphql.github.io/graphql-spec/June2018/#InputValueDefinition
// https://graphql.github.io/graphql-spec/June2018/#sec-The-__InputValue-Type
class GParameter internal constructor(
	typeFactory: TypeFactory,
	input: GQLInput.InputValue
) {

	val defaultValue = input.defaultValue // FIXME allow all types
	val description = input.description
	val directives = input.directives.map(::GDirective)
	val name = input.name
	val type = typeFactory.get(input.type)


//		init {
//			require(Specification.isValidInputValueName(name)) { "'name' is not a valid name: $name" }
//			require(Specification.isInputType(type)) {
//				"'type' must be an input type: $type"
//			}
//		}


	companion object
}
