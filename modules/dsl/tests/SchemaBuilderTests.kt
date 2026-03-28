package testing

import io.fluidsonic.graphql.*
import kotlin.test.*


class SchemaBuilderTests {

	@Test
	fun scalarType() {
		val schema = GraphQL.schema {
			val Date by type

			Query {
				field("today" of Date)
			}

			Scalar(Date)
		}

		assertEquals(
			actual = schema.toString(),
			expected = """
				type Query {
					today: Date
				}

				scalar Date
			""".trimIndent(),
		)
	}


	@Test
	fun scalarType_withDescription() {
		val schema = GraphQL.schema {
			val Date by type

			Query {
				field("today" of Date)
			}

			Scalar(Date) {
				description("An ISO 8601 date string")
			}
		}

		assertEquals(
			actual = schema.toString(),
			expected = """
				type Query {
					today: Date
				}

				"An ISO 8601 date string"
				scalar Date
			""".trimIndent(),
		)
	}


	@Test
	fun scalarType_withDirective() {
		val schema = GraphQL.schema {
			val Date by type

			Query {
				field("today" of Date)
			}

			Scalar(Date) {
				directive("specifiedBy") {
					argument("url" with value("https://example.com"))
				}
			}
		}

		assertEquals(
			actual = schema.toString(),
			expected = """
				type Query {
					today: Date
				}

				scalar Date @specifiedBy(url: "https://example.com")
			""".trimIndent(),
		)
	}


	@Test
	fun enumType() {
		val schema = GraphQL.schema {
			val Color by type

			Query {
				field("favoriteColor" of Color)
			}

			Enum(Color) {
				value("RED")
				value("GREEN")
				value("BLUE")
			}
		}

		assertEquals(
			actual = schema.toString(),
			expected = """
				type Query {
					favoriteColor: Color
				}

				enum Color {
					RED
					GREEN
					BLUE
				}
			""".trimIndent(),
		)
	}


	@Test
	fun enumType_withDescriptionsAndDeprecation() {
		val schema = GraphQL.schema {
			val Status by type

			Query {
				field("status" of Status)
			}

			Enum(Status) {
				description("The status of an item")

				value("ACTIVE") {
					description("Item is active")
				}
				value("INACTIVE") {
					deprecated("Use ARCHIVED instead")
				}
				value("ARCHIVED")
			}
		}

		assertEquals(
			actual = schema.toString(),
			expected = """
				type Query {
					status: Status
				}

				"The status of an item"
				enum Status {
					"Item is active"
					ACTIVE

					INACTIVE @deprecated(reason: "Use ARCHIVED instead")

					ARCHIVED
				}
			""".trimIndent(),
		)
	}


	@Test
	fun inputObjectType() {
		val schema = GraphQL.schema {
			val CreateUserInput by type

			Query {
				field("dummy" of String)
			}

			InputObject(CreateUserInput) {
				argument("name" of !String)
				argument("email" of !String)
				argument("age" of Int)
			}
		}

		assertEquals(
			actual = schema.toString(),
			expected = """
				type Query {
					dummy: String
				}

				input CreateUserInput {
					name: String!
					email: String!
					age: Int
				}
			""".trimIndent(),
		)
	}


	@Test
	fun inputObjectType_withDefaultValues() {
		val schema = GraphQL.schema {
			val FilterInput by type

			Query {
				field("dummy" of String)
			}

			InputObject(FilterInput) {
				argument("limit" of Int default value(10))
				argument("offset" of Int default value(0))
				argument("active" of Boolean default value(true))
			}
		}

		assertEquals(
			actual = schema.toString(),
			expected = """
				type Query {
					dummy: String
				}

				input FilterInput {
					limit: Int = 10
					offset: Int = 0
					active: Boolean = true
				}
			""".trimIndent(),
		)
	}


