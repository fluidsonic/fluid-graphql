package io.fluidsonic.graphql

// The introspection object type shells. Field order is the order in which introspection reports them, so it
// is part of the observable output and must stay as written.
// https://spec.graphql.org/draft/#sec-Schema-Introspection

private val directiveRef = GTypeRef("__Directive")
private val directiveLocationRef = GTypeRef("__DirectiveLocation")
private val enumValueRef = GTypeRef("__EnumValue")
private val fieldRef = GTypeRef("__Field")
private val inputValueRef = GTypeRef("__InputValue")
private val typeRef = GTypeRef("__Type")
private val typeKindRef = GTypeRef("__TypeKind")

/** A non-null list of non-null [elementType], the shape every introspection list field uses. */
private fun listOfNonNull(elementType: GTypeRef): GListTypeRef = GListTypeRef(elementType.nonNullableRef)

/**
 * The `includeDeprecated: Boolean! = false` argument shared by `__Schema.directives` and by `__Type`'s
 * `fields`, `enumValues` and `inputFields`.
 *
 * Non-null with a default, as the specification declares it — a client may omit it but must not pass null.
 */
private fun includeDeprecatedArgument(): GFieldArgumentDefinition = GFieldArgumentDefinition(
	name = "includeDeprecated",
	type = GBooleanTypeRef.nonNullableRef,
	defaultValue = GBooleanValue(false),
)

/** Creates a fresh `__Schema` shell. */
internal fun schemaType(): GObjectType = GObjectType(
	name = "__Schema",
	description = "A GraphQL Schema defines the capabilities of a GraphQL server. " +
		"It exposes all available types and directives on the server, as well as the entry points for query, mutation, and subscription operations.",
	fieldDefinitions = listOf(
		GFieldDefinition(
			name = "description",
			type = GStringTypeRef,
		),
		GFieldDefinition(
			name = "types",
			type = listOfNonNull(typeRef).nonNullableRef,
			description = "A list of all types supported by this server.",
		),
		GFieldDefinition(
			name = "queryType",
			type = typeRef.nonNullableRef,
			description = "The type that query operations will be rooted at.",
		),
		GFieldDefinition(
			name = "mutationType",
			type = typeRef,
			description = "If this server supports mutation, the type that mutation operations will be rooted at.",
		),
		GFieldDefinition(
			name = "subscriptionType",
			type = typeRef,
			description = "If this server support subscription, the type that subscription operations will be rooted at.",
		),
		GFieldDefinition(
			name = "directives",
			type = listOfNonNull(directiveRef).nonNullableRef,
			description = "A list of all directives supported by this server.",
			argumentDefinitions = listOf(includeDeprecatedArgument()),
		),
	),
)

/** Creates a fresh `__Type` shell. */
internal fun typeType(): GObjectType = GObjectType(
	name = "__Type",
	description = "The fundamental unit of any GraphQL Schema is the type. " +
		"There are many kinds of types in GraphQL as represented by the `__TypeKind` enum.\n\n" +
		"Depending on the kind of a type, certain fields describe information about that type. " +
		"Scalar types provide no information beyond a name and description, while Enum types provide their values. " +
		"Object and Interface types provide the fields they describe. " +
		"Abstract types, Union and Interface, provide the Object types possible at runtime. " +
		"List and NonNull types compose other types.",
	fieldDefinitions = listOf(
		GFieldDefinition(name = "kind", type = typeKindRef.nonNullableRef),
		GFieldDefinition(name = "name", type = GStringTypeRef),
		GFieldDefinition(name = "description", type = GStringTypeRef),
		GFieldDefinition(name = "specifiedByURL", type = GStringTypeRef),
		GFieldDefinition(name = "isOneOf", type = GBooleanTypeRef),
		GFieldDefinition(
			name = "fields",
			type = listOfNonNull(fieldRef),
			argumentDefinitions = listOf(includeDeprecatedArgument()),
		),
		GFieldDefinition(name = "interfaces", type = listOfNonNull(typeRef)),
		GFieldDefinition(name = "possibleTypes", type = listOfNonNull(typeRef)),
		GFieldDefinition(
			name = "enumValues",
			type = listOfNonNull(enumValueRef),
			argumentDefinitions = listOf(includeDeprecatedArgument()),
		),
		GFieldDefinition(
			name = "inputFields",
			type = listOfNonNull(inputValueRef),
			argumentDefinitions = listOf(includeDeprecatedArgument()),
		),
		GFieldDefinition(name = "ofType", type = typeRef),
	),
)

