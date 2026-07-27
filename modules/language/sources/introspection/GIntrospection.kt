package io.fluidsonic.graphql

/**
 * The eight types the specification reserves for schema introspection.
 *
 * Every [GSchema] carries its own instances of these types as ordinary members of its type map, so a client
 * can reach `__Schema`, `__Type` and their siblings through the same name lookup as any user type.
 *
 * The definitions here are shells — pure data describing the shape of introspection. They carry no resolvers
 * and no node extensions: resolving an introspection field needs an executor, which this module knows
 * nothing about, so an executor supplies the resolution itself.
 */
// https://spec.graphql.org/draft/#sec-Schema-Introspection
@InternalGraphqlApi
public object GIntrospection {

	/** The names of the eight introspection types, in the order [types] returns them. */
	public val typeNames: List<String> = listOf(
		"__Schema",
		"__Type",
		"__Field",
		"__InputValue",
		"__EnumValue",
		"__TypeKind",
		"__Directive",
		"__DirectiveLocation",
	)

	/**
	 * Creates a fresh instance of each of the eight introspection types.
	 *
	 * A new set of new instances is returned on every call: introspection types are per-schema, never shared,
	 * so that an extension attached to one schema's `__Type` cannot be seen by another schema.
	 */
	public fun types(): List<GNamedType> = listOf(
		schemaType(),
		typeType(),
		fieldType(),
		inputValueType(),
		enumValueType(),
		typeKindType(),
		directiveType(),
		directiveLocationType(),
	)
}
