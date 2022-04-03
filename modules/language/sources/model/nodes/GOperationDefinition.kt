package io.fluidsonic.graphql


public class GOperationDefinition(
	public val type: GOperationType,
	name: GName? = null,
	public val selectionSet: GSelectionSet,
	override val variableDefinitions: List<GVariableDefinition> = emptyList(),
	override val directives: List<GDirective> = emptyList(),
	origin: GDocumentPosition? = null,
	extensions: GNodeExtensionSet<GOperationDefinition> = GNodeExtensionSet.empty(),
) :
	GExecutableDefinition(
		extensions = extensions,
		origin = origin
	),
	GNode.WithDirectives,
	GNode.WithOptionalName,
	GNode.WithVariableDefinitions {

	override val nameNode: GName? = name


	public constructor(
		type: GOperationType,
		name: String? = null,
		selectionSet: GSelectionSet,
		variableDefinitions: List<GVariableDefinition> = emptyList(),
		directives: List<GDirective> = emptyList(),
		extensions: GNodeExtensionSet<GOperationDefinition> = GNodeExtensionSet.empty(),
	) : this(
		type = type,
		name = name?.let(::GName),
		selectionSet = selectionSet,
		variableDefinitions = variableDefinitions,
		directives = directives,
		extensions = extensions
	)


	override fun equalsNode(other: GNode, includingOrigin: Boolean): Boolean =
		this === other || (
			other is GOperationDefinition &&
				directives.equalsNode(other.directives, includingOrigin = includingOrigin) &&
				nameNode.equalsNode(other.nameNode, includingOrigin = includingOrigin) &&
				selectionSet.equalsNode(other.selectionSet, includingOrigin = includingOrigin) &&
				type == other.type &&
				variableDefinitions.equalsNode(other.variableDefinitions, includingOrigin = includingOrigin) &&
				(!includingOrigin || origin == other.origin)
			)


	public companion object
}
