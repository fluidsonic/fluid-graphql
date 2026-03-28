package io.fluidsonic.graphql


/**
 * Context provided to a [GFieldResolver] when resolving a field.
 *
 * Gives access to the coerced argument values, the field's schema definition, the current
 * response path, and the parent object type. Call [next] to delegate to the next resolver
 * in the chain (e.g. when implementing middleware-style resolvers).
 */
public interface GFieldResolverContext : GExecutorContext.Child {

	/** Coerced argument values keyed by argument name. */
	public val arguments: Map<String, Any?>

	/** The schema definition of the field being resolved. */
	public val fieldDefinition: GFieldDefinition

	/** The response path to the field currently being resolved. */
	public val path: GPath

	/** The object type that contains the field being resolved. */
	public val parentType: GObjectType

	/**
	 * Delegates to the next field resolver in the chain.
	 *
	 * Returns `null` if there is no further resolver, or when the next resolver returns `null`.
	 */
	public suspend fun next(): Any?
}
