package testing

import io.fluidsonic.graphql.*
import kotlin.test.*
import kotlinx.coroutines.test.*

// GraphQL Spec §4.3 — Field Introspection
class FieldIntrospectionTests {

	@Test
	fun testFieldName() = runTest {
		val schema = GraphQL.schema {
			val MyObject by type
			Query {
				field("obj" of MyObject) { resolve { null } }
			}
			Object<Any>(MyObject) {
				field("username" of String) { resolve { "" } }
			}
		}
		val executor = GExecutor.default(schema = schema)
		val result = executor.serializeResult(executor.execute("""
			{
			  __type(name: "MyObject") {
			    fields { name }
			  }
			}
		""".trimIndent()))

		@Suppress("UNCHECKED_CAST")
		val data = result["data"] as Map<String, Any?>
		@Suppress("UNCHECKED_CAST")
		val type = data["__type"] as Map<String, Any?>
		@Suppress("UNCHECKED_CAST")
		val fields = type["fields"] as List<Map<String, Any?>>
		val fieldNames = fields.map { it["name"] }
		assertTrue(fieldNames.contains("username"), "Expected 'username' in fields")
	}


	@Test
	fun testFieldType() = runTest {
		val schema = GraphQL.schema {
			val MyObject by type
			Query {
				field("obj" of MyObject) { resolve { null } }
			}
			Object<Any>(MyObject) {
				field("score" of Int) { resolve { 42 } }
			}
		}
		val executor = GExecutor.default(schema = schema)
		val result = executor.serializeResult(executor.execute("""
			{
			  __type(name: "MyObject") {
			    fields {
			      name
			      type { name kind }
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
		val scoreField = fields.first { it["name"] == "score" }
		@Suppress("UNCHECKED_CAST")
		val fieldType = scoreField["type"] as Map<String, Any?>
		assertEquals(expected = "Int", actual = fieldType["name"])
		assertEquals(expected = "SCALAR", actual = fieldType["kind"])
	}


	@Test
	fun testFieldIsDeprecatedFalseByDefault() = runTest {
		val schema = GraphQL.schema {
			val MyObject by type
			Query {
				field("obj" of MyObject) { resolve { null } }
			}
			Object<Any>(MyObject) {
				field("active" of String) { resolve { "" } }
			}
		}
		val executor = GExecutor.default(schema = schema)
		val result = executor.serializeResult(executor.execute("""
			{
			  __type(name: "MyObject") {
			    fields { name isDeprecated }
			  }
			}
		""".trimIndent()))

		@Suppress("UNCHECKED_CAST")
		val data = result["data"] as Map<String, Any?>
		@Suppress("UNCHECKED_CAST")
		val type = data["__type"] as Map<String, Any?>
		@Suppress("UNCHECKED_CAST")
		val fields = type["fields"] as List<Map<String, Any?>>
		val activeField = fields.first { it["name"] == "active" }
		assertEquals(expected = false, actual = activeField["isDeprecated"])
	}


	@Test
	fun testFieldArgs() = runTest {
		val schema = GraphQL.schema {
			val MyObject by type
			Query {
				field("obj" of MyObject) { resolve { null } }
			}
			Object<Any>(MyObject) {
				field("greet" of String) {
					argument("language" of String)
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
			        type { name }
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
		assertEquals(expected = 1, actual = args.size)
		assertEquals(expected = "language", actual = args[0]["name"])
		@Suppress("UNCHECKED_CAST")
		val argType = args[0]["type"] as Map<String, Any?>
		assertEquals(expected = "String", actual = argType["name"])
	}


	@Test
	fun testFieldDescription() = runTest {
		val schema = GraphQL.schema {
			val MyObject by type
			Query {
				field("obj" of MyObject) { resolve { null } }
			}
			Object<Any>(MyObject) {
				field("name" of String) {
					description("The name of the entity")
					resolve { "" }
				}
			}
		}
		val executor = GExecutor.default(schema = schema)
		val result = executor.serializeResult(executor.execute("""
			{
			  __type(name: "MyObject") {
			    fields { name description }
			  }
			}
		""".trimIndent()))

		@Suppress("UNCHECKED_CAST")
		val data = result["data"] as Map<String, Any?>
		@Suppress("UNCHECKED_CAST")
		val type = data["__type"] as Map<String, Any?>
		@Suppress("UNCHECKED_CAST")
		val fields = type["fields"] as List<Map<String, Any?>>
		val nameField = fields.first { it["name"] == "name" }
		assertEquals(expected = "The name of the entity", actual = nameField["description"])
	}


	@Test
	fun testDeprecatedFieldWithReason() = runTest {
		val schema = GraphQL.schema {
			val MyObject by type
			Query {
				field("obj" of MyObject) { resolve { null } }
			}
			Object<Any>(MyObject) {
				field("oldField" of String) {
					deprecated("Use newField instead")
					resolve { "" }
				}
				field("newField" of String) {
					resolve { "" }
				}
			}
		}
		val executor = GExecutor.default(schema = schema)
		// By default (includeDeprecated: false) deprecated fields are included in the result
		// We query all fields to find oldField
		val result = executor.serializeResult(executor.execute("""
			{
			  __type(name: "MyObject") {
			    fields {
			      name
			      isDeprecated
			      deprecationReason
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
		val oldField = fields.first { it["name"] == "oldField" }
		assertEquals(expected = true, actual = oldField["isDeprecated"])
		assertEquals(expected = "Use newField instead", actual = oldField["deprecationReason"])
	}
}
