package io.fluidsonic.graphql


internal object Introspection {

	private val Directive = GTypeRef("__Directive")
	private val DirectiveLocation = GTypeRef("__DirectiveLocation")
	private val EnumValue = GTypeRef("__EnumValue")
	private val Field = GTypeRef("__Field")
	private val InputValue = GTypeRef("__InputValue")
	private val Schema = GTypeRef("__Schema")
	private val Type = GTypeRef("__Type")
	private val TypeKind = GTypeRef("__TypeKind")


	// https://graphql.github.io/graphql-spec/June2018/#sec-Schema-Introspection
	@Suppress("RemoveExplicitTypeArguments")
	val schema = graphql.schema {
		Object<GSchema>(Schema) {
			description(
				"A GraphQL Schema defines the capabilities of a GraphQL server. " +
					"It exposes all available types and directives on the server, as well as the entry points for query, mutation, and subscription operations."
			)

			field("types" of !List(!Type)) {
				description("A list of all types supported by this server.")
				resolve<List<GType>> { it.types }
			}

			field("queryType" of Type) {
				description("If this server supports queries, the type that query operations will be rooted at.")
				resolve<GType?> { it.queryType }
			}

			field("mutationType" of Type) {
				description("If this server supports mutation, the type that mutation operations will be rooted at.")
				resolve<GType?> { it.mutationType }
			}

			field("subscriptionType" of Type) {
				description("If this server support subscription, the type that subscription operations will be rooted at.")
				resolve<GType?> { it.subscriptionType }
			}

			field("directives" of !List(!Directive)) {
				description("A list of all directives supported by this server.")
				resolve<List<GDirectiveDefinition>> { it.directiveDefinitions }
			}
		}

		Object<GType>(Type) {
			description(
				"The fundamental unit of any GraphQL Schema is the type. " +
					"There are many kinds of types in GraphQL as represented by the `__TypeKind` enum.\n\n" +
					"Depending on the kind of a type, certain fields describe information about that type. " +
					"Scalar types provide no information beyond a name and description, while Enum types provide their values. " +
					"Object and Interface types provide the fields they describe. " +
					"Abstract types, Union and Interface, provide the Object types possible at runtime. " +
					"List and NonNull types compose other types."
			)

			field("kind" of !TypeKind) {
				resolve<String> { it.kind.name }
			}

			field("name" of String) {
				resolve<String?> { (it as? GNamedType)?.name }
			}

			field("description" of String) {
				resolve<String?> { (it as? GNamedType)?.description }
			}

			field("specifiedByURL" of String) {
				resolve { type ->
					(type as? GScalarType)
						?.directive(GLanguage.defaultSpecifiedByDirective.name)
						?.argument("url")
						?.value
						?.let { it as? GStringValue }
						?.value
				}
			}

			field("fields" of List(!Field)) {
				argument("includeDeprecated" of Boolean default value(false))

				resolve<List<GFieldDefinition>?> { type ->
					if (type !is GNode.WithFieldDefinitions)
						return@resolve null

					var fieldDefinitions = type.fieldDefinitions
					if (arguments["includeDeprecated"] as Boolean)
						fieldDefinitions = fieldDefinitions.filter { it.deprecation === null }

					return@resolve fieldDefinitions
				}
			}

			field("interfaces" of List(!Type)) {
				resolve<List<GInterfaceType>?> { type ->
					(type as? GNode.WithInterfaces)
						?.interfaces
						?.map { interfaceTypeRef ->
							TypeResolver.resolveTypeAs<GInterfaceType>(introspectedSchema, interfaceTypeRef, includeIntrospection = false)
								?: error("Introspection cannot resolve type '$interfaceTypeRef'. The schema should be validated before execution.")
						}
				}
			}

			field("possibleTypes" of List(!Type)) {
				resolve<List<GType>?> { type ->
					(type as? GAbstractType)
						?.let { introspectedSchema.getPossibleTypes(it) }
				}
			}

			field("enumValues" of List(!EnumValue)) {
				argument("includeDeprecated" of Boolean default value(false))

				resolve<List<GEnumValueDefinition>?> { type ->
					if (type !is GEnumType)
						return@resolve null

					var values = type.values
					if (arguments["includeDeprecated"] as Boolean)
						values = values.filter { it.deprecation === null }

					return@resolve values
				}
			}

			field("inputFields" of List(!InputValue)) {
				resolve<List<GArgumentDefinition>?> { (it as? GNode.WithArgumentDefinitions)?.argumentDefinitions }
			}

			field("ofType" of Type) {
				resolve<GType?> { (it as? GWrappingType)?.wrappedType }
			}
		}

		Object<GFieldDefinition>(Field) {
			description(
				"Object and Interface types are described by a list of Fields, each of which has a name, potentially a list of arguments, " +
					"and a return type."
			)

			field("name" of !String) {
				resolve<String> { it.name }
			}

			field("description" of String) {
				resolve<String?> { it.description }
			}

			field("args" of !List(!InputValue)) {
				resolve<List<GArgumentDefinition>> { it.argumentDefinitions }
			}

			field("type" of !Type) {
				resolve<GType> { fieldDefinition ->
					TypeResolver.resolveType(introspectedSchema, fieldDefinition.type, includeIntrospection = false)
						?: error("Introspection cannot resolve type '${fieldDefinition.type}'. The schema should be validated before execution.")
				}
			}

			field("isDeprecated" of !Boolean) {
				resolve<Boolean> { it.deprecation !== null }
			}

			field("deprecationReason" of String) {
				resolve<String?> { it.deprecationReason }
			}
		}

		Object<GArgumentDefinition>(InputValue) {
			description(
				"Arguments provided to Fields or Directives and the input fields of an InputObject are represented as Input Values which describe their " +
					"type and optionally a default value."
			)

			field("name" of !String) {
				resolve<String> { it.name }
			}

			field("description" of String) {
				resolve<String?> { it.description }
			}

			field("type" of !Type) {
				resolve<GType> { argumentDefinition ->
					TypeResolver.resolveType(introspectedSchema, argumentDefinition.type, includeIntrospection = false)
						?: error("Introspection cannot resolve type '${argumentDefinition.type}'. The schema should be validated before execution.")
				}
			}

			field("defaultValue" of String) {
				description("A GraphQL-formatted string representing the default value for this input value.")

				resolve<String?> { it.defaultValue?.toString() }
			}
		}

		Object<GEnumValueDefinition>(EnumValue) {
			description(
				"One possible value for a given Enum. Enum values are unique values, not a placeholder for a string or numeric value. " +
					"However an Enum value is returned in a JSON response as a string."
			)

			field("name" of !String) {
				resolve<String> { it.name }
			}

			field("description" of String) {
				resolve<String?> { it.description }
			}

			field("isDeprecated" of !Boolean) {
				resolve<Boolean> { it.deprecation !== null }
			}

			field("deprecationReason" of String) {
				resolve<String?> { it.deprecationReason }
			}
		}

		Enum(TypeKind) {
			value("SCALAR") {
				description("Indicates this type is a scalar.")
			}
			value("OBJECT") {
				description("Indicates this type is an object. `fields` and `interfaces` are valid fields.")
			}
			value("INTERFACE") {
				description("Indicates this type is an interface. `fields`, `interfaces`, and `possibleTypes` are valid fields.")
			}
			value("UNION") {
				description("Indicates this type is a union. `possibleTypes` is a valid field.")
			}
			value("ENUM") {
				description("Indicates this type is an enum. `enumValues` is a valid field.")
			}
			value("INPUT_OBJECT") {
				description("Indicates this type is an input object. `inputFields` is a valid field.")
			}
			value("LIST") {
				description("Indicates this type is a list. `ofType` is a valid field.")
			}
			value("NON_NULL") {
				description("Indicates this type is a non-null. `ofType` is a valid field.")
			}
		}

		Object<GDirectiveDefinition>(Directive) {
			description(
				"A Directive provides a way to describe alternate runtime execution and type validation behavior in a GraphQL document.\n\n" +
					"In some cases, you need to provide options to alter GraphQL's execution behavior in ways field arguments will not suffice, " +
					"such as conditionally including or skipping a field. Directives provide this by describing additional information to the executor."
			)

			field("name" of !String) {
				resolve<String> { it.name }
			}

			field("description" of String) {
				resolve<String?> { it.description }
			}

			field("locations" of !List(!DirectiveLocation)) {
				resolve<List<String>> { definition ->
					definition.locations.map { it.name }.sorted()
				}
			}

			field("args" of !List(!InputValue)) {
				resolve<List<GDirectiveArgumentDefinition>> { it.argumentDefinitions }
			}
		}

		Enum(DirectiveLocation) {
			value("QUERY") {
				description("Location adjacent to a query operation.")
			}
			value("MUTATION") {
				description("Location adjacent to a mutation operation.")
			}
			value("SUBSCRIPTION") {
				description("Location adjacent to a subscription operation.")
			}
			value("FIELD") {
				description("Location adjacent to a field.")
			}
			value("FRAGMENT_DEFINITION") {
				description("Location adjacent to a fragment definition.")
			}
			value("FRAGMENT_SPREAD") {
				description("Location adjacent to a fragment spread.")
			}
			value("INLINE_FRAGMENT") {
				description("Location adjacent to an inline fragment.")
			}
			value("VARIABLE_DEFINITION") {
				description("Location adjacent to a variable definition.")
			}
			value("SCHEMA") {
				description("Location adjacent to a schema definition.")
			}
			value("SCALAR") {
				description("Location adjacent to a scalar definition.")
			}
			value("OBJECT") {
				description("Location adjacent to an object type definition.")
			}
			value("FIELD_DEFINITION") {
				description("Location adjacent to a field definition.")
			}
			value("ARGUMENT_DEFINITION") {
				description("Location adjacent to an argument definition.")
			}
			value("INTERFACE") {
				description("Location adjacent to an interface definition.")
			}
			value("UNION") {
				description("Location adjacent to a union definition.")
			}
			value("ENUM") {
				description("Location adjacent to an enum definition.")
			}
			value("ENUM_VALUE") {
				description("Location adjacent to an enum value definition.")
			}
			value("INPUT_OBJECT") {
				description("Location adjacent to an input object type definition.")
			}
			value("INPUT_FIELD_DEFINITION") {
				description("Location adjacent to an input object field definition.")
			}
		}
	}


