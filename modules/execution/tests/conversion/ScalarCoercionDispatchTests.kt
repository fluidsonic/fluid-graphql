/*
 * Behaviour the four characterisation matrices cannot express: how the converters choose between an
 * attached extension coercer, the leaf type's own coercion member, and the generic-AST fallback.
 */
package testing

import io.fluidsonic.graphql.GErrorException
import io.fluidsonic.graphql.GExecutor
import io.fluidsonic.graphql.GSchema
import io.fluidsonic.graphql.GTypeRef
import io.fluidsonic.graphql.GraphQL
import io.fluidsonic.graphql.arguments
import io.fluidsonic.graphql.coerceInputLiteral
import io.fluidsonic.graphql.coerceInputValue
import io.fluidsonic.graphql.coerceOutputValue
import io.fluidsonic.graphql.resolve
import io.fluidsonic.graphql.schema
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

private const val CUSTOM_TYPE = "Custom"

private suspend fun executeAndSerialize(schema: GSchema, source: String, variableValues: Map<String, Any?> = emptyMap()): Map<String, Any?> {
	val executor = GExecutor.default(schema = schema)

	return executor.serializeResult(executor.execute(source, variableValues = variableValues))
}

@Suppress("UNCHECKED_CAST")
private fun dataValue(result: Map<String, Any?>, key: String): Any? = (result["data"] as Map<String, Any?>?)?.get(key)

@Suppress("UNCHECKED_CAST")
private fun firstErrorMessage(result: Map<String, Any?>): String? = (result["errors"] as List<Map<String, Any?>>?)?.firstOrNull()?.get("message") as String?

class ScalarCoercionDispatchTests {

	// Precedence: the attached coercer replaces the type's own coercion, which for a custom scalar is the
	// identity.
	@Test
	fun outputCoercion_attachedCoercerWinsOverTheTypesOwnMember() = runTest {
		val schema = GraphQL.schema {
			Scalar(GTypeRef(CUSTOM_TYPE)) {
				coerceOutputValue { value -> "coercer saw $value" }
			}

			Query {
				field("value" of GTypeRef(CUSTOM_TYPE)) {
					resolve { 42 }
				}
			}
		}

		assertEquals(actual = dataValue(executeAndSerialize(schema, "{ value }"), "value"), expected = "coercer saw 42")
	}

	@Test
	fun outputCoercion_fallsBackToTheTypesOwnMemberWithoutAnAttachedCoercer() = runTest {
		val schema = GraphQL.schema {
			Query {
				field("value" of GTypeRef("Int")) {
					resolve { true }
				}
			}
		}

		assertEquals(actual = dataValue(executeAndSerialize(schema, "{ value }"), "value"), expected = 1)
	}

	// This used to be a silent KNOWN LIMITATION: declaring `Scalar(Int)` to hang a coercer off a built-in
	// produced a schema in which the coercer was unreachable, because the built-in replaced the declaration
	// during indexing and the built-in's own coercion answered instead. Redefining a built-in scalar is now
	// refused outright, so the trap reports itself rather than swallowing the coercer. Replacing a built-in
	// scalar's coercion remains unsupported — that has not changed, only how loudly it fails.
	@Test
	fun schema_refusesToDeclareAScalarNamedAfterABuiltin() = runTest {
		val exception = assertFailsWith<GErrorException> {
			GraphQL.schema {
				Scalar(GTypeRef("Int")) {
					coerceOutputValue { value -> "coercer saw $value" }
				}

				Query {
					field("value" of GTypeRef("Int")) {
						resolve { true }
					}
				}
			}
		}

		assertEquals(actual = exception.errors.single().message, expected = "Cannot redefine built-in scalar type \"Int\".")
	}

	@Test
	fun variableCoercion_attachedCoercerWinsOverTheTypesOwnMember() = runTest {
		val schema = GraphQL.schema {
			Scalar(GTypeRef(CUSTOM_TYPE)) {
				coerceInputValue { value -> "coercer saw $value" }
			}

			Query {
				field("echo" of String) {
					argument("value" of GTypeRef(CUSTOM_TYPE))
					resolve { arguments["value"].toString() }
				}
			}
		}

		val result = executeAndSerialize(schema, "query(\$v: $CUSTOM_TYPE) { echo(value: \$v) }", variableValues = mapOf("v" to 7))
		assertEquals(actual = dataValue(result, "echo"), expected = "coercer saw 7")
	}

	@Test
	fun literalCoercion_attachedCoercerWinsOverTheTypesOwnMember() = runTest {
		val schema = GraphQL.schema {
			Scalar(GTypeRef(CUSTOM_TYPE)) {
				coerceInputLiteral { value -> "coercer saw $value" }
			}

			Query {
				field("echo" of String) {
					argument("value" of GTypeRef(CUSTOM_TYPE))
					resolve { arguments["value"].toString() }
				}
			}
		}

		assertEquals(actual = dataValue(executeAndSerialize(schema, "{ echo(value: 3) }"), "echo"), expected = "coercer saw 3")
	}

	// A scalar that defines no literal coercion at all is still consulted on the literal path: the
	// converter converts the AST generically and hands the result to `coerceInputValue`. For a custom
	// scalar that member is the identity, so the coerced argument is the unwrapped literal — an input
	// object becomes a Map and an enum literal becomes its name.
	@Test
	fun literalCoercion_fallsBackToTheGenericAstConversionWhenNoLiteralCoercionExists() = runTest {
		val schema = GraphQL.schema {
			Scalar(GTypeRef(CUSTOM_TYPE))

			Query {
				field("echo" of String) {
					argument("value" of GTypeRef(CUSTOM_TYPE))
					resolve { arguments["value"].let { "${it?.let { value -> value::class.simpleName }}:$it" } }
				}
			}
		}

		assertEquals(actual = dataValue(executeAndSerialize(schema, "{ echo(value: {a: 1}) }"), "echo"), expected = "LinkedHashMap:{a=1}")
		assertEquals(actual = dataValue(executeAndSerialize(schema, "{ echo(value: FOO) }"), "echo"), expected = "String:FOO")
	}

	// The scalar members throw a bare, context-free message; the converter is what adds the position.
	@Test
	fun outputCoercion_enrichesTheBareMessageFromTheScalar() = runTest {
		val schema = GraphQL.schema {
			Query {
				field("value" of GTypeRef("Int")) {
					resolve { 1.5 }
				}
			}
		}

		assertEquals(
			actual = firstErrorMessage(executeAndSerialize(schema, "{ value }")),
			expected = "Output coercion encountered an invalid resolved value for field 'value' of type 'Int' in type 'Query' " +
				"(Int cannot represent non-integer value: 1.5):\nkotlin.Double: 1.5",
		)
	}

	@Test
	fun variableCoercion_enrichesTheBareMessageFromTheScalar() = runTest {
		val schema = GraphQL.schema {
			Query {
				field("echo" of String) {
					argument("value" of GTypeRef("Int"))
					resolve { arguments["value"].toString() }
				}
			}
		}

		val result = executeAndSerialize(schema, "query(\$v: Int) { echo(value: \$v) }", variableValues = mapOf("v" to 1.5))
		assertEquals(
			actual = firstErrorMessage(result),
			expected = "Double value is not valid for variable 'v' with type 'Int' (Int cannot represent non-integer value: 1.5).",
		)
	}
}
