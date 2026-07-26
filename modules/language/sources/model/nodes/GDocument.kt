package io.fluidsonic.graphql

/**
 * The root AST node of a GraphQL document, containing all top-level definitions.
 *
 * A document may hold any mix of [GExecutableDefinition]s (operations and fragments) and
 * [GTypeSystemDefinition]s (type/directive/schema definitions). Use [parse] to build a
 * document from a string, [operation] to look up a named operation, and [fragment] to look
 * up a named fragment.
 *
 * @see GSchema to build a resolved schema from a document.
 */
public class GDocument(
	public val definitions: List<GDefinition>,
	origin: GDocumentPosition? = null,
	extensions: GNodeExtensionSet<GDocument> = GNodeExtensionSet.empty(),
) : GNode(
	extensions = extensions,
	origin = origin,
) {

	override fun equalsNode(other: GNode, includingOrigin: Boolean): Boolean = this === other ||
		(
			other is GDocument &&
				definitions.equalsNode(other.definitions, includingOrigin = includingOrigin) &&
				(!includingOrigin || origin == other.origin)
			)

	/** Returns the fragment definition with the given [name], or `null` if not found. */
	public fun fragment(name: String): GFragmentDefinition? {
		for (definition in definitions) {
			if (definition is GFragmentDefinition && definition.name == name) {
				return definition
			}
		}

		return null
	}

	/** Returns the operation definition with the given [name] (or the anonymous operation when `null`), or `null` if not found. */
	public fun operation(name: String?): GOperationDefinition? {
		for (definition in definitions) {
			if (definition is GOperationDefinition && definition.name == name) {
				return definition
			}
		}

		return null
	}

	public companion object {

		/**
		 * Parses a GraphQL document from [source].
		 *
		 * Returns a [GResult.Success] with the parsed document, or a [GResult.Failure] with parse errors.
		 *
		 * [maxTokens] caps how many lexical tokens the document may contain; the parse is aborted with a syntax error
		 * as soon as the cap is exceeded. Whitespace, commas, comments and the end of input are not counted, and a
		 * block string counts as a single token. It defaults to `null`, which imposes no limit.
		 *
		 * Parsing is recursive, so a sufficiently deeply nested document can still exhaust the call stack regardless
		 * of [maxTokens]. Set [maxTokens] when parsing untrusted input.
		 */
		public fun parse(source: GDocumentSource.Parsable, maxTokens: Int? = null): GResult<GDocument> = Parser.parseDocument(source, maxTokens = maxTokens)

		/**
		 * Parses a GraphQL document from a raw string.
		 *
		 * @see parse for the meaning of [maxTokens].
		 */
		public fun parse(content: String, name: String = "<document>", maxTokens: Int? = null): GResult<GDocument> =
			parse(GDocumentSource.of(content = content, name = name), maxTokens = maxTokens)
	}
}
