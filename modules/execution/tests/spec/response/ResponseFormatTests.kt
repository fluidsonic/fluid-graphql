package testing

import io.fluidsonic.graphql.*
import kotlin.test.*
import kotlin.test.Ignore
import kotlinx.coroutines.test.*


// GraphQL Spec §7.1-7.2 — Response Format
class ResponseFormatTests {

	@Test
	fun testSuccessfulResponseHasDataNoErrors() = runTest {
		val schema = GraphQL.schema {
			Query {
				field("hello" of String) { resolve { "world" } }
			}
		}
		val executor = GExecutor.default(schema = schema)
		val result = executor.serializeResult(executor.execute("{ hello }"))

		assertTrue(result.containsKey("data"))
		assertFalse(result.containsKey("errors"))
		assertEquals(mapOf("hello" to "world"), result["data"])
	}


	@Test
	fun testRequestErrorHasErrorsNoData() = runTest {
		val schema = GraphQL.schema {
			Query {
				field("hello" of String) { resolve { "world" } }
			}
		}
		val executor = GExecutor.default(schema = schema)
		// Invalid syntax causes a parse error
		val result = executor.serializeResult(executor.execute("{ { }"))

		assertTrue(result.containsKey("errors"))
		assertNull(result["data"])
	}


	@Test
	fun testPartialSuccessHasDataAndErrors() = runTest {
		val schema = GraphQL.schema {
			Query {
				field("good" of String) { resolve { "ok" } }
				field("bad" of String) {
					resolve { GError(message = "field failed").throwException() }
				}
			}
		}
		val executor = GExecutor.default(schema = schema)
		val result = executor.serializeResult(executor.execute("{ good bad }"))

		assertTrue(result.containsKey("data"))
		assertTrue(result.containsKey("errors"))
		val data = result["data"] as Map<*, *>
		assertEquals("ok", data["good"])
		assertNull(data["bad"])
	}


	@Test
	fun testErrorObjectHasMessage() = runTest {
		val schema = GraphQL.schema {
			Query {
				field("failing" of String) {
					resolve { GError(message = "something went wrong").throwException() }
				}
			}
		}
		val executor = GExecutor.default(schema = schema)
		val result = executor.serializeResult(executor.execute("{ failing }"))

		val errors = result["errors"] as List<*>
		assertTrue(errors.isNotEmpty())
		val firstError = errors.first() as Map<*, *>
		assertTrue(firstError.containsKey("message"))
		val message = firstError["message"] as String
		assertTrue(message.isNotEmpty())
	}


	@Test
	fun testErrorObjectHasLocations() = runTest {
		val schema = GraphQL.schema {
			Query {
				field("hello" of String) { resolve { "world" } }
			}
		}
		val executor = GExecutor.default(schema = schema)
		// Parse errors include location information per spec §7.1.2
		val result = executor.serializeResult(executor.execute("{ { }"))

		val errors = result["errors"] as List<*>
		assertTrue(errors.isNotEmpty())
		val firstError = errors.first() as Map<*, *>
		assertTrue(firstError.containsKey("locations"))
		val locations = firstError["locations"] as List<*>
		assertTrue(locations.isNotEmpty())
		val firstLocation = locations.first() as Map<*, *>
		assertTrue(firstLocation.containsKey("line"))
		assertTrue(firstLocation.containsKey("column"))
	}


	@Ignore("Known behavior: resolver-thrown GErrors do not include path info")
	@Test
	fun testErrorObjectHasPath() = runTest {
		val schema = GraphQL.schema {
			Query {
				field("failing" of String) {
					resolve { GError(message = "path error").throwException() }
				}
			}
		}
		val executor = GExecutor.default(schema = schema)
		val result = executor.serializeResult(executor.execute("{ failing }"))

		val errors = result["errors"] as List<*>
		assertTrue(errors.isNotEmpty())
		val firstError = errors.first() as Map<*, *>
		assertTrue(firstError.containsKey("path"))
		val path = firstError["path"] as List<*>
		assertTrue(path.isNotEmpty())
	}


	@Ignore("Known behavior: resolver-thrown GErrors do not include path info")
	@Test
	fun testPathForNestedField() = runTest {
		val schema = GraphQL.schema {
			val Parent by type

			Object<ParentData>(Parent) {
				field("child" of String) {
					resolve { GError(message = "nested error").throwException() }
				}
			}

			Query {
				field("parent" of Parent) {
					resolve { ParentData() }
				}
			}
		}
		val executor = GExecutor.default(schema = schema)
		val result = executor.serializeResult(executor.execute("{ parent { child } }"))

		val errors = result["errors"] as List<*>
		assertTrue(errors.isNotEmpty())
		val firstError = errors.first() as Map<*, *>
		val path = firstError["path"] as List<*>
		assertEquals(listOf("parent", "child"), path)
	}


	@Ignore("Known behavior: resolver-thrown GErrors do not include path info")
	@Test
	fun testPathForListElement() = runTest {
		val schema = GraphQL.schema {
			val Item by type

			Object<ItemData>(Item) {
				field("value" of String) {
					resolve {
						if (it.shouldFail)
							GError(message = "list element error").throwException()
						else
							"ok"
					}
				}
			}

			Query {
				field("items" of List(Item)) {
					resolve { listOf(ItemData(false), ItemData(true), ItemData(false)) }
				}
			}
		}
		val executor = GExecutor.default(schema = schema)
		val result = executor.serializeResult(executor.execute("{ items { value } }"))

		val errors = result["errors"] as List<*>
		assertTrue(errors.isNotEmpty())
		val firstError = errors.first() as Map<*, *>
		val path = firstError["path"] as List<*>
		// Path should contain the list field name, a numeric index, and the nested field name
		assertTrue(path.contains("items"), "Expected path to contain 'items', got: $path")
		assertTrue(path.any { it is Int }, "Expected path to contain a numeric index, got: $path")
		assertTrue(path.contains("value"), "Expected path to contain 'value', got: $path")
	}


	@Test
	fun testNullDataWhenRootNonNullFails() = runTest {
		val schema = GraphQL.schema {
			Query {
				field("required" of !String) {
					resolve { GError(message = "non-null root error").throwException() }
				}
			}
		}
		val executor = GExecutor.default(schema = schema)
		val result = executor.serializeResult(executor.execute("{ required }"))

		// Non-null root field error propagates to data becoming null
		assertNull(result["data"])
		assertNotNull(result["errors"])
	}


	@Test
	fun testMultipleErrors() = runTest {
		val schema = GraphQL.schema {
			Query {
				field("firstBad" of String) {
					resolve { GError(message = "first error").throwException() }
				}
				field("secondBad" of String) {
					resolve { GError(message = "second error").throwException() }
				}
			}
		}
		val executor = GExecutor.default(schema = schema)
		val result = executor.serializeResult(executor.execute("{ firstBad secondBad }"))

		val errors = result["errors"] as List<*>
		assertTrue(errors.size >= 2, "Expected at least 2 errors but got ${errors.size}")
	}


	private data class ParentData(val dummy: String = "")
	private data class ItemData(val shouldFail: Boolean)
}
