package testing

import io.fluidsonic.graphql.GDocument
import io.fluidsonic.graphql.GExecutor
import io.fluidsonic.graphql.GFieldResolver
import io.fluidsonic.graphql.GSchema
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

// These tests deliberately use the `execute(document: GDocument)` overload, which does not validate,
// so that they exercise argument coercion itself rather than the validation rules.
//
// An argument or input object field that is not supplied, has no default value and accepts null must be
// *absent* from the coerced map rather than present as null or reported as missing.
// https://spec.graphql.org/draft/#sec-Coercing-Field-Arguments
// https://spec.graphql.org/draft/#sec-Input-Objects.Input-Coercion
class OmittedArgumentCoercionTests {

	@Test
	fun testNullableArgumentWithoutDefault_isOmitted() = runTest {
		assertEquals(actual = coerceArguments(document = "{ nullable }"), expected = emptyMap())
	}

	@Test
	fun testNullableArgumentWithDefault_usesDefault() = runTest {
		assertEquals(actual = coerceArguments(document = "{ defaulted }"), expected = mapOf("input" to 42))
	}

	@Test
	fun testNonNullArgumentWithoutDefault_isRejected() = runTest {
		val result = executor { }.execute(GDocument.parse("{ required }").valueOrThrow())

		assertErrors(
			expected = listOf(
				"""
					A value of type 'Int!' must be provided for argument 'input'.

					<document>:1:3
					1 | { required }
					  |   ^
				""",
			),
			actual = result.errors,
		)
	}

	@Test
	fun testOmittedNullableInputObjectField_isOmitted() = runTest {
		assertEquals(
			actual = coerceArguments(document = "{ object(input: { a: 1 }) }"),
			expected = mapOf("input" to mapOf("a" to 1)),
		)
	}

	private suspend fun coerceArguments(document: String): Map<String, Any?> {
		var arguments: Map<String, Any?>? = null
		val result = executor { arguments = it }.execute(GDocument.parse(document).valueOrThrow())

		assertErrors(expected = emptyList(), actual = result.errors)

		return assertNotNull(arguments, "The field resolver was not invoked.")
	}

	private fun executor(captureArguments: (Map<String, Any?>) -> Unit): GExecutor = GExecutor.default(
		schema = schema,
		fieldResolver = GFieldResolver<Any> {
			captureArguments(arguments)

			"resolved"
		},
	)

	private companion object {

		val schema: GSchema = GSchema.parse(
			"""
			|input Input {
			|   a: Int
			|   b: Int
			|}
			|
			|type Query {
			|   nullable(input: Input): String
			|   defaulted(input: Int = 42): String
			|   object(input: Input): String
			|   required(input: Int!): String
			|}
			""".trimMargin(),
		).valueOrThrow()
	}
}
