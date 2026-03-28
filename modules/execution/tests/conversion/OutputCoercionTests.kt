package testing

import io.fluidsonic.graphql.*
import kotlin.test.*
import kotlinx.coroutines.test.*


class OutputCoercionTests {

	@Test
	fun testScalarOutputCoercion_string() = runTest {
		assertExecution(
			schema = GraphQL.schema {
				Query {
					field("value" of String) {
						resolve { "hello" }
					}
				}
			},
			document = """{ value }""",
			expected = mapOf("data" to mapOf("value" to "hello"))
		)
	}


	@Test
	fun testScalarOutputCoercion_int() = runTest {
		assertExecution(
			schema = GraphQL.schema {
				Query {
					field("value" of Int) {
						resolve { 42 }
					}
				}
			},
			document = """{ value }""",
			expected = mapOf("data" to mapOf("value" to 42))
		)
	}


	@Test
	fun testScalarOutputCoercion_float() = runTest {
		assertExecution(
			schema = GraphQL.schema {
				Query {
					field("value" of Float) {
						resolve { 3.14 }
					}
				}
			},
			document = """{ value }""",
			expected = mapOf("data" to mapOf("value" to 3.14))
		)
	}


	@Test
	fun testScalarOutputCoercion_floatFromInt() = runTest {
		assertExecution(
			schema = GraphQL.schema {
				Query {
					field("value" of Float) {
						resolve { 5 }
					}
				}
			},
			document = """{ value }""",
			expected = mapOf("data" to mapOf("value" to 5.0))
		)
	}


	@Test
	fun testScalarOutputCoercion_boolean() = runTest {
		assertExecution(
			schema = GraphQL.schema {
				Query {
					field("value" of Boolean) {
						resolve { true }
					}
				}
			},
			document = """{ value }""",
			expected = mapOf("data" to mapOf("value" to true))
		)
	}


	@Test
	fun testScalarOutputCoercion_id_fromString() = runTest {
		assertExecution(
			schema = GraphQL.schema {
				Query {
					field("value" of ID) {
						resolve { "abc" }
					}
				}
			},
			document = """{ value }""",
			expected = mapOf("data" to mapOf("value" to "abc"))
		)
	}


	@Test
	fun testScalarOutputCoercion_id_fromInt() = runTest {
		assertExecution(
			schema = GraphQL.schema {
				Query {
					field("value" of ID) {
						resolve { 123 }
					}
				}
			},
			document = """{ value }""",
			expected = mapOf("data" to mapOf("value" to "123"))
		)
	}


	@Test
	fun testEnumOutputCoercion() = runTest {
		assertExecution(
			schema = GraphQL.schema {
				val Status by type

				Enum(Status) {
					value("ACTIVE")
					value("INACTIVE")
				}

				Query {
					field("status" of Status) {
						resolve { "ACTIVE" }
					}
				}
			},
			document = """{ status }""",
			expected = mapOf("data" to mapOf("status" to "ACTIVE"))
		)
	}


	@Test
	fun testObjectOutputCoercion() = runTest {
		assertExecution(
			schema = GraphQL.schema {
				val User by type

				Object(User) {
					field("name" of String) {
						resolve { "Alice" }
					}
					field("age" of Int) {
						resolve { 30 }
					}
				}

				Query {
					field("user" of User) {
						resolve { Unit }
					}
				}
			},
			document = """{ user { name age } }""",
			expected = mapOf("data" to mapOf("user" to mapOf("name" to "Alice", "age" to 30)))
		)
	}


	@Test
	fun testListOutputCoercion() = runTest {
		assertExecution(
			schema = GraphQL.schema {
				Query {
					field("values" of List(Int)) {
						resolve { listOf(1, 2, 3) }
					}
				}
			},
			document = """{ values }""",
			expected = mapOf("data" to mapOf("values" to listOf(1, 2, 3)))
		)
	}


	@Test
	fun testNonNullFieldReturningNull_producesError() = runTest {
		val schema = GraphQL.schema {
			Query {
				field("value" of !String) {
					resolve { null }
				}
			}
		}

		val executor = GExecutor.default(schema = schema)
		// Non-null field returning null throws IllegalStateException
		assertFailsWith<IllegalStateException> {
			executor.execute("""{ value }""")
		}
	}


