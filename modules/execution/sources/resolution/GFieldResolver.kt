package io.fluidsonic.graphql


/**
 * Resolves a GraphQL field value given its [Parent] object.
 *
 * The [GFieldResolverContext] receiver provides access to arguments, field definition, path,
 * and the broader execution context. Return any value compatible with the field's declared type,
 * or `null` for nullable fields.
 *
 * Attach a resolver to a field via the DSL `resolve { }` builder or by setting
 * [GFieldDefinition.resolver] on the schema node extension set.
 * Provide a fallback via [GExecutor.default]'s `fieldResolver` parameter.
 */
public fun interface GFieldResolver<in Parent : Any> {

	public suspend fun GFieldResolverContext.resolveField(parent: Parent): Any?
}


@SchemaBuilderKeywordB // FIXME
public suspend fun <Parent : Any> GFieldResolver<Parent>.resolveField(parent: Parent, context: GFieldResolverContext): Any? =
	with(context) { resolveField(parent) }
