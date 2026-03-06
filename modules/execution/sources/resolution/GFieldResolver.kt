package io.fluidsonic.graphql


public fun interface GFieldResolver<in Parent : Any> {

	public suspend fun GFieldResolverContext.resolveField(parent: Parent): Any?
}


@SchemaBuilderKeywordB // FIXME
public suspend fun <Parent : Any> GFieldResolver<Parent>.resolveField(parent: Parent, context: GFieldResolverContext): Any? =
	with(context) { resolveField(parent) }