	@Test
	fun objectType() {
		val schema = GraphQL.schema {
			val User by type

			Query {
				field("user" of User)
			}

			Object(User) {
				field("id" of !ID)
				field("name" of !String)
				field("email" of String)
			}
		}

		assertEquals(
			actual = schema.toString(),
			expected = """
				type Query {
					user: User
				}

				type User {
					id: ID!
					name: String!
					email: String
				}
			""".trimIndent(),
		)
	}


	@Test
	fun objectType_withFieldArguments() {
		val schema = GraphQL.schema {
			val User by type
			val Post by type

			Query {
				field("user" of User)
			}

			Object(User) {
				field("id" of !ID)
				field("posts" of List(Post)) {
					argument("first" of Int default value(10))
					argument("after" of String)
				}
			}

			Object(Post) {
				field("id" of !ID)
				field("title" of !String)
			}
		}

		assertEquals(
			actual = schema.toString(),
			expected = """
				type Query {
					user: User
				}

				type User {
					id: ID!
					posts(first: Int = 10, after: String): [Post]
				}

				type Post {
					id: ID!
					title: String!
				}
			""".trimIndent(),
		)
	}


	@Test
	fun objectType_implementsInterface() {
		val schema = GraphQL.schema {
			val Node by type
			val User by type

			Query {
				field("node" of Node)
			}

			Interface(Node) {
				field("id" of !ID)
			}

			Object(User implements Node) {
				field("id" of !ID)
				field("name" of !String)
			}
		}

		assertEquals(
			actual = schema.toString(),
			expected = """
				type Query {
					node: Node
				}

				interface Node {
					id: ID!
				}

				type User implements Node {
					id: ID!
					name: String!
				}
			""".trimIndent(),
		)
	}


	@Test
	fun objectType_implementsMultipleInterfaces() {
		val schema = GraphQL.schema {
			val Node by type
			val Timestamped by type
			val User by type

			Query {
				field("user" of User)
			}

			Interface(Node) {
				field("id" of !ID)
			}

			Interface(Timestamped) {
				field("createdAt" of !String)
			}

			Object(User implements Node and Timestamped) {
				field("id" of !ID)
				field("createdAt" of !String)
				field("name" of !String)
			}
		}

		assertEquals(
			actual = schema.toString(),
			expected = """
				type Query {
					user: User
				}

				interface Node {
					id: ID!
				}

				interface Timestamped {
					createdAt: String!
				}

				type User implements Node & Timestamped {
					id: ID!
					createdAt: String!
					name: String!
				}
			""".trimIndent(),
		)
	}


	@Test
	fun interfaceType() {
		val schema = GraphQL.schema {
			val Vehicle by type

			Query {
				field("vehicle" of Vehicle)
			}

			Interface(Vehicle) {
				field("make" of !String)
				field("model" of !String)
				field("year" of !Int)
			}
		}

		assertEquals(
			actual = schema.toString(),
			expected = """
				type Query {
					vehicle: Vehicle
				}

				interface Vehicle {
					make: String!
					model: String!
					year: Int!
				}
			""".trimIndent(),
		)
	}


	@Test
	fun unionType() {
		val schema = GraphQL.schema {
			val SearchResult by type
			val User by type
			val Post by type

			Query {
				field("search" of List(SearchResult))
			}

			Object(User) {
				field("id" of !ID)
				field("name" of !String)
			}

			Object(Post) {
				field("id" of !ID)
				field("title" of !String)
			}

			Union(SearchResult with User or Post)
		}

		assertEquals(
			actual = schema.toString(),
			expected = """
				type Query {
					search: [SearchResult]
				}

				type User {
					id: ID!
					name: String!
				}

				type Post {
					id: ID!
					title: String!
				}

				union SearchResult = User | Post
			""".trimIndent(),
		)
	}


	@Test
	fun unionType_withDescriptionAndDirective() {
		val schema = GraphQL.schema {
			val SearchResult by type
			val User by type
			val Post by type

			Query {
				field("search" of List(SearchResult))
			}

			Object(User) {
				field("id" of !ID)
			}

			Object(Post) {
				field("id" of !ID)
			}

			Union(SearchResult with User or Post) {
				description("A search result")
				directive("custom")
			}
		}

		assertEquals(
			actual = schema.toString(),
			expected = """
				type Query {
					search: [SearchResult]
				}

				type User {
					id: ID!
				}

				type Post {
					id: ID!
				}

				"A search result"
				union SearchResult @custom = User | Post
			""".trimIndent(),
		)
	}


