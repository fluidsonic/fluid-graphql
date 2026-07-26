package testing

import io.fluidsonic.graphql.DirectiveLocationValidityRule
import io.fluidsonic.graphql.ValueValidityRule
import kotlin.test.Test

// https://spec.graphql.org/draft/#sec-Type-System.Directives.Built-in-Directives
class BuiltinDirectiveValidationTests {

	// DIRECTIVE_DEFINITION is deliberately absent: the SDL grammar has no directives slot on a
	// directive definition, so that location is asserted by inspecting the definition instead.
	// https://spec.graphql.org/draft/#sec--deprecated
	@Test
	fun testDeprecatedDirective_isAcceptedOnAllSdlReachableLocations() {
		assertValidationRule(
			rule = DirectiveLocationValidityRule,
			errors = emptyList(),
			document = """
				|enum Enum {
				|   VALUE @deprecated
				|}
				|
				|input Input {
				|   field: String @deprecated
				|}
				|
				|type Query {
				|   field(argument: String @deprecated): String @deprecated
				|}
			""",
		)
	}

	// https://spec.graphql.org/draft/#sec-OneOf-Input-Objects.Input-Coercion
	@Test
	fun testOneOfInputObject_rejectsMultipleFields() {
		assertValidationRule(
			rule = ValueValidityRule,
			errors = listOf(
				"""
					Within OneOf Input Object type "OneOfInput", exactly one field must be specified, and the value for that field must be non-null.

					<document>:1:16
					1 | { field(input: { a: 1, b: 2 }) }
					  |                ^

					<document>:12:17
					11 | type Query {
					12 |    field(input: OneOfInput): String
					   |                 ^
					13 |    plain(input: PlainInput): String
				""",
			),
			document = "{ field(input: { a: 1, b: 2 }) }",
			schema = oneOfSchema,
		)
	}

	@Test
	fun testOneOfInputObject_rejectsNoField() {
		assertValidationRule(
			rule = ValueValidityRule,
			errors = listOf(
				"""
					Within OneOf Input Object type "OneOfInput", exactly one field must be specified, and the value for that field must be non-null.

					<document>:1:16
					1 | { field(input: {}) }
					  |                ^

					<document>:12:17
					11 | type Query {
					12 |    field(input: OneOfInput): String
					   |                 ^
					13 |    plain(input: PlainInput): String
				""",
			),
			document = "{ field(input: {}) }",
			schema = oneOfSchema,
		)
	}

	@Test
	fun testOneOfInputObject_rejectsNullField() {
		assertValidationRule(
			rule = ValueValidityRule,
			errors = listOf(
				"""
					Within OneOf Input Object type "OneOfInput", exactly one field must be specified, and the value for that field must be non-null.

					<document>:1:16
					1 | { field(input: { a: null }) }
					  |                ^

					<document>:12:17
					11 | type Query {
					12 |    field(input: OneOfInput): String
					   |                 ^
					13 |    plain(input: PlainInput): String
				""",
			),
			document = "{ field(input: { a: null }) }",
			schema = oneOfSchema,
		)
	}

	@Test
	fun testOneOfInputObject_acceptsExactlyOneNonNullField() {
		assertValidationRule(
			rule = ValueValidityRule,
			errors = emptyList(),
			document = "{ field(input: { a: 1 }) }",
			schema = oneOfSchema,
		)
	}

	// Validation counts syntactic fields only — it cannot know the runtime value of a variable.
	//
	// The variable is `Int!` rather than `Int` because a nullable one is invalid here for a separate
	// reason: a OneOf field's variable must be non-nullable. That is `VariablesInAllowedPositionRule`'s
	// concern, not this rule's, and it is covered by `VariablesInAllowedPositionRuleTest` — using a
	// nullable variable here would make the fixture rely on a document that does not validate.
	@Test
	fun testOneOfInputObject_acceptsSingleFieldProvidedByVariable() {
		assertValidationRule(
			rule = ValueValidityRule,
			errors = emptyList(),
			document = "query someQuery(${'$'}a: Int!) { field(input: { a: ${'$'}a }) }",
			schema = oneOfSchema,
		)
	}

	@Test
	fun testOneOfInputObject_rejectsMultipleFieldsEvenWhenProvidedByVariables() {
		assertValidationRule(
			rule = ValueValidityRule,
			errors = listOf(
				"""
					Within OneOf Input Object type "OneOfInput", exactly one field must be specified, and the value for that field must be non-null.

					<document>:1:50
					1 | query someQuery(${'$'}a: Int, ${'$'}b: Int) { field(input: { a: ${'$'}a, b: ${'$'}b }) }
					  |                                                  ^

					<document>:12:17
					11 | type Query {
					12 |    field(input: OneOfInput): String
					   |                 ^
					13 |    plain(input: PlainInput): String
				""",
			),
			document = "query someQuery(${'$'}a: Int, ${'$'}b: Int) { field(input: { a: ${'$'}a, b: ${'$'}b }) }",
			schema = oneOfSchema,
		)
	}

	// A default value for a OneOf Input Object type is validated the same way.
	@Test
	fun testOneOfInputObject_rejectsInvalidDefaultValue() {
		assertValidationRule(
			rule = ValueValidityRule,
			errors = listOf(
				"""
					Within OneOf Input Object type "OneOfInput", exactly one field must be specified, and the value for that field must be non-null.

					<document>:1:38
					1 | query someQuery(${'$'}input: OneOfInput = { a: 1, b: 2 }) { field(input: ${'$'}input) }
					  |                                      ^
				""",
			),
			document = "query someQuery(${'$'}input: OneOfInput = { a: 1, b: 2 }) { field(input: ${'$'}input) }",
			schema = oneOfSchema,
		)
	}

	@Test
	fun testPlainInputObject_acceptsMultipleFields() {
		assertValidationRule(
			rule = ValueValidityRule,
			errors = emptyList(),
			document = "{ plain(input: { a: 1, b: 2 }) }",
			schema = oneOfSchema,
		)
	}

	private companion object {

		const val oneOfSchema = """
			|input OneOfInput @oneOf {
			|   a: Int
			|   b: Int
			|}
			|
			|input PlainInput {
			|   a: Int
			|   b: Int
			|}
			|
			|type Query {
			|   field(input: OneOfInput): String
			|   plain(input: PlainInput): String
			|}
		"""
	}
}
