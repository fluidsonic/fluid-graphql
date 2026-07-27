/*
 * Introspection executes against the user's own schema — there is no second schema to swap in mid-execution.
 *
 * The canonical query text is `getIntrospectionQuery()` of graphql@17.0.2, printed verbatim from the package
 * and pasted here; that is the document GraphiQL and every codegen tool sends.
 */
package testing

import io.fluidsonic.graphql.GExecutor
import io.fluidsonic.graphql.GObjectType
import io.fluidsonic.graphql.GSchema
import io.fluidsonic.graphql.GTypeRef
import io.fluidsonic.graphql.GraphQL
import io.fluidsonic.graphql.resolve
import io.fluidsonic.graphql.resolver
import io.fluidsonic.graphql.schema
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

// Ported verbatim from `getIntrospectionQuery()` of graphql@17.0.2.
private const val canonicalIntrospectionQuery = """
    query IntrospectionQuery {
      __schema {

        queryType { name kind }
        mutationType { name kind }
        subscriptionType { name kind }
        types {
          ...FullType
        }
        directives {
          name
          description



          locations
          args {
            ...InputValue
          }
        }
      }
    }

    fragment FullType on __Type {
      kind
      name
      description


      fields(includeDeprecated: true) {
        name
        description
        args {
          ...InputValue
        }
        type {
          ...TypeRef
        }
        isDeprecated
        deprecationReason
      }
      inputFields {
        ...InputValue
      }
      interfaces {
        ...TypeRef
      }
      enumValues(includeDeprecated: true) {
        name
        description
        isDeprecated
        deprecationReason
      }
      possibleTypes {
        ...TypeRef
      }
    }

    fragment InputValue on __InputValue {
      name
      description
      type { ...TypeRef }
      defaultValue


    }

    fragment TypeRef on __Type {
      kind
      name
      ofType {
        name
        kind
        ofType {
          name
          kind
          ofType {
            name
            kind
            ofType {
              name
              kind
              ofType {
                name
                kind
                ofType {
                  name
                  kind
                  ofType {
                    name
                    kind
                    ofType {
                      name
                      kind
                    }
                  }
                }
              }
            }
          }
        }
      }
    }
  """

private val introspectionTypeNames = listOf(
	"__Directive",
	"__DirectiveLocation",
	"__EnumValue",
	"__Field",
	"__InputValue",
	"__Schema",
	"__Type",
	"__TypeKind",
)

private fun minimalSchema(): GSchema = GraphQL.schema {
	Query {
		field("dummy" of String) { resolve { "" } }
	}
}

@Suppress("UNCHECKED_CAST")
private suspend fun executeSuccessfully(schema: GSchema, query: String): Map<String, Any?> {
	val executor = GExecutor.default(schema = schema)
	val result = executor.serializeResult(executor.execute(query))

	assertNull(actual = result["errors"], message = "query must execute without errors")

	return assertNotNull(actual = result["data"], message = "query must produce data") as Map<String, Any?>
}

class IntrospectionSchemaMergeTests {

	// The `__Schema` shell must match what graphql-js exposes, or a generated client disagrees with the
	// server about the meta-schema itself. Shape taken from `printType(__Schema)` on graphql@17.0.2.
	@Suppress("UNCHECKED_CAST")
	@Test
	fun schemaType_matchesTheReferenceShape() = runTest {
		val data = executeSuccessfully(
			minimalSchema(),
			"""{ __type(name: "__Schema") { fields { name type { kind name ofType { kind name } } args { name type { kind ofType { name } } } } } }""",
		)
		val fields = ((data["__type"] as Map<String, Any?>)["fields"] as List<Map<String, Any?>>).associateBy { it["name"] as String }

		assertEquals(
			actual = fields.keys.toList(),
			expected = listOf("description", "types", "queryType", "mutationType", "subscriptionType", "directives"),
		)

		// `description` is nullable, `queryType` is not — a schema always has a query root.
		assertEquals(actual = (fields.getValue("description")["type"] as Map<String, Any?>)["name"], expected = "String")
		assertEquals(actual = (fields.getValue("queryType")["type"] as Map<String, Any?>)["kind"], expected = "NON_NULL")
		assertEquals(actual = (fields.getValue("mutationType")["type"] as Map<String, Any?>)["name"], expected = "__Type")

		val directivesArguments = fields.getValue("directives")["args"] as List<Map<String, Any?>>
		val includeDeprecated = assertNotNull(directivesArguments.singleOrNull { it["name"] == "includeDeprecated" })

		assertEquals(actual = (includeDeprecated["type"] as Map<String, Any?>)["kind"], expected = "NON_NULL")
	}

	@Suppress("UNCHECKED_CAST")
	@Test
	fun schemaDescription_isExposed() = runTest {
		val schema = GSchema.parse("\"\"\"The docs.\"\"\" schema { query: Query } type Query { dummy: String }").valueOrThrow()
		val data = executeSuccessfully(schema, "{ __schema { description } }")

		assertEquals(actual = (data["__schema"] as Map<String, Any?>)["description"], expected = "The docs.")
	}

	@Suppress("UNCHECKED_CAST")
	@Test
	fun schemaTypes_listTheIntrospectionTypesAlongsideTheUsers() = runTest {
		val data = executeSuccessfully(minimalSchema(), "{ __schema { types { name } } }")
		val schemaData = data["__schema"] as Map<String, Any?>
		val names = (schemaData["types"] as List<Map<String, Any?>>).map { it["name"] as String }

		assertEquals(actual = names.sorted(), expected = (listOf("Boolean", "Query", "String") + introspectionTypeNames).sorted())
	}

