package testing

import io.fluidsonic.graphql.*
import kotlin.test.*
import kotlinx.coroutines.test.*

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
		assertNull(result["data"])
		assertNotNull(result["errors"])
	}


	@Test
	fun testValidationErrorReturnsNullData() = runTest {
		val schema = GraphQL.schema {
			Query {
				field("foo" of String) { resolve { "bar" } }
			}
		}
		val executor = GExecutor.default(schema = schema)
		// unknownField does not exist on Query — causes a runtime field error
		val result = executor.serializeResult(executor.execute("{ unknownField }"))
		assertNull(result["data"])
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
			actual = result
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
			actual = result
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
			actual = result
		)
	}
}
