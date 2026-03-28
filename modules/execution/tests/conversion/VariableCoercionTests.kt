package testing

import io.fluidsonic.graphql.*
import kotlin.test.*
import kotlinx.coroutines.test.*


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
			expected = mapOf("data" to mapOf("echo" to "hello"))
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
			expected = mapOf("data" to mapOf("echo" to 42))
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
			expected = mapOf("data" to mapOf("echo" to true))
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
			expected = mapOf("data" to mapOf("echo" to 3.14))
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
			expected = mapOf("data" to mapOf("echo" to 5.0))
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
			expected = mapOf("data" to mapOf("echo" to "abc"))
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
			expected = mapOf("data" to mapOf("echo" to "123"))
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
			variableValues = mapOf("v" to "not-an-int")
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
			variableValues = emptyMap()
		)
		assertTrue(result.errors.isNotEmpty(), "Expected errors for missing required variable")
	}


	@Test
	fun testDefaultValueForMissingOptionalVariable() = runTest {
		assertExecution(
			schema = stringSchema,
			document = """query(${'$'}v: String = "default") { echo(value: ${'$'}v) }""",
			variableValues = emptyMap(),
			expected = mapOf("data" to mapOf("echo" to "default"))
		)
	}


	@Test
	fun testNullVariableForNullableParameter() = runTest {
		assertExecution(
			schema = stringSchema,
			document = """query(${'$'}v: String) { echo(value: ${'$'}v) }""",
			variableValues = mapOf("v" to null),
			expected = mapOf("data" to mapOf("echo" to null))
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
			variableValues = mapOf("v" to null)
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
			expected = mapOf("data" to mapOf("sum" to 60))
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
			expected = mapOf("data" to mapOf("color" to "GREEN"))
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
			variableValues = mapOf("v" to "YELLOW")
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
			expected = mapOf("data" to mapOf("sum" to 30))
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
			variableValues = mapOf("p" to "not-a-map")
		)
		assertTrue(result.errors.isNotEmpty(), "Expected errors for wrong input object variable type")
	}


	@Test
	fun testVariableWithDefaultValue_overriddenByProvided() = runTest {
		assertExecution(
			schema = stringSchema,
			document = """query(${'$'}v: String = "default") { echo(value: ${'$'}v) }""",
			variableValues = mapOf("v" to "provided"),
			expected = mapOf("data" to mapOf("echo" to "provided"))
		)
	}
}
