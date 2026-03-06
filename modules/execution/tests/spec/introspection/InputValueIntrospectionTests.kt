package testing

import io.fluidsonic.graphql.*
import kotlin.test.*
import kotlinx.coroutines.test.*

// GraphQL Spec §4.4 — Input Value Introspection
class InputValueIntrospectionTests {

	@Test
	fun testInputValueName() = runTest {
		val schema = GraphQL.schema {
			val MyObject by type
			Query {
				field("obj" of MyObject) { resolve { null } }
			}
			Object<Any>(MyObject) {
				field("compute" of String) {
					argument("factor" of Int)
					resolve { "" }
				}
			}
		}
		val executor = GExecutor.default(schema = schema)
		val result = executor.serializeResult(executor.execute("""
			{
			  __type(name: "MyObject") {
			    fields {
			      name
			      args { name }
			    }
			  }
			}
		""".trimIndent()))

		@Suppress("UNCHECKED_CAST")
		val data = result["data"] as Map<String, Any?>
		@Suppress("UNCHECKED_CAST")
		val type = data["__type"] as Map<String, Any?>
		@Suppress("UNCHECKED_CAST")
		val fields = type["fields"] as List<Map<String, Any?>>
		val computeField = fields.first { it["name"] == "compute" }
		@Suppress("UNCHECKED_CAST")
		val args = computeField["args"] as List<Map<String, Any?>>
		assertEquals(expected = 1, actual = args.size)
		assertEquals(expected = "factor", actual = args[0]["name"])
	}


	@Test
	fun testInputValueType() = runTest {
		val schema = GraphQL.schema {
			val MyObject by type
			Query {
				field("obj" of MyObject) { resolve { null } }
			}
			Object<Any>(MyObject) {
				field("search" of String) {
					argument("query" of String)
					resolve { "" }
				}
			}
		}
		val executor = GExecutor.default(schema = schema)
		val result = executor.serializeResult(executor.execute("""
			{
			  __type(name: "MyObject") {
			    fields {
			      name
			      args {
			        name
			        type { name kind }
			      }
			    }
			  }
			}
		""".trimIndent()))

		@Suppress("UNCHECKED_CAST")
		val data = result["data"] as Map<String, Any?>
		@Suppress("UNCHECKED_CAST")
		val type = data["__type"] as Map<String, Any?>
		@Suppress("UNCHECKED_CAST")
		val fields = type["fields"] as List<Map<String, Any?>>
		val searchField = fields.first { it["name"] == "search" }
		@Suppress("UNCHECKED_CAST")
		val args = searchField["args"] as List<Map<String, Any?>>
		val queryArg = args.first { it["name"] == "query" }
		@Suppress("UNCHECKED_CAST")
		val argType = queryArg["type"] as Map<String, Any?>
		assertEquals(expected = "String", actual = argType["name"])
		assertEquals(expected = "SCALAR", actual = argType["kind"])
	}


	@Test
	fun testInputValueDefaultValueNull() = runTest {
		val schema = GraphQL.schema {
			val MyObject by type
			Query {
				field("obj" of MyObject) { resolve { null } }
			}
			Object<Any>(MyObject) {
				field("find" of String) {
					argument("id" of String)
					resolve { "" }
				}
			}
		}
		val executor = GExecutor.default(schema = schema)
		val result = executor.serializeResult(executor.execute("""
			{
			  __type(name: "MyObject") {
			    fields {
			      name
			      args {
			        name
			        defaultValue
			      }
			    }
			  }
			}
		""".trimIndent()))

		@Suppress("UNCHECKED_CAST")
		val data = result["data"] as Map<String, Any?>
		@Suppress("UNCHECKED_CAST")
		val type = data["__type"] as Map<String, Any?>
		@Suppress("UNCHECKED_CAST")
		val fields = type["fields"] as List<Map<String, Any?>>
		val findField = fields.first { it["name"] == "find" }
		@Suppress("UNCHECKED_CAST")
		val args = findField["args"] as List<Map<String, Any?>>
		val idArg = args.first { it["name"] == "id" }
		assertNull(idArg["defaultValue"])
	}


	@Test
	fun testInputValueDefaultValueString() = runTest {
		val schema = GraphQL.schema {
			val MyObject by type
			Query {
				field("obj" of MyObject) { resolve { null } }
			}
			Object<Any>(MyObject) {
				field("greet" of String) {
					argument("language" of String default value("en"))
					resolve { "" }
				}
			}
		}
		val executor = GExecutor.default(schema = schema)
		val result = executor.serializeResult(executor.execute("""
			{
			  __type(name: "MyObject") {
			    fields {
			      name
			      args {
			        name
			        defaultValue
			      }
			    }
			  }
			}
		""".trimIndent()))

		@Suppress("UNCHECKED_CAST")
		val data = result["data"] as Map<String, Any?>
		@Suppress("UNCHECKED_CAST")
		val type = data["__type"] as Map<String, Any?>
		@Suppress("UNCHECKED_CAST")
		val fields = type["fields"] as List<Map<String, Any?>>
		val greetField = fields.first { it["name"] == "greet" }
		@Suppress("UNCHECKED_CAST")
		val args = greetField["args"] as List<Map<String, Any?>>
		val languageArg = args.first { it["name"] == "language" }
		assertNotNull(languageArg["defaultValue"])
		// The defaultValue is returned as a GraphQL-formatted string representation
		val defaultValue = languageArg["defaultValue"] as String
		assertTrue(defaultValue.contains("en"), "Expected default value to contain 'en', got: $defaultValue")
	}


	@Test
	fun testInputValueIsDeprecatedNotImplemented() = runTest {
		val schema = GraphQL.schema {
			val MyObject by type
			Query {
				field("obj" of MyObject) { resolve { null } }
			}
			Object<Any>(MyObject) {
				field("compute" of String) {
					argument("factor" of Int)
					resolve { "" }
				}
			}
		}
		val executor = GExecutor.default(schema = schema)
		val result = executor.serializeResult(executor.execute("""
			{
			  __type(name: "MyObject") {
			    fields {
			      name
			      args {
			        name
			        isDeprecated
			        deprecationReason
			      }
			    }
			  }
			}
		""".trimIndent()))

		@Suppress("UNCHECKED_CAST")
		val data = result["data"] as Map<String, Any?>
		@Suppress("UNCHECKED_CAST")
		val type = data["__type"] as Map<String, Any?>
		@Suppress("UNCHECKED_CAST")
		val fields = type["fields"] as List<Map<String, Any?>>
		val computeField = fields.first { it["name"] == "compute" }
		@Suppress("UNCHECKED_CAST")
		val args = computeField["args"] as List<Map<String, Any?>>
		val factorArg = args.first { it["name"] == "factor" }
		assertEquals(expected = false, actual = factorArg["isDeprecated"])
		assertNull(factorArg["deprecationReason"])
	}
}
