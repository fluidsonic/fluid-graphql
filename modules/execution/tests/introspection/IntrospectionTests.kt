package testing

import io.fluidsonic.graphql.GExecutor
import io.fluidsonic.graphql.GraphQL
import io.fluidsonic.graphql.default
import io.fluidsonic.graphql.document
import io.fluidsonic.graphql.schema
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

/** The `typeFragment` shape of [IntrospectionTests.testFragments] for the introspection type [name]. */
private fun introspectedType(name: String, fieldNames: List<String>? = null, enumValueNames: List<String>? = null): Map<String, Any?> = mapOf(
	"enumValues" to enumValueNames?.map { mapOf("name" to it) },
	"fields" to fieldNames?.map { mapOf("name" to it) },
	"inputFields" to null,
	"name" to name,
)

class IntrospectionTests {

	@Test
	fun testFragments() = runTest {
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
			expected = mapOf(
				"data" to mapOf(
					"__schema" to mapOf(
						"directives" to listOf(
							mapOf("name" to "deprecated"),
							mapOf("name" to "include"),
							mapOf("name" to "oneOf"),
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
							// `Float`, `ID` and `Int` are absent: this schema refers to none of them, and a
							// built-in scalar is registered only when something refers to it. `Boolean` and
							// `String` stay because the built-in directives take arguments of those types.
							mapOf(
								"enumValues" to null,
								"fields" to null,
								"inputFields" to null,
								"name" to "String",
							),
							// The introspection types are ordinary members of the type map, so they describe
							// themselves here just like any user type. graphql@17.0.2 reports them too.
							introspectedType("__Schema", fieldNames = listOf("description", "types", "queryType", "mutationType", "subscriptionType", "directives")),
							introspectedType(
								"__Type",
								fieldNames = listOf(
									"kind",
									"name",
									"description",
									"specifiedByURL",
									"isOneOf",
									"fields",
									"interfaces",
									"possibleTypes",
									"enumValues",
									"inputFields",
									"ofType",
								),
							),
							introspectedType("__Field", fieldNames = listOf("name", "description", "args", "type", "isDeprecated", "deprecationReason")),
							introspectedType(
								"__InputValue",
								fieldNames = listOf("name", "description", "type", "defaultValue", "isDeprecated", "deprecationReason"),
							),
							introspectedType("__EnumValue", fieldNames = listOf("name", "description", "isDeprecated", "deprecationReason")),
							introspectedType(
								"__TypeKind",
								enumValueNames = listOf("SCALAR", "OBJECT", "INTERFACE", "UNION", "ENUM", "INPUT_OBJECT", "LIST", "NON_NULL"),
							),
							introspectedType("__Directive", fieldNames = listOf("name", "description", "isRepeatable", "locations", "args")),
							introspectedType(
								"__DirectiveLocation",
								enumValueNames = listOf(
									"QUERY",
									"MUTATION",
									"SUBSCRIPTION",
									"FIELD",
									"FRAGMENT_DEFINITION",
									"FRAGMENT_SPREAD",
									"INLINE_FRAGMENT",
									"VARIABLE_DEFINITION",
									"SCHEMA",
									"SCALAR",
									"OBJECT",
									"FIELD_DEFINITION",
									"ARGUMENT_DEFINITION",
									"INTERFACE",
									"UNION",
									"ENUM",
									"ENUM_VALUE",
									"INPUT_OBJECT",
									"INPUT_FIELD_DEFINITION",
									"DIRECTIVE_DEFINITION",
								),
							),
						),
					),
				),
			),
			actual = result,
		)
	}

	companion object {

		private val schema = GraphQL.schema {
			Query {
			}
		}
	}
}
