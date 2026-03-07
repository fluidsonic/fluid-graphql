package io.fluidsonic.graphql


/** A `{ ... }` block containing a list of [GSelection]s in a GraphQL operation or fragment. */
public class GSelectionSet(
	public val selections: List<GSelection>,
	origin: GDocumentPosition? = null,
	extensions: GNodeExtensionSet<GSelectionSet> = GNodeExtensionSet.empty(),
) : GNode(
	extensions = extensions,
	origin = origin
) {

	override fun equalsNode(other: GNode, includingOrigin: Boolean): Boolean =
		this === other || (
			other is GSelectionSet &&
				selections.equalsNode(other.selections, includingOrigin = includingOrigin) &&
				(!includingOrigin || origin == other.origin)
			)


	public companion object
}
