package tests

import io.fluidsonic.graphql.*
import io.fluidsonic.graphql.GAst.*
import kotlin.test.*


// https://github.com/graphql/graphql-js/blob/master/src/language/__tests__/parser-test.js
class AstParsingTest {

	@Test
	fun `accepts variable inline values`() {
		parseDocument("{ field(complex: { a: { b: [ \$var ] } }) }")
	}


	@Test
	fun `accepts variable definition directives`() {
		parseDocument("query Foo(\$x: Boolean = false @bar) { field }")
	}


	@Test
	fun `accepts multi-byte characters`() {
		val ast = parseDocument("""
			|# This comment has a \u0A0A multi-byte character.
			|{ field(arg: "Has a \u0A0A multi-byte character.") }
		""")
		assertEquals(
			expected = "Has a \u0A0A multi-byte character.",
			actual = ast.definitions.single().asOperation()
				.selectionSet.selections.single().asFieldSelection()
				.arguments.single()
				.value.asString()
				.value
		)
	}


	@Test
	fun `accepts kitchen sink`() {
		parseDocument(kitchenSinkQuery)
	}


	@Test
	fun `accepts non-keywords anywhere a Name is allowed`() {
		val nonKeywords = listOf("false", "fragment", "mutation", "null", "on", "query", "subscription", "true")
		for (keyword in nonKeywords) {
			// You can't define or reference a fragment named `on`.
			val fragmentName = keyword.takeUnless { it == "on" } ?: "a"
			val document = """
				|query $keyword {
				|	... $fragmentName
				|	... on $keyword { field }
				|}
				|	fragment $fragmentName on Type {
				|	$keyword($keyword: $$keyword)
				|	@$keyword($keyword: $keyword)
				|}
			"""

			parseDocument(document, name = "keyword \"$keyword\"")
		}
	}


	@Test
	fun `parses anonymous mutation operations`() {
		parseDocument("mutation { mutationField }")
	}


	@Test
	fun `parses anonymous query operations`() {
		parseDocument("query { queryField }")
	}


	@Test
	fun `parses anonymous subscription operations`() {
		parseDocument("subscription { subscriptionField }")
	}


	@Test
	fun `parses named mutation operations`() {
		parseDocument("mutation Foo { mutationField }")
	}


	@Test
	fun `parses named query operations`() {
		parseDocument("query Foo { queryField }")
	}


	@Test
	fun `parses named subscription operations`() {
		parseDocument("subscription Foo { subscriptionField }")
	}


	@Test
	fun `creates ast`() {
		val document = parseDocument("""
			|{
			|  node(id: 4) {
			|    id,
			|    name
			|  }
			|}
			|
		""")

		document.assert {
			assertAt(0 .. 41)

			definitions.assertOneOf<Definition.Operation> {
				assertAt(0 .. 40)
				assertEquals(expected = GOperationType.query, actual = type)
				assertNull(name)
				assertEquals(expected = emptyList(), actual = variableDefinitions)
				assertEquals(expected = emptyList(), actual = directives)

				selectionSet.assert {
					assertAt(0 .. 40)

					selections.assertOneOf<Selection.Field> {
						assertAt(4 .. 38)
						assertNull(alias)
						assertEquals(expected = emptyList(), actual = directives)

						name.assert {
							assertAt(4 .. 8)
							assertEquals(expected = "node", actual = value)
						}

						arguments.assertOne {
							assertAt(9 .. 14)
							assertEquals(expected = emptyList(), actual = directives)

							name.assert {
								assertAt(9 .. 11)
								assertEquals(expected = "id", actual = value)
							}
						}

						selectionSet.assert {
							assertAt(16 .. 38)

							selections.assert(2) {

								get(0).assertOf<Selection.Field> {
									assertAt(22 .. 24)
									assertNull(alias)
									assertEquals(expected = emptyList(), actual = arguments)
									assertEquals(expected = emptyList(), actual = directives)
									assertNull(selectionSet)

									name.assert {
										assertAt(22 .. 24)
										assertEquals(expected = "id", actual = value)
									}
								}

								get(1).assertOf<Selection.Field> {
									assertAt(30 .. 34)
									assertNull(alias)
									assertEquals(expected = emptyList(), actual = arguments)
									assertEquals(expected = emptyList(), actual = directives)
									assertNull(selectionSet)

									name.assert {
										assertAt(30 .. 34)
										assertEquals(expected = "name", actual = value)
									}
								}
							}
						}
					}
				}
			}
		}
	}


	@Test
	fun `creates ast from nameless query without variables`() {
		val document = parseDocument("""
			|query {
			|  node {
			|    id
			|  }
			|}
			|
		""")

		document.assert {
			assertAt(0 .. 30)

			definitions.assertOneOf<Definition.Operation> {
				assertAt(0 .. 29)
				assertEquals(expected = GOperationType.query, actual = type)
				assertNull(name)
				assertEquals(expected = emptyList(), actual = variableDefinitions)
				assertEquals(expected = emptyList(), actual = directives)

				selectionSet.assert {
					assertAt(6 .. 29)

					selections.assertOneOf<Selection.Field> {
						assertAt(10 .. 27)
						assertNull(alias)
						assertEquals(expected = emptyList(), actual = arguments)
						assertEquals(expected = emptyList(), actual = directives)

						name.assert {
							assertAt(10 .. 14)
							assertEquals(expected = "node", actual = value)
						}

						selectionSet.assert {
							assertAt(15 .. 27)

							selections.assertOneOf<Selection.Field> {
								assertAt(21 .. 23)
								assertNull(alias)
								assertEquals(expected = emptyList(), actual = arguments)
								assertEquals(expected = emptyList(), actual = directives)
								assertNull(selectionSet)

								name.assert {
									assertAt(21 .. 23)
									assertEquals(expected = "id", actual = value)
								}
							}
						}
					}
				}
			}
		}
	}


