package io.fluidsonic.graphql


public class GVariableDefinition(
	name: GName,
	override val type: GTypeRef,
	override val defaultValue: GValue? = null,
	override val directives: List<GDirective> = emptyList(),
	origin: GDocumentPosition? = null,
	extensions: GNodeExtensionSet<GVariableDefinition> = GNodeExtensionSet.empty(),
) :
	GNode(
		extensions = extensions,
		origin = origin
	),
	GNode.WithDefaultValue,
	GNode.WithDirectives,
	GNode.WithName {

	override val nameNode: GName = name


	public constructor(
		name: String,
		type: GTypeRef,
		defaultValue: GValue? = null,
		directives: List<GDirective> = emptyList(),
		extensions: GNodeExtensionSet<GVariableDefinition> = GNodeExtensionSet.empty(),
	) : this(
		name = GName(name),
		type = type,
		defaultValue = defaultValue,
		directives = directives,
		extensions = extensions
	)


	override fun equalsNode(other: GNode, includingOrigin: Boolean): Boolean =
		this === other || (
			other is GVariableDefinition &&
				defaultValue.equalsNode(other.defaultValue, includingOrigin = includingOrigin) &&
				directives.equalsNode(other.directives, includingOrigin = includingOrigin) &&
				nameNode.equalsNode(other.nameNode, includingOrigin = includingOrigin) &&
				type.equalsNode(other.type, includingOrigin = includingOrigin) &&
				(!includingOrigin || origin == other.origin)
			)


	public companion object
}
