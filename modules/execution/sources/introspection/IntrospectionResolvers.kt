package io.fluidsonic.graphql

/**
 * The resolvers for the introspection types the language module merges into every schema.
 *
 * The language module owns the shape of `__Schema`, `__Type` and their siblings but cannot resolve them —
 * a resolver needs this module's [GFieldResolverContext]. The shells therefore stay pure data and the
 * resolvers live here instead, looked up by the type and field being resolved.
 *
 * The table is immutable and holds nothing but stateless functions, so it is shared safely by every schema
 * in the process; nothing about a particular schema is recorded in it.
 */
internal object IntrospectionResolvers {

	private val resolversByType: Map<String, Map<String, GFieldResolver<*>>> = mapOf(
		"__Schema" to schemaFieldResolvers(),
		"__Type" to typeFieldResolvers(),
		"__Field" to fieldFieldResolvers(),
		"__InputValue" to inputValueFieldResolvers(),
		"__EnumValue" to enumValueFieldResolvers(),
		"__Directive" to directiveFieldResolvers(),
	)

	/**
	 * Returns the resolver for the field named [fieldName] of the introspection type named [typeName], or
	 * `null` when the pair names no introspection field.
	 */
	fun resolverFor(typeName: String, fieldName: String): GFieldResolver<*>? = resolversByType[typeName]?.get(fieldName)
}

/** Narrows [resolve] to the parent type it expects, so that the table can hold resolvers of mixed parents. */
private fun <Parent : Any> introspectionResolver(resolve: suspend GFieldResolverContext.(parent: Parent) -> Any?): GFieldResolver<Parent> =
	GFieldResolver(resolve)

/**
 * Resolves [ref] against the schema being introspected.
 *
 * @throws IllegalStateException if the schema does not define the referenced type, which means it was not
 *   validated before execution.
 */
internal fun GFieldResolverContext.resolveIntrospectedType(ref: GTypeRef): GType = checkNotNull(execution.schema.resolveType(ref)) {
	"Introspection cannot resolve type '$ref'. The schema should be validated before execution."
}

private fun schemaFieldResolvers(): Map<String, GFieldResolver<*>> = mapOf(
	"description" to introspectionResolver<GSchema> { it.description },
	"types" to introspectionResolver<GSchema> { it.types },
	"queryType" to introspectionResolver<GSchema> { it.queryType },
	"mutationType" to introspectionResolver<GSchema> { it.mutationType },
	"subscriptionType" to introspectionResolver<GSchema> { it.subscriptionType },
	"directives" to introspectionResolver<GSchema> { it.directiveDefinitions },
)

private fun typeFieldResolvers(): Map<String, GFieldResolver<*>> = mapOf(
	"kind" to introspectionResolver<GType> { it.kind.name },
	"name" to introspectionResolver<GType> { (it as? GNamedType)?.name },
	"description" to introspectionResolver<GType> { (it as? GNamedType)?.description },
	"specifiedByURL" to introspectionResolver<GType> { type ->
		(type as? GScalarType)
			?.directive(GLanguage.defaultSpecifiedByDirective.name)
			?.argument("url")
			?.value
			?.let { it as? GStringValue }
			?.value
	},
	// `null` for anything that is not an input object, mirroring graphql-js.
	// https://spec.graphql.org/draft/#sec-OneOf-Input-Objects
	"isOneOf" to introspectionResolver<GType> { type ->
		(type as? GInputObjectType)?.let { it.directive(GLanguage.defaultOneOfDirective.name) !== null }
	},
	"fields" to introspectionResolver<GType> { type ->
		when (type) {
			is GNode.WithFieldDefinitions -> type.fieldDefinitions.filterDeprecated(includeDeprecated = includeDeprecated()) { it.deprecation }
			else -> null
		}
	},
	"interfaces" to introspectionResolver<GType> { type ->
		(type as? GNode.WithInterfaces)?.interfaces?.map { resolveIntrospectedType(it) }
	},
	"possibleTypes" to introspectionResolver<GType> { type ->
		(type as? GAbstractType)?.let { execution.schema.getPossibleTypes(it) }
	},
	"enumValues" to introspectionResolver<GType> { type ->
		when (type) {
			is GEnumType -> type.values.filterDeprecated(includeDeprecated = includeDeprecated()) { it.deprecation }
			else -> null
		}
	},
	"inputFields" to introspectionResolver<GType> { (it as? GNode.WithArgumentDefinitions)?.argumentDefinitions },
	"ofType" to introspectionResolver<GType> { (it as? GWrappingType)?.wrappedType },
)

private fun fieldFieldResolvers(): Map<String, GFieldResolver<*>> = mapOf(
	"name" to introspectionResolver<GFieldDefinition> { it.name },
	"description" to introspectionResolver<GFieldDefinition> { it.description },
	"args" to introspectionResolver<GFieldDefinition> { it.argumentDefinitions },
	"type" to introspectionResolver<GFieldDefinition> { resolveIntrospectedType(it.type) },
	"isDeprecated" to introspectionResolver<GFieldDefinition> { it.deprecation !== null },
	"deprecationReason" to introspectionResolver<GFieldDefinition> { it.deprecationReason },
)

private fun inputValueFieldResolvers(): Map<String, GFieldResolver<*>> = mapOf(
	"name" to introspectionResolver<GArgumentDefinition> { it.name },
	"description" to introspectionResolver<GArgumentDefinition> { it.description },
	"type" to introspectionResolver<GArgumentDefinition> { resolveIntrospectedType(it.type) },
	"defaultValue" to introspectionResolver<GArgumentDefinition> { it.defaultValue?.toString() },
	"isDeprecated" to introspectionResolver<GArgumentDefinition> { it.deprecation !== null },
	"deprecationReason" to introspectionResolver<GArgumentDefinition> { it.deprecationReason },
)

private fun enumValueFieldResolvers(): Map<String, GFieldResolver<*>> = mapOf(
	"name" to introspectionResolver<GEnumValueDefinition> { it.name },
	"description" to introspectionResolver<GEnumValueDefinition> { it.description },
	"isDeprecated" to introspectionResolver<GEnumValueDefinition> { it.deprecation !== null },
	"deprecationReason" to introspectionResolver<GEnumValueDefinition> { it.deprecationReason },
)

private fun directiveFieldResolvers(): Map<String, GFieldResolver<*>> = mapOf(
	"name" to introspectionResolver<GDirectiveDefinition> { it.name },
	"description" to introspectionResolver<GDirectiveDefinition> { it.description },
	"isRepeatable" to introspectionResolver<GDirectiveDefinition> { it.isRepeatable },
	"locations" to introspectionResolver<GDirectiveDefinition> { definition -> definition.locations.map { it.name }.sorted() },
	"args" to introspectionResolver<GDirectiveDefinition> { it.argumentDefinitions },
)

/** The value of the `includeDeprecated` argument of `__Type.fields` and `__Type.enumValues`. */
private fun GFieldResolverContext.includeDeprecated(): Boolean = arguments["includeDeprecated"] as Boolean

/** Returns [this] unchanged when [includeDeprecated] is `true`, else without the elements [deprecation] marks. */
private fun <Element> List<Element>.filterDeprecated(includeDeprecated: Boolean, deprecation: (Element) -> GDirective?): List<Element> =
	when (includeDeprecated) {
		true -> this
		false -> filter { deprecation(it) === null }
	}
