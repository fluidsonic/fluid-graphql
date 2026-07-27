package io.fluidsonic.graphql

// The introspection enum type shells. Value order is the order in which introspection reports them, so it is
// part of the observable output and must stay as written.
// https://spec.graphql.org/draft/#sec-Schema-Introspection

/** Creates a fresh `__TypeKind` shell. */
internal fun typeKindType(): GEnumType = GEnumType(
	name = GName("__TypeKind"),
	values = listOf(
		GEnumValueDefinition(name = "SCALAR", description = "Indicates this type is a scalar."),
		GEnumValueDefinition(name = "OBJECT", description = "Indicates this type is an object. `fields` and `interfaces` are valid fields."),
		GEnumValueDefinition(
			name = "INTERFACE",
			description = "Indicates this type is an interface. `fields`, `interfaces`, and `possibleTypes` are valid fields.",
		),
		GEnumValueDefinition(name = "UNION", description = "Indicates this type is a union. `possibleTypes` is a valid field."),
		GEnumValueDefinition(name = "ENUM", description = "Indicates this type is an enum. `enumValues` is a valid field."),
		GEnumValueDefinition(name = "INPUT_OBJECT", description = "Indicates this type is an input object. `inputFields` is a valid field."),
		GEnumValueDefinition(name = "LIST", description = "Indicates this type is a list. `ofType` is a valid field."),
		GEnumValueDefinition(name = "NON_NULL", description = "Indicates this type is a non-null. `ofType` is a valid field."),
	),
)

/** Creates a fresh `__DirectiveLocation` shell. */
internal fun directiveLocationType(): GEnumType = GEnumType(
	name = GName("__DirectiveLocation"),
	values = listOf(
		GEnumValueDefinition(name = "QUERY", description = "Location adjacent to a query operation."),
		GEnumValueDefinition(name = "MUTATION", description = "Location adjacent to a mutation operation."),
		GEnumValueDefinition(name = "SUBSCRIPTION", description = "Location adjacent to a subscription operation."),
		GEnumValueDefinition(name = "FIELD", description = "Location adjacent to a field."),
		GEnumValueDefinition(name = "FRAGMENT_DEFINITION", description = "Location adjacent to a fragment definition."),
		GEnumValueDefinition(name = "FRAGMENT_SPREAD", description = "Location adjacent to a fragment spread."),
		GEnumValueDefinition(name = "INLINE_FRAGMENT", description = "Location adjacent to an inline fragment."),
		GEnumValueDefinition(name = "VARIABLE_DEFINITION", description = "Location adjacent to a variable definition."),
		GEnumValueDefinition(name = "SCHEMA", description = "Location adjacent to a schema definition."),
		GEnumValueDefinition(name = "SCALAR", description = "Location adjacent to a scalar definition."),
		GEnumValueDefinition(name = "OBJECT", description = "Location adjacent to an object type definition."),
		GEnumValueDefinition(name = "FIELD_DEFINITION", description = "Location adjacent to a field definition."),
		GEnumValueDefinition(name = "ARGUMENT_DEFINITION", description = "Location adjacent to an argument definition."),
		GEnumValueDefinition(name = "INTERFACE", description = "Location adjacent to an interface definition."),
		GEnumValueDefinition(name = "UNION", description = "Location adjacent to a union definition."),
		GEnumValueDefinition(name = "ENUM", description = "Location adjacent to an enum definition."),
		GEnumValueDefinition(name = "ENUM_VALUE", description = "Location adjacent to an enum value definition."),
		GEnumValueDefinition(name = "INPUT_OBJECT", description = "Location adjacent to an input object type definition."),
		GEnumValueDefinition(name = "INPUT_FIELD_DEFINITION", description = "Location adjacent to an input object field definition."),
		GEnumValueDefinition(name = "DIRECTIVE_DEFINITION", description = "Location adjacent to a directive definition."),
	),
)
