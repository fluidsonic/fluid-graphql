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
	@SchemaBuilderKeywordB // FIXME ok?
	public val arguments: Map<String, Any?>

	@SchemaBuilderKeywordB // FIXME ok?
	public val fieldDefinition: GFieldDefinition

	/** The response path to the field currently being resolved. */
	@SchemaBuilderKeywordB
	public val path: GPath

	@SchemaBuilderKeywordB // FIXME ok?
	public val parentType: GObjectType

	/**
	 * Delegates to the next field resolver in the chain.
	 *
	 * Returns `null` if there is no further resolver, or when the next resolver returns `null`.
	 */
	@SchemaBuilderKeywordB // FIXME
	public suspend fun next(): Any?
}
