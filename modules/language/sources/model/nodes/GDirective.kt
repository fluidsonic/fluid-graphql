package io.fluidsonic.graphql


/**
 * A GraphQL directive application (`@name(arg: value)`).
 *
 * The directive's definition (if any) is resolved from the schema via [GSchema.directiveDefinition].
 */
public class GDirective(
	name: GName,
	override val arguments: List<GArgument> = emptyList(),
	origin: GDocumentPosition? = null,
	extensions: GNodeExtensionSet<GDirective> = GNodeExtensionSet.empty(),
) :
	GNode(
		extensions = extensions,
		origin = origin
	),
	GNode.WithArguments,
	GNode.WithName {

	override val nameNode: GName = name


	public constructor(
		name: String,
		arguments: List<GArgument> = emptyList(),
		extensions: GNodeExtensionSet<GDirective> = GNodeExtensionSet.empty(),
	) : this(
		name = GName(name),
		arguments = arguments,
		extensions = extensions
	)


	override fun equalsNode(other: GNode, includingOrigin: Boolean): Boolean =
		this === other || (
			other is GDirective &&
				arguments.equalsNode(other.arguments, includingOrigin = includingOrigin) &&
				nameNode.equalsNode(other.nameNode, includingOrigin = includingOrigin) &&
				(!includingOrigin || origin == other.origin)
			)


	public companion object
}
