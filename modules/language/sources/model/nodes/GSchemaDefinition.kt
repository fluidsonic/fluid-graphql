package io.fluidsonic.graphql


/**
 * An explicit `schema { query: Query mutation: Mutation ... }` definition in a GraphQL SDL document.
 *
 * When absent, the [GSchema] factory uses the conventional root type names (`Query`, `Mutation`, `Subscription`).
 */
public class GSchemaDefinition(
	override val operationTypeDefinitions: List<GOperationTypeDefinition>,
	override val descriptionNode: GStringValue? = null,
	override val directives: List<GDirective> = emptyList(),
	origin: GDocumentPosition? = null,
	extensions: GNodeExtensionSet<GSchemaDefinition> = GNodeExtensionSet.empty(),
) :
	GTypeSystemDefinition(
		extensions = extensions,
		origin = origin
	),
	GNode.WithDirectives,
	GNode.WithOperationTypeDefinitions,
	GNode.WithOptionalDescription {

	public constructor(
		operationTypeDefinitions: List<GOperationTypeDefinition>,
		description: String? = null,
		directives: List<GDirective> = emptyList(),
	) : this(
		descriptionNode = description?.let(::GStringValue),
		operationTypeDefinitions = operationTypeDefinitions,
		directives = directives,
	)


	override fun equalsNode(other: GNode, includingOrigin: Boolean): Boolean =
		this === other || (
			other is GSchemaDefinition &&
				descriptionNode.equalsNode(other.descriptionNode, includingOrigin = includingOrigin) &&
				directives.equalsNode(other.directives, includingOrigin = includingOrigin) &&
				operationTypeDefinitions.equalsNode(other.operationTypeDefinitions, includingOrigin = includingOrigin) &&
				(!includingOrigin || origin == other.origin)
			)


	public companion object
}
