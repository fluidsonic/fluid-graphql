package testing

import io.fluidsonic.graphql.*
import kotlin.test.*


class IntrospectionTests {

	@Test
	fun testFragments() = runBlockingTest {
		val document = """
			|{
			|  __schema {
			|    ...schemaFragment
			|  }
			|}
			|
			|fragment directiveFragment on __Directive {
			|  name
			|}
			|fragment enumValueFragment on __EnumValue {
			|  name
			|}
			|fragment fieldFragment on __Field {
			|  name
			|}
			|fragment inputValueFragment on __InputValue {
			|  name
			|}
			|fragment schemaFragment on __Schema {
			|  directives {
			|    ...directiveFragment
			|  }
			|  types {
			|    ...typeFragment
			|  }
			|}
			|fragment typeFragment on __Type {
			|  enumValues {
			|    ...enumValueFragment
			|  }
			|  fields {
			|    ...fieldFragment
			|  }
			|  inputFields {
			|    ...inputValueFragment
			|  }
			|  name
			|}
		""".trimMargin()

		val executor = GExecutor.default(schema = schema)
		val result = executor.serializeResult(executor.execute(document))
		assertEquals(
			expected = mapOf("data" to mapOf(
				"__schema" to mapOf(
					"directives" to listOf(
						mapOf("name" to "deprecated"),
						mapOf("name" to "include"),
						mapOf("name" to "skip"),
						mapOf("name" to "specifiedBy"),
					),
					"types" to listOf(
						mapOf(
							"enumValues" to null,
							"fields" to emptyList<Map<String, Any?>>(),
							"inputFields" to null,
							"name" to "Query",
						),
						mapOf(
							"enumValues" to null,
							"fields" to null,
							"inputFields" to null,
							"name" to "Boolean",
						),
						mapOf(
							"enumValues" to null,
							"fields" to null,
							"inputFields" to null,
							"name" to "Float",
						),
						mapOf(
							"enumValues" to null,
							"fields" to null,
							"inputFields" to null,
							"name" to "ID",
						),
						mapOf(
							"enumValues" to null,
							"fields" to null,
							"inputFields" to null,
							"name" to "Int",
						),
						mapOf(
							"enumValues" to null,
							"fields" to null,
							"inputFields" to null,
							"name" to "String",
						),
					)
				),
			)),
			actual = result
		)
	}


	companion object {

		private val schema = graphql.schema {
			Query {

			}
		}
	}
}
