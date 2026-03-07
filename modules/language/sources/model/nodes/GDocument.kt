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
	origin = origin
) {

	override fun equalsNode(other: GNode, includingOrigin: Boolean): Boolean =
		this === other || (
			other is GDocument &&
				definitions.equalsNode(other.definitions, includingOrigin = includingOrigin) &&
				(!includingOrigin || origin == other.origin)
			)


	/** Returns the fragment definition with the given [name], or `null` if not found. */
	public fun fragment(name: String): GFragmentDefinition? {
		for (definition in definitions)
			if (definition is GFragmentDefinition && definition.name == name)
				return definition

		return null
	}


	/** Returns the operation definition with the given [name] (or the anonymous operation when `null`), or `null` if not found. */
	public fun operation(name: String?): GOperationDefinition? {
		for (definition in definitions)
			if (definition is GOperationDefinition && definition.name == name)
				return definition

		return null
	}


	public companion object {

		/**
		 * Parses a GraphQL document from [source].
		 *
		 * Returns a [GResult.Success] with the parsed document, or a [GResult.Failure] with parse errors.
		 */
		public fun parse(source: GDocumentSource.Parsable): GResult<GDocument> =
			Parser.parseDocument(source)


		/** Parses a GraphQL document from a raw string. */
		public fun parse(content: String, name: String = "<document>"): GResult<GDocument> =
			parse(GDocumentSource.of(content = content, name = name))
	}
}
