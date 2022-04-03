package io.fluidsonic.graphql


public class GVariableRef(
	name: GName,
	origin: GDocumentPosition? = null,
	extensions: GNodeExtensionSet<GVariableRef> = GNodeExtensionSet.empty(),
) : GValue(
	extensions = extensions,
	origin = origin
) {

	public val name: String get() = nameNode.value
	public val nameNode: GName = name

	override val kind: Kind get() = Kind.VARIABLE


	public constructor(
		name: String,
		extensions: GNodeExtensionSet<GVariableRef> = GNodeExtensionSet.empty(),
	) : this(
		name = GName(name),
		extensions = extensions
	)


	override fun equals(other: Any?): Boolean =
		this === other || (other is GVariableRef && name == other.name)


	override fun equalsNode(other: GNode, includingOrigin: Boolean): Boolean =
		this === other || (
			other is GVariableRef &&
				nameNode.equalsNode(other.nameNode, includingOrigin = includingOrigin) &&
				(!includingOrigin || origin == other.origin)
			)


	override fun hashCode(): Int =
		name.hashCode()


	override fun unwrap(): Nothing =
		error("Cannot unwrap a GraphQL variable: $name")


	public companion object
}
