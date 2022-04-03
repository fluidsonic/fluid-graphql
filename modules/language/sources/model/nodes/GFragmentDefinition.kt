package io.fluidsonic.graphql


public class GFragmentDefinition(
	name: GName,
	public val typeCondition: GNamedTypeRef,
	public val selectionSet: GSelectionSet,
	override val variableDefinitions: List<GVariableDefinition> = emptyList(),
	override val directives: List<GDirective> = emptyList(),
	origin: GDocumentPosition? = null,
	extensions: GNodeExtensionSet<GFragmentDefinition> = GNodeExtensionSet.empty(),
) :
	GExecutableDefinition(
		extensions = extensions,
		origin = origin
	),
	GNode.WithDirectives,
	GNode.WithName,
	GNode.WithVariableDefinitions {

	override val nameNode: GName = name


	public constructor(
		name: String,
		typeCondition: GNamedTypeRef,
		selectionSet: GSelectionSet,
		variableDefinitions: List<GVariableDefinition> = emptyList(),
		directives: List<GDirective> = emptyList(),
		extensions: GNodeExtensionSet<GFragmentDefinition> = GNodeExtensionSet.empty(),
	) : this(
		name = GName(name),
		typeCondition = typeCondition,
		selectionSet = selectionSet,
		variableDefinitions = variableDefinitions,
		directives = directives,
		extensions = extensions
	)


	override fun equalsNode(other: GNode, includingOrigin: Boolean): Boolean =
		this === other || (
			other is GFragmentDefinition &&
				directives.equalsNode(other.directives, includingOrigin = includingOrigin) &&
				nameNode.equalsNode(other.nameNode, includingOrigin = includingOrigin) &&
				selectionSet.equalsNode(other.selectionSet, includingOrigin = includingOrigin) &&
				typeCondition.equalsNode(other.typeCondition, includingOrigin = includingOrigin) &&
				variableDefinitions.equalsNode(other.variableDefinitions, includingOrigin = includingOrigin) &&
				(!includingOrigin || origin == other.origin)
			)


	public companion object
}
