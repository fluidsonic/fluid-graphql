package testing

import io.fluidsonic.graphql.*
import kotlin.test.*
import kotlinx.coroutines.test.*

// GraphQL Spec §6.2 — Executing Operations
class ExecutingOperationsTests {

	@Test
	fun testQueryFieldsCanExecuteInParallel() = runTest {
		val schema = GraphQL.schema {
			Query {
				field("a" of String) { resolve { "aValue" } }
				field("b" of String) { resolve { "bValue" } }
				field("c" of String) { resolve { "cValue" } }
			}
		}
		val executor = GExecutor.default(schema = schema)
		val result = executor.serializeResult(executor.execute("{ a b c }"))
		assertEquals(
			expected = mapOf("data" to mapOf("a" to "aValue", "b" to "bValue", "c" to "cValue")),
			actual = result
		)
	}


	@Test
	fun testMutationFieldsExecuteSerially() = runTest {
		val executionOrder = mutableListOf<String>()
		val schema = GraphQL.schema {
			Mutation {
				field("first" of String) {
					resolve {
						executionOrder += "first"
						"firstResult"
					}
				}
				field("second" of String) {
					resolve {
						executionOrder += "second"
						"secondResult"
					}
				}
			}
		}
		val executor = GExecutor.default(schema = schema)
		executor.execute("mutation { first second }")
		assertEquals(expected = listOf("first", "second"), actual = executionOrder)
	}


	@Test
	fun testAnonymousQueryExecution() = runTest {
		val schema = GraphQL.schema {
			Query {
				field("hello" of String) { resolve { "world" } }
			}
		}
		val executor = GExecutor.default(schema = schema)
		// Shorthand anonymous query
		val result = executor.serializeResult(executor.execute("{ hello }"))
		assertEquals(
			expected = mapOf("data" to mapOf("hello" to "world")),
			actual = result
		)
	}


	@Test
	fun testNamedQueryExecution() = runTest {
		val schema = GraphQL.schema {
			Query {
				field("hello" of String) { resolve { "world" } }
			}
		}
		val executor = GExecutor.default(schema = schema)
		val result = executor.serializeResult(executor.execute("query MyQuery { hello }"))
		assertEquals(
			expected = mapOf("data" to mapOf("hello" to "world")),
			actual = result
		)
	}


	@Test
	fun testQueryWithVariables() = runTest {
		val schema = GraphQL.schema {
			Query {
				field("greet" of String) {
					argument("name" of String)
					resolve { "Hello, ${arguments["name"]}!" }
				}
			}
		}
		val executor = GExecutor.default(schema = schema)
		val result = executor.serializeResult(
			executor.execute(
				documentSource = "query(\$name: String) { greet(name: \$name) }",
				variableValues = mapOf("name" to "Alice")
			)
		)
		assertEquals(
			expected = mapOf("data" to mapOf("greet" to "Hello, Alice!")),
			actual = result
		)
	}


	@Test
	fun testQueryWithDefaultVariable() = runTest {
		val schema = GraphQL.schema {
			Query {
				field("flag" of Boolean) {
					argument("enabled" of Boolean)
					resolve { arguments["enabled"] as Boolean? ?: false }
				}
			}
		}
		val executor = GExecutor.default(schema = schema)
		// Default value of true specified in the query, no value passed in variableValues
		val result = executor.serializeResult(
			executor.execute(
				documentSource = "query(\$enabled: Boolean = true) { flag(enabled: \$enabled) }",
				variableValues = emptyMap()
			)
		)
		assertEquals(
			expected = mapOf("data" to mapOf("flag" to true)),
			actual = result
		)
	}


	@Test
	fun testNullVariableValueVsOmittedVariable() = runTest {
		val schema = GraphQL.schema {
			Query {
				field("echo" of String) {
					argument("value" of String)
					resolve {
						val v = arguments["value"]
						if (v == null) "was-null" else "was-$v"
					}
				}
			}
		}
		val executor = GExecutor.default(schema = schema)

		// Providing a value
		val resultWithValue = executor.serializeResult(
			executor.execute(
				documentSource = "query(\$value: String) { echo(value: \$value) }",
				variableValues = mapOf("value" to "hello")
			)
		)
		assertEquals(
			expected = mapOf("data" to mapOf("echo" to "was-hello")),
			actual = resultWithValue
		)

		// Omitting the variable — no default, so argument is null
		val resultOmitted = executor.serializeResult(
			executor.execute(
				documentSource = "query(\$value: String) { echo(value: \$value) }",
				variableValues = emptyMap()
			)
		)
		assertEquals(
			expected = mapOf("data" to mapOf("echo" to "was-null")),
			actual = resultOmitted
		)
	}
}
