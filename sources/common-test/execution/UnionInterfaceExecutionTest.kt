package tests

import io.fluidsonic.graphql.*
import kotlin.test.*


// https://github.com/graphql/graphql-js/blob/master/src/execution/__tests__/union-interface-test.js
class UnionInterfaceExecutionTest {

	@Test
	fun `can introspect on union and intersection types`() {
		val document = GDocument.parse("""
			|{
			|  Named: __type(name: "Named") {
			|    kind
			|    name
			|    fields { name }
			|    interfaces { name }
			|    possibleTypes { name }
			|    enumValues { name }
			|    inputFields { name }
			|  }
			|  Mammal: __type(name: "Mammal") {
			|    kind
			|    name
			|    fields { name }
			|    interfaces { name }
			|    possibleTypes { name }
			|    enumValues { name }
			|    inputFields { name }
			|  }
			|  Pet: __type(name: "Pet") {
			|    kind
			|    name
			|    fields { name }
			|    interfaces { name }
			|    possibleTypes { name }
			|    enumValues { name }
			|    inputFields { name }
			|  }
			|}
		""".trimMargin())

		val result = document.execute(
			schema = schema,
			rootValue = Unit
		)

		assertEquals(
			expected = mapOf(
				"data" to mapOf(
					"Named" to mapOf(
						"kind" to "INTERFACE",
						"name" to "Named",
						"fields" to listOf(mapOf("name" to "name")),
						"interfaces" to emptyList<Any>(),
						"possibleTypes" to listOf(
							mapOf("name" to "Person"),
							mapOf("name" to "Dog"),
							mapOf("name" to "Cat")
						),
						"enumValues" to null,
						"inputFields" to null
					),
					"Mammal" to mapOf(
						"kind" to "INTERFACE",
						"name" to "Mammal",
						"fields" to listOf(
							mapOf("name" to "progeny"),
							mapOf("name" to "mother"),
							mapOf("name" to "father")
						),
						"interfaces" to listOf(mapOf("name" to "Life")),
						"possibleTypes" to listOf(
							mapOf("name" to "Person"),
							mapOf("name" to "Dog"),
							mapOf("name" to "Cat")
						),
						"enumValues" to null,
						"inputFields" to null
					),
					"Pet" to mapOf(
						"kind" to "UNION",
						"name" to "Pet",
						"fields" to null,
						"interfaces" to null,
						"possibleTypes" to listOf(
							mapOf("name" to "Dog"),
							mapOf("name" to "Cat")
						),
						"enumValues" to null,
						"inputFields" to null
					)
				)
			),
			actual = result
		)
	}


	// FIXME If this query is invalid, then why does the JS lib unit-test it?!
//	@Test
//	fun `executes using union types`() {
//		val document = GDocument.parse("""
//			|{
//			|  __typename
//			|  name
//			|  pets {
//			|    __typename
//			|    name
//			|    barks
//			|    meows
//			|  }
//			|}
//		""".trimMargin())
//
//		// FIXME simplify
//		val context = GExecutor.default.createContext(
//			schema = schema,
//			document = document,
//			rootValue = john
//		).value!!
//
//		val result = GExecutor.default.executeRequest(context = context)
//
//		assertEquals(
//			expected = mapOf(
//				"data" to mapOf(
//					"__typename" to "Person",
//					"name" to "John",
//					"pets" to listOf(
//						mapOf(
//							"__typename" to "Cat",
//							"name" to "Garfield",
//							"meows" to false
//						),
//						mapOf(
//							"__typename" to "Dog",
//							"name" to "Odie",
//							"barks" to true
//						)
//					)
//				)
//			),
//			actual = result
//		)
//	}


	@Test
	fun `executes using union types with inline fragments`() {
		val document = GDocument.parse("""
			|{
			|  __typename
			|  name
			|  pets {
			|    __typename
			|    ... on Dog {
			|      name
			|      barks
			|    }
			|    ... on Cat {
			|      name
			|      meows
			|    }
			|  }
			|}
		""".trimMargin())

		val result = document.execute(
			schema = schema,
			rootValue = john
		)

		assertEquals(
			expected = mapOf(
				"data" to mapOf(
					"__typename" to "Person",
					"name" to "John",
					"pets" to listOf(
						mapOf(
							"__typename" to "Cat",
							"name" to "Garfield",
							"meows" to false
						),
						mapOf(
							"__typename" to "Dog",
							"name" to "Odie",
							"barks" to true
						)
					)
				)
			),
			actual = result
		)
	}


	@Test
	fun `executes using interface types with inline fragments`() {
		val document = GDocument.parse("""
			|{
			|  __typename
			|  name
			|  friends {
			|    __typename
			|    name
			|    ... on Dog {
			|      barks
			|    }
			|    ... on Cat {
			|      meows
			|    }
			|    ... on Mammal {
			|      mother {
			|        __typename
			|        ... on Dog {
			|          name
			|          barks
			|        }
			|        ... on Cat {
			|          name
			|          meows
			|        }
			|      }
			|    }
			|  }
			|}
		""".trimMargin())

		val result = document.execute(
			schema = schema,
			rootValue = john
		)

		assertEquals(
			expected = mapOf(
				"data" to mapOf(
					"__typename" to "Person",
					"name" to "John",
					"friends" to listOf(
						mapOf(
							"__typename" to "Person",
							"name" to "Liz",
							"mother" to null
						),
						mapOf(
							"__typename" to "Dog",
							"name" to "Odie",
							"barks" to true,
							"mother" to mapOf(
								"__typename" to "Dog",
								"name" to "Odie's Mom",
								"barks" to true
							)
						)
					)
				)
			),
			actual = result
		)
	}


