package io.fluidsonic.graphql


/**
 * A field selection in a GraphQL operation or fragment (`alias: fieldName(args) { ... }`).
 *
 * The [alias] overrides the response key; if absent, [name] is used as the response key.
 * Leaf fields have no [selectionSet]; object/interface/union fields have one.
 */
public class GFieldSelection(
	name: GName,
	public val selectionSet: GSelectionSet? = null,
	override val arguments: List<GArgument> = emptyList(),
	alias: GName? = null,
	directives: List<GDirective> = emptyList(),
	origin: GDocumentPosition? = null,
	extensions: GNodeExtensionSet<GFieldSelection> = GNodeExtensionSet.empty(),
) :
	GSelection(
		directives = directives,
		extensions = extensions,
		origin = origin
	),
	GNode.WithArguments {

	public val alias: String? get() = aliasNode?.value
	public val aliasNode: GName? = alias
	public val name: String get() = nameNode.value
	public val nameNode: GName = name


	public constructor(
		name: String,
		selectionSet: GSelectionSet? = null,
		arguments: List<GArgument> = emptyList(),
		alias: String? = null,
		directives: List<GDirective> = emptyList(),
		extensions: GNodeExtensionSet<GFieldSelection> = GNodeExtensionSet.empty(),
	) : this(
		name = GName(name),
		selectionSet = selectionSet,
		arguments = arguments,
		alias = alias?.let(::GName),
		directives = directives,
		extensions = extensions
	)


	override fun equalsNode(other: GNode, includingOrigin: Boolean): Boolean =
		this === other || (
			other is GFieldSelection &&
				aliasNode.equalsNode(other.aliasNode, includingOrigin = includingOrigin) &&
				arguments.equalsNode(other.arguments, includingOrigin = includingOrigin) &&
				directives.equalsNode(other.directives, includingOrigin = includingOrigin) &&
				nameNode.equalsNode(other.nameNode, includingOrigin = includingOrigin) &&
				selectionSet.equalsNode(other.selectionSet, includingOrigin = includingOrigin) &&
				(!includingOrigin || origin == other.origin)
			)


	public companion object
}
