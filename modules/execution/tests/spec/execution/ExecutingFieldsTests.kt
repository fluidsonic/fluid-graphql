package testing

import io.fluidsonic.graphql.*
import kotlin.test.*
import kotlinx.coroutines.test.*

// GraphQL Spec §6.4 — Executing Fields
class ExecutingFieldsTests {

	// --- Basic field execution ---

	@Test
	fun testScalarFieldReturnsValue() = runTest {
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
	fun testNullableFieldReturnsNull() = runTest {
		val schema = GraphQL.schema {
			Query {
				field("nullable" of String) { resolve { null } }
			}
		}
		val executor = GExecutor.default(schema = schema)
		val result = executor.serializeResult(executor.execute("{ nullable }"))
		assertEquals(
			expected = mapOf("data" to mapOf("nullable" to null)),
			actual = result
		)
	}


	@Test
	fun testListFieldReturnsElements() = runTest {
		val schema = GraphQL.schema {
			Query {
				field("items" of List(String)) { resolve { listOf("a", "b", "c") } }
			}
		}
		val executor = GExecutor.default(schema = schema)
		val result = executor.serializeResult(executor.execute("{ items }"))
		assertEquals(
			expected = mapOf("data" to mapOf("items" to listOf("a", "b", "c"))),
			actual = result
		)
	}


	@Test
	fun testNestedObjectField() = runTest {
		val schema = GraphQL.schema {
			val Inner by type

			Object<InnerData>(Inner) {
				field("value" of String) { resolve { it.value } }
			}

			Query {
				field("inner" of Inner) {
					resolve { InnerData("nested") }
				}
			}
		}
		val executor = GExecutor.default(schema = schema)
		val result = executor.serializeResult(executor.execute("{ inner { value } }"))
		assertEquals(
			expected = mapOf("data" to mapOf("inner" to mapOf("value" to "nested"))),
			actual = result
		)
	}


	@Test
	fun testTypenameOnObjectType() = runTest {
		val schema = GraphQL.schema {
			Query {
				field("foo" of String) { resolve { "bar" } }
			}
		}
		val executor = GExecutor.default(schema = schema)
		val result = executor.serializeResult(executor.execute("{ __typename }"))
		assertEquals(
			expected = mapOf("data" to mapOf("__typename" to "Query")),
			actual = result
		)
	}


	@Test
	fun testTypenameOnInterface() = runTest {
		val schema = GraphQL.schema {
			val Animal by type
			val Dog by type

			Interface(Animal) {
				field("name" of String)
			}

			Object<DogData>(Dog implements Animal) {
				field("name" of String) { resolve { it.name } }
			}

			Query {
				field("animal" of Animal) {
					resolve { DogData("Rex") }
				}
			}
		}
		val executor = GExecutor.default(schema = schema)
		val result = executor.serializeResult(executor.execute("{ animal { __typename name } }"))
		val animalData = (result["data"] as Map<*, *>)["animal"] as Map<*, *>
		assertEquals(expected = "Dog", actual = animalData["__typename"])
		assertEquals(expected = "Rex", actual = animalData["name"])
	}


	@Test
	fun testTypenameOnUnion() = runTest {
		val schema = GraphQL.schema {
			val SearchResult by type
			val Dog by type
			val Cat by type

			Object<DogData>(Dog) {
				field("name" of String) { resolve { it.name } }
			}

			Object<CatData>(Cat) {
				field("name" of String) { resolve { it.name } }
			}

			Union(SearchResult with Dog or Cat)

			Query {
				field("search" of SearchResult) {
					resolve { DogData("Buddy") }
				}
			}
		}
		val executor = GExecutor.default(schema = schema)
		val result = executor.serializeResult(
			executor.execute("{ search { __typename ... on Dog { name } } }")
		)
		val searchData = (result["data"] as Map<*, *>)["search"] as Map<*, *>
		assertEquals(expected = "Dog", actual = searchData["__typename"])
		assertEquals(expected = "Buddy", actual = searchData["name"])
	}


	// --- Null propagation ---

	@Test
	fun testNullableFieldErrorBecomesNull() = runTest {
		val schema = GraphQL.schema {
			Query {
				field("safe" of String) { resolve { "ok" } }
				field("risky" of String) {
					resolve {
						GError(message = "field error").throwException()
					}
				}
			}
		}
		val executor = GExecutor.default(schema = schema)
		val result = executor.serializeResult(executor.execute("{ safe risky }"))
		val data = result["data"] as Map<*, *>
		assertEquals(expected = "ok", actual = data["safe"])
		assertNull(data["risky"])
		assertNotNull(result["errors"])
	}


	@Test
	fun testNonNullFieldPropagatesError() = runTest {
		val schema = GraphQL.schema {
			val Parent by type

			Object<ParentData>(Parent) {
				field("nonNullChild" of !String) {
					resolve { GError(message = "non-null field error").throwException() }
				}
			}

			Query {
				field("parent" of Parent) {
					resolve { ParentData() }
				}
			}
		}
		val executor = GExecutor.default(schema = schema)
		val result = executor.serializeResult(executor.execute("{ parent { nonNullChild } }"))
		// Non-null field returning null propagates error to nullable parent
		val data = result["data"] as Map<*, *>
		assertNull(data["parent"])
		assertNotNull(result["errors"])
	}


	@Test
	fun testNonNullChainPropagation() = runTest {
		// Non-null field errors inside an object — error propagates to the enclosing nullable parent field
		val schema = GraphQL.schema {
			val Inner by type

			Object<InnerData>(Inner) {
				field("value" of !String) {
					resolve { GError(message = "chain error").throwException() }
				}
			}

			Query {
				field("inner" of !Inner) {
					resolve { InnerData("ignored") }
				}
			}
		}
		val executor = GExecutor.default(schema = schema)
		val result = executor.serializeResult(executor.execute("{ inner { value } }"))
		// Error propagates: non-null value field errors, inner becomes null, error is reported
		val data = result["data"] as Map<*, *>
		assertNull(data["inner"])
		assertNotNull(result["errors"])
	}


	@Test
	fun testPartialSuccessWithErrors() = runTest {
		val schema = GraphQL.schema {
			Query {
				field("good" of String) { resolve { "goodValue" } }
				field("bad" of String) {
					resolve { GError(message = "bad field").throwException() }
				}
			}
		}
		val executor = GExecutor.default(schema = schema)
		val result = executor.serializeResult(executor.execute("{ good bad }"))
		val data = result["data"] as Map<*, *>
		assertEquals(expected = "goodValue", actual = data["good"])
		assertNull(data["bad"])
		assertNotNull(result["errors"])
	}


	// --- Aliasing ---

	@Test
	fun testAliasChangesResponseKey() = runTest {
		val schema = GraphQL.schema {
			Query {
				field("realName" of String) { resolve { "value" } }
			}
		}
		val executor = GExecutor.default(schema = schema)
		val result = executor.serializeResult(executor.execute("{ myAlias: realName }"))
		val data = result["data"] as Map<*, *>
		assertTrue("myAlias" in data)
		assertTrue("realName" !in data)
		assertEquals(expected = "value", actual = data["myAlias"])
	}


	@Test
	fun testMultipleAliasesSameField() = runTest {
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
			executor.execute("""{ a: greet(name: "Alice") b: greet(name: "Bob") }""")
		)
		assertEquals(
			expected = mapOf("data" to mapOf("a" to "Hello, Alice!", "b" to "Hello, Bob!")),
			actual = result
		)
	}


