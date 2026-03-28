package testing

import io.fluidsonic.graphql.*
import kotlin.test.*
import kotlinx.coroutines.test.*


class InputCoercionTests {

	@Test
	fun testScalarCoercion_string() = runTest {
		assertExecution(
			schema = GraphQL.schema {
				Query {
					field("echo" of String) {
						argument("value" of String)
						resolve { arguments["value"] as String? }
					}
				}
			},
			document = """{ echo(value: "hello") }""",
			expected = mapOf("data" to mapOf("echo" to "hello"))
		)
	}


	@Test
	fun testScalarCoercion_int() = runTest {
		assertExecution(
			schema = GraphQL.schema {
				Query {
					field("echo" of Int) {
						argument("value" of Int)
						resolve { arguments["value"] as Int? }
					}
				}
			},
			document = """{ echo(value: 42) }""",
			expected = mapOf("data" to mapOf("echo" to 42))
		)
	}


	@Test
	fun testScalarCoercion_float() = runTest {
		assertExecution(
			schema = GraphQL.schema {
				Query {
					field("echo" of Float) {
						argument("value" of Float)
						resolve { arguments["value"] as Double? }
					}
				}
			},
			document = """{ echo(value: 3.14) }""",
			expected = mapOf("data" to mapOf("echo" to 3.14))
		)
	}


	@Test
	fun testScalarCoercion_floatFromInt() = runTest {
		assertExecution(
			schema = GraphQL.schema {
				Query {
					field("echo" of Float) {
						argument("value" of Float)
						resolve { arguments["value"] as Double? }
					}
				}
			},
			document = """{ echo(value: 5) }""",
			expected = mapOf("data" to mapOf("echo" to 5.0))
		)
	}


	@Test
	fun testScalarCoercion_boolean() = runTest {
		assertExecution(
			schema = GraphQL.schema {
				Query {
					field("echo" of Boolean) {
						argument("value" of Boolean)
						resolve { arguments["value"] as Boolean? }
					}
				}
			},
			document = """{ echo(value: true) }""",
			expected = mapOf("data" to mapOf("echo" to true))
		)
	}


	@Test
	fun testScalarCoercion_id_fromString() = runTest {
		assertExecution(
			schema = GraphQL.schema {
				Query {
					field("echo" of ID) {
						argument("value" of ID)
						resolve { arguments["value"] as String? }
					}
				}
			},
			document = """{ echo(value: "abc123") }""",
			expected = mapOf("data" to mapOf("echo" to "abc123"))
		)
	}


	@Test
	fun testScalarCoercion_id_fromInt() = runTest {
		assertExecution(
			schema = GraphQL.schema {
				Query {
					field("echo" of ID) {
						argument("value" of ID)
						resolve { arguments["value"] as String? }
					}
				}
			},
			document = """{ echo(value: 123) }""",
			expected = mapOf("data" to mapOf("echo" to "123"))
		)
	}


	@Test
	fun testEnumInputCoercion() = runTest {
		assertExecution(
			schema = GraphQL.schema {
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
			},
			document = """{ color(value: RED) }""",
			expected = mapOf("data" to mapOf("color" to "RED"))
		)
	}


	@Test
	fun testInputObjectCoercion() = runTest {
		assertExecution(
			schema = GraphQL.schema {
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
			},
			document = """{ sum(point: { x: 3, y: 4 }) }""",
			expected = mapOf("data" to mapOf("sum" to 7))
		)
	}


	@Test
	fun testListInputCoercion() = runTest {
		assertExecution(
			schema = GraphQL.schema {
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
			},
			document = """{ sum(values: [1, 2, 3]) }""",
			expected = mapOf("data" to mapOf("sum" to 6))
		)
	}


	@Test
	fun testListInputCoercion_singleValueCoercedToList() = runTest {
		assertExecution(
			schema = GraphQL.schema {
				Query {
					field("count" of Int) {
						argument("values" of List(Int))
						resolve {
							@Suppress("UNCHECKED_CAST")
							val values = arguments["values"] as List<Int?>
							values.size
						}
					}
				}
			},
			document = """{ count(values: 42) }""",
			expected = mapOf("data" to mapOf("count" to 1))
		)
	}