	// FIXME experimental
//	@Test
//	fun `accepts fragment defined variables`() {
//		parseDocument("fragment a(\$v: Boolean = false) on t { f(v: \$v) }")
//	}


	@Test
	fun `contains references to source`() {
		val source = object : GSource.Parsable {
			override val content = "{ id }"
			override val name = "custom"
		}
		val document = GAst.parseDocument(source)
		assertSame(expected = source, actual = document.origin.source)
	}


	@Test
	fun `parses null value`() {
		parseValue("null").assertOf<Value.Null> {
			assertAt(0 .. 4)
		}
	}


	@Test
	fun `parses list values`() {
		parseValue("""[123 "abc"]""").assertOf<Value.List> {
			assertAt(0 .. 11)

			elements.assert(2) {

				get(0).assertOf<Value.Int> {
					assertAt(1 .. 4)
					assertEquals(expected = "123", actual = value)
				}

				get(1).assertOf<Value.String> {
					assertAt(5 .. 10)
					assertEquals(expected = "abc", actual = value)
					assertFalse(isBlock)
				}
			}
		}
	}


	@Test
	fun `parses block string`() {
		parseValue("[\"\"\"long\"\"\" \"short\"]").assertOf<Value.List> {
			assertAt(0 .. 20)

			elements.assert(2) {

				get(0).assertOf<Value.String> {
					assertAt(1 .. 11)
					assertEquals(expected = "long", actual = value)
					assertTrue(isBlock)
				}

				get(1).assertOf<Value.String> {
					assertAt(12 .. 19)
					assertEquals(expected = "short", actual = value)
					assertFalse(isBlock)
				}
			}
		}
	}


	@Test
	fun `parses well known types`() {
		parseTypeReference("String").assertOf<TypeReference.Named> {
			assertAt(0 .. 6)

			name.assert {
				assertAt(0 .. 6)
				assertEquals(expected = "String", actual = value)
			}
		}
	}


	@Test
	fun `parses custom types`() {
		parseTypeReference("MyType").assertOf<TypeReference.Named> {
			assertAt(0 .. 6)

			name.assert {
				assertAt(0 .. 6)
				assertEquals(expected = "MyType", actual = value)
			}
		}
	}


	@Test
	fun `parses list types`() {
		parseTypeReference("[MyType]").assertOf<TypeReference.List> {
			assertAt(0 .. 8)

			elementType.assertOf<TypeReference.Named> {
				assertAt(1 .. 7)

				name.assert {
					assertAt(1 .. 7)
					assertEquals(expected = "MyType", actual = value)
				}
			}
		}
	}


	@Test
	fun `parses non-null types`() {
		parseTypeReference("MyType!").assertOf<TypeReference.NonNull> {
			assertAt(0 .. 7)

			nullableType.assertOf<TypeReference.Named> {
				assertAt(0 .. 6)

				name.assert {
					assertAt(0 .. 6)
					assertEquals(expected = "MyType", actual = value)
				}
			}
		}
	}


	@Test
	fun `parses nested types`() {
		parseTypeReference("[MyType!]").assertOf<TypeReference.List> {
			assertAt(0 .. 9)

			elementType.assertOf<TypeReference.NonNull> {
				assertAt(1 .. 8)

				nullableType.assertOf<TypeReference.Named> {
					assertAt(1 .. 7)

					name.assert {
						assertAt(1 .. 7)
						assertEquals(expected = "MyType", actual = value)
					}
				}
			}
		}
	}


	private fun parseDocument(content: String, name: String = "<test>") =
		GAst.parseDocument(content.trimMargin(), name = name)


	private fun parseTypeReference(content: String, name: String = "<test>") =
		GAst.parseTypeReference(content.trimMargin(), name = name)


	private fun parseValue(content: String, name: String = "<test>") =
		GAst.parseValue(content.trimMargin(), name = name)


	private fun GAst.assertAt(range: IntRange) =
		assertEquals(expected = origin.startPosition .. origin.endPosition, actual = range)


	private fun Definition.asOperation() =
		this as Definition.Operation


	private inline fun <T : GAst> T?.assert(block: T.() -> Unit) {
		assertNotNull(this)

		block()
	}


	private inline fun <T : GAst> List<T>.assert(count: Int, block: List<T>.() -> Unit) {
		assertEquals(expected = count, actual = size)

		block()
	}


	private inline fun <reified T : GAst> GAst?.assertOf(block: T.() -> Unit) {
		assertNotNull(this)
		assertTrue { this is T }

		(this as T).block()
	}


	private inline fun <T> List<T>.assertOne(block: T.() -> Unit) {
		assertEquals(expected = 1, actual = size)

		single().block()
	}


	private inline fun <reified T : GAst> List<*>.assertOneOf(block: T.() -> Unit) {
		assertEquals(expected = 1, actual = size)

		val single = single()
		assertTrue { single is T }

		(single as T).block()
	}


	private fun Selection.asFieldSelection() =
		this as Selection.Field


	private fun Value.asString() =
		this as Value.String
}
