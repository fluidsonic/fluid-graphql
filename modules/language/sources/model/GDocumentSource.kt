package io.fluidsonic.graphql


public interface GDocumentSource {

	public val content: String?
	public val name: String


	public interface Parsable : GDocumentSource {

		override val content: String


		public fun makeOrigin(startPosition: Int, endPosition: Int, column: Int, line: Int): GDocumentPosition? =
			null
	}


	public companion object {

		public fun of(content: String, name: String = "<unknown>"): Parsable =
			object : Parsable {

				override val content = content
				override val name = name
			}
	}
}
