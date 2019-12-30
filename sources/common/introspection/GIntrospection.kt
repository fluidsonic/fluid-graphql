package io.fluidsonic.graphql


// Internal for now. Review API before making it public.
internal object GIntrospection {

	private val Directive = GTypeRef("__Directive")
	private val DirectiveLocation = GTypeRef("__DirectiveLocation")
	private val EnumValue = GTypeRef("___EnumValue")
	private val Field = GTypeRef("__Field")
	private val InputValue = GTypeRef("__InputValue")
	private val Schema = GTypeRef("__Schema")
	private val Type = GTypeRef("__Type")
	private val TypeKind = GTypeRef("__TypeKind")


	// https://graphql.github.io/graphql-spec/June2018/#sec-Schema-Introspection
	@Suppress("RemoveExplicitTypeArguments")
	val schema = schema {
		Object<GSchema>(Schema) {
			description(
				"A GraphQL Schema defines the capabilities of a GraphQL server. " +
					"It exposes all available types and directives on the server, as well as the entry points for query, mutation, and subscription operations."
			)

			field("types" of !List(!Type)) {
				description("A list of all types supported by this server.")
				resolve<List<GType>> { types }
			}

			field("queryType" of !Type) {
				description("The type that query operations will be rooted at.")
				resolve<GType> { queryType!! } // FIXME optional or not?
			}

			field("mutationType" of Type) {
				description("If this server supports mutation, the type that mutation operations will be rooted at.")
				resolve<GType?> { mutationType }
			}

			field("subscriptionType" of Type) {
				description("If this server support subscription, the type that subscription operations will be rooted at.")
				resolve<GType?> { subscriptionType }
			}

			field("directives" of !List(!Directive)) {
				description("A list of all directives supported by this server.")
				resolve<List<GDirectiveDefinition>> { directiveDefinitions }
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
				resolve<String> { kind.name }
			}

			field("name" of String) {
				resolve<String?> { (this as? GNamedType)?.name }
			}

			field("description" of String) {
				resolve<String?> { (this as? GNamedType)?.description }
			}

			field("fields" of List(!Field)) {
				argument("includeDeprecated" of Boolean default false)

				resolve<List<GFieldDefinition>?> { context ->
					if (this !is GNode.WithFieldDefinitions)
						return@resolve null

					var fieldDefinitions = fieldDefinitions
					if (!context.booleanArgument("includeDeprecated"))
						fieldDefinitions = fieldDefinitions.filter { it.deprecation === null }

					return@resolve fieldDefinitions
				}
			}

			field("interfaces" of List(!Type)) {
				resolve<List<GInterfaceType>?> { context ->
					(this as? GNode.WithInterfaces)
						?.interfaces
						?.mapNotNull { context.schema.resolveTypeAs<GInterfaceType>(it) }
				}
			}

			field("possibleTypes" of List(!Type)) {
				resolve<List<GType>?> { context ->
					(this as? GAbstractType)
						?.let { context.schema.getPossibleTypes(it) }
				}
			}

			field("enumValues" of List(!EnumValue)) {
				argument("includeDeprecated" of Boolean default false)

				resolve<List<GEnumValueDefinition>?> { context ->
					if (this !is GEnumType)
						return@resolve null

					var values = values
					if (!context.booleanArgument("includeDeprecated"))
						values = values.filter { it.deprecation === null }

					return@resolve values
				}
			}

			field("inputFields" of List(!InputValue)) {
				resolve<List<GArgumentDefinition>?> { (this as? GNode.WithArgumentDefinitions)?.argumentDefinitions }
			}

			field("ofType" of Type) {
				resolve<GType?> { (this as? GWrappingType)?.wrappedType }
			}
		}

		Object<GFieldDefinition>(Field) {
			description(
				"Object and Interface types are described by a list of Fields, each of which has a name, potentially a list of arguments, " +
					"and a return type."
			)

			field("name" of !String) {
				resolve<String> { name }
			}

			field("description" of String) {
				resolve<String?> { description }
			}

			field("args" of !List(!InputValue)) {
				resolve<List<GArgumentDefinition>> { argumentDefinitions }
			}

			field("type" of !Type) {
				resolve<GType> { it.schema.resolveType(type)!! }
			}

			field("isDeprecated" of !Boolean) {
				resolve<Boolean> { deprecation !== null }
			}

			field("deprecationReason" of String) {
				resolve<String?> { deprecationReason }
			}

		}

		Object(InputValue) {
			description(
				"Arguments provided to Fields or Directives and the input fields of an InputObject are represented as Input Values which describe their " +
					"type and optionally a default value."
			)

			field("name" of !String)
			field("description" of String)
			field("type" of !Type)
			field("defaultValue" of !String) {
				description("A GraphQL-formatted string representing the default value for this input value.")
			}
		}

		Object(EnumValue) {
			description(
				"One possible value for a given Enum. Enum values are unique values, not a placeholder for a string or numeric value. " +
					"However an Enum value is returned in a JSON response as a string."
			)

			field("name" of !String)
			field("description" of String)
			field("isDeprecated" of !Boolean)
			field("deprecationReason" of String)
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

		Object(Directive) {
			description(
				"A Directive provides a way to describe alternate runtime execution and type validation behavior in a GraphQL document.\n\n" +
					"In some cases, you need to provide options to alter GraphQL's execution behavior in ways field arguments will not suffice, " +
					"such as conditionally including or skipping a field. Directives provide this by describing additional information to the executor."
			)

			field("name" of !String)
			field("description" of String)
			field("locations" of !List(!DirectiveLocation))
			field("args" of !List(!InputValue))
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


	val schemaField = GFieldDefinition(
		name = "__schema",
		type = Schema.nonNullableRef,
		description = "Access the current type schema of this server.",
		resolver = GFieldResolver.of<Any> { it.schema }
	)

	val typeField = GFieldDefinition(
		name = "__type",
		type = Type,
		description = "Request the type information of a single type.",
		arguments = listOf(
			GFieldArgumentDefinition(
				name = "name",
				type = GStringTypeRef.nonNullableRef
			)
		),
		resolver = GFieldResolver.of<Any> { it.schema.resolveType(it.stringArgument("name")) }
	)

	val typenameField = GFieldDefinition(
		name = "__typename",
		type = GStringTypeRef.nonNullableRef,
		description = "The name of the current Object type at runtime.",
		resolver = GFieldResolver.of<Any> { it.parentType.name }
	)
}
