package testing

import io.fluidsonic.graphql.GDocument
import io.fluidsonic.graphql.GError
import io.fluidsonic.graphql.GExecutor
import io.fluidsonic.graphql.GResult
import io.fluidsonic.graphql.GRootResolver
import io.fluidsonic.graphql.GRootResolverContext
import io.fluidsonic.graphql.GraphQL
import io.fluidsonic.graphql.default
import io.fluidsonic.graphql.resolve
import io.fluidsonic.graphql.schema
import io.fluidsonic.graphql.validate
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

// Gate A — a request error omits the "data" key entirely, a field error keeps it.
// https://spec.graphql.org/draft/#sec-Response
// https://spec.graphql.org/draft/#sec-Errors
class RequestErrorResponseShapeTests {

	private val querySchema = GraphQL.schema {
		Query {
			field("hello" of String) { resolve { "world" } }
			field("withArgument" of String) {
				argument("value" of Int)
				resolve { "ok" }
			}
		}
	}

	// --- Request error kind 1: parse failure.

	@Test
	fun testParseFailure_omitsDataKey() = runTest {
		val executor = GExecutor.default(schema = querySchema)
		val result = executor.serializeResult(executor.execute("{ { }"))

		assertFalse(result.containsKey("data"), "expected the 'data' key to be absent but got: $result")
		assertTrue(result.containsKey("errors"))
	}

	// --- Request error kind 2: unknown operation name.

	@Test
	fun testUnknownOperationName_omitsDataKey() = runTest {
		val executor = GExecutor.default(schema = querySchema)
		val result = executor.serializeResult(executor.execute("query A { hello }", operationName = "B"))

		assertFalse(result.containsKey("data"), "expected the 'data' key to be absent but got: $result")
		assertTrue(result.containsKey("errors"))
	}

	@Test
	fun testMissingAnonymousOperation_omitsDataKey() = runTest {
		val executor = GExecutor.default(schema = querySchema)
		val result = executor.serializeResult(executor.execute("query A { hello } query B { hello }"))

		assertFalse(result.containsKey("data"), "expected the 'data' key to be absent but got: $result")
		assertTrue(result.containsKey("errors"))
	}

	// --- Request error kind 3: variable coercion failure.

	@Test
	fun testVariableCoercionFailure_omitsDataKey() = runTest {
		val executor = GExecutor.default(schema = querySchema)
		val result = executor.serializeResult(
			executor.execute(
				"query (\$value: Int) { withArgument(value: \$value) }",
				variableValues = mapOf("value" to "not an int"),
			),
		)

		assertFalse(result.containsKey("data"), "expected the 'data' key to be absent but got: $result")
		assertTrue(result.containsKey("errors"))
	}

	// --- Request error kind 4: root resolution failure.

	@Test
	fun testRootResolutionFailure_omitsDataKey() = runTest {
		val executor = GExecutor.default(
			schema = querySchema,
			rootResolver = object : GRootResolver {

				// Deliberately a plain field error — the executor must classify root resolution failures itself.
				override suspend fun GRootResolverContext.resolveRoot(): Any = GError(message = "cannot resolve the root").throwException()
			},
		)
		val result = executor.serializeResult(executor.execute("{ hello }"))

		assertFalse(result.containsKey("data"), "expected the 'data' key to be absent but got: $result")
		assertTrue(result.containsKey("errors"))
	}

	// --- Request error kind 5: mutation against a query-only schema.

	@Test
	fun testMutationAgainstQueryOnlySchema_omitsDataKey() = runTest {
		val executor = GExecutor.default(schema = querySchema)
		val result = executor.serializeResult(executor.execute("mutation { hello }"))

		assertFalse(result.containsKey("data"), "expected the 'data' key to be absent but got: $result")
		assertTrue(result.containsKey("errors"))
	}

	// --- Request error kind 6: validation failure.

	@Test
	fun testValidationErrorsAreRequestErrors() {
		val document = GDocument.parse("{ thisFieldDoesNotExist }").valueOrThrow()
		val errors = document.validate(querySchema)

		assertTrue(errors.isNotEmpty(), "expected validation to report errors")
		for (error in errors) {
			assertTrue(error.isRequestError, "expected a request error but got: $error")
		}
	}

	@Test
	fun testSerializedValidationFailure_omitsDataKey() {
		val document = GDocument.parse("{ thisFieldDoesNotExist }").valueOrThrow()
		val errors = document.validate(querySchema)
		val executor = GExecutor.default(schema = querySchema)

		val result = executor.serializeResult(GResult.failure(errors))

		assertFalse(result.containsKey("data"), "expected the 'data' key to be absent but got: $result")
		assertTrue(result.containsKey("errors"))
	}

	// --- The other direction: a field error keeps the "data" key, set to null.

	@Test
	fun testRootNonNullFieldError_keepsDataKeyAsNull() = runTest {
		val schema = GraphQL.schema {
			Query {
				field("required" of !String) {
					resolve { GError(message = "non-null root error").throwException() }
				}
			}
		}
		val executor = GExecutor.default(schema = schema)
		val result = executor.serializeResult(executor.execute("{ required }"))

		assertTrue(result.containsKey("data"), "expected the 'data' key to be present but got: $result")
		assertNull(result["data"])
		assertTrue(result.containsKey("errors"))
	}

	@Test
	fun testRootNonNullFieldResolvingToNull_keepsDataKeyAsNull() = runTest {
		val schema = GraphQL.schema {
			Query {
				field("required" of !String) { resolve { null } }
			}
		}
		val executor = GExecutor.default(schema = schema)
		val result = executor.serializeResult(executor.execute("{ required }"))

		assertTrue(result.containsKey("data"), "expected the 'data' key to be present but got: $result")
		assertNull(result["data"])
		assertTrue(result.containsKey("errors"))
	}

	// --- serializeResult exercised from outside the executor, as a server front-end does when it builds a
	// failure itself rather than getting one back from `execute`.

	@Test
	fun testSerializeResult_withExternallyBuiltRequestError_omitsDataKey() {
		val executor = GExecutor.default(schema = querySchema)
		val result = executor.serializeResult(
			GResult.failure(GError(message = "built by the caller", isRequestError = true)),
		)

		assertFalse(result.containsKey("data"), "expected the 'data' key to be absent but got: $result")
		assertTrue(result.containsKey("errors"))
	}

	@Test
	fun testSerializeResult_withExternallyBuiltFieldError_keepsDataKeyAsNull() {
		val executor = GExecutor.default(schema = querySchema)
		val result = executor.serializeResult(
			GResult.failure(GError(message = "built by the caller")),
		)

		assertTrue(result.containsKey("data"), "expected the 'data' key to be present but got: $result")
		assertNull(result["data"])
		assertTrue(result.containsKey("errors"))
	}

	@Test
	fun testSerializeResult_withMixedErrors_omitsDataKey() {
		val executor = GExecutor.default(schema = querySchema)
		val result = executor.serializeResult(
			GResult.failure(
				listOf(
					GError(message = "a field error"),
					GError(message = "a request error", isRequestError = true),
				),
			),
		)

		assertFalse(result.containsKey("data"), "expected the 'data' key to be absent but got: $result")
	}
}
