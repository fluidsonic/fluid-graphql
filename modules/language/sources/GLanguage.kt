package io.fluidsonic.graphql


// https://spec.graphql.org/October2021/
public object GLanguage {

	public const val defaultQueryTypeName: String = "Query"
	public const val defaultMutationTypeName: String = "Mutation"
	public const val defaultSubscriptionTypeName: String = "Subscription"

	private const val introspectionNamePrefix = "__"
	private val nameRegex = Regex("[_A-Za-z][_0-9A-Za-z]*")


	// FIXME add default directives to schemas
	public val defaultDeprecatedDirective: GDirectiveDefinition = GDirectiveDefinition(
		name = "deprecated",
		argumentDefinitions = listOf(
			GDirectiveArgumentDefinition(
				name = "reason",
				type = GStringTypeRef,
				description = "Explains why this element was deprecated, usually also including a suggestion for how to access supported similar data. " +
					"Formatted using the Markdown syntax (as specified by [CommonMark](https://commonmark.org/).",
				defaultValue = GStringValue("No longer supported")
			)
		),
		description = "Marks an element of a GraphQL schema as no longer supported.",
		locations = setOf(
			GDirectiveLocation.ENUM_VALUE,
			GDirectiveLocation.FIELD_DEFINITION
		)
	)


	public val defaultIncludeDirective: GDirectiveDefinition = GDirectiveDefinition(
		name = "include",
		argumentDefinitions = listOf(
			GDirectiveArgumentDefinition(
				name = "if",
				type = GBooleanTypeRef.nonNullableRef,
				description = "Included when true."
			)
		),
		description = "Directs the executor to include this field or fragment only when the `if` argument is true.",
		locations = setOf(
			GDirectiveLocation.FIELD,
			GDirectiveLocation.FRAGMENT_SPREAD,
			GDirectiveLocation.INLINE_FRAGMENT
		)
	)


	// Non-standard, see https://github.com/graphql/graphql-spec/issues/872
	public val defaultOptionalDirective: GDirectiveDefinition = GDirectiveDefinition(
		name = "optional",
		description = "Accepts the complete absence of a value even if the argument is of a non-null type.",
		locations = setOf(
			GDirectiveLocation.ARGUMENT_DEFINITION,
			GDirectiveLocation.INPUT_FIELD_DEFINITION,
		)
	)


	public val defaultSkipDirective: GDirectiveDefinition = GDirectiveDefinition(
		name = "skip",
		argumentDefinitions = listOf(
			GDirectiveArgumentDefinition(
				name = "if",
				type = GBooleanTypeRef.nonNullableRef,
				description = "Skipped when true."
			)
		),
		description = "Directs the executor to skip this field or fragment when the `if` argument is true.",
		locations = setOf(
			GDirectiveLocation.FIELD,
			GDirectiveLocation.FRAGMENT_SPREAD,
			GDirectiveLocation.INLINE_FRAGMENT
		)
	)


	public val defaultSpecifiedByDirective: GDirectiveDefinition = GDirectiveDefinition(
		name = "specifiedBy",
		argumentDefinitions = listOf(
			GDirectiveArgumentDefinition(
				name = "url",
				type = GStringTypeRef.nonNullableRef,
				description = "The URL that specifies the behavior of this scalar."
			)
		),
		description = "Exposes a URL that specifies the behavior of this scalar.",
		locations = setOf(
			GDirectiveLocation.SCALAR,
		)
	)


	// https://spec.graphql.org/October2021/#sec-Enum-Value
	// https://spec.graphql.org/October2021/#sec-Names.Reserved-Names
	public fun isValidEnumValue(name: String): Boolean =
		!name.startsWith(introspectionNamePrefix) &&
			name != "true" &&
			name != "false" &&
			name != "null" &&
			isValidName(name)


	// https://spec.graphql.org/October2021/#sec-Objects
	// https://spec.graphql.org/October2021/#sec-Names.Reserved-Names
	public fun isValidFieldName(name: String): Boolean =
		!name.startsWith(introspectionNamePrefix) &&
			isValidName(name)


	// https://spec.graphql.org/October2021/#sec-Language.Fragments
	// https://spec.graphql.org/October2021/#sec-Names.Reserved-Names
	public fun isValidFragmentName(name: String): Boolean =
		!name.startsWith(introspectionNamePrefix) &&
			name != "on" &&
			isValidName(name)


	// https://spec.graphql.org/October2021/#InputValueDefinition
	// https://spec.graphql.org/October2021/#sec-Names.Reserved-Names
	public fun isValidInputValueName(name: String): Boolean =
		!name.startsWith(introspectionNamePrefix) &&
			isValidName(name)


	// https://spec.graphql.org/October2021/#Name
	// https://spec.graphql.org/October2021/#sec-Names.Reserved-Names
	public fun isValidIntrospectionName(name: String): Boolean =
		name.startsWith(introspectionNamePrefix) &&
			isValidName(name)


	// https://spec.graphql.org/October2021/#Name
	public fun isValidName(name: String): Boolean =
		nameRegex matches name


	// https://spec.graphql.org/October2021/#TypeDefinition
	// https://spec.graphql.org/October2021/#sec-Names.Reserved-Names
	public fun isValidTypeName(name: String): Boolean =
		!name.startsWith(introspectionNamePrefix) &&
			isValidName(name)
}
