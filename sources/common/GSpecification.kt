package io.fluidsonic.graphql


typealias GSelectionSet = List<GSelection>
typealias GVariableValues = Map<String, GValue>


// https://graphql.github.io/graphql-spec/June2018/
object GSpecification {

	val builtinScalarNames = listOf("Boolean", "Float", "ID", "Int", "String")

	const val defaultQueryTypeName = "Query"
	const val defaultMutationTypeName = "Mutation"
	const val defaultSubscriptionTypeName = "Subscription"
	const val introspectionNamePrefix = "__"

	val nameRegex = Regex("[_A-Za-z][_0-9A-Za-z]*")


	val defaultDeprecatedDirective = GQLInput.DirectiveDefinition(
		name = "deprecated",
		arguments = listOf(
			GQLInput.InputValue(
				name = "reason",
				type = GStringTypeRef,
				description = "Explains why this element was deprecated, usually also including a suggestion for how to access supported similar data. " +
					"Formatted using the Markdown syntax (as specified by [CommonMark](https://commonmark.org/).",
				defaultValue = GValue.String("No longer supported")
			)
		),
		description = "Marks an element of a GraphQL schema as no longer supported.",
		locations = listOf(
			GDirectiveLocation.ENUM_VALUE,
			GDirectiveLocation.FIELD_DEFINITION
		)
	)


	val defaultIncludeDirective = GQLInput.DirectiveDefinition(
		name = "include",
		arguments = listOf(
			GQLInput.InputValue(
				name = "if",
				type = GNonNullTypeRef(GBooleanTypeRef),
				description = "Included when true."
			)
		),
		description = "Directs the executor to include this field or fragment only when the `if` argument is true.",
		locations = listOf(
			GDirectiveLocation.FIELD,
			GDirectiveLocation.FRAGMENT_SPREAD,
			GDirectiveLocation.INLINE_FRAGMENT
		)
	)


	val defaultSkipDirective = GQLInput.DirectiveDefinition(
		name = "skip",
		arguments = listOf(
			GQLInput.InputValue(
				name = "if",
				type = GNonNullTypeRef(GBooleanTypeRef),
				description = "Skipped when true."
			)
		),
		description = "Directs the executor to skip this field or fragment when the `if` argument is true.",
		locations = listOf(
			GDirectiveLocation.FIELD,
			GDirectiveLocation.FRAGMENT_SPREAD,
			GDirectiveLocation.INLINE_FRAGMENT
		)
	)


	// https://graphql.github.io/graphql-spec/June2018/#sec-Enum-Value
	// https://graphql.github.io/graphql-spec/June2018/#sec-Reserved-Names
	fun isValidEnumValue(name: String) =
		!name.startsWith(introspectionNamePrefix) &&
			name != "true" &&
			name != "false" &&
			name != "null" &&
			isValidName(name)


	// https://graphql.github.io/graphql-spec/June2018/#FieldDefinition
	// https://graphql.github.io/graphql-spec/June2018/#sec-Reserved-Names
	fun isValidFieldName(name: String) =
		!name.startsWith(introspectionNamePrefix) &&
			isValidName(name)


	// https://graphql.github.io/graphql-spec/June2018/#InputValueDefinition
	// https://graphql.github.io/graphql-spec/June2018/#sec-Reserved-Names
	fun isValidInputValueName(name: String) =
		!name.startsWith(introspectionNamePrefix) &&
			isValidName(name)


	// https://graphql.github.io/graphql-spec/June2018/#Name
	// https://graphql.github.io/graphql-spec/June2018/#sec-Reserved-Names
	fun isValidIntrospectionName(name: String) =
		name.startsWith(introspectionNamePrefix) &&
			isValidName(name)


	// https://graphql.github.io/graphql-spec/June2018/#Name
	// https://graphql.github.io/graphql-spec/June2018/#sec-Reserved-Names
	fun isValidName(name: String) =
		nameRegex matches name


	// https://graphql.github.io/graphql-spec/June2018/#TypeDefinition
	// https://graphql.github.io/graphql-spec/June2018/#sec-Reserved-Names
	fun isValidTypeName(name: String) =
		!name.startsWith(introspectionNamePrefix) &&
			isValidName(name)


	// https://graphql.github.io/graphql-spec/draft/#MergeSelectionSets()
	fun mergeSelectionSets(fields: List<GFieldSelection>): GSelectionSet {
		val selectionSet = mutableListOf<GSelection>()
		for (field in fields) {
			val fieldSelectionSet = field.selectionSet
			if (fieldSelectionSet.isNotEmpty())
				selectionSet += fieldSelectionSet
		}

		return selectionSet
	}


	private fun requireValidName(name: String) {
		require(isValidName(name)) { "'name' is not a valid type name: $name" }
		require(!builtinScalarNames.contains(name)) { "'name' must not be of a builtin type: $name" }
	}
}
