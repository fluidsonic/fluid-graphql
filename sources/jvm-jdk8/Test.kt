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

		Directive("myDirective") {
			on(OBJECT)
			on(ENUM_VALUE or QUERY or INLINE_FRAGMENT)
			description("An @directive on the schema with an argument")

			argument("argument" of String) {
				deprecated()
			}
		}

		Query {
			field("hero" of Character) {
				argument("episode" of Episode)
			}
			field("droid" of Droid) {
				argument("id" of !ID)
			}
		}

		Interface(Character) {
			directive("hello")

			field("id" of !ID)
			field("name" of String)
			field("friends" of List(Character))
			field("appearsIn" of !List(Episode))
		}

		Scalar(Date) {
			directive("hello")

			description("ISO date")
		}

		Object(Droid implements Character) {
			directive("hello")

			field("id" of !ID)
			field("name" of !String)
			field("friends" of List(Character)) {
				deprecated("no friends for droids")
			}
			field("appearsIn" of !List(Episode))
			field("primaryFunction" of String)
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

		Object(Human implements Character and StarshipOwner) {
			field("id" of !ID)
			field("name" of !String)
			field("friends" of List(Character))
			field("appearsIn" of !List(Episode))
			field("starships" of List(Starship))
			field("totalCredits" of Int)
		}

		Enum(LengthUnit) {
			value("FEET")
			value("METERS")
		}

		InputObject(ReviewInput) {
			directive("hello")

			argument("stars" of !Int default 2)
			argument("commentary" of String)
		}

		Union(SearchResult with Droid or Human or Starship) {
			description("When searching…")
			directive("hello")
		}

		Object(Starship) {
			field("id" of !ID)
			field("name" of !String)
			field("length" of Float) {
				argument("unit" of LengthUnit default "METERS") {
					description("nice")
					directive("hello")
				}
			}
		}

		Interface(StarshipOwner) {
			field("starships" of List(Starship))
		}
	}

//
//	val req = GDocument(
//		definitions = listOf(
//			GOperationDefinition(
//				type = GOperationType.query,
//				selectionSet = listOf(
//					GFieldSelection(
//						name = "hero",
//						selectionSet = listOf(
//							GFieldSelection(
//								name = "id"
//							)
//						)
//					)
//				)
//			)
//		)
//	)

	val req = GDocument(
		definitions = listOf(
			GOperationDefinition(
				type = GOperationType.query,
				selectionSet = listOf(
					GFieldSelection("__typename"),
					GFieldSelection(
						name = "__type",
						arguments = listOf(
							GArgument(name = "name", value = "Droid")
						),
						selectionSet = listOf(
							GFieldSelection(name = "name"),
							GFieldSelection(
								name = "fields",
								selectionSet = listOf(
									GFieldSelection("name"),
									GFieldSelection(name = "type", selectionSet = listOf(
										GFieldSelection("name"),
										GFieldSelection("kind"),
										GFieldSelection("ofType")
									))
								)
							)
						)
					)
				)
			)
		)
	)

	val con = GExecutor.default.createContext(
		schema = gql,
		document = req,
		rootValue = mapOf("id" to "Hey")
	).value!!

	val r = GExecutor.default.executeRequest(con)

	println(r)
}
