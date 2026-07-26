package testing

import io.fluidsonic.graphql.GDocument
import io.fluidsonic.graphql.GExecutor
import io.fluidsonic.graphql.GFieldResolver
import io.fluidsonic.graphql.GSchema
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

// These tests deliberately use the `execute(document: GDocument)` overload, which does not validate,
// so that they exercise input coercion itself rather than the validation rules.
//
// Note: `singleOneOf` takes a OneOf Input Object that declares a single field. A two-field type
// cannot be used for the accepting cases because `NodeInputConverter` currently rejects every
// omitted nullable input field ("A value of type 'Int' must be provided for argument 'b'."),
// which is a separate pre-existing defect unrelated to `@oneOf`.
// https://spec.graphql.org/draft/#sec-OneOf-Input-Objects.Input-Coercion
class OneOfDirectiveCoercionTests {

	@Test
	fun testLiteral_rejectsMultipleFields() = runTest {
		assertCoercionError(
			document = "{ field(input: { a: 1, b: 2 }) }",
			expectedError = """
				Within OneOf Input Object type "OneOfInput", exactly one field must be specified, and the value for that field must be non-null.

				<document>:1:16
				1 | { field(input: { a: 1, b: 2 }) }
				  |                ^
			""",
		)
	}

	@Test
	fun testLiteral_rejectsNoField() = runTest {
		assertCoercionError(
			document = "{ singleOneOf(input: {}) }",
			expectedError = """
				Within OneOf Input Object type "SingleOneOfInput", exactly one field must be specified, and the value for that field must be non-null.

				<document>:1:22
				1 | { singleOneOf(input: {}) }
				  |                      ^
			""",
		)
	}

	@Test
	fun testLiteral_rejectsNullField() = runTest {
		assertCoercionError(
			document = "{ singleOneOf(input: { a: null }) }",
			expectedError = """
				Within OneOf Input Object type "SingleOneOfInput", exactly one field must be specified, and the value for that field must be non-null.

				<document>:1:22
				1 | { singleOneOf(input: { a: null }) }
				  |                      ^
			""",
		)
	}

	// The single field is present syntactically, so only its runtime value can violate the constraint.
	@Test
	fun testLiteral_rejectsFieldWhoseVariableIsNull() = runTest {
		assertCoercionError(
			document = "query someQuery(${'$'}a: Int) { singleOneOf(input: { a: ${'$'}a }) }",
			variableValues = mapOf("a" to null),
			expectedError = """
				Expected variable "${'$'}a" provided to field "a" for OneOf Input Object type "SingleOneOfInput" not to be null.

				<document>:1:47
				1 | query someQuery(${'$'}a: Int) { singleOneOf(input: { a: ${'$'}a }) }
				  |                                               ^
			""",
		)
	}

	@Test
	fun testLiteral_rejectsFieldWhoseVariableIsAbsent() = runTest {
		assertCoercionError(
			document = "query someQuery(${'$'}a: Int) { singleOneOf(input: { a: ${'$'}a }) }",
			expectedError = """
				Expected variable "${'$'}a" provided to field "a" for OneOf Input Object type "SingleOneOfInput" to provide a runtime value.

				<document>:1:47
				1 | query someQuery(${'$'}a: Int) { singleOneOf(input: { a: ${'$'}a }) }
				  |                                               ^
			""",
		)
	}

	@Test
	fun testLiteral_acceptsExactlyOneNonNullField() = runTest {
		assertCoercionSuccess(document = "{ singleOneOf(input: { a: 1 }) }", expected = mapOf("singleOneOf" to "resolved"))
	}

	@Test
	fun testVariable_rejectsMultipleFields() = runTest {
		assertCoercionError(
			document = "query someQuery(${'$'}input: OneOfInput) { field(input: ${'$'}input) }",
			variableValues = mapOf("input" to mapOf("a" to 1, "b" to 2)),
			expectedError = """
				Within OneOf Input Object type "OneOfInput", exactly one field must be specified, and the value for that field must be non-null.

				<document>:1:17
				1 | query someQuery(${'$'}input: OneOfInput) { field(input: ${'$'}input) }
				  |                 ^
			""",
		)
	}

	@Test
	fun testVariable_rejectsNoField() = runTest {
		assertCoercionError(
			document = "query someQuery(${'$'}input: OneOfInput) { field(input: ${'$'}input) }",
			variableValues = mapOf("input" to emptyMap<String, Any?>()),
			expectedError = """
				Within OneOf Input Object type "OneOfInput", exactly one field must be specified, and the value for that field must be non-null.

				<document>:1:17
				1 | query someQuery(${'$'}input: OneOfInput) { field(input: ${'$'}input) }
				  |                 ^
			""",
		)
	}

	@Test
	fun testVariable_rejectsNullField() = runTest {
		assertCoercionError(
			document = "query someQuery(${'$'}input: OneOfInput) { field(input: ${'$'}input) }",
			variableValues = mapOf("input" to mapOf("a" to null)),
			expectedError = """
				Within OneOf Input Object type "OneOfInput", exactly one field must be specified, and the value for that field must be non-null.

				<document>:1:17
				1 | query someQuery(${'$'}input: OneOfInput) { field(input: ${'$'}input) }
				  |                 ^
			""",
		)
	}

	// The variable path drops absent fields, so a two-field type works here.
	@Test
	fun testVariable_acceptsExactlyOneNonNullField() = runTest {
		assertCoercionSuccess(
			document = "query someQuery(${'$'}input: OneOfInput) { field(input: ${'$'}input) }",
			variableValues = mapOf("input" to mapOf("a" to 1)),
			expected = mapOf("field" to "resolved"),
		)
	}

	@Test
	fun testPlainInputObject_acceptsMultipleFields() = runTest {
		assertCoercionSuccess(document = "{ plain(input: { a: 1, b: 2 }) }", expected = mapOf("plain" to "resolved"))
	}

	private suspend fun assertCoercionError(document: String, variableValues: Map<String, Any?> = emptyMap(), expectedError: String) {
		val result = executor().execute(GDocument.parse(document).valueOrThrow(), variableValues = variableValues)

		assertErrors(expected = listOf(expectedError), actual = result.errors)
	}

	private suspend fun assertCoercionSuccess(document: String, variableValues: Map<String, Any?> = emptyMap(), expected: Map<String, Any?>) {
		val executor = executor()
		val result = executor.serializeResult(
			executor.execute(GDocument.parse(document).valueOrThrow(), variableValues = variableValues),
		)

		assertEquals(actual = result, expected = mapOf("data" to expected))
	}

	private fun executor(): GExecutor = GExecutor.default(
		schema = schema,
		fieldResolver = GFieldResolver<Any> { "resolved" },
	)

	private companion object {

		val schema: GSchema = GSchema.parse(
			"""
			|input OneOfInput @oneOf {
			|   a: Int
			|   b: Int
			|}
			|
			|input SingleOneOfInput @oneOf {
			|   a: Int
			|}
			|
			|input PlainInput {
			|   a: Int
			|   b: Int
			|}
			|
			|type Query {
			|   field(input: OneOfInput): String
			|   singleOneOf(input: SingleOneOfInput): String
			|   plain(input: PlainInput): String
			|}
			""".trimMargin(),
		).valueOrThrow()
	}
}