	@Test
	fun testNestedObjectOutputCoercion() = runTest {
		assertExecution(
			schema = GraphQL.schema {
				val AddressType by type
				val PersonType by type

				Object<Address>(AddressType) {
					field("city" of String) {
						resolve { it.city }
					}
					field("zip" of String) {
						resolve { it.zip }
					}
				}

				Object<Person>(PersonType) {
					field("name" of String) {
						resolve { it.name }
					}
					field("address" of AddressType) {
						resolve { it.address }
					}
				}

				Query {
					field("person" of PersonType) {
						resolve { Person("Alice", Address("Berlin", "10115")) }
					}
				}
			},
			document = """{ person { name address { city zip } } }""",
			expected = mapOf(
				"data" to mapOf(
					"person" to mapOf(
						"name" to "Alice",
						"address" to mapOf(
							"city" to "Berlin",
							"zip" to "10115"
						)
					)
				)
			)
		)
	}


	@Test
	fun testListOfObjectsOutputCoercion() = runTest {
		assertExecution(
			schema = GraphQL.schema {
				val ItemType by type

				Object<Item>(ItemType) {
					field("id" of Int) {
						resolve { it.id }
					}
					field("label" of String) {
						resolve { it.label }
					}
				}

				Query {
					field("items" of List(ItemType)) {
						resolve { listOf(Item(1, "first"), Item(2, "second")) }
					}
				}
			},
			document = """{ items { id label } }""",
			expected = mapOf(
				"data" to mapOf(
					"items" to listOf(
						mapOf("id" to 1, "label" to "first"),
						mapOf("id" to 2, "label" to "second")
					)
				)
			)
		)
	}


	@Test
	fun testInterfaceTypeResolution() = runTest {
		assertExecution(
			schema = interfaceSchema,
			document = """{ animal { name ... on Dog { breed } } }""",
			expected = mapOf(
				"data" to mapOf(
					"animal" to mapOf(
						"name" to "Rex",
						"breed" to "Labrador"
					)
				)
			)
		)
	}


	@Test
	fun testUnionTypeResolution() = runTest {
		assertExecution(
			schema = unionSchema,
			document = """{ search { ... on TextResult { text } ... on NumberResult { number } } }""",
			expected = mapOf(
				"data" to mapOf(
					"search" to mapOf(
						"text" to "found it"
					)
				)
			)
		)
	}


	@Test
	fun testNullableFieldReturningNull() = runTest {
		assertExecution(
			schema = GraphQL.schema {
				Query {
					field("value" of String) {
						resolve { null }
					}
				}
			},
			document = """{ value }""",
			expected = mapOf("data" to mapOf("value" to null))
		)
	}


	@Test
	fun testCustomScalarOutputCoercion() = runTest {
		assertExecution(
			schema = GraphQL.schema {
				val DateScalar by type

				Scalar(DateScalar) {
					coerceOutput { input ->
						(input as String).uppercase()
					}
				}

				Query {
					field("date" of DateScalar) {
						resolve { "march" }
					}
				}
			},
			document = """{ date }""",
			expected = mapOf("data" to mapOf("date" to "MARCH"))
		)
	}


	companion object {

		private val interfaceSchema = GraphQL.schema {
			val Animal by type
			val Dog by type
			val Cat by type

			Interface(Animal) {
				field("name" of String)
			}

			Object<DogData>(Dog implements Animal) {
				field("name" of String) {
					resolve { it.name }
				}
				field("breed" of String) {
					resolve { it.breed }
				}
			}

			Object<CatData>(Cat implements Animal) {
				field("name" of String) {
					resolve { it.name }
				}
				field("indoor" of Boolean) {
					resolve { it.indoor }
				}
			}

			Query {
				field("animal" of Animal) {
					resolve { DogData("Rex", "Labrador") }
				}
			}
		}


		private val unionSchema = GraphQL.schema {
			val TextResult by type
			val NumberResult by type
			val SearchResult by type

			Object<TextResultData>(TextResult) {
				field("text" of String) {
					resolve { it.text }
				}
			}

			Object<NumberResultData>(NumberResult) {
				field("number" of Int) {
					resolve { it.number }
				}
			}

			Union(SearchResult with TextResult or NumberResult)

			Query {
				field("search" of SearchResult) {
					resolve { TextResultData("found it") }
				}
			}
		}
	}


	private data class Address(val city: String, val zip: String)
	private data class Person(val name: String, val address: Address)
	private data class Item(val id: Int, val label: String)
	private data class DogData(val name: String, val breed: String)
	private data class CatData(val name: String, val indoor: Boolean)
	private data class TextResultData(val text: String)
	private data class NumberResultData(val number: Int)
}
