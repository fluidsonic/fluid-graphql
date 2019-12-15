package io.fluidsonic.graphql


enum class GDirectiveLocation {

	ARGUMENT_DEFINITION,
	ENUM,
	ENUM_VALUE,
	FIELD,
	FIELD_DEFINITION,
	FRAGMENT_DEFINITION,
	FRAGMENT_SPREAD,
	INLINE_FRAGMENT,
	INPUT_FIELD_DEFINITION,
	INPUT_OBJECT,
	INTERFACE,
	MUTATION,
	OBJECT,
	QUERY,
	SCALAR,
	SCHEMA,
	SUBSCRIPTION,
	UNION,
	VARIABLE_DEFINITION;


	companion object {

		internal fun build(ast: AstNode.Name) =
			values().firstOrNull { it.name == ast.value }
				?: error("Invalid directive location '${ast.value}'")
	}
}
