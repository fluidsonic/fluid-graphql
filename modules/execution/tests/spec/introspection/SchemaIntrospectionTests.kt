package testing

import io.fluidsonic.graphql.*
import kotlin.test.*
import kotlinx.coroutines.test.*

// GraphQL Spec §4.1 — The __schema Meta-Field
class SchemaIntrospectionTests {

	@Test
	fun testSchemaQueryType() = runTest {
		val schema = GraphQL.schema {
			Query {
				field("dummy" of String) { resolve { "" } }
			}
		}
		val executor = GExecutor.default(schema = schema)
		val result = executor.serializeResult(executor.execute("""
			{
			  __schema {
			    queryType { name }
			  }
			}
		""".trimIndent()))

		@Suppress("UNCHECKED_CAST")
		val data = result["data"] as Map<String, Any?>
		@Suppress("UNCHECKED_CAST")
		val schemaData = data["__schema"] as Map<String, Any?>
		@Suppress("UNCHECKED_CAST")
		val queryType = schemaData["queryType"] as Map<String, Any?>
		assertEquals(expected = "Query", actual = queryType["name"])
	}


	@Test
	fun testSchemaMutationTypePresent() = runTest {
		val schema = GraphQL.schema {
			Query {
				field("dummy" of String) { resolve { "" } }
			}
			Mutation {
				field("doSomething" of String) { resolve { "" } }
			}
		}
		val executor = GExecutor.default(schema = schema)
		val result = executor.serializeResult(executor.execute("""
			{
			  __schema {
			    mutationType { name }
			  }
			}
		""".trimIndent()))

		@Suppress("UNCHECKED_CAST")
		val data = result["data"] as Map<String, Any?>
		@Suppress("UNCHECKED_CAST")
		val schemaData = data["__schema"] as Map<String, Any?>
		@Suppress("UNCHECKED_CAST")
		val mutationType = schemaData["mutationType"] as Map<String, Any?>
		assertEquals(expected = "Mutation", actual = mutationType["name"])
	}


	@Test
	fun testSchemaMutationTypeAbsent() = runTest {
		val schema = GraphQL.schema {
			Query {
				field("dummy" of String) { resolve { "" } }
			}
		}
		val executor = GExecutor.default(schema = schema)
		val result = executor.serializeResult(executor.execute("""
			{
			  __schema {
			    mutationType { name }
			  }
			}
		""".trimIndent()))

		@Suppress("UNCHECKED_CAST")
		val data = result["data"] as Map<String, Any?>
		@Suppress("UNCHECKED_CAST")
		val schemaData = data["__schema"] as Map<String, Any?>
		assertNull(schemaData["mutationType"])
	}


	@Test
	fun testSchemaSubscriptionTypeAbsent() = runTest {
		val schema = GraphQL.schema {
			Query {
				field("dummy" of String) { resolve { "" } }
			}
		}
		val executor = GExecutor.default(schema = schema)
		val result = executor.serializeResult(executor.execute("""
			{
			  __schema {
			    subscriptionType { name }
			  }
			}
		""".trimIndent()))

		@Suppress("UNCHECKED_CAST")
		val data = result["data"] as Map<String, Any?>
		@Suppress("UNCHECKED_CAST")
		val schemaData = data["__schema"] as Map<String, Any?>
		assertNull(schemaData["subscriptionType"])
	}


	@Test
	fun testSchemaTypesIncludesUserTypes() = runTest {
		val schema = GraphQL.schema {
			val MyObject by type
			Query {
				field("obj" of MyObject) { resolve { null } }
			}
			Object<Any>(MyObject) {
				field("id" of String) { resolve { "" } }
			}
		}
		val executor = GExecutor.default(schema = schema)
		val result = executor.serializeResult(executor.execute("""
			{
			  __schema {
			    types { name }
			  }
			}
		""".trimIndent()))

		@Suppress("UNCHECKED_CAST")
		val data = result["data"] as Map<String, Any?>
		@Suppress("UNCHECKED_CAST")
		val schemaData = data["__schema"] as Map<String, Any?>
		@Suppress("UNCHECKED_CAST")
		val types = schemaData["types"] as List<Map<String, Any?>>
		val typeNames = types.map { it["name"] }
		assertTrue(typeNames.contains("Query"), "Expected 'Query' in types but got: $typeNames")
		assertTrue(typeNames.contains("MyObject"), "Expected 'MyObject' in types but got: $typeNames")
	}


