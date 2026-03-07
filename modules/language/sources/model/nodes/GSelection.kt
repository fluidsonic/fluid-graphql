package io.fluidsonic.graphql


/**
 * Sealed base class for the three kinds of field selections in a GraphQL operation or fragment:
 * [GFieldSelection], [GFragmentSelection] (named fragment spread), and [GInlineFragmentSelection].
 */
public sealed class GSelection(
	override val directives: List<GDirective>,
	extensions: GNodeExtensionSet<GSelection>,
	origin: GDocumentPosition?,
) :
	GNode(
		extensions = extensions,
		origin = origin
	),
	GNode.WithDirectives {

	public companion object
}
