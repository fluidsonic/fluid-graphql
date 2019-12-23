package io.fluidsonic.graphql


interface GSource {

	val content: String?
	val name: String


	interface Parsable : GSource {

		override val content: String


		fun makeOrigin(startPosition: Int, endPosition: Int, column: Int, line: Int): GOrigin? =
			null
	}


	companion object {

		fun of(content: String, name: String = "<unknown>") =
			object : Parsable {

				override val content = content
				override val name = name
			}
	}
}
