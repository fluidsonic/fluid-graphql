package io.fluidsonic.graphql


interface GSource {

	val content: String?
	val name: String


	interface Parsable : GSource {

		override val content: String
	}


	companion object {

		fun of(content: String, name: String = "<unknown>") =
			object : Parsable {

				override val content = content
				override val name = name
			}
	}
}
