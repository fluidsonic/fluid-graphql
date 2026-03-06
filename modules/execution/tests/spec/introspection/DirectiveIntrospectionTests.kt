package testing

import io.fluidsonic.graphql.*
import kotlin.test.*
import kotlinx.coroutines.test.*

// GraphQL Spec §4.6 — Directive Introspection
class DirectiveIntrospectionTests {

	@Test
	fun testBuiltinDirectivesPresent() = runTest {
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
		assertTrue(directiveNames.contains("skip"), "Expected 'skip' directive")
		assertTrue(directiveNames.contains("include"), "Expected 'include' directive")
		assertTrue(directiveNames.contains("deprecated"), "Expected 'deprecated' directive")
		assertTrue(directiveNames.contains("specifiedBy"), "Expected 'specifiedBy' directive")
	}


	@Test
	fun testSkipDirectiveLocations() = runTest {
		val schema = GraphQL.schema {
			Query {
				field("dummy" of String) { resolve { "" } }
			}
		}
		val executor = GExecutor.default(schema = schema)
		val result = executor.serializeResult(executor.execute("""
			{
			  __schema {
			    directives {
			      name
			      locations
			    }
			  }
			}
		""".trimIndent()))

		@Suppress("UNCHECKED_CAST")
		val data = result["data"] as Map<String, Any?>
		@Suppress("UNCHECKED_CAST")
		val schemaData = data["__schema"] as Map<String, Any?>
		@Suppress("UNCHECKED_CAST")
		val directives = schemaData["directives"] as List<Map<String, Any?>>
		val skipDirective = directives.first { it["name"] == "skip" }
		@Suppress("UNCHECKED_CAST")
		val locations = skipDirective["locations"] as List<String>
		assertTrue(locations.contains("FIELD"), "Expected 'FIELD' in @skip locations, got: $locations")
	}


	@Test
	fun testIncludeDirectiveLocations() = runTest {
		val schema = GraphQL.schema {
			Query {
				field("dummy" of String) { resolve { "" } }
			}
		}
		val executor = GExecutor.default(schema = schema)
		val result = executor.serializeResult(executor.execute("""
			{
			  __schema {
			    directives {
			      name
			      locations
			    }
			  }
			}
		""".trimIndent()))

		@Suppress("UNCHECKED_CAST")
		val data = result["data"] as Map<String, Any?>
		@Suppress("UNCHECKED_CAST")
		val schemaData = data["__schema"] as Map<String, Any?>
		@Suppress("UNCHECKED_CAST")
		val directives = schemaData["directives"] as List<Map<String, Any?>>
		val includeDirective = directives.first { it["name"] == "include" }
		@Suppress("UNCHECKED_CAST")
		val locations = includeDirective["locations"] as List<String>
		assertTrue(locations.contains("FIELD"), "Expected 'FIELD' in @include locations, got: $locations")
	}


	@Test
	fun testDeprecatedDirectiveArgs() = runTest {
		val schema = GraphQL.schema {
			Query {
				field("dummy" of String) { resolve { "" } }
			}
		}
		val executor = GExecutor.default(schema = schema)
		val result = executor.serializeResult(executor.execute("""
			{
			  __schema {
			    directives {
			      name
			      args { name }
			    }
			  }
			}
		""".trimIndent()))

		@Suppress("UNCHECKED_CAST")
		val data = result["data"] as Map<String, Any?>
		@Suppress("UNCHECKED_CAST")
		val schemaData = data["__schema"] as Map<String, Any?>
		@Suppress("UNCHECKED_CAST")
		val directives = schemaData["directives"] as List<Map<String, Any?>>
		val deprecatedDirective = directives.first { it["name"] == "deprecated" }
		@Suppress("UNCHECKED_CAST")
		val args = deprecatedDirective["args"] as List<Map<String, Any?>>
		val argNames = args.map { it["name"] }
		assertTrue(argNames.contains("reason"), "Expected 'reason' arg in @deprecated, got: $argNames")
	}


	@Test
	fun testDirectiveHasNameAndLocations() = runTest {
		val schema = GraphQL.schema {
			Query {
				field("dummy" of String) { resolve { "" } }
			}
		}
		val executor = GExecutor.default(schema = schema)
		val result = executor.serializeResult(executor.execute("""
			{
			  __schema {
			    directives {
			      name
			      locations
			    }
			  }
			}
		""".trimIndent()))

		@Suppress("UNCHECKED_CAST")
		val data = result["data"] as Map<String, Any?>
		@Suppress("UNCHECKED_CAST")
		val schemaData = data["__schema"] as Map<String, Any?>
		@Suppress("UNCHECKED_CAST")
		val directives = schemaData["directives"] as List<Map<String, Any?>>
		assertTrue(directives.isNotEmpty(), "Expected at least one directive")
		for (directive in directives) {
			val name = directive["name"] as String?
			assertNotNull(name, "Directive name should not be null")
			assertTrue(name.isNotEmpty(), "Directive name should not be empty")
			@Suppress("UNCHECKED_CAST")
			val locations = directive["locations"] as List<String>
			assertTrue(locations.isNotEmpty(), "Directive '$name' should have at least one location")
		}
	}
}
