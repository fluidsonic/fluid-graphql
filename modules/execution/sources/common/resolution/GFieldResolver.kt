package io.fluidsonic.graphql


public fun interface GFieldResolver<in Parent : Any> {

	public suspend fun GFieldResolverContext.resolveField(parent: Parent): Any?
}


@SchemaBuilderKeywordB // FIXME
public suspend fun <Parent : Any> GFieldResolver<Parent>.resolveField(parent: Parent, context: GFieldResolverContext): Any? =
	with(context) { resolveField(parent) }


// Remove when fixed: https://youtrack.jetbrains.com/issue/KT-40165
@Suppress("ObjectLiteralToLambda")
public fun <Parent : Any> GFieldResolver(resolveField: GFieldResolverContext.(parent: Parent) -> Any?): GFieldResolver<Parent> =
	object : GFieldResolver<Parent> {

		override suspend fun GFieldResolverContext.resolveField(parent: Parent): Any? =
			resolveField(parent)
	}
