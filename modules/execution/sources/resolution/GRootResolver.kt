package io.fluidsonic.graphql


/**
 * Provides the root value passed to top-level field resolvers as their `parent` argument.
 *
 * Provide a root resolver via [GExecutor.default]'s `rootResolver` parameter.
 * Use [constant] to always return the same object, or [unit] when your field resolvers
 * don't need a meaningful root value.
 */
public fun interface GRootResolver {

	public suspend fun GRootResolverContext.resolveRoot(): Any


	public companion object {

		/** Returns a [GRootResolver] that always provides [root] as the root value. */
		public fun constant(root: Any): GRootResolver =
			Constant(root)


		/** Returns a [GRootResolver] that always provides [Unit] as the root value. */
		public fun unit(): GRootResolver =
			constant(Unit)
	}


	private class Constant(private val root: Any) : GRootResolver {

		override suspend fun GRootResolverContext.resolveRoot() =
			root
	}
}
