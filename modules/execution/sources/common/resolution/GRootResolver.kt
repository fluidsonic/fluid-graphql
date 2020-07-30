package io.fluidsonic.graphql


public interface GRootResolver {

	public suspend fun resolveRoot(context: GRootResolverContext): Any


	public companion object {

		public fun constant(root: Any): GRootResolver =
			Constant(root)


		public operator fun invoke(resolve: suspend GRootResolverContext.() -> Any): GRootResolver =
			Function(resolve)


		public fun unit(): GRootResolver =
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
