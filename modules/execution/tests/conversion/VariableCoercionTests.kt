package testing

import io.fluidsonic.graphql.GExecutor
import io.fluidsonic.graphql.GInputValueCoercer
import io.fluidsonic.graphql.GraphQL
import io.fluidsonic.graphql.InputObjectInputValueCoercerExtensionKey
import io.fluidsonic.graphql.arguments
import io.fluidsonic.graphql.default
import io.fluidsonic.graphql.document
import io.fluidsonic.graphql.isNotEmpty
import io.fluidsonic.graphql.resolve
import io.fluidsonic.graphql.schema
import io.fluidsonic.graphql.type
import io.fluidsonic.graphql.value
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class VariableCoercionTests {

	private val stringSchema = GraphQL.schema {
		Query {
			field("echo" of String) {
				argument("value" of String)
				resolve { arguments["value"] as String? }
			}
		}
	}

	@Test
	fun testVariableWithCorrectType_string() = runTest {
		assertExecution(
			schema = stringSchema,
			document = """query(${'$'}v: String) { echo(value: ${'$'}v) }""",
			variableValues = mapOf("v" to "hello"),
			expected = mapOf("data" to mapOf("echo" to "hello")),
		)
	}

	@Test
	fun testVariableWithCorrectType_int() = runTest {
		val schema = GraphQL.schema {
			Query {
				field("echo" of Int) {
					argument("value" of Int)
					resolve { arguments["value"] as Int? }
				}
			}
		}

		assertExecution(
			schema = schema,
			document = """query(${'$'}v: Int) { echo(value: ${'$'}v) }""",
			variableValues = mapOf("v" to 42),
			expected = mapOf("data" to mapOf("echo" to 42)),
		)
	}

	@Test
	fun testVariableWithCorrectType_boolean() = runTest {
		val schema = GraphQL.schema {
			Query {
				field("echo" of Boolean) {
					argument("value" of Boolean)
					resolve { arguments["value"] as Boolean? }
				}
			}
		}

		assertExecution(
			schema = schema,
			document = """query(${'$'}v: Boolean) { echo(value: ${'$'}v) }""",
			variableValues = mapOf("v" to true),
			expected = mapOf("data" to mapOf("echo" to true)),
		)
	}

	@Test
	fun testVariableWithCorrectType_float() = runTest {
		val schema = GraphQL.schema {
			Query {
				field("echo" of Float) {
					argument("value" of Float)
					resolve { arguments["value"] as Double? }
				}
			}
		}

		assertExecution(
			schema = schema,
			document = """query(${'$'}v: Float) { echo(value: ${'$'}v) }""",
			variableValues = mapOf("v" to 3.14),
			expected = mapOf("data" to mapOf("echo" to 3.14)),
		)
	}

	@Test
	fun testVariableWithCorrectType_floatFromInt() = runTest {
		val schema = GraphQL.schema {
			Query {
				field("echo" of Float) {
					argument("value" of Float)
					resolve { arguments["value"] as Double? }
				}
			}
		}

		assertExecution(
			schema = schema,
			document = """query(${'$'}v: Float) { echo(value: ${'$'}v) }""",
			variableValues = mapOf("v" to 5),
			expected = mapOf("data" to mapOf("echo" to 5.0)),
		)
	}

	@Test
	fun testVariableWithCorrectType_id_fromString() = runTest {
		val schema = GraphQL.schema {
			Query {
				field("echo" of ID) {
					argument("value" of ID)
					resolve { arguments["value"] as String? }
				}
			}
		}

		assertExecution(
			schema = schema,
			document = """query(${'$'}v: ID) { echo(value: ${'$'}v) }""",
			variableValues = mapOf("v" to "abc"),
			expected = mapOf("data" to mapOf("echo" to "abc")),
		)
	}

	@Test
	fun testVariableWithCorrectType_id_fromInt() = runTest {
		val schema = GraphQL.schema {
			Query {
				field("echo" of ID) {
					argument("value" of ID)
					resolve { arguments["value"] as String? }
				}
			}
		}

		assertExecution(
			schema = schema,
			document = """query(${'$'}v: ID) { echo(value: ${'$'}v) }""",
			variableValues = mapOf("v" to 123),
			expected = mapOf("data" to mapOf("echo" to "123")),
		)
	}

	@Test
	fun testVariableWithWrongType() = runTest {
		val schema = GraphQL.schema {
			Query {
				field("echo" of Int) {
					argument("value" of Int)
					resolve { arguments["value"] as Int? }
				}
			}
		}

		val executor = GExecutor.default(schema = schema)
		val result = executor.execute(
			"""query(${'$'}v: Int) { echo(value: ${'$'}v) }""",
			variableValues = mapOf("v" to "not-an-int"),
		)
		assertTrue(result.errors.isNotEmpty(), "Expected errors for wrong variable type")
	}

	@Test
	fun testMissingRequiredVariable() = runTest {
		val schema = GraphQL.schema {
			Query {
				field("echo" of String) {
					argument("value" of !String)
					resolve { arguments["value"] as String }
				}
			}
		}

		val executor = GExecutor.default(schema = schema)
		val result = executor.execute(
			"""query(${'$'}v: String!) { echo(value: ${'$'}v) }""",
			variableValues = emptyMap(),
		)
		assertTrue(result.errors.isNotEmpty(), "Expected errors for missing required variable")
	}

	@Test
	fun testDefaultValueForMissingOptionalVariable() = runTest {
		assertExecution(
			schema = stringSchema,
			document = """query(${'$'}v: String = "default") { echo(value: ${'$'}v) }""",
			variableValues = emptyMap(),
			expected = mapOf("data" to mapOf("echo" to "default")),
		)
	}

	@Test
	fun testNullVariableForNullableParameter() = runTest {
		assertExecution(
			schema = stringSchema,
			document = """query(${'$'}v: String) { echo(value: ${'$'}v) }""",
			variableValues = mapOf("v" to null),
			expected = mapOf("data" to mapOf("echo" to null)),
		)
	}

	@Test
	fun testNullVariableForNonNullParameter() = runTest {
		val schema = GraphQL.schema {
			Query {
				field("echo" of String) {
					argument("value" of !String)
					resolve { arguments["value"] as String }
				}
			}
		}

		val executor = GExecutor.default(schema = schema)
		val result = executor.execute(
			"""query(${'$'}v: String!) { echo(value: ${'$'}v) }""",
			variableValues = mapOf("v" to null),
		)
		assertTrue(result.errors.isNotEmpty(), "Expected errors for null in non-null variable")
	}

	@Test
	fun testListVariableCoercion() = runTest {
		val schema = GraphQL.schema {
			Query {
				field("sum" of Int) {
					argument("values" of List(Int))
					resolve {
						@Suppress("UNCHECKED_CAST")
						val values = arguments["values"] as List<Int?>
						values.filterNotNull().sum()
					}
				}
			}
		}

		assertExecution(
			schema = schema,
			document = """query(${'$'}v: [Int]) { sum(values: ${'$'}v) }""",
			variableValues = mapOf("v" to listOf(10, 20, 30)),
			expected = mapOf("data" to mapOf("sum" to 60)),
		)
	}

	@Test
	fun testEnumVariableCoercion() = runTest {
		val schema = GraphQL.schema {
			val Color by type

			Enum(Color) {
				value("RED")
				value("GREEN")
				value("BLUE")
			}

			Query {
				field("color" of String) {
					argument("value" of Color)
					resolve { arguments["value"] as String? }
				}
			}
		}

		assertExecution(
			schema = schema,
			document = """query(${'$'}v: Color) { color(value: ${'$'}v) }""",
			variableValues = mapOf("v" to "GREEN"),
			expected = mapOf("data" to mapOf("color" to "GREEN")),
		)
	}

	@Test
	fun testEnumVariableCoercion_invalidValue() = runTest {
		val schema = GraphQL.schema {
			val Color by type

			Enum(Color) {
				value("RED")
				value("GREEN")
				value("BLUE")
			}

			Query {
				field("color" of String) {
					argument("value" of Color)
					resolve { arguments["value"] as String? }
				}
			}
		}

		val executor = GExecutor.default(schema = schema)
		val result = executor.execute(
			"""query(${'$'}v: Color) { color(value: ${'$'}v) }""",
			variableValues = mapOf("v" to "YELLOW"),
		)
		assertTrue(result.errors.isNotEmpty(), "Expected errors for invalid enum variable value")
	}

	@Test
	fun testInputObjectVariableCoercion() = runTest {
		val schema = GraphQL.schema {
			val Point by type

			InputObject(Point) {
				argument("x" of Int)
				argument("y" of Int)
			}

			Query {
				field("sum" of Int) {
					argument("point" of Point)
					resolve {
						@Suppress("UNCHECKED_CAST")
						val point = arguments["point"] as Map<String, Any?>
						(point["x"] as Int) + (point["y"] as Int)
					}
				}
			}
		}

		assertExecution(
			schema = schema,
			document = """query(${'$'}p: Point) { sum(point: ${'$'}p) }""",
			variableValues = mapOf("p" to mapOf("x" to 10, "y" to 20)),
			expected = mapOf("data" to mapOf("sum" to 30)),
		)
	}

	@Test
	fun testInputObjectVariableCoercion_wrongType() = runTest {
		val schema = GraphQL.schema {
			val Point by type

			InputObject(Point) {
				argument("x" of Int)
				argument("y" of Int)
			}

			Query {
				field("sum" of Int) {
					argument("point" of Point)
					resolve {
						@Suppress("UNCHECKED_CAST")
						val point = arguments["point"] as Map<String, Any?>
						(point["x"] as Int) + (point["y"] as Int)
					}
				}
			}
		}

		val executor = GExecutor.default(schema = schema)
		val result = executor.execute(
			"""query(${'$'}p: Point) { sum(point: ${'$'}p) }""",
			variableValues = mapOf("p" to "not-a-map"),
		)
		assertTrue(result.errors.isNotEmpty(), "Expected errors for wrong input object variable type")
	}

	@Test
	fun testInputObjectVariableCoercion_skipsCoercerWhenFieldIsInvalid() = runTest {
		assertInputObjectCoercion(
			document = """query(${'$'}reason: Reason) { reason(value: ${'$'}reason) }""",
			variableValues = mapOf("reason" to mapOf("type" to "dod")),
			expectedErrors = listOf(
				"String value is not valid for type 'ReasonType' in variable 'reason.type' with type 'ReasonType!' (valid values: ACCIDENT, ILLNESS).",
			),
			expectedCoercerCalls = 0,
		)
	}

	@Test
	fun testInputObjectVariableCoercion_skipsCoercerWhenRequiredFieldIsMissing() = runTest {
		assertInputObjectCoercion(
			document = """query(${'$'}reason: Reason) { reason(value: ${'$'}reason) }""",
			variableValues = mapOf("reason" to emptyMap<String, Any?>()),
			expectedErrors = listOf(
				"A value must be provided for variable 'reason.type' of type 'ReasonType!'.",
			),
			expectedCoercerCalls = 0,
		)
	}

	@Test
	fun testInputObjectVariableCoercion_skipsCoercerWhenNonNullFieldIsNull() = runTest {
		assertInputObjectCoercion(
			document = """query(${'$'}reason: Reason) { reason(value: ${'$'}reason) }""",
			variableValues = mapOf("reason" to mapOf("type" to null)),
			expectedErrors = listOf(
				"Null value is not valid for variable 'reason.type' with type 'ReasonType!'.",
			),
			expectedCoercerCalls = 0,
		)
	}

	@Test
	fun testInputObjectVariableCoercion_skipsCoercerWhenListFieldElementIsInvalid() = runTest {
		assertInputObjectCoercion(
			document = """query(${'$'}batch: Batch) { batch(value: ${'$'}batch) }""",
			variableValues = mapOf("batch" to mapOf("types" to listOf("ACCIDENT", "dod"))),
			expectedErrors = listOf(
				"String value is not valid for type 'ReasonType' in variable 'batch.types[1]' with type '[ReasonType!]!' (valid values: ACCIDENT, ILLNESS).",
			),
			expectedCoercerCalls = 0,
		)
	}

	@Test
	fun testInputObjectVariableCoercion_skipsOuterCoercerWhenNestedObjectFieldIsInvalid() = runTest {
		assertInputObjectCoercion(
			document = """query(${'$'}holder: Holder) { holder(value: ${'$'}holder) }""",
			variableValues = mapOf("holder" to mapOf("reason" to mapOf("type" to "dod"))),
			expectedErrors = listOf(
				"String value is not valid for type 'ReasonType' in variable 'holder.reason.type' with type 'ReasonType!' (valid values: ACCIDENT, ILLNESS).",
			),
			expectedCoercerCalls = 0,
		)
	}

	// The failed element stays in the list rather than being dropped, so only the error count can tell the outer object.
	// The one expected call is the valid first element's.
	@Test
	fun testInputObjectVariableCoercion_skipsOuterCoercerWhenObjectInListFieldIsInvalid() = runTest {
		assertInputObjectCoercion(
			document = """query(${'$'}holder: Holder) { holder(value: ${'$'}holder) }""",
			variableValues = mapOf("holder" to mapOf("reasons" to listOf(mapOf("type" to "ACCIDENT"), mapOf("type" to "dod")))),
			expectedErrors = listOf(
				"String value is not valid for type 'ReasonType' in variable 'holder.reasons[1].type' with type 'ReasonType!' (valid values: ACCIDENT, ILLNESS).",
			),
			expectedCoercerCalls = 1,
		)
	}

	@Test
	fun testInputObjectVariableCoercion_runsCoercerWhenOnlyAnotherVariableIsInvalid() = runTest {
		assertInputObjectCoercion(
			document = """query(${'$'}type: ReasonType!, ${'$'}reason: Reason!) { type(value: ${'$'}type) reason(value: ${'$'}reason) }""",
			variableValues = mapOf("type" to "dod", "reason" to mapOf("type" to "ACCIDENT")),
			expectedErrors = listOf(
				"String value is not valid for type 'ReasonType' in variable 'type' with type 'ReasonType!' (valid values: ACCIDENT, ILLNESS).",
			),
			expectedCoercerCalls = 1,
		)
	}

	@Test
	fun testVariableWithDefaultValue_overriddenByProvided() = runTest {
		assertExecution(
			schema = stringSchema,
			document = """query(${'$'}v: String = "default") { echo(value: ${'$'}v) }""",
			variableValues = mapOf("v" to "provided"),
			expected = mapOf("data" to mapOf("echo" to "provided")),
		)
	}

	private suspend fun assertInputObjectCoercion(document: String, variableValues: Map<String, Any?>, expectedErrors: List<String>, expectedCoercerCalls: Int) {
		// Counts calls instead of failing inside the coercer, because the executor turns an exception thrown there into an error.
		var coercerCalls = 0
		val countingCoercer = GInputValueCoercer<Map<String, Any?>> { value ->
			coercerCalls += 1
			value
		}

		val schema = GraphQL.schema {
			val Batch by type
			val Holder by type
			val Reason by type
			val ReasonType by type

			Enum(ReasonType) {
				value("ACCIDENT")
				value("ILLNESS")
			}

			InputObject(Batch) {
				argument("types" of !List(!ReasonType))
				extension(InputObjectInputValueCoercerExtensionKey, countingCoercer)
			}

			InputObject(Holder) {
				argument("reason" of Reason)
				argument("reasons" of List(!Reason))
				extension(InputObjectInputValueCoercerExtensionKey, countingCoercer)
			}

			InputObject(Reason) {
				argument("type" of !ReasonType)
				extension(InputObjectInputValueCoercerExtensionKey, countingCoercer)
			}

			Query {
				field("batch" of String) {
					argument("value" of Batch)
					resolve { "resolved" }
				}
				field("holder" of String) {
					argument("value" of Holder)
					resolve { "resolved" }
				}
				field("reason" of String) {
					argument("value" of Reason)
					resolve { "resolved" }
				}
				field("type" of String) {
					argument("value" of ReasonType)
					resolve { "resolved" }
				}
			}
		}

		val result = GExecutor.default(schema = schema).execute(document, variableValues = variableValues)

		assertEquals(expected = expectedErrors, actual = result.errors.map { it.message })
		assertEquals(expected = expectedCoercerCalls, actual = coercerCalls, message = "Input object coercer calls")
	}
}
