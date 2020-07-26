package io.fluidsonic.graphql


interface GRootResolver {

	suspend fun resolveRoot(context: GRootResolverContext): Any


	companion object {

		fun constant(root: Any): GRootResolver =
			Constant(root)


		operator fun invoke(
			resolver: suspend GRootResolverContext.() -> Any
		) =
			object : GRootResolver {

				override suspend fun resolveRoot(context: GRootResolverContext) =
					with(context) { resolver() }
			}


		fun unit(): GRootResolver =
			constant(Unit)
	}


	private class Constant(val root: Any) : GRootResolver {

		override suspend fun resolveRoot(context: GRootResolverContext) =
			root
	}
}
