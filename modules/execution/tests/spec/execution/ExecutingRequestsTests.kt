package testing

import io.fluidsonic.graphql.GDocument
import io.fluidsonic.graphql.GExecutor
import io.fluidsonic.graphql.GResult
import io.fluidsonic.graphql.GraphQL
import io.fluidsonic.graphql.default
import io.fluidsonic.graphql.document
import io.fluidsonic.graphql.resolve
import io.fluidsonic.graphql.schema
import io.fluidsonic.graphql.validate
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

// GraphQL Spec §6.1 — Executing Requests
class ExecutingRequestsTests {

	@Test
	fun testParseErrorReturnsErrorWithNoData() = runTest {
		val schema = GraphQL.schema {
			Query {
				field("foo" of String) { resolve { "bar" } }
			}
		}
		val executor = GExecutor.default(schema = schema)
		val result = executor.serializeResult(executor.execute("{ {"))
		// A parse failure is a request error, so the "data" key is absent entirely.
		assertFalse(result.containsKey("data"), "expected the 'data' key to be absent but got: $result")
		assertNotNull(result["errors"])
	}

	@Test
	fun testValidationErrorOmitsData() = runTest {
		val schema = GraphQL.schema {
			Query {
				field("foo" of String) { resolve { "bar" } }
			}
		}
		val executor = GExecutor.default(schema = schema)
		val document = GDocument.parse("{ unknownField }").valueOrThrow()

		val errors = document.validate(schema)
		assertTrue(errors.isNotEmpty(), "expected validation to report errors")

		// A validation failure is a request error, so the "data" key is absent entirely.
		val result = executor.serializeResult(GResult.failure(errors))
		assertFalse(result.containsKey("data"), "expected the 'data' key to be absent but got: $result")
		assertNotNull(result["errors"])
	}

	@Test
	fun testSuccessfulExecution() = runTest {
		val schema = GraphQL.schema {
			Query {
				field("hello" of String) { resolve { "world" } }
			}
		}
		val executor = GExecutor.default(schema = schema)
		val result = executor.serializeResult(executor.execute("{ hello }"))
		assertEquals(
			expected = mapOf("data" to mapOf("hello" to "world")),
			actual = result,
		)
	}

	@Test
	fun testSelectingByOperationName() = runTest {
		val schema = GraphQL.schema {
			Query {
				field("foo" of String) { resolve { "foo-value" } }
				field("bar" of String) { resolve { "bar-value" } }
			}
		}
		val executor = GExecutor.default(schema = schema)
		val document = """
			query OpFoo { foo }
			query OpBar { bar }
		""".trimIndent()
		val result = executor.serializeResult(executor.execute(document, operationName = "OpBar"))
		assertEquals(
			expected = mapOf("data" to mapOf("bar" to "bar-value")),
			actual = result,
		)
	}

	@Test
	fun testErrorWhenNoOperationNameWithMultipleOps() = runTest {
		val schema = GraphQL.schema {
			Query {
				field("foo" of String) { resolve { "foo-value" } }
			}
		}
		val executor = GExecutor.default(schema = schema)
		val document = """
			query OpA { foo }
			query OpB { foo }
		""".trimIndent()
		val result = executor.serializeResult(executor.execute(document, operationName = null))
		assertNotNull(result["errors"])
	}

	@Test
	fun testErrorWhenOperationNameDoesNotMatch() = runTest {
		val schema = GraphQL.schema {
			Query {
				field("foo" of String) { resolve { "foo-value" } }
			}
		}
		val executor = GExecutor.default(schema = schema)
		val result = executor.serializeResult(executor.execute("query MyOp { foo }", operationName = "WrongName"))
		assertNotNull(result["errors"])
	}

	@Test
	fun testNullOperationNameWithSingleOp() = runTest {
		val schema = GraphQL.schema {
			Query {
				field("foo" of String) { resolve { "foo-value" } }
			}
		}
		val executor = GExecutor.default(schema = schema)
		// Single named operation with null operationName — should use the one operation
		val result = executor.serializeResult(executor.execute("query MyOp { foo }", operationName = null))
		assertEquals(
			expected = mapOf("data" to mapOf("foo" to "foo-value")),
			actual = result,
		)
	}
}
