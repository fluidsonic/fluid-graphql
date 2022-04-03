package io.fluidsonic.graphql


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
