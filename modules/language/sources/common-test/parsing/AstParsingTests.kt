package testing

import io.fluidsonic.graphql.*
import kotlin.test.*


// https://github.com/graphql/graphql-js/blob/master/src/language/__tests__/parser-test.js
class AstParsingTests {

	@Test
	fun testAcceptsVariableInlineValues() {
		parseDocument("{ field(complex: { a: { b: [ \$var ] } }) }")
	}


	@Test
	fun testAcceptsVariableDefinitionDirectives() {
		parseDocument("query Foo(\$x: Boolean = false @bar) { field }")
	}


	@Test
	fun testAcceptsMultiByteCharacters() {
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
	fun testAcceptsKitchenSink() {
		parseDocument(kitchenSinkQuery)
	}


	@Test
	fun testAcceptsNonKeywordsAnywhereANameIsAllowed() {
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
	fun testParsesAnonymousMutationOperations() {
		parseDocument("mutation { mutationField }")
	}


	@Test
	fun testParsesAnonymousQueryOperations() {
		parseDocument("query { queryField }")
	}


	@Test
	fun parsesAnonymousSubscriptionOperations() {
		parseDocument("subscription { subscriptionField }")
	}


	@Test
	fun testParsesNamedMutationOperations() {
		parseDocument("mutation Foo { mutationField }")
	}


	@Test
	fun testParsesNamedQueryOperations() {
		parseDocument("query Foo { queryField }")
	}


	@Test
	fun testParsesNamedSubscriptionOperations() {
		parseDocument("subscription Foo { subscriptionField }")
	}


	@Test
	fun testCreatesAst() {
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

			definitions.assertOneOf<GOperationDefinition> {
				assertAt(0 .. 40)
				assertEquals(expected = GOperationType.query, actual = type)
				assertNull(name)
				assertEquals(expected = emptyList(), actual = variableDefinitions)
				assertEquals(expected = emptyList(), actual = directives)

				selectionSet.assert {
					assertAt(0 .. 40)

					selections.assertOneOf<GFieldSelection> {
						assertAt(4 .. 38)
						assertNull(aliasNode)
						assertEquals(expected = emptyList(), actual = directives)

						nameNode.assert {
							assertAt(4 .. 8)
							assertEquals(expected = "node", actual = value)
						}

						arguments.assertOne {
							assertAt(9 .. 14)
							assertEquals(expected = emptyList(), actual = directives)

							nameNode.assert {
								assertAt(9 .. 11)
								assertEquals(expected = "id", actual = value)
							}
						}

						selectionSet.assert {
							assertAt(16 .. 38)

							selections.assertMany(2) {

								get(0).assertOf<GFieldSelection> {
									assertAt(22 .. 24)
									assertNull(aliasNode)
									assertEquals(expected = emptyList(), actual = arguments)
									assertEquals(expected = emptyList(), actual = directives)
									assertNull(selectionSet)

									nameNode.assert {
										assertAt(22 .. 24)
										assertEquals(expected = "id", actual = value)
									}
								}

								get(1).assertOf<GFieldSelection> {
									assertAt(30 .. 34)
									assertNull(aliasNode)
									assertEquals(expected = emptyList(), actual = arguments)
									assertEquals(expected = emptyList(), actual = directives)
									assertNull(selectionSet)

									nameNode.assert {
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
	fun testCreatesAstFromNamelessQueryWithoutVariables() {
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

			definitions.assertOneOf<GOperationDefinition> {
				assertAt(0 .. 29)
				assertEquals(expected = GOperationType.query, actual = type)
				assertNull(name)
				assertEquals(expected = emptyList(), actual = variableDefinitions)
				assertEquals(expected = emptyList(), actual = directives)

				selectionSet.assert {
					assertAt(6 .. 29)

					selections.assertOneOf<GFieldSelection> {
						assertAt(10 .. 27)
						assertNull(aliasNode)
						assertEquals(expected = emptyList(), actual = arguments)
						assertEquals(expected = emptyList(), actual = directives)

						nameNode.assert {
							assertAt(10 .. 14)
							assertEquals(expected = "node", actual = value)
						}

						selectionSet.assert {
							assertAt(15 .. 27)

							selections.assertOneOf<GFieldSelection> {
								assertAt(21 .. 23)
								assertNull(aliasNode)
								assertEquals(expected = emptyList(), actual = arguments)
								assertEquals(expected = emptyList(), actual = directives)
								assertNull(selectionSet)

								nameNode.assert {
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
//	fun testAcceptsFragmentDefinedVariables() {
//		parseDocument("fragment a(\$v: Boolean = false) on t { f(v: \$v) }")
//	}


	@Test
	fun testContainsReferencesToSource() {
		val source = object : GDocumentSource.Parsable {
			override val content = "{ id }"
			override val name = "custom"
		}
		val document = GDocument.parse(source).valueWithoutErrorsOrThrow()
		assertSame(expected = source, actual = document.origin?.source)
	}


	@Test
	fun testParsesNullValue() {
		parseValue("null").assertOf<GNullValue> {
			assertAt(0 .. 4)
		}
	}


	@Test
	fun testParsesListValues() {
		parseValue("""[123 "abc"]""").assertOf<GListValue> {
			assertAt(0 .. 11)

			elements.assertMany(2) {

				get(0).assertOf<GIntValue> {
					assertAt(1 .. 4)
					assertEquals(expected = 123, actual = value)
				}

				get(1).assertOf<GStringValue> {
					assertAt(5 .. 10)
					assertEquals(expected = "abc", actual = value)
					assertFalse(isBlock)
				}
			}
		}
	}


	@Test
	fun testParsesBlockString() {
		parseValue("[\"\"\"long\"\"\" \"short\"]").assertOf<GListValue> {
			assertAt(0 .. 20)

			elements.assertMany(2) {

				get(0).assertOf<GStringValue> {
					assertAt(1 .. 11)
					assertEquals(expected = "long", actual = value)
					assertTrue(isBlock)
				}

				get(1).assertOf<GStringValue> {
					assertAt(12 .. 19)
					assertEquals(expected = "short", actual = value)
					assertFalse(isBlock)
				}
			}
		}
	}


	@Test
	fun testParsesWellKnownTypes() {
		parseTypeReference("String").assertOf<GNamedTypeRef> {
			assertAt(0 .. 6)

			nameNode.assert {
				assertAt(0 .. 6)
				assertEquals(expected = "String", actual = value)
			}
		}
	}


	@Test
	fun testParsesCustomTypes() {
		parseTypeReference("MyType").assertOf<GNamedTypeRef> {
			assertAt(0 .. 6)

			nameNode.assert {
				assertAt(0 .. 6)
				assertEquals(expected = "MyType", actual = value)
			}
		}
	}


	@Test
	fun testParsesListTypes() {
		parseTypeReference("[MyType]").assertOf<GListTypeRef> {
			assertAt(0 .. 8)

			elementType.assertOf<GNamedTypeRef> {
				assertAt(1 .. 7)

				nameNode.assert {
					assertAt(1 .. 7)
					assertEquals(expected = "MyType", actual = value)
				}
			}
		}
	}


	@Test
	fun testParsesNonNullTypes() {
		parseTypeReference("MyType!").assertOf<GNonNullTypeRef> {
			assertAt(0 .. 7)

			nullableRef.assertOf<GNamedTypeRef> {
				assertAt(0 .. 6)

				nameNode.assert {
					assertAt(0 .. 6)
					assertEquals(expected = "MyType", actual = value)
				}
			}
		}
	}


	@Test
	fun testParsesNestedTypes() {
		parseTypeReference("[MyType!]").assertOf<GListTypeRef> {
			assertAt(0 .. 9)

			elementType.assertOf<GNonNullTypeRef> {
				assertAt(1 .. 8)

				nullableRef.assertOf<GNamedTypeRef> {
					assertAt(1 .. 7)

					nameNode.assert {
						assertAt(1 .. 7)
						assertEquals(expected = "MyType", actual = value)
					}
				}
			}
		}
	}


	@Test
	fun testProducesCorrectLineNumber() {
		val ast = assertAst("""
		|
		|# foo
		|scalar Test
		""")
		assertEquals(expected = 3, actual = (ast.definitions.first() as GScalarType).nameNode.origin?.line)
	}


	@Test
	fun testProducesCorrectLineNumberAfterBlockString() {
		val ast = assertAst("""
		|
		|# foo
		|""${'"'}
		|description
		|line
		|line
		|""${'"'}
		|scalar Test
		""")
		assertEquals(expected = 8, actual = (ast.definitions.first() as GScalarType).nameNode.origin?.line)
	}


	private fun parseDocument(content: String, name: String = "<test>"): GDocument =
		GDocument.parse(content.trimMargin(), name = name).valueWithoutErrorsOrThrow()


	private fun parseTypeReference(content: String, name: String = "<test>"): GTypeRef =
		GTypeRef.parse(content.trimMargin(), name = name).valueWithoutErrorsOrThrow()


	private fun parseValue(content: String, name: String = "<test>"): GValue =
		GValue.parse(content.trimMargin(), name = name).valueWithoutErrorsOrThrow()
}
