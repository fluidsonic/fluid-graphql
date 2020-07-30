package io.fluidsonic.graphql


public interface GFieldResolver<in Parent : Any> {

	public suspend fun resolveField(parent: Parent, context: GFieldResolverContext): Any?


	public companion object {

		public operator fun <Parent : Any> invoke(resolve: suspend GFieldResolverContext.(parent: Parent) -> Any?): GFieldResolver<Parent> =
			Function(resolve)
	}


	private class Function<Parent : Any>(private val resolve: suspend GFieldResolverContext.(parent: Parent) -> Any?) : GFieldResolver<Parent> {

		override suspend fun resolveField(parent: Parent, context: GFieldResolverContext) =
			resolve(context, parent)
	}
}
