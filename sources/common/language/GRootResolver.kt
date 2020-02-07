package io.fluidsonic.graphql


interface GRootResolver<in Environment : Any> {

	suspend fun resolveRoot(context: GRootResolverContext<Environment>): Any


	companion object {

		operator fun <Environment : Any> invoke(
			resolver: suspend GRootResolverContext<Environment>.() -> Any
		) =
			object : GRootResolver<Environment> {

				override suspend fun resolveRoot(context: GRootResolverContext<Environment>) =
					with(context) { resolver() }
			}
	}
}
