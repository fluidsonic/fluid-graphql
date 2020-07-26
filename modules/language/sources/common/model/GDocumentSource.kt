package io.fluidsonic.graphql


interface GDocumentSource {

	val content: String?
	val name: String


	interface Parsable : GDocumentSource {

		override val content: String


		fun makeOrigin(startPosition: Int, endPosition: Int, column: Int, line: Int): GDocumentPosition? =
			null
	}


	companion object {

		fun of(content: String, name: String = "<unknown>"): Parsable =
			object : Parsable {

				override val content = content
				override val name = name
			}
	}
}
