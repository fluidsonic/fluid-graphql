package testing

import io.fluidsonic.graphql.GExecutor
import io.fluidsonic.graphql.GSchema
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

// https://spec.graphql.org/draft/#sec-Schema-Introspection
class BuiltinDirectiveIntrospectionTests {

	// __Directive.isRepeatable is part of the introspection schema and must reflect the definition.
	@Test
	fun testDirectiveIntrospection_exposesIsRepeatable() = runTest {
		val directives = introspectDirectives(
			"""
			|directive @repeatableDirective repeatable on FIELD
			|directive @singularDirective on FIELD
			|
			|type Query { field: String }
			""".trimMargin(),
		)

		assertEquals(actual = directives["repeatableDirective"], expected = true)
		assertEquals(actual = directives["singularDirective"], expected = false)
		assertEquals(actual = directives["deprecated"], expected = false)
	}

	// https://spec.graphql.org/draft/#sec-OneOf-Input-Objects
	@Test
	fun testTypeIntrospection_exposesIsOneOf() = runTest {
		val types = introspectIsOneOf(
			"""
			|input OneOfInput @oneOf {
			|   a: Int
			|   b: Int
			|}
			|
			|input PlainInput {
			|   a: Int
			|}
			|
			|type Query { field(oneOf: OneOfInput, plain: PlainInput): String }
			""".trimMargin(),
		)

		assertEquals(actual = types["OneOfInput"], expected = true)
		assertEquals(actual = types["PlainInput"], expected = false)

		// Upstream reports `null` for everything that is not an input object.
		assertEquals(actual = types["Query"], expected = null)
		assertEquals(actual = types["String"], expected = null)
	}

	private suspend fun introspectDirectives(schemaSource: String): Map<String, Boolean?> {
		val data = execute(schemaSource, "{ __schema { directives { name isRepeatable } } }")
		val schemaData = assertNotNull(data["__schema"] as? Map<*, *>)
		val directives = assertNotNull(schemaData["directives"] as? List<*>)

		return directives.associate { directive ->
			directive as Map<*, *>

			directive["name"] as String to directive["isRepeatable"] as Boolean?
		}
	}

	private suspend fun introspectIsOneOf(schemaSource: String): Map<String, Boolean?> {
		val data = execute(schemaSource, "{ __schema { types { name isOneOf } } }")
		val schemaData = assertNotNull(data["__schema"] as? Map<*, *>)
		val types = assertNotNull(schemaData["types"] as? List<*>)

		return types.associate { type ->
			type as Map<*, *>

			type["name"] as String to type["isOneOf"] as Boolean?
		}
	}

	private suspend fun execute(schemaSource: String, document: String): Map<*, *> {
		val schema = GSchema.parse(schemaSource).valueOrThrow()
		val executor = GExecutor.default(schema = schema)
		val result = executor.serializeResult(executor.execute(document))

		assertEquals(actual = result["errors"], expected = null, message = "Introspection must not fail.")

		return assertNotNull(result["data"] as? Map<*, *>)
	}
}
