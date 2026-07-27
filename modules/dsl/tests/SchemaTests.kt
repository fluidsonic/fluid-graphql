package testing

import io.fluidsonic.graphql.GraphQL
import io.fluidsonic.graphql.schema
import io.fluidsonic.graphql.type
import io.fluidsonic.graphql.value
import kotlin.test.Test
import kotlin.test.assertEquals

class SchemaTests {

	@Test
	fun test() {
		val actual = GraphQL.schema {
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
			${tripleQuote}An @directive on the schema with an argument$tripleQuote
			directive @myDirective(argument: String @deprecated) on ENUM_VALUE | QUERY | INLINE_FRAGMENT

			type Query {
				hero(episode: Episode): Character
				droid(id: ID!): Droid
			}

			${tripleQuote}Cool!$tripleQuote
			enum Episode {
				${tripleQuote}Cool!$tripleQuote
				NEW_HOPE @deprecated(reason: "no more hope")
				EMPIRE
				JEDI
			}

			enum LengthUnit {
				FEET
				METERS
			}

			input ReviewInput {
				stars: Int! = 2
				commentary: String
			}

			interface Character {
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
				friends: [Character] @deprecated
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
					${tripleQuote}nice$tripleQuote
					unit: LengthUnit = "METERS"
				): Float
			}

			${tripleQuote}ISO date$tripleQuote
			scalar Date

			${tripleQuote}When searching…$tripleQuote
			union SearchResult = Droid | Human | Starship
		""".trimIndent()

		assertEquals(expected = expected, actual = actual)
	}
}

/** Lets the expectation above contain GraphQL block strings, which a Kotlin raw string cannot spell directly. */
private const val tripleQuote = "\"\"\""
