package io.fluidsonic.graphql


interface GFieldResolver<in Parent : Any> {

	suspend fun resolveField(parent: Parent, context: GFieldResolverContext): Any?


	companion object {

		operator fun <Parent : Any> invoke(resolve: suspend GFieldResolverContext.(parent: Parent) -> Any?): GFieldResolver<Parent> =
			Function(resolve)
	}


	private class Function<Parent : Any>(private val resolve: suspend GFieldResolverContext.(parent: Parent) -> Any?) : GFieldResolver<Parent> {

		override suspend fun resolveField(parent: Parent, context: GFieldResolverContext) =
			resolve(context, parent)
	}
}