/** Creates a fresh `__Field` shell. */
internal fun fieldType(): GObjectType = GObjectType(
	name = "__Field",
	description = "Object and Interface types are described by a list of Fields, each of which has a name, potentially a list of arguments, " +
		"and a return type.",
	fieldDefinitions = listOf(
		GFieldDefinition(name = "name", type = GStringTypeRef.nonNullableRef),
		GFieldDefinition(name = "description", type = GStringTypeRef),
		GFieldDefinition(name = "args", type = listOfNonNull(inputValueRef).nonNullableRef),
		GFieldDefinition(name = "type", type = typeRef.nonNullableRef),
		GFieldDefinition(name = "isDeprecated", type = GBooleanTypeRef.nonNullableRef),
		GFieldDefinition(name = "deprecationReason", type = GStringTypeRef),
	),
)

/** Creates a fresh `__InputValue` shell. */
internal fun inputValueType(): GObjectType = GObjectType(
	name = "__InputValue",
	description = "Arguments provided to Fields or Directives and the input fields of an InputObject are represented as Input Values which describe their " +
		"type and optionally a default value.",
	fieldDefinitions = listOf(
		GFieldDefinition(name = "name", type = GStringTypeRef.nonNullableRef),
		GFieldDefinition(name = "description", type = GStringTypeRef),
		GFieldDefinition(name = "type", type = typeRef.nonNullableRef),
		GFieldDefinition(
			name = "defaultValue",
			type = GStringTypeRef,
			description = "A GraphQL-formatted string representing the default value for this input value.",
		),
		GFieldDefinition(name = "isDeprecated", type = GBooleanTypeRef.nonNullableRef),
		GFieldDefinition(name = "deprecationReason", type = GStringTypeRef),
	),
)

/** Creates a fresh `__EnumValue` shell. */
internal fun enumValueType(): GObjectType = GObjectType(
	name = "__EnumValue",
	description = "One possible value for a given Enum. Enum values are unique values, not a placeholder for a string or numeric value. " +
		"However an Enum value is returned in a JSON response as a string.",
	fieldDefinitions = listOf(
		GFieldDefinition(name = "name", type = GStringTypeRef.nonNullableRef),
		GFieldDefinition(name = "description", type = GStringTypeRef),
		GFieldDefinition(name = "isDeprecated", type = GBooleanTypeRef.nonNullableRef),
		GFieldDefinition(name = "deprecationReason", type = GStringTypeRef),
	),
)

/** Creates a fresh `__Directive` shell. */
internal fun directiveType(): GObjectType = GObjectType(
	name = "__Directive",
	description = "A Directive provides a way to describe alternate runtime execution and type validation behavior in a GraphQL document.\n\n" +
		"In some cases, you need to provide options to alter GraphQL's execution behavior in ways field arguments will not suffice, " +
		"such as conditionally including or skipping a field. Directives provide this by describing additional information to the executor.",
	fieldDefinitions = listOf(
		GFieldDefinition(name = "name", type = GStringTypeRef.nonNullableRef),
		GFieldDefinition(name = "description", type = GStringTypeRef),
		GFieldDefinition(name = "isRepeatable", type = GBooleanTypeRef.nonNullableRef),
		GFieldDefinition(name = "locations", type = listOfNonNull(directiveLocationRef).nonNullableRef),
		GFieldDefinition(name = "args", type = listOfNonNull(inputValueRef).nonNullableRef),
	),
)
