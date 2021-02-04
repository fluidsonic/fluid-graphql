package testing

import io.fluidsonic.graphql.*
import kotlin.test.*


class DslTests {

	@Test
	fun testCompleteDsl() {
		val actual = graphql.schema {
			val Character by type
			val Date by type
			val Droid by type
			val Episode by type
			val Human by type
			val LengthUnit by type
			val ReviewInput by type
			val SearchResult by type
			val Starship by type
			val StarshipOwner by type

			Query {
				field("hero" of Character) {
					argument("episode" of Episode)
				}
				field("droid" of Droid) {
					argument("id" of !ID)
				}
			}

			Enum(Episode) {
				description("Cool!")
				directive("hello")

				value("NEW_HOPE") {
					description("Cool!")
					deprecated("no more hope")
					directive("hello")
				}
				value("EMPIRE")
				value("JEDI")
			}

			Enum(LengthUnit) {
				value("FEET")
				value("METERS")
			}

			InputObject(ReviewInput) {
				directive("hello")

				argument("stars" of !Int default value(2))
				argument("commentary" of String)
			}

			Interface(Character) {
				directive("hello")

				field("id" of !ID)
				field("name" of String)
				field("friends" of List(Character))
				field("appearsIn" of !List(Episode))
			}

			Interface(StarshipOwner) {
				field("starships" of List(Starship))
			}

			Object(Droid implements Character) {
				directive("hello")

				field("id" of !ID)
				field("name" of !String)
				field("friends" of List(Character)) {
					deprecated(reason = null)
				}
				field("appearsIn" of !List(Episode))
				field("primaryFunction" of String)
			}

			Object(Human implements Character and StarshipOwner) {
				field("id" of !ID)
				field("name" of !String)
				field("friends" of List(Character))
				field("appearsIn" of !List(Episode))
				field("starships" of List(Starship))
				field("totalCredits" of Int)
			}

			Object(Starship) {
				field("id" of !ID)
				field("name" of !String)
				field("length" of Float) {
					argument("unit" of LengthUnit default value("METERS")) {
						description("nice")
						directive("hello")
					}
				}
			}

			Scalar(Date) {
				directive("hello")

				description("ISO date")
			}

			Union(SearchResult with Droid or Human or Starship) {
				description("When searching…")
				directive("hello")
			}

			Directive("myDirective") {
				on(OBJECT)
				on(ENUM_VALUE or QUERY or INLINE_FRAGMENT)
				description("An @directive on the schema with an argument")

				argument("argument" of String) {
					deprecated()
				}
			}
		}.toString()

		val expected = """
			type Query {
				hero(episode: Episode): Character
				droid(id: ID!): Droid
			}

			"Cool!"
			enum Episode @hello {
				"Cool!"
				NEW_HOPE @deprecated(reason: "no more hope") @hello

				EMPIRE

				JEDI
			}

			enum LengthUnit {
				FEET
				METERS
			}

			input ReviewInput @hello {
				stars: Int! = 2
				commentary: String
			}

			interface Character @hello {
				id: ID!
				name: String
				friends: [Character]
				appearsIn: [Episode]!
			}

			interface StarshipOwner {
				starships: [Starship]
			}

			type Droid implements Character {
				id: ID!
				name: String!
				friends: [Character] @deprecated(reason: null)
				appearsIn: [Episode]!
				primaryFunction: String
			}

			type Human implements Character & StarshipOwner {
				id: ID!
				name: String!
				friends: [Character]
				appearsIn: [Episode]!
				starships: [Starship]
				totalCredits: Int
			}

			type Starship {
				id: ID!
				name: String!
				length(
					"nice"
					unit: LengthUnit = "METERS" @hello
				): Float
			}

			"ISO date"
			scalar Date @hello

			"When searching…"
			union SearchResult @hello = Droid | Human | Starship

			"An @directive on the schema with an argument"
			directive @myDirective(argument: String @deprecated(reason: "No longer supported")) on ENUM_VALUE | QUERY | INLINE_FRAGMENT
		""".trimIndent()

		assertEquals(expected = expected, actual = actual)
	}
}