	// The list introspection reports is the schema's own `types`, in its own order — proof that introspection
	// enumerates one type-identity domain rather than a second, swapped-in schema.
	@Suppress("UNCHECKED_CAST")
	@Test
	fun schemaTypes_areTheSchemasOwnTypesInOrder() = runTest {
		val schema = minimalSchema()
		val data = executeSuccessfully(schema, "{ __schema { types { name } } }")
		val schemaData = data["__schema"] as Map<String, Any?>
		val names = (schemaData["types"] as List<Map<String, Any?>>).map { it["name"] as String }

		assertEquals(actual = names, expected = schema.types.map { it.name })
	}

	@Suppress("UNCHECKED_CAST")
	@Test
	fun type_resolvesAnIntrospectionTypeByName() = runTest {
		val data = executeSuccessfully(minimalSchema(), """{ __type(name: "__Type") { kind name fields { name } } }""")
		val type = assertNotNull(actual = data["__type"]) as Map<String, Any?>

		assertEquals(actual = type["kind"], expected = "OBJECT")
		assertEquals(actual = type["name"], expected = "__Type")
		assertEquals(
			actual = (type["fields"] as List<Map<String, Any?>>).map { it["name"] as String }.sorted(),
			expected = listOf(
				"description",
				"enumValues",
				"fields",
				"inputFields",
				"interfaces",
				"isOneOf",
				"kind",
				"name",
				"ofType",
				"possibleTypes",
				"specifiedByURL",
			),
		)
	}

	@Suppress("UNCHECKED_CAST")
	@Test
	fun type_resolvesAUserTypeByName() = runTest {
		val data = executeSuccessfully(minimalSchema(), """{ __type(name: "Query") { kind name } }""")
		val type = assertNotNull(actual = data["__type"]) as Map<String, Any?>

		assertEquals(actual = type["name"], expected = "Query")
	}

	@Test
	fun typename_stillResolves() = runTest {
		val data = executeSuccessfully(minimalSchema(), "{ __typename dummy }")

		assertEquals(actual = data["__typename"], expected = "Query")
	}

	@Suppress("UNCHECKED_CAST")
	@Test
	fun typename_stillResolvesInsideAnIntrospectionSubtree() = runTest {
		val data = executeSuccessfully(minimalSchema(), "{ __schema { __typename queryType { __typename name } } }")
		val schemaData = data["__schema"] as Map<String, Any?>

		assertEquals(actual = schemaData["__typename"], expected = "__Schema")
		assertEquals(actual = (schemaData["queryType"] as Map<String, Any?>)["__typename"], expected = "__Type")
	}

	// The introspection types the language module merges in are pure data: the resolvers live in the
	// executor's dispatch, not on the nodes. Asserted *after* an introspection query has run, so that no
	// amount of prior execution can have decorated the shells.
	@Test
	fun introspectionFieldDefinitions_carryNoNodeExtensions() = runTest {
		val schema = minimalSchema()

		executeSuccessfully(schema, "{ __schema { types { name } } }")

		for (name in introspectionTypeNames) {
			val type = schema.resolveType(name)
			if (type !is GObjectType) {
				continue
			}

			for (fieldDefinition in type.fieldDefinitions) {
				assertTrue(
					actual = fieldDefinition.extensions.isEmpty(),
					message = "`$name.${fieldDefinition.name}` must carry no node extensions but carries ${fieldDefinition.extensions}",
				)
				assertNull(actual = fieldDefinition.resolver, message = "$name.${fieldDefinition.name}")
			}
		}
	}

	// Nothing has to be primed for introspection to work: a schema built here and queried immediately must
	// answer correctly, without any prior execution having initialised the executor's internals.
	@Test
	fun introspection_resolvesOnAFreshSchemaWithoutPriorExecution() = runTest {
		val data = executeSuccessfully(GSchema.parse("type Query { a: String }").valueWithoutErrorsOrThrow(), "{ __schema { queryType { name } } }")
		val schemaData = data["__schema"] as Map<String, Any?>

		assertEquals(actual = (schemaData["queryType"] as Map<String, Any?>)["name"], expected = "Query")
	}

	// The single highest-value assertion of the merge: the document every GraphQL tool sends must come back
	// without an error.
	@Suppress("UNCHECKED_CAST")
	@Test
	fun canonicalIntrospectionQuery_executesEndToEnd() = runTest {
		val schema = GraphQL.schema {
			Query {
				field("dummy" of String) {
					argument("count" of GTypeRef("Int"))
					resolve { "" }
				}
			}
		}
		val data = executeSuccessfully(schema, canonicalIntrospectionQuery)
		val schemaData = data["__schema"] as Map<String, Any?>
		val types = schemaData["types"] as List<Map<String, Any?>>

		assertEquals(actual = types.map { it["name"] as String }.sorted(), expected = (listOf("Boolean", "Int", "Query", "String") + introspectionTypeNames).sorted())

		val typeType = assertNotNull(actual = types.singleOrNull { it["name"] == "__Type" }, message = "`__Type` must be described by the canonical query")

		assertTrue(actual = (typeType["fields"] as List<Map<String, Any?>>).isNotEmpty(), message = "`__Type` must report its fields")

		val typeKind = assertNotNull(actual = types.singleOrNull { it["name"] == "__TypeKind" })

		assertEquals(
			actual = (typeKind["enumValues"] as List<Map<String, Any?>>).map { it["name"] as String }.sorted(),
			expected = listOf("ENUM", "INPUT_OBJECT", "INTERFACE", "LIST", "NON_NULL", "OBJECT", "SCALAR", "UNION"),
		)
	}
}
