package io.fluidsonic.graphql

/**
 * The three introspection meta-fields, which belong to no type and are therefore injected rather than
 * merged into the schema.
 *
 * The fields *of* the introspection types are resolved by [IntrospectionResolvers] instead; these three are
 * nodes this module creates itself, so they carry their resolvers directly.
 */
// https://spec.graphql.org/draft/#sec-Schema-Introspection
internal object Introspection {

	/** The `__schema` meta-field, available on the query root type only. */
	val schemaField = GFieldDefinition(
		name = "__schema",
		type = GTypeRef("__Schema").nonNullableRef,
		description = "Access the current type schema of this server.",
		extensions = GNodeExtensionSet {
			resolver = GFieldResolver<GSchema> { it }
		},
	)

	/** The `__type` meta-field, available on the query root type only. */
	val typeField = GFieldDefinition(
		name = "__type",
		type = GTypeRef("__Type"),
		description = "Request the type information of a single type.",
		argumentDefinitions = listOf(
			GFieldArgumentDefinition(
				name = "name",
				type = GStringTypeRef.nonNullableRef,
			),
		),
		extensions = GNodeExtensionSet {
			resolver = GFieldResolver<GSchema> { it.resolveType(arguments["name"] as String) }
		},
	)

	/** The `__typename` meta-field, available on every object, interface and union type. */
	val typenameField = GFieldDefinition(
		name = "__typename",
		type = GStringTypeRef.nonNullableRef,
		description = "The name of the current Object type at runtime.",
		extensions = GNodeExtensionSet {
			resolver = GFieldResolver<GObjectType> { it.name }
		},
	)

	/**
	 * Returns the `__Schema` type of [schema].
	 *
	 * @throws IllegalStateException if [schema] does not carry the introspection types, which cannot happen
	 *   for a schema built through any of the library's factories.
	 */
	fun schemaType(schema: GSchema): GObjectType = introspectionObjectType(schema = schema, name = "__Schema")

	/**
	 * Returns the `__Type` type of [schema].
	 *
	 * @throws IllegalStateException if [schema] does not carry the introspection types, which cannot happen
	 *   for a schema built through any of the library's factories.
	 */
	fun typeType(schema: GSchema): GObjectType = introspectionObjectType(schema = schema, name = "__Type")

	private fun introspectionObjectType(schema: GSchema, name: String): GObjectType =
		checkNotNull(schema.resolveType(name) as GObjectType?) { "The schema does not define the introspection type '$name'." }
}