	@Test
	fun testSchemaTypesIncludesBuiltinScalars() = runTest {
		val schema = GraphQL.schema {
			Query {
				field("dummy" of String) { resolve { "" } }
			}
		}
		val executor = GExecutor.default(schema = schema)
		val result = executor.serializeResult(executor.execute("""
			{
			  __schema {
			    types { name }
			  }
			}
		""".trimIndent()))

		@Suppress("UNCHECKED_CAST")
		val data = result["data"] as Map<String, Any?>
		@Suppress("UNCHECKED_CAST")
		val schemaData = data["__schema"] as Map<String, Any?>
		@Suppress("UNCHECKED_CAST")
		val types = schemaData["types"] as List<Map<String, Any?>>
		val typeNames = types.map { it["name"] }
		assertTrue(typeNames.contains("Boolean"), "Expected 'Boolean' in types")
		assertTrue(typeNames.contains("Int"), "Expected 'Int' in types")
		assertTrue(typeNames.contains("String"), "Expected 'String' in types")
		assertTrue(typeNames.contains("Float"), "Expected 'Float' in types")
		assertTrue(typeNames.contains("ID"), "Expected 'ID' in types")
	}


	@Test
	fun testSchemaDirectivesIncludesBuiltins() = runTest {
		val schema = GraphQL.schema {
			Query {
				field("dummy" of String) { resolve { "" } }
			}
		}
		val executor = GExecutor.default(schema = schema)
		val result = executor.serializeResult(executor.execute("""
			{
			  __schema {
			    directives { name }
			  }
			}
		""".trimIndent()))

		@Suppress("UNCHECKED_CAST")
		val data = result["data"] as Map<String, Any?>
		@Suppress("UNCHECKED_CAST")
		val schemaData = data["__schema"] as Map<String, Any?>
		@Suppress("UNCHECKED_CAST")
		val directives = schemaData["directives"] as List<Map<String, Any?>>
		val directiveNames = directives.map { it["name"] }
		assertTrue(directiveNames.contains("skip"), "Expected 'skip' in directives")
		assertTrue(directiveNames.contains("include"), "Expected 'include' in directives")
		assertTrue(directiveNames.contains("deprecated"), "Expected 'deprecated' in directives")
		assertTrue(directiveNames.contains("specifiedBy"), "Expected 'specifiedBy' in directives")
	}


	@Test
	fun testTypeQueryByName() = runTest {
		val schema = GraphQL.schema {
			Query {
				field("dummy" of String) { resolve { "" } }
			}
		}
		val executor = GExecutor.default(schema = schema)
		val result = executor.serializeResult(executor.execute("""
			{
			  __type(name: "Query") {
			    name
			    kind
			  }
			}
		""".trimIndent()))

		@Suppress("UNCHECKED_CAST")
		val data = result["data"] as Map<String, Any?>
		@Suppress("UNCHECKED_CAST")
		val type = data["__type"] as Map<String, Any?>
		assertEquals(expected = "Query", actual = type["name"])
		assertEquals(expected = "OBJECT", actual = type["kind"])
	}


	@Test
	fun testTypeQueryNonexistentName() = runTest {
		val schema = GraphQL.schema {
			Query {
				field("dummy" of String) { resolve { "" } }
			}
		}
		val executor = GExecutor.default(schema = schema)
		val result = executor.serializeResult(executor.execute("""
			{
			  __type(name: "DoesNotExist") {
			    name
			  }
			}
		""".trimIndent()))

		@Suppress("UNCHECKED_CAST")
		val data = result["data"] as Map<String, Any?>
		assertNull(data["__type"])
	}
}