	val directiveType: GObjectType = TypeResolver.resolveTypeAs(schema, Directive)!!
	val directiveLocationType: GEnumType = TypeResolver.resolveTypeAs(schema, DirectiveLocation)!!
	val enumValueType: GObjectType = TypeResolver.resolveTypeAs(schema, EnumValue)!!
	val fieldType: GObjectType = TypeResolver.resolveTypeAs(schema, Field)!!
	val inputValueType: GObjectType = TypeResolver.resolveTypeAs(schema, InputValue)!!
	val schemaType: GObjectType = TypeResolver.resolveTypeAs(schema, Schema)!!
	val typeType: GObjectType = TypeResolver.resolveTypeAs(schema, Type)!!
	val typeKindType: GEnumType = TypeResolver.resolveTypeAs(schema, TypeKind)!!

	val schemaField = GFieldDefinition(
		name = "__schema",
		type = Schema.nonNullableRef,
		description = "Access the current type schema of this server.",
		extensions = GNodeExtensionSet {
			resolver = GFieldResolver<GSchema> { it }
		}
	)

	val typeField = GFieldDefinition(
		name = "__type",
		type = Type,
		description = "Request the type information of a single type.",
		argumentDefinitions = listOf(
			GFieldArgumentDefinition(
				name = "name",
				type = GStringTypeRef.nonNullableRef
			)
		),
		extensions = GNodeExtensionSet {
			resolver = GFieldResolver<GSchema> { TypeResolver.resolveType(it, arguments["name"] as String) }
		}
	)

	val typenameField = GFieldDefinition(
		name = "__typename",
		type = GStringTypeRef.nonNullableRef,
		description = "The name of the current Object type at runtime.",
		extensions = GNodeExtensionSet {
			resolver = GFieldResolver<GObjectType> { it.name }
		}
	)


	private val GFieldResolverContext.introspectedSchema: GSchema
		get() = execution.root as GSchema
}
