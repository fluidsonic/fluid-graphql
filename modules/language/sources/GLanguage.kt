package io.fluidsonic.graphql


/**
 * Constants, built-in directive definitions, and validation utilities for the GraphQL language.
 *
 * The default directive definitions ([defaultDeprecatedDirective], [defaultIncludeDirective],
 * [defaultSkipDirective], [defaultSpecifiedByDirective]) are automatically added to every schema
 * built with the [GSchema] factory. The non-standard [defaultOptionalDirective] is added only
 * when `supportOptional = true` is passed.
 *
 * Name validation functions follow the GraphQL October 2021 specification.
 */
// https://spec.graphql.org/October2021/
public object GLanguage {

	/** Default conventional root type name for query operations. */
	public const val defaultQueryTypeName: String = "Query"

	/** Default conventional root type name for mutation operations. */
	public const val defaultMutationTypeName: String = "Mutation"

	/** Default conventional root type name for subscription operations. */
	public const val defaultSubscriptionTypeName: String = "Subscription"

	private const val introspectionNamePrefix = "__"
	private val nameRegex = Regex("[_A-Za-z][_0-9A-Za-z]*")


	// FIXME add default directives to schemas
	/** The built-in `@deprecated` directive for marking schema elements as no longer supported. */
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


	/** The built-in `@include(if: Boolean!)` directive for conditionally including a field or fragment. */
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


	/**
	 * Non-standard `@optional` directive that allows omitting a value for a non-null argument or input field.
	 *
	 * Must be enabled explicitly by passing `supportOptional = true` to the [GSchema] factory.
	 *
	 * @see <a href="https://github.com/graphql/graphql-spec/issues/872">graphql-spec#872</a>
	 */
	// Non-standard, see https://github.com/graphql/graphql-spec/issues/872
	public val defaultOptionalDirective: GDirectiveDefinition = GDirectiveDefinition(
		name = "optional",
		description = "Accepts the complete absence of a value even if the argument is of a non-null type.",
		locations = setOf(
			GDirectiveLocation.ARGUMENT_DEFINITION,
			GDirectiveLocation.INPUT_FIELD_DEFINITION,
		)
	)


	/** The built-in `@skip(if: Boolean!)` directive for conditionally skipping a field or fragment. */
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


	/** The built-in `@specifiedBy(url: String!)` directive for linking a custom scalar to its specification. */
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


	/**
	 * Returns `true` if [name] is a valid GraphQL enum value name.
	 *
	 * Enum value names must be valid identifiers, must not start with `__`,
	 * and must not be `true`, `false`, or `null`.
	 */
	// https://spec.graphql.org/October2021/#sec-Enum-Value
	// https://spec.graphql.org/October2021/#sec-Names.Reserved-Names
	public fun isValidEnumValue(name: String): Boolean =
		!name.startsWith(introspectionNamePrefix) &&
			name != "true" &&
			name != "false" &&
			name != "null" &&
			isValidName(name)


	/**
	 * Returns `true` if [name] is a valid GraphQL field name.
	 *
	 * Field names must be valid identifiers and must not start with `__`.
	 */
	// https://spec.graphql.org/October2021/#sec-Objects
	// https://spec.graphql.org/October2021/#sec-Names.Reserved-Names
	public fun isValidFieldName(name: String): Boolean =
		!name.startsWith(introspectionNamePrefix) &&
			isValidName(name)


	/**
	 * Returns `true` if [name] is a valid GraphQL fragment name.
	 *
	 * Fragment names must be valid identifiers, must not start with `__`, and must not be `on`.
	 */
	// https://spec.graphql.org/October2021/#sec-Language.Fragments
	// https://spec.graphql.org/October2021/#sec-Names.Reserved-Names
	public fun isValidFragmentName(name: String): Boolean =
		!name.startsWith(introspectionNamePrefix) &&
			name != "on" &&
			isValidName(name)


	/**
	 * Returns `true` if [name] is a valid GraphQL input value (argument/field) name.
	 *
	 * Input value names must be valid identifiers and must not start with `__`.
	 */
	// https://spec.graphql.org/October2021/#InputValueDefinition
	// https://spec.graphql.org/October2021/#sec-Names.Reserved-Names
	public fun isValidInputValueName(name: String): Boolean =
		!name.startsWith(introspectionNamePrefix) &&
			isValidName(name)


	/**
	 * Returns `true` if [name] is a valid introspection identifier.
	 *
	 * Introspection names must be valid identifiers and must start with `__`.
	 */
	// https://spec.graphql.org/October2021/#Name
	// https://spec.graphql.org/October2021/#sec-Names.Reserved-Names
	public fun isValidIntrospectionName(name: String): Boolean =
		name.startsWith(introspectionNamePrefix) &&
			isValidName(name)


	/**
	 * Returns `true` if [name] matches the GraphQL `Name` production: `[_A-Za-z][_0-9A-Za-z]*`.
	 *
	 * This is the base check used by all the more specific `isValid*` functions.
	 */
	// https://spec.graphql.org/October2021/#Name
	public fun isValidName(name: String): Boolean =
		nameRegex matches name


	/**
	 * Returns `true` if [name] is a valid user-defined GraphQL type name.
	 *
	 * Type names must be valid identifiers and must not start with `__` (reserved for introspection).
	 */
	// https://spec.graphql.org/October2021/#TypeDefinition
	// https://spec.graphql.org/October2021/#sec-Names.Reserved-Names
	public fun isValidTypeName(name: String): Boolean =
		!name.startsWith(introspectionNamePrefix) &&
			isValidName(name)
}