	@Test
	fun testNullHandling_nullableArgumentWithNull() = runTest {
		assertExecution(
			schema = GraphQL.schema {
				Query {
					field("echo" of String) {
						argument("value" of String)
						resolve { arguments["value"] as String? }
					}
				}
			},
			document = """{ echo(value: null) }""",
			expected = mapOf("data" to mapOf("echo" to null))
		)
	}


	@Test
	fun testNullHandling_nonNullArgumentWithNull() = runTest {
		val schema = GraphQL.schema {
			Query {
				field("echo" of String) {
					argument("value" of !String)
					resolve { arguments["value"] as String }
				}
			}
		}

		val executor = GExecutor.default(schema = schema)
		val result = executor.execute("""{ echo(value: null) }""")
		assertTrue(result.errors.isNotEmpty(), "Expected errors for null in non-null position")
	}


	@Test
	fun testDefaultValueApplication() = runTest {
		assertExecution(
			schema = GraphQL.schema {
				Query {
					field("echo" of String) {
						argument("value" of String default value(GStringValue("fallback")))
						resolve { arguments["value"] as String? }
					}
				}
			},
			document = """{ echo }""",
			expected = mapOf("data" to mapOf("echo" to "fallback"))
		)
	}


	@Test
	fun testDefaultValueApplication_intDefault() = runTest {
		assertExecution(
			schema = GraphQL.schema {
				Query {
					field("echo" of Int) {
						argument("value" of Int default value(GIntValue(99)))
						resolve { arguments["value"] as Int? }
					}
				}
			},
			document = """{ echo }""",
			expected = mapOf("data" to mapOf("echo" to 99))
		)
	}


	@Test
	fun testNestedInputObjectCoercion() = runTest {
		assertExecution(
			schema = GraphQL.schema {
				val Inner by type
				val Outer by type

				InputObject(Inner) {
					argument("value" of Int)
				}

				InputObject(Outer) {
					argument("inner" of Inner)
				}

				Query {
					field("extract" of Int) {
						argument("input" of Outer)
						resolve {
							@Suppress("UNCHECKED_CAST")
							val outer = arguments["input"] as Map<String, Any?>
							@Suppress("UNCHECKED_CAST")
							val inner = outer["inner"] as Map<String, Any?>
							inner["value"] as Int
						}
					}
				}
			},
			document = """{ extract(input: { inner: { value: 42 } }) }""",
			expected = mapOf("data" to mapOf("extract" to 42))
		)
	}


	@Test
	fun testVariableReferenceInArgument() = runTest {
		assertExecution(
			schema = GraphQL.schema {
				Query {
					field("echo" of String) {
						argument("value" of String)
						resolve { arguments["value"] as String? }
					}
				}
			},
			document = """query(${'$'}v: String) { echo(value: ${'$'}v) }""",
			variableValues = mapOf("v" to "from-variable"),
			expected = mapOf("data" to mapOf("echo" to "from-variable"))
		)
	}


	@Test
	fun testVariableReference_missingOptionalVariable_usesArgumentDefault() = runTest {
		assertExecution(
			schema = GraphQL.schema {
				Query {
					field("echo" of String) {
						argument("value" of String default value(GStringValue("default-arg")))
						resolve { arguments["value"] as String? }
					}
				}
			},
			document = """query(${'$'}v: String) { echo(value: ${'$'}v) }""",
			variableValues = emptyMap(),
			expected = mapOf("data" to mapOf("echo" to "default-arg"))
		)
	}


	@Test
	fun testEnumInputCoercion_invalidValue() = runTest {
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
		val result = executor.execute("""{ color(value: YELLOW) }""")
		assertTrue(result.errors.isNotEmpty(), "Expected errors for invalid enum value")
	}
}