	@Test
	fun `accepts fragment conditions of abstract types`() {
		val document = GDocument.parse("""
			|{
			|  __typename
			|  name
			|  pets {
			|    ...PetFields,
			|    ...on Mammal {
			|      mother {
			|        ...ProgenyFields
			|      }
			|    }
			|  }
			|  friends { ...FriendFields }
			|}
			|
			|fragment PetFields on Pet {
			|  __typename
			|  ... on Dog {
			|    name
			|    barks
			|  }
			|  ... on Cat {
			|    name
			|    meows
			|  }
			|}
			|
			|fragment FriendFields on Named {
			|  __typename
			|  name
			|  ... on Dog {
			|    barks
			|  }
			|  ... on Cat {
			|    meows
			|  }
			|}
			|
			|fragment ProgenyFields on Life {
			|  progeny {
			|    __typename
			|  }
			|}
		""".trimMargin())

		val result = document.execute(
			schema = schema,
			rootValue = john
		)

		assertEquals(
			expected = mapOf(
				"data" to mapOf(
					"__typename" to "Person",
					"name" to "John",
					"pets" to listOf(
						mapOf(
							"__typename" to "Cat",
							"name" to "Garfield",
							"meows" to false,
							"mother" to mapOf(
								"progeny" to listOf(mapOf("__typename" to "Cat"))
							)
						),
						mapOf(
							"__typename" to "Dog",
							"name" to "Odie",
							"barks" to true,
							"mother" to mapOf(
								"progeny" to listOf(mapOf("__typename" to "Dog"))
							)
						)
					),
					"friends" to listOf(
						mapOf(
							"__typename" to "Person",
							"name" to "Liz"
						),
						mapOf(
							"__typename" to "Dog",
							"name" to "Odie",
							"barks" to true
						)
					)
				)
			),
			actual = result
		)
	}


	companion object {

		private val garfield = Cat(
			name = "Garfield",
			meows = false,
			mother = Cat(
				name = "Garfield's Mom",
				meows = false
			)
		)

		private val odie = Dog(
			name = "Odie",
			barks = true,
			mother = Dog(
				name = "Odie's Mom",
				barks = true
			)
		)

		private val liz = Person(
			name = "Liz"
		)


		private val john = Person(
			name = "John",
			pets = listOf(garfield, odie),
			friends = listOf(liz, odie)
		)


		private val schema = schema {

			val Cat by type
			val Dog by type
			val Life by type
			val Mammal by type
			val Named by type
			val Pet by type
			val Person by type

			Query(Person)

			Object<Person>(Person implements Named and Mammal and Life) {
				field("name" of String) {
					resolve { name }
				}
				field("pets" of List(Pet)) {
					resolve { pets }
				}
				field("friends" of List(Named)) {
					resolve { friends }
				}
				field("progeny" of List(Person)) {
					resolve { progeny }
				}
				field("mother" of Person) {
					resolve { mother }
				}
				field("father" of Person) {
					resolve { father }
				}
			}

			Interface(Named) {
				field("name" of String)
			}

			Interface(Life) {
				field("progeny" of List(Life))
			}

			Interface(Mammal implements Life) {
				field("progeny" of List(Mammal))
				field("mother" of Mammal)
				field("father" of Mammal)
			}

			Object<Dog>(Dog implements Mammal and Life and Named) {
				field("name" of String) {
					resolve { name }
				}
				field("barks" of Boolean) {
					resolve { barks }
				}
				field("progeny" of List(Dog)) {
					resolve { progeny }
				}
				field("mother" of Dog) {
					resolve { mother }
				}
				field("father" of Dog) {
					resolve { father }
				}
			}

			Object<Cat>(Cat implements Mammal and Life and Named) {
				field("name" of String) {
					resolve { name }
				}
				field("meows" of Boolean) {
					resolve { meows }
				}
				field("progeny" of List(Cat)) {
					resolve { progeny }
				}
				field("mother" of Cat) {
					resolve { mother }
				}
				field("father" of Cat) {
					resolve { father }
				}
			}

			Union(Pet with Dog or Cat)
		}
	}


	private class Cat(
		var name: String,
		var meows: Boolean,
		var mother: Cat? = null,
		var father: Cat? = null,
		var progeny: List<Cat> = emptyList()
	) {

		init {
			mother?.let { it.progeny += this }
			father?.let { it.progeny += this }
		}


		override fun toString() =
			"Cat($name)"
	}


	private class Dog(
		var name: String,
		var barks: Boolean,
		var mother: Dog? = null,
		var father: Dog? = null,
		var progeny: List<Dog> = emptyList()
	) {

		init {
			mother?.let { it.progeny += this }
			father?.let { it.progeny += this }
		}


		override fun toString() =
			"Dog($name)"
	}


	private class Person(
		var name: String,
		var pets: List<Any> = emptyList(),
		var friends: List<Any> = emptyList(),
		var mother: Person? = null,
		var father: Person? = null,
		var progeny: List<Person> = emptyList()
	) {

		init {
			mother?.let { it.progeny += this }
			father?.let { it.progeny += this }
		}


		override fun toString() =
			"Person($name)"
	}
}
