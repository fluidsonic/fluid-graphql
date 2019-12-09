import io.fluidsonic.graphql.*


fun main() {

	val gql = schema {

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

		Directive("schemaDirective") {
			on(OBJECT)
			on(ENUM_VALUE or QUERY or INLINE_FRAGMENT)
			description("An @directive on the schema with an argument")

			"argument" of String {
				deprecated()
			}
		}

		Query {
			"hero" of Character {
				"episode" of Episode
			}
			"droid" of Droid {
				"id" of !ID
			}
		}

		Interface(Character) {
			directive("hello")

			"id" of !ID
			"name" of String
			"friends" of List(Character)
			"appearsIn" of !List(Episode)
			"test" of List(Episode) {
				directive("hello") {
					"a" with false
					"b" with enumValue("HUH")
					"c" with 2.2
					"d" with 2
					"e" with listOf(3)
					"f" with null
					"g" with "abc"
					"i" with mapOf("a" to "b")
				}

				description("cool")
			}
		}

		Scalar(Date) {
			directive("hello")

			description("ISO date")
		}

		Object(Droid implements Character) {
			directive("hello")

			"id" of !ID
			"name" of !String
			"friends" of List(Character) {
				deprecated("no friends for droids")
			}
			"appearsIn" of !List(Episode)
			"primaryFunction" of String
		}

		Enum(Episode) {
			description("Cool!")
			directive("hello")

			-"NEW_HOPE" {
				description("Cool!")
				deprecated("no more hope")
				directive("hello")
			}
			-"EMPIRE"
			-"JEDI"
		}

		Object(Human implements Character and StarshipOwner) {
			"id" of !ID
			"name" of !String
			"friends" of List(Character)
			"appearsIn" of !List(Episode)
			"starships" of List(Starship)
			"totalCredits" of Int
		}

		Enum(LengthUnit) {
			-"FEET"
			-"METERS"
		}

		InputObject(ReviewInput) {
			directive("hello")

			"stars" of !Int default 2
			"commentary" of String
		}

		Union(SearchResult with Droid or Human or Starship) {
			description("When searching…")
			directive("hello")
		}

		Object(Starship) {
			"id" of !ID
			"name" of !String
			"length" of Float {
				"unit" of LengthUnit default enumValue("METERS") {
					description("nice")
					directive("hello")
				}
			}
		}

		Interface(StarshipOwner) {
			"starships" of List(Starship)
		}
	}

	println(gql)
}
