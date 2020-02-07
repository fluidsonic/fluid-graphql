package io.fluidsonic.graphql


interface GFieldResolver<in Environment : Any, in Parent : Any> {

	suspend fun resolveField(parent: Parent, context: GFieldResolverContext<Environment>): Any?


	companion object {

		operator fun <Environment : Any, Parent : Any> invoke(
			resolver: suspend GFieldResolverContext<Environment>.(parent: Parent) -> Any?
		) =
			object : GFieldResolver<Environment, Parent> {

				override suspend fun resolveField(parent: Parent, context: GFieldResolverContext<Environment>) =
					with(context) { resolver(parent) }
			}
	}
}
