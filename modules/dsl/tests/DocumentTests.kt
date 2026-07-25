package testing

import io.fluidsonic.graphql.GraphQL
import io.fluidsonic.graphql.arguments
import io.fluidsonic.graphql.directives
import io.fluidsonic.graphql.mutation
import io.fluidsonic.graphql.obj
import io.fluidsonic.graphql.query
import io.fluidsonic.graphql.type
import kotlin.test.Test
import kotlin.test.assertEquals

class DocumentTests {

	@Test
	fun testComplexDocument() {
		assertEquals(
			actual = GraphQL {
				val Type by type
				val fragment by fragment(Type) {
					__typename()
				}

				query {
					val variable by variable(!List(!Type))

					directives { "foo" { arguments { "a" to variable } } }
					fragment(fragment)
					"foo" {
						directives { "foo" { arguments { "a" to obj { "b" to variable } } } }

						__typename()
						"bar" {
							fragment(fragment) {
								directives { "foo" { arguments { "a" to 1 } } }
							}
							directives { "foo" { arguments { "a" to 1 } } }
						}
					}
				}

				mutation {
					"addUser" {
						arguments {
							"emailAddress" to "foo@bar.com"
							"password" to "secret"
						}

						"id"()
						"name"()
					}
				}
			}.toString(),
			expected = """
				fragment fragment on Type {
					__typename
				}


				query (${'$'}variable: [Type!]!) @foo(a: ${'$'}variable) {
					...fragment
					foo @foo(a: {
						b: ${'$'}variable
					}) {
						__typename
						bar @foo(a: 1) {
							...fragment @foo(a: 1)
						}
					}
				}


				mutation {
					addUser(emailAddress: "foo@bar.com", password: "secret") {
						id
						name
					}
				}

			""".trimIndent(),
		)
	}
}