	// --- Skip/Include combinations ---

	@Test
	fun testSkipAndIncludeBothTrue() = runTest {
		val schema = GraphQL.schema {
			Query {
				field("foo" of String) { resolve { "fooValue" } }
			}
		}
		val executor = GExecutor.default(schema = schema)
		// @skip(if: false) @include(if: true) → field is included
		val result = executor.serializeResult(executor.execute("{ foo @skip(if: false) @include(if: true) }"))
		val data = result["data"] as Map<*, *>
		assertEquals(expected = "fooValue", actual = data["foo"])
	}


	@Test
	fun testSkipTrueOverridesIncludeTrue() = runTest {
		val schema = GraphQL.schema {
			Query {
				field("foo" of String) { resolve { "fooValue" } }
				field("bar" of String) { resolve { "barValue" } }
			}
		}
		val executor = GExecutor.default(schema = schema)
		// @skip(if: true) @include(if: true) → field is excluded (skip wins)
		val result = executor.serializeResult(executor.execute("{ foo @skip(if: true) @include(if: true) bar }"))
		val data = result["data"] as Map<*, *>
		assertTrue("foo" !in data)
		assertEquals(expected = "barValue", actual = data["bar"])
	}


	private data class InnerData(val value: String)
	private data class ParentData(val dummy: String = "")
	private data class DogData(val name: String)
	private data class CatData(val name: String)
}