	@Test
	fun directiveDefinition() {
		val schema = GraphQL.schema {
			Query {
				field("dummy" of String)
			}

			Directive("auth") {
				description("Requires authentication")
				on(FIELD_DEFINITION or OBJECT)
			}
		}

		assertEquals(
			actual = schema.toString(),
			expected = """
				type Query {
					dummy: String
				}

				"Requires authentication"
				directive @auth on FIELD_DEFINITION | OBJECT
			""".trimIndent(),
		)
	}


	@Test
	fun directiveDefinition_withArguments() {
		val schema = GraphQL.schema {
			Query {
				field("dummy" of String)
			}

			Directive("cacheControl") {
				on(FIELD_DEFINITION or OBJECT)

				argument("maxAge" of Int)
				argument("scope" of String default value("PUBLIC"))
			}
		}

		assertEquals(
			actual = schema.toString(),
			expected = """
				type Query {
					dummy: String
				}

				directive @cacheControl(maxAge: Int, scope: String = "PUBLIC") on FIELD_DEFINITION | OBJECT
			""".trimIndent(),
		)
	}


	@Test
	fun nonNullAndListTypeWrappers() {
		val schema = GraphQL.schema {
			val Item by type

			Query {
				field("requiredString" of !String)
				field("optionalList" of List(Item))
				field("requiredListOfRequired" of !List(!Item))
			}

			Object(Item) {
				field("id" of !ID)
			}
		}

		assertEquals(
			actual = schema.toString(),
			expected = """
				type Query {
					requiredString: String!
					optionalList: [Item]
					requiredListOfRequired: [Item!]!
				}

				type Item {
					id: ID!
				}
			""".trimIndent(),
		)
	}


	@Test
	fun fieldDescriptions() {
		val schema = GraphQL.schema {
			val User by type

			Query {
				field("user" of User) {
					description("Fetches a user by ID")
					argument("id" of !ID) {
						description("The user's unique identifier")
					}
				}
			}

			Object(User) {
				field("id" of !ID) {
					description("Unique identifier")
				}
				field("name" of !String) {
					description("Display name")
				}
			}
		}

		assertEquals(
			actual = schema.toString(),
			expected = """
				type Query {
					"Fetches a user by ID"
					user(
						"The user's unique identifier"
						id: ID!
					): User
				}

				type User {
					"Unique identifier"
					id: ID!

					"Display name"
					name: String!
				}
			""".trimIndent(),
		)
	}


	@Test
	fun multipleTypesInOneSchema() {
		val schema = GraphQL.schema {
			val User by type
			val Post by type
			val Comment by type
			val Role by type

			Query {
				field("user" of User)
				field("posts" of List(Post))
			}

			Mutation {
				field("createPost" of Post) {
					argument("title" of !String)
				}
			}

			Enum(Role) {
				value("ADMIN")
				value("USER")
			}

			Object(User) {
				field("id" of !ID)
				field("name" of !String)
				field("role" of !Role)
				field("posts" of List(Post))
			}

			Object(Post) {
				field("id" of !ID)
				field("title" of !String)
				field("comments" of List(Comment))
			}

			Object(Comment) {
				field("id" of !ID)
				field("text" of !String)
				field("author" of !User)
			}
		}

		assertEquals(
			actual = schema.toString(),
			expected = """
				type Query {
					user: User
					posts: [Post]
				}

				type Mutation {
					createPost(title: String!): Post
				}

				enum Role {
					ADMIN
					USER
				}

				type User {
					id: ID!
					name: String!
					role: Role!
					posts: [Post]
				}

				type Post {
					id: ID!
					title: String!
					comments: [Comment]
				}

				type Comment {
					id: ID!
					text: String!
					author: User!
				}
			""".trimIndent(),
		)
	}


