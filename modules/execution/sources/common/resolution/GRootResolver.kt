package io.fluidsonic.graphql


public fun interface GRootResolver {

	public suspend fun GRootResolverContext.resolveRoot(): Any


	public companion object {

		public fun constant(root: Any): GRootResolver =
			Constant(root)


		public fun unit(): GRootResolver =
			constant(Unit)
	}


	private class Constant(private val root: Any) : GRootResolver {

		override suspend fun GRootResolverContext.resolveRoot() =
			root
	}
}
