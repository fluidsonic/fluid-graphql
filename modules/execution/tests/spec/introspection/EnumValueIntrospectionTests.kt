package testing

import io.fluidsonic.graphql.*
import kotlin.test.*
import kotlinx.coroutines.test.*

// GraphQL Spec §4.5 — Enum Value Introspection
class EnumValueIntrospectionTests {

	@Test
	fun testEnumValueName() = runTest {
		val schema = GraphQL.schema {
			val Direction by type
			Query {
				field("dir" of Direction) { resolve { "NORTH" } }
			}
			Enum(Direction) {
				value("NORTH")
				value("SOUTH")
			}
		}
		val executor = GExecutor.default(schema = schema)
		val result = executor.serializeResult(executor.execute("""
			{
			  __type(name: "Direction") {
			    enumValues { name }
			  }
			}
		""".trimIndent()))

		@Suppress("UNCHECKED_CAST")
		val data = result["data"] as Map<String, Any?>
		@Suppress("UNCHECKED_CAST")
		val type = data["__type"] as Map<String, Any?>
		@Suppress("UNCHECKED_CAST")
		val enumValues = type["enumValues"] as List<Map<String, Any?>>
		val names = enumValues.map { it["name"] }
		assertTrue(names.contains("NORTH"), "Expected 'NORTH' in enumValues")
		assertTrue(names.contains("SOUTH"), "Expected 'SOUTH' in enumValues")
	}


	@Test
	fun testEnumValueIsDeprecatedFalse() = runTest {
		val schema = GraphQL.schema {
			val Status by type
			Query {
				field("status" of Status) { resolve { "ACTIVE" } }
			}
			Enum(Status) {
				value("ACTIVE")
			}
		}
		val executor = GExecutor.default(schema = schema)
		val result = executor.serializeResult(executor.execute("""
			{
			  __type(name: "Status") {
			    enumValues { name isDeprecated }
			  }
			}
		""".trimIndent()))

		@Suppress("UNCHECKED_CAST")
		val data = result["data"] as Map<String, Any?>
		@Suppress("UNCHECKED_CAST")
		val type = data["__type"] as Map<String, Any?>
		@Suppress("UNCHECKED_CAST")
		val enumValues = type["enumValues"] as List<Map<String, Any?>>
		val activeValue = enumValues.first { it["name"] == "ACTIVE" }
		assertEquals(expected = false, actual = activeValue["isDeprecated"])
	}


	@Test
	fun testEnumValueIsDeprecatedTrue() = runTest {
		val schema = GraphQL.schema {
			val Status by type
			Query {
				field("status" of Status) { resolve { "ACTIVE" } }
			}
			Enum(Status) {
				value("ACTIVE")
				value("LEGACY") {
					deprecated("Use ACTIVE instead")
				}
			}
		}
		val executor = GExecutor.default(schema = schema)
		val result = executor.serializeResult(executor.execute("""
			{
			  __type(name: "Status") {
			    enumValues(includeDeprecated: true) { name isDeprecated deprecationReason }
			  }
			}
		""".trimIndent()))

		@Suppress("UNCHECKED_CAST")
		val data = result["data"] as Map<String, Any?>
		@Suppress("UNCHECKED_CAST")
		val type = data["__type"] as Map<String, Any?>
		@Suppress("UNCHECKED_CAST")
		val enumValues = type["enumValues"] as List<Map<String, Any?>>
		val legacyValue = enumValues.first { it["name"] == "LEGACY" }
		assertEquals(expected = true, actual = legacyValue["isDeprecated"])
		assertEquals(expected = "Use ACTIVE instead", actual = legacyValue["deprecationReason"])
	}


	@Test
	fun testMultipleEnumValues() = runTest {
		val schema = GraphQL.schema {
			val Color by type
			Query {
				field("color" of Color) { resolve { "RED" } }
			}
			Enum(Color) {
				value("RED")
				value("GREEN")
				value("BLUE")
			}
		}
		val executor = GExecutor.default(schema = schema)
		val result = executor.serializeResult(executor.execute("""
			{
			  __type(name: "Color") {
			    enumValues { name }
			  }
			}
		""".trimIndent()))

		@Suppress("UNCHECKED_CAST")
		val data = result["data"] as Map<String, Any?>
		@Suppress("UNCHECKED_CAST")
		val type = data["__type"] as Map<String, Any?>
		@Suppress("UNCHECKED_CAST")
		val enumValues = type["enumValues"] as List<Map<String, Any?>>
		assertEquals(expected = 3, actual = enumValues.size)
		val names = enumValues.map { it["name"] }
		assertTrue(names.contains("RED"))
		assertTrue(names.contains("GREEN"))
		assertTrue(names.contains("BLUE"))
	}
}