	@Test
	fun deprecatedFields() {
		val schema = GraphQL.schema {
			val User by type

			Query {
				field("user" of User)
			}

			Object(User) {
				field("id" of !ID)
				field("name" of !String)
				field("email" of String) {
					deprecated("Use contactEmail instead")
				}
				field("contactEmail" of String)
			}
		}

		assertEquals(
			actual = schema.toString(),
			expected = """
				type Query {
					user: User
				}

				type User {
					id: ID!
					name: String!
					email: String @deprecated(reason: "Use contactEmail instead")
					contactEmail: String
				}
			""".trimIndent(),
		)
	}


	@Test
	fun subscriptionRootType() {
		val schema = GraphQL.schema {
			val Message by type

			Query {
				field("dummy" of String)
			}

			Subscription {
				field("messageAdded" of !Message)
			}

			Object(Message) {
				field("id" of !ID)
				field("text" of !String)
			}
		}

		assertEquals(
			actual = schema.toString(),
			expected = """
				type Query {
					dummy: String
				}

				type Subscription {
					messageAdded: Message!
				}

				type Message {
					id: ID!
					text: String!
				}
			""".trimIndent(),
		)
	}


	@Test
	fun objectTypeWithDirective() {
		// Note: The printer currently does not output directives on object types,
		// so we verify via the schema model directly.
		val schema = GraphQL.schema {
			val User by type

			Query {
				field("user" of User)
			}

			Object(User) {
				directive("cacheControl") {
					argument("maxAge" with value(300))
				}

				field("id" of !ID)
				field("name" of !String)
			}
		}

		val userType = schema.resolveType("User") as GObjectType
		assertEquals(actual = userType.directives.size, expected = 1)
		assertEquals(actual = userType.directives[0].name, expected = "cacheControl")
		assertEquals(actual = userType.directives[0].arguments[0].name, expected = "maxAge")
		assertEquals(actual = (userType.directives[0].arguments[0].value as GIntValue).value, expected = 300)
	}


	@Test
	fun builtinScalarTypes() {
		val schema = GraphQL.schema {
			Query {
				field("aString" of String)
				field("anInt" of Int)
				field("aFloat" of Float)
				field("aBoolean" of Boolean)
				field("anId" of ID)
			}
		}

		assertEquals(
			actual = schema.toString(),
			expected = """
				type Query {
					aString: String
					anInt: Int
					aFloat: Float
					aBoolean: Boolean
					anId: ID
				}
			""".trimIndent(),
		)
	}


	@Test
	fun inputObjectWithDirective() {
		val schema = GraphQL.schema {
			val CreateInput by type

			Query {
				field("dummy" of String)
			}

			InputObject(CreateInput) {
				directive("validated")

				argument("name" of !String)
			}
		}

		assertEquals(
			actual = schema.toString(),
			expected = """
				type Query {
					dummy: String
				}

				input CreateInput @validated {
					name: String!
				}
			""".trimIndent(),
		)
	}


	@Test
	fun interfaceWithDirectiveAndDescription() {
		val schema = GraphQL.schema {
			val Entity by type

			Query {
				field("entity" of Entity)
			}

			Interface(Entity) {
				description("A database entity")
				directive("key")

				field("id" of !ID)
			}
		}

		assertEquals(
			actual = schema.toString(),
			expected = """
				type Query {
					entity: Entity
				}

				"A database entity"
				interface Entity @key {
					id: ID!
				}
			""".trimIndent(),
		)
	}


	@Test
	fun nestedListTypes() {
		val schema = GraphQL.schema {
			Query {
				field("matrix" of List(List(Int)))
				field("requiredMatrix" of !List(!List(!Int)))
			}
		}

		assertEquals(
			actual = schema.toString(),
			expected = """
				type Query {
					matrix: [[Int]]
					requiredMatrix: [[Int!]!]!
				}
			""".trimIndent(),
		)
	}
}
