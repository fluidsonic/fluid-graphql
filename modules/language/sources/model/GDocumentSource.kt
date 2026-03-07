package io.fluidsonic.graphql


/**
 * Represents the source of a GraphQL document (SDL or query string).
 *
 * The base interface is sufficient for display purposes; implement [Parsable] to allow the
 * parser to produce [GDocumentPosition] values with source context.
 *
 * Use the [GDocumentSource.of] factory to create a simple in-memory source.
 */
public interface GDocumentSource {

	/** The raw text content, or `null` if the content is not available. */
	public val content: String?

	/** A human-readable name for this source, such as a file path or `"<document>"`. */
	public val name: String


	/**
	 * A [GDocumentSource] that provides the full content string to the parser.
	 *
	 * Implement [makeOrigin] to produce rich [GDocumentPosition] values that include
	 * line, column, and source context in error messages.
	 */
	public interface Parsable : GDocumentSource {

		override val content: String


		/**
		 * Creates a [GDocumentPosition] for a span in this source, or `null` to suppress
		 * source context in error messages.
		 */
		public fun makeOrigin(startPosition: Int, endPosition: Int, column: Int, line: Int): GDocumentPosition? =
			null
	}


	public companion object {

		/** Creates a simple in-memory [Parsable] source from the given [content] string. */
		public fun of(content: String, name: String = "<unknown>"): Parsable =
			object : Parsable {

				override val content = content
				override val name = name
			}
	}
}
