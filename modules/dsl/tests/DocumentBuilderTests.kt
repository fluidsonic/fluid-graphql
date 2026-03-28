package testing

import io.fluidsonic.graphql.*
import kotlin.test.*


class DocumentBuilderTests {

	@Test
	fun queryWithVariables() {
		assertEquals(
			actual = GraphQL.document {
				query("GetUser") {
					val Type by type
					val id by variable(!Type)

					"user" {
						arguments { "id" to id }
						"id"()
						"name"()
					}
				}
			}.toString(),
			expected = """
				query GetUser(${'$'}id: Type!) {
					user(id: ${'$'}id) {
						id
						name
					}
				}

			""".trimIndent(),
		)
	}


	@Test
	fun mutationOperation() {
		assertEquals(
			actual = GraphQL.document {
				mutation("CreateUser") {
					"createUser" {
						arguments {
							"name" to "Alice"
							"email" to "alice@example.com"
						}

						"id"()
						"name"()
					}
				}
			}.toString(),
			expected = """
				mutation CreateUser {
					createUser(name: "Alice", email: "alice@example.com") {
						id
						name
					}
				}

			""".trimIndent(),
		)
	}


	@Test
	fun fragmentDefinitionAndSpread() {
		assertEquals(
			actual = GraphQL {
				val User by type
				val userFields by fragment(User) {
					"id"()
					"name"()
					"email"()
				}

				query {
					"user" {
						fragment(userFields)
					}
				}
			}.toString(),
			expected = """
				fragment userFields on User {
					id
					name
					email
				}


				{
					user {
						...userFields
					}
				}

			""".trimIndent(),
		)
	}


	@Test
	fun inlineFragment() {
		assertEquals(
			actual = GraphQL.document {
				query {
					"search" {
						on("User") {
							"name"()
							"email"()
						}
						on("Post") {
							"title"()
							"body"()
						}
					}
				}
			}.toString(),
			expected = """
				{
					search {
						... on User {
							name
							email
						}
						... on Post {
							title
							body
						}
					}
				}

			""".trimIndent(),
		)
	}


	@Test
	fun nestedFieldSelections() {
		assertEquals(
			actual = GraphQL.document {
				query {
					"user" {
						"id"()
						"profile" {
							"avatar" {
								"url"()
								"width"()
								"height"()
							}
							"bio"()
						}
					}
				}
			}.toString(),
			expected = """
				{
					user {
						id
						profile {
							avatar {
								url
								width
								height
							}
							bio
						}
					}
				}

			""".trimIndent(),
		)
	}


	@Test
	fun directiveOnField() {
		assertEquals(
			actual = GraphQL.document {
				query {
					val includeEmail by variable("Boolean")

					"user" {
						"id"()
						"name"()
						"email" {
							directives { "include" { arguments { "if" to includeEmail } } }
						}
					}
				}
			}.toString(),
			expected = """
				query (${'$'}includeEmail: Boolean) {
					user {
						id
						name
						email @include(if: ${'$'}includeEmail)
					}
				}

			""".trimIndent(),
		)
	}


	@Test
	fun fieldAlias() {
		assertEquals(
			actual = GraphQL.document {
				query {
					"user"(alias = "currentUser") {
						"id"()
						"name"()
					}
					"user"(alias = "otherUser") {
						arguments { "id" to "123" }
						"id"()
						"name"()
					}
				}
			}.toString(),
			expected = """
				{
					currentUser: user {
						id
						name
					}
					otherUser: user(id: "123") {
						id
						name
					}
				}

			""".trimIndent(),
		)
	}


	@Test
	fun fragmentSpreadWithDirective() {
		assertEquals(
			actual = GraphQL {
				val User by type
				val userFields by fragment(User) {
					"id"()
					"name"()
				}

				query {
					"user" {
						fragment(userFields) {
							directives { "include" { arguments { "if" to true } } }
						}
					}
				}
			}.toString(),
			expected = """
				fragment userFields on User {
					id
					name
				}


				{
					user {
						...userFields @include(if: true)
					}
				}

			""".trimIndent(),
		)
	}


	@Test
	fun inlineFragmentWithDirective() {
		assertEquals(
			actual = GraphQL.document {
				query {
					"search" {
						on("User") {
							directives { "skip" { arguments { "if" to false } } }

							"name"()
						}
					}
				}
			}.toString(),
			expected = """
				{
					search {
						... on User @skip(if: false) {
							name
						}
					}
				}

			""".trimIndent(),
		)
	}


	@Test
	fun subscriptionOperation() {
		assertEquals(
			actual = GraphQL.document {
				subscription("OnMessage") {
					"messageAdded" {
						"id"()
						"text"()
						"sender" {
							"name"()
						}
					}
				}
			}.toString(),
			expected = """
				subscription OnMessage {
					messageAdded {
						id
						text
						sender {
							name
						}
					}
				}

			""".trimIndent(),
		)
	}


	@Test
	fun multipleOperationsInDocument() {
		assertEquals(
			actual = GraphQL.document {
				query("GetUser") {
					"user" {
						"id"()
						"name"()
					}
				}
				mutation("UpdateUser") {
					"updateUser" {
						arguments { "name" to "Bob" }
						"id"()
					}
				}
			}.toString(),
			expected = """
				query GetUser {
					user {
						id
						name
					}
				}


				mutation UpdateUser {
					updateUser(name: "Bob") {
						id
					}
				}

			""".trimIndent(),
		)
	}


	@Test
	fun variableWithDefault() {
		assertEquals(
			actual = GraphQL.document {
				query("GetUsers") {
					val limit = variable("limit", "Int") {
						default(10)
					}

					"users" {
						arguments { "limit" to limit }
						"id"()
					}
				}
			}.toString(),
			expected = """
				query GetUsers(${'$'}limit: Int = 10) {
					users(limit: ${'$'}limit) {
						id
					}
				}

			""".trimIndent(),
		)
	}


	@Test
	fun objectArgumentValues() {
		assertEquals(
			actual = GraphQL.document {
				mutation {
					"createUser" {
						arguments {
							"input" to obj {
								"name" to "Alice"
								"age" to 30
								"active" to true
							}
						}
						"id"()
					}
				}
			}.toString(),
			expected = """
				mutation {
					createUser(input: {
						name: "Alice",
						age: 30,
						active: true
					}) {
						id
					}
				}

			""".trimIndent(),
		)
	}


	@Test
	fun listArgumentValues() {
		assertEquals(
			actual = GraphQL.document {
				query {
					"users" {
						arguments {
							"ids" to list {
								add("1")
								add("2")
								add("3")
							}
						}
						"id"()
					}
				}
			}.toString(),
			expected = """
				{
					users(ids: ["1", "2", "3"]) {
						id
					}
				}

			""".trimIndent(),
		)
	}
}
