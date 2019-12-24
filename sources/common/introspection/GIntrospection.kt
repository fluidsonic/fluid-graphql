package io.fluidsonic.graphql


object GIntrospection {

	private const val directiveMetaName = "__Directive"
	private const val directiveLocationMetaName = "__DirectiveLocation"
	private const val enumValueMetaName = "___EnumValue"
	private const val fieldMetaName = "__Field"
	private const val inputValueMetaName = "__InputValue"
	private const val schemaFieldName = "__schema"
	private const val schemaMetaName = "__Schema"
	private const val typeFieldName = "__type"
	private const val typeKindMetaName = "__TypeKind"
	private const val typeMetaName = "__Type"
	private const val typenameFieldName = "__typename"


	// https://graphql.github.io/graphql-spec/June2018/#sec-Schema-Introspection
	val schema = schema {
		val __Directive = type(directiveMetaName)
		val __DirectiveLocation = type(directiveLocationMetaName)
		val __EnumValue = type(enumValueMetaName)
		val __Field = type(fieldMetaName)
		val __InputValue = type(inputValueMetaName)
		val __Schema = type(schemaMetaName)
		val __Type = type(typeMetaName)
		val __TypeKind = type(typeKindMetaName)

		Object<GSchema>(__Schema) {
			description(
				"A GraphQL Schema defines the capabilities of a GraphQL server. " +
					"It exposes all available types and directives on the server, as well as the entry points for query, mutation, and subscription operations."
			)

			field("types" of !List(!__Type)) {
				description("A list of all types supported by this server.")
				resolve { types.values } // FIXME move generic to obj def?
			}

			field("queryType" of !__Type) {
				description("The type that query operations will be rooted at.")
				resolve { queryType }
			}

			field("mutationType" of __Type) {
				description("If this server supports mutation, the type that mutation operations will be rooted at.")
				resolve { mutationType }
			}

			field("subscriptionType" of __Type) {
				description("If this server support subscription, the type that subscription operations will be rooted at.")
				resolve { subscriptionType }
			}

			field("directives" of !List(!__Directive)) {
				description("A list of all directives supported by this server.")
				resolve { directives }
			}
		}

		Object<GType>(__Type) {
			description(
				"The fundamental unit of any GraphQL Schema is the type. " +
					"There are many kinds of types in GraphQL as represented by the `__TypeKind` enum.\n\n" +
					"Depending on the kind of a type, certain fields describe information about that type. " +
					"Scalar types provide no information beyond a name and description, while Enum types provide their values. " +
					"Object and Interface types provide the fields they describe. " +
					"Abstract types, Union and Interface, provide the Object types possible at runtime. " +
					"List and NonNull types compose other types."
			)

			field("kind" of !__TypeKind) {
				resolve { kind.name }
			}

			field("name" of String) {
				resolve { (this as? GNamedType)?.name }
			}

			field("description" of String) {
				resolve { (this as? GNamedType)?.description }
			}

			field("fields" of List(!__Field)) {
				argument("includeDeprecated" of Boolean default false)

				resolve { context ->
					if (this !is GType.WithFields)
						return@resolve null

					var fields = fields.values
					if (context.booleanArgument("includeDeprecated"))
						fields = fields.filterNot { it.isDeprecated }

					return@resolve fields
				}
			}

			field("interfaces" of List(!__Type)) {
				resolve { (it as? GType.WithInterfaces)?.interfaces }
			}

			field("possibleTypes" of List(!__Type)) {
				// FIXME generalize
				resolve { context ->
					when (this) {
						is GInterfaceType ->
							// TODO probably inefficient
							context.schema.types.values
								.filterIsInstance<GType.WithInterfaces>() // FIXME list interfaces?
								.filter { it.interfaces.contains(this) }

						is GUnionType ->
							types

						else ->
							null
					}
				}
			}

			field("enumValues" of List(!__EnumValue)) {
				argument("includeDeprecated" of Boolean default false)

				resolve { context ->
					if (this !is GEnumType)
						return@resolve null

					var values = values.values
					if (context.booleanArgument("includeDeprecated"))
						values = values.filterNot { it.isDeprecated }

					return@resolve values
				}
			}

			field("inputFields" of List(!__InputValue)) {
				resolve { (it as? GType.WithArguments)?.arguments?.values }
			}

			field("ofType" of __Type) {
				resolve { (it as? GWrappingType)?.ofType }
			}
		}

		Object<GFieldDefinition>(__Field) {
			description(
				"Object and Interface types are described by a list of Fields, each of which has a name, potentially a list of arguments, " +
					"and a return type."
			)

			field("name" of !String) {
				resolve { name }
			}

			field("description" of String) {
				resolve { description }
			}

			field("args" of !List(!__InputValue)) {
				resolve { arguments.values }
			}

			field("type" of !__Type) {
				resolve { type }
			}

			field("isDeprecated" of !Boolean) {
				resolve { isDeprecated }
			}

			field("deprecationReason" of String) {
				resolve { deprecationReason }
			}

		}

		Object(__InputValue) {
			description(
				"Arguments provided to Fields or Directives and the input fields of an InputObject are represented as Input Values which describe their " +
					"type and optionally a default value."
			)

			field("name" of !String)
			field("description" of String)
			field("type" of !__Type)
			field("defaultValue" of !String) {
				description("A GraphQL-formatted string representing the default value for this input value.")
			}
		}

		Object(__EnumValue) {
			description(
				"One possible value for a given Enum. Enum values are unique values, not a placeholder for a string or numeric value. " +
					"However an Enum value is returned in a JSON response as a string."
			)

			field("name" of !String)
			field("description" of String)
			field("isDeprecated" of !Boolean)
			field("deprecationReason" of String)
		}

		Enum(__TypeKind) {
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

		Object(__Directive) {
			description(
				"A Directive provides a way to describe alternate runtime execution and type validation behavior in a GraphQL document.\n\n" +
					"In some cases, you need to provide options to alter GraphQL's execution behavior in ways field arguments will not suffice, " +
					"such as conditionally including or skipping a field. Directives provide this by describing additional information to the executor."
			)

			field("name" of !String)
			field("description" of String)
			field("locations" of !List(!__DirectiveLocation))
			field("args" of !List(!__InputValue))
		}

		Enum(__DirectiveLocation) {
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


	val Directive = schema.types[directiveMetaName] as GObjectType
	val DirectiveLocation = schema.types[directiveLocationMetaName] as GEnumType
	val EnumValue = schema.types[enumValueMetaName] as GObjectType
	val Field = schema.types[fieldMetaName] as GObjectType
	val InputValue = schema.types[inputValueMetaName] as GObjectType
	val Schema = schema.types[schemaMetaName] as GObjectType
	val Type = schema.types[typeMetaName] as GObjectType
	val TypeKind = schema.types[typeKindMetaName] as GEnumType


	val schemaField = GFieldDefinition(
		name = schemaFieldName,
		type = GNonNullType(Schema),
		description = "Access the current type schema of this server.",
		resolver = GFieldResolver.of<GType> { schema }
	)

	val typeField = GFieldDefinition(
		name = typeFieldName,
		type = Type,
		description = "Request the type information of a single type.",
		arguments = listOf(
			GArgumentDefinition(
				name = "name",
				type = GNonNullType(GStringType)
			)
		),
		resolver = GFieldResolver.of<GType> { schema.resolveType(it.stringArgument("name")) }
	)

	val typenameField = GFieldDefinition(
		name = typenameFieldName,
		type = GNonNullType(GStringType),
		description = "The name of the current Object type at runtime.",
		resolver = GFieldResolver.of<Nothing> { it.parentType.name }
	)
}
