package testing

import io.fluidsonic.graphql.*
import kotlin.test.*
import kotlinx.coroutines.test.*

// GraphQL Spec §6.3 — Executing Selection Sets
class ExecutingSelectionSetsTests {

	@Test
	fun testFlatSelectionSet() = runTest {
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
	fun testFragmentSpreadMerging() = runTest {
		val schema = GraphQL.schema {
			Query {
				field("foo" of String) { resolve { "fooValue" } }
				field("bar" of String) { resolve { "barValue" } }
			}
		}
		val executor = GExecutor.default(schema = schema)
		val document = """
			{ foo ...F }
			fragment F on Query { bar }
		""".trimIndent()
		val result = executor.serializeResult(executor.execute(document))
		assertEquals(
			expected = mapOf("data" to mapOf("foo" to "fooValue", "bar" to "barValue")),
			actual = result
		)
	}


	@Test
	fun testInlineFragmentMerging() = runTest {
		val schema = GraphQL.schema {
			Query {
				field("foo" of String) { resolve { "fooValue" } }
				field("bar" of String) { resolve { "barValue" } }
			}
		}
		val executor = GExecutor.default(schema = schema)
		val document = """
			{ foo ... { bar } }
		""".trimIndent()
		val result = executor.serializeResult(executor.execute(document))
		assertEquals(
			expected = mapOf("data" to mapOf("foo" to "fooValue", "bar" to "barValue")),
			actual = result
		)
	}


	@Test
	fun testSameResponseNameMerging() = runTest {
		val schema = GraphQL.schema {
			Query {
				field("foo" of String) { resolve { "fooValue" } }
			}
		}
		val executor = GExecutor.default(schema = schema)
		// Two aliases with same name selecting the same field — result should contain key once
		val result = executor.serializeResult(executor.execute("{ x: foo x: foo }"))
		val data = result["data"] as Map<*, *>
		assertEquals(expected = 1, actual = data.size)
		assertEquals(expected = "fooValue", actual = data["x"])
	}


	@Test
	fun testSkipDirectiveExcludesField() = runTest {
		val schema = GraphQL.schema {
			Query {
				field("included" of String) { resolve { "yes" } }
				field("excluded" of String) { resolve { "no" } }
			}
		}
		val executor = GExecutor.default(schema = schema)
		val result = executor.serializeResult(executor.execute("{ included excluded @skip(if: true) }"))
		val data = result["data"] as Map<*, *>
		assertTrue("excluded" !in data)
		assertEquals(expected = "yes", actual = data["included"])
	}


	@Test
	fun testSkipFalseIncludesField() = runTest {
		val schema = GraphQL.schema {
			Query {
				field("foo" of String) { resolve { "fooValue" } }
			}
		}
		val executor = GExecutor.default(schema = schema)
		val result = executor.serializeResult(executor.execute("{ foo @skip(if: false) }"))
		val data = result["data"] as Map<*, *>
		assertEquals(expected = "fooValue", actual = data["foo"])
	}


	@Test
	fun testIncludeDirectiveIncludesField() = runTest {
		val schema = GraphQL.schema {
			Query {
				field("foo" of String) { resolve { "fooValue" } }
			}
		}
		val executor = GExecutor.default(schema = schema)
		val result = executor.serializeResult(executor.execute("{ foo @include(if: true) }"))
		val data = result["data"] as Map<*, *>
		assertEquals(expected = "fooValue", actual = data["foo"])
	}


	@Ignore("Known bug: @include(if: false) does not exclude the field")
	@Test
	fun testIncludeFalseExcludesField() = runTest {
		val schema = GraphQL.schema {
			Query {
				field("foo" of String) { resolve { "fooValue" } }
				field("bar" of String) { resolve { "barValue" } }
			}
		}
		val executor = GExecutor.default(schema = schema)
		val result = executor.serializeResult(executor.execute("{ foo @include(if: false) bar }"))
		val data = result["data"] as Map<*, *>
		assertTrue("foo" !in data)
		assertEquals(expected = "barValue", actual = data["bar"])
	}


	@Test
	fun testTypedInlineFragmentOnObject() = runTest {
		val schema = GraphQL.schema {
			val Dog by type

			Object<DogData>(Dog) {
				field("name" of String) { resolve { it.name } }
				field("barks" of Boolean) { resolve { it.barks } }
			}

			Query {
				field("dog" of Dog) {
					resolve { DogData("Rex", barks = true) }
				}
			}
		}
		val executor = GExecutor.default(schema = schema)
		val document = """
			{ dog { name ... on Dog { barks } } }
		""".trimIndent()
		val result = executor.serializeResult(executor.execute(document))
		assertEquals(
			expected = mapOf("data" to mapOf("dog" to mapOf("name" to "Rex", "barks" to true))),
			actual = result
		)
	}


	@Test
	fun testTypedInlineFragmentOnWrongType() = runTest {
		val schema = GraphQL.schema {
			val Dog by type
			val Cat by type

			Object<DogData>(Dog) {
				field("name" of String) { resolve { it.name } }
				field("barks" of Boolean) { resolve { it.barks } }
			}

			Object<CatData>(Cat) {
				field("name" of String) { resolve { it.name } }
				field("meows" of Boolean) { resolve { it.meows } }
			}

			Query {
				field("dog" of Dog) {
					resolve { DogData("Rex", barks = true) }
				}
			}
		}
		val executor = GExecutor.default(schema = schema)
		// Inline fragment on Cat — should not be included since the value is a Dog
		val document = """
			{ dog { name ... on Cat { meows } } }
		""".trimIndent()
		val result = executor.serializeResult(executor.execute(document))
		val dogData = (result["data"] as Map<*, *>)["dog"] as Map<*, *>
		assertTrue("meows" !in dogData)
		assertEquals(expected = "Rex", actual = dogData["name"])
	}


	private data class DogData(val name: String, val barks: Boolean)
	private data class CatData(val name: String, val meows: Boolean)
}
