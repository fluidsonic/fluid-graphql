package testing

import io.fluidsonic.graphql.GExecutor
import io.fluidsonic.graphql.GSchema
import io.fluidsonic.graphql.GTypeRef
import io.fluidsonic.graphql.GraphQL
import io.fluidsonic.graphql.Object
import io.fluidsonic.graphql.default
import io.fluidsonic.graphql.directives
import io.fluidsonic.graphql.resolve
import io.fluidsonic.graphql.schema
import io.fluidsonic.graphql.type
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

/** Runs `{ __schema { types { name } } }` against [schema] and returns the type names it reports. */
@Suppress("UNCHECKED_CAST")
private suspend fun introspectedTypeNames(schema: GSchema): List<String> {
	val executor = GExecutor.default(schema = schema)
	val result = executor.serializeResult(executor.execute("{ __schema { types { name } } }"))
	val data = result["data"] as Map<String, Any?>
	val schemaData = data["__schema"] as Map<String, Any?>

	return (schemaData["types"] as List<Map<String, Any?>>).map { it["name"] as String }
}

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
		val result = executor.serializeResult(
			executor.execute(
				"""
			{
			  __schema {
			    queryType { name }
			  }
			}
				""".trimIndent(),
			),
		)

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
		val result = executor.serializeResult(
			executor.execute(
				"""
			{
			  __schema {
			    mutationType { name }
			  }
			}
				""".trimIndent(),
			),
		)

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
		val result = executor.serializeResult(
			executor.execute(
				"""
			{
			  __schema {
			    mutationType { name }
			  }
			}
				""".trimIndent(),
			),
		)

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
		val result = executor.serializeResult(
			executor.execute(
				"""
			{
			  __schema {
			    subscriptionType { name }
			  }
			}
				""".trimIndent(),
			),
		)

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
		val result = executor.serializeResult(
			executor.execute(
				"""
			{
			  __schema {
			    types { name }
			  }
			}
				""".trimIndent(),
			),
		)

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
				field("dummy" of String) {
					argument("boolean" of GTypeRef("Boolean"))
					argument("float" of GTypeRef("Float"))
					argument("id" of GTypeRef("ID"))
					argument("int" of GTypeRef("Int"))
					resolve { "" }
				}
			}
		}
		val typeNames = introspectedTypeNames(schema)

		for (name in listOf("Boolean", "Float", "ID", "Int", "String")) {
			assertEquals(actual = typeNames.count { it == name }, expected = 1, message = "occurrences of '$name' in types: $typeNames")
		}
	}

	// A built-in scalar reaches `__schema.types` only when the schema refers to it, matching graphql-js.
	// `Boolean` and `String` are the exception: the built-in directives take arguments of those types, as do
	// the introspection types — which `__schema.types` lists alongside the user's, again matching graphql-js.
	@Test
	fun testSchemaTypesOmitUnreferencedBuiltinScalars() = runTest {
		val schema = GraphQL.schema {
			Query {
				field("dummy" of String) { resolve { "" } }
			}
		}
		val typeNames = introspectedTypeNames(schema)

		assertEquals(
			actual = typeNames.sorted(),
			expected = listOf(
				"Boolean",
				"Query",
				"String",
				"__Directive",
				"__DirectiveLocation",
				"__EnumValue",
				"__Field",
				"__InputValue",
				"__Schema",
				"__Type",
				"__TypeKind",
			),
		)
	}

	@Test
	fun testSchemaDirectivesIncludesBuiltins() = runTest {
		val schema = GraphQL.schema {
			Query {
				field("dummy" of String) { resolve { "" } }
			}
		}
		val executor = GExecutor.default(schema = schema)
		val result = executor.serializeResult(
			executor.execute(
				"""
			{
			  __schema {
			    directives { name }
			  }
			}
				""".trimIndent(),
			),
		)

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
		val result = executor.serializeResult(
			executor.execute(
				"""
			{
			  __type(name: "Query") {
			    name
			    kind
			  }
			}
				""".trimIndent(),
			),
		)

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
		val result = executor.serializeResult(
			executor.execute(
				"""
			{
			  __type(name: "DoesNotExist") {
			    name
			  }
			}
				""".trimIndent(),
			),
		)

		@Suppress("UNCHECKED_CAST")
		val data = result["data"] as Map<String, Any?>
		assertNull(data["__type"])
	}
}
