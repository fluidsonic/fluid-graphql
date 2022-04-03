package io.fluidsonic.graphql


public class GFragmentSelection(
	name: GName,
	directives: List<GDirective> = emptyList(),
	origin: GDocumentPosition? = null,
	extensions: GNodeExtensionSet<GFragmentSelection> = GNodeExtensionSet.empty(),
) :
	GSelection(
		directives = directives,
		extensions = extensions,
		origin = origin
	) {

	public val name: String get() = nameNode.value
	public val nameNode: GName = name


	public constructor(
		name: String,
		directives: List<GDirective> = emptyList(),
		extensions: GNodeExtensionSet<GFragmentSelection> = GNodeExtensionSet.empty(),
	) : this(
		name = GName(name),
		directives = directives,
		extensions = extensions
	)


	override fun equalsNode(other: GNode, includingOrigin: Boolean): Boolean =
		this === other || (
			other is GFragmentSelection &&
				directives.equalsNode(other.directives, includingOrigin = includingOrigin) &&
				nameNode.equalsNode(other.nameNode, includingOrigin = includingOrigin) &&
				(!includingOrigin || origin == other.origin)
			)


	public companion object
}
