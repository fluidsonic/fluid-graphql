package io.fluidsonic.graphql


// https://graphql.github.io/graphql-spec/June2018/#FieldsDefinition
// https://graphql.github.io/graphql-spec/June2018/#sec-The-__Field-Type
class GFieldDefinition internal constructor(
	typeFactory: TypeFactory,
	input: GQLInput.Field
) {

	val args = input.args.map { GParameter(typeFactory, it) }
	val deprecationReason = input.deprecationReason
	val description = input.description
	val directives = input.directives.map(::GDirective)
	val isDeprecated = input.isDeprecated
	val name = input.name
	val type = typeFactory.get(input.type)


//		init {
//			require(Specification.isValidFieldName(name)) { "'name' is not a valid name: $name" }
//			require(Specification.isOutputType(type)) {
//				"'type' must be an output type: $type"
//			}
//		}

	companion object
}
