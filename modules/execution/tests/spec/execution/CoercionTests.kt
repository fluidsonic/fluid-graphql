package testing

import io.fluidsonic.graphql.*
import kotlin.test.*
import kotlinx.coroutines.test.*

// GraphQL Spec §6.4 — Input/Output Coercion
class CoercionTests {

	// --- Input coercion (argument values passed to resolvers) ---

	@Test
	fun testIntInputCoercion() = runTest {
		val schema = GraphQL.schema {
			Query {
				field("echo" of Int) {
					argument("arg" of Int)
					resolve { arguments["arg"] as Int? }
				}
			}
		}
		val executor = GExecutor.default(schema = schema)
		val result = executor.serializeResult(executor.execute("{ echo(arg: 42) }"))
		assertEquals(
			expected = mapOf("data" to mapOf("echo" to 42)),
			actual = result
		)
	}


	@Test
	fun testStringInputCoercion() = runTest {
		val schema = GraphQL.schema {
			Query {
				field("echo" of String) {
					argument("arg" of String)
					resolve { arguments["arg"] as String? }
				}
			}
		}
		val executor = GExecutor.default(schema = schema)
		val result = executor.serializeResult(executor.execute("""{ echo(arg: "hello") }"""))
		assertEquals(
			expected = mapOf("data" to mapOf("echo" to "hello")),
			actual = result
		)
	}


	@Test
	fun testBooleanInputCoercion() = runTest {
		val schema = GraphQL.schema {
			Query {
				field("echo" of Boolean) {
					argument("arg" of Boolean)
					resolve { arguments["arg"] as Boolean? }
				}
			}
		}
		val executor = GExecutor.default(schema = schema)
		val result = executor.serializeResult(executor.execute("{ echo(arg: true) }"))
		assertEquals(
			expected = mapOf("data" to mapOf("echo" to true)),
			actual = result
		)
	}


	@Test
	fun testNullInputCoercion() = runTest {
		val schema = GraphQL.schema {
			Query {
				field("echo" of String) {
					argument("arg" of String)
					resolve {
						val v = arguments["arg"]
						if (v == null) "was-null" else "was-$v"
					}
				}
			}
		}
		val executor = GExecutor.default(schema = schema)
		val result = executor.serializeResult(executor.execute("{ echo(arg: null) }"))
		assertEquals(
			expected = mapOf("data" to mapOf("echo" to "was-null")),
			actual = result
		)
	}


	@Test
	fun testListInputCoercion() = runTest {
		val schema = GraphQL.schema {
			Query {
				field("sum" of Int) {
					argument("nums" of List(Int))
					resolve {
						@Suppress("UNCHECKED_CAST")
						(arguments["nums"] as List<Int>?)?.sum() ?: 0
					}
				}
			}
		}
		val executor = GExecutor.default(schema = schema)
		val result = executor.serializeResult(executor.execute("{ sum(nums: [1, 2, 3]) }"))
		assertEquals(
			expected = mapOf("data" to mapOf("sum" to 6)),
			actual = result
		)
	}


	@Test
	fun testSingleValueCoercedToList() = runTest {
		val schema = GraphQL.schema {
			Query {
				field("first" of Int) {
					argument("nums" of List(Int))
					resolve {
						@Suppress("UNCHECKED_CAST")
						(arguments["nums"] as List<Int>?)?.firstOrNull()
					}
				}
			}
		}
		val executor = GExecutor.default(schema = schema)
		// Single value 1 passed where a list is expected — should be coerced to [1]
		val result = executor.serializeResult(executor.execute("{ first(nums: 1) }"))
		assertEquals(
			expected = mapOf("data" to mapOf("first" to 1)),
			actual = result
		)
	}


	@Test
	fun testEnumInputCoercion() = runTest {
		val schema = GraphQL.schema {
			val Status by type

			Enum(Status) {
				value("ACTIVE")
				value("INACTIVE")
			}

			Query {
				field("echo" of String) {
					argument("status" of Status)
					resolve { arguments["status"] as String? }
				}
			}
		}
		val executor = GExecutor.default(schema = schema)
		val result = executor.serializeResult(executor.execute("{ echo(status: ACTIVE) }"))
		assertEquals(
			expected = mapOf("data" to mapOf("echo" to "ACTIVE")),
			actual = result
		)
	}


	// --- Variable input coercion ---

	@Test
	fun testIntVariableCoercion() = runTest {
		val schema = GraphQL.schema {
			Query {
				field("echo" of Int) {
					argument("x" of Int)
					resolve { arguments["x"] as Int? }
				}
			}
		}
		val executor = GExecutor.default(schema = schema)
		val result = executor.serializeResult(
			executor.execute(
				documentSource = "query(\$x: Int) { echo(x: \$x) }",
				variableValues = mapOf("x" to 5)
			)
		)
		assertEquals(
			expected = mapOf("data" to mapOf("echo" to 5)),
			actual = result
		)
	}


	@Test
	fun testStringVariableCoercion() = runTest {
		val schema = GraphQL.schema {
			Query {
				field("echo" of String) {
					argument("s" of String)
					resolve { arguments["s"] as String? }
				}
			}
		}
		val executor = GExecutor.default(schema = schema)
		val result = executor.serializeResult(
			executor.execute(
				documentSource = "query(\$s: String) { echo(s: \$s) }",
				variableValues = mapOf("s" to "foo")
			)
		)
		assertEquals(
			expected = mapOf("data" to mapOf("echo" to "foo")),
			actual = result
		)
	}


	// --- Output coercion ---

	@Test
	fun testStringOutput() = runTest {
		val schema = GraphQL.schema {
			Query {
				field("value" of String) { resolve { "hello" } }
			}
		}
		val executor = GExecutor.default(schema = schema)
		val result = executor.serializeResult(executor.execute("{ value }"))
		assertEquals(
			expected = mapOf("data" to mapOf("value" to "hello")),
			actual = result
		)
	}


	@Test
	fun testIntOutput() = runTest {
		val schema = GraphQL.schema {
			Query {
				field("value" of Int) { resolve { 42 } }
			}
		}
		val executor = GExecutor.default(schema = schema)
		val result = executor.serializeResult(executor.execute("{ value }"))
		assertEquals(
			expected = mapOf("data" to mapOf("value" to 42)),
			actual = result
		)
	}


	@Test
	fun testBooleanOutput() = runTest {
		val schema = GraphQL.schema {
			Query {
				field("value" of Boolean) { resolve { true } }
			}
		}
		val executor = GExecutor.default(schema = schema)
		val result = executor.serializeResult(executor.execute("{ value }"))
		assertEquals(
			expected = mapOf("data" to mapOf("value" to true)),
			actual = result
		)
	}


	@Test
	fun testNullOutput() = runTest {
		val schema = GraphQL.schema {
			Query {
				field("value" of String) { resolve { null } }
			}
		}
		val executor = GExecutor.default(schema = schema)
		val result = executor.serializeResult(executor.execute("{ value }"))
		assertEquals(
			expected = mapOf("data" to mapOf("value" to null)),
			actual = result
		)
	}


	@Test
	fun testListOutput() = runTest {
		val schema = GraphQL.schema {
			Query {
				field("items" of List(String)) { resolve { listOf("a", "b") } }
			}
		}
		val executor = GExecutor.default(schema = schema)
		val result = executor.serializeResult(executor.execute("{ items }"))
		assertEquals(
			expected = mapOf("data" to mapOf("items" to listOf("a", "b"))),
			actual = result
		)
	}
}
