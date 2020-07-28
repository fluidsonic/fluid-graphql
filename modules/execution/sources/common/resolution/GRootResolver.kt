package io.fluidsonic.graphql


interface GRootResolver {

	suspend fun resolveRoot(context: GRootResolverContext): Any


	companion object {

		fun constant(root: Any): GRootResolver =
			Constant(root)


		operator fun invoke(resolve: suspend GRootResolverContext.() -> Any): GRootResolver =
			Function(resolve)


		fun unit(): GRootResolver =
			constant(Unit)
	}


	private class Constant(private val root: Any) : GRootResolver {

		override suspend fun resolveRoot(context: GRootResolverContext) =
			root
	}


	private class Function(private val resolve: suspend GRootResolverContext.() -> Any) : GRootResolver {

		override suspend fun resolveRoot(context: GRootResolverContext) =
			resolve(context)
	}
}
