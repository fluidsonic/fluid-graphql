package io.fluidsonic.graphql


// Make fun interface when fixed: https://youtrack.jetbrains.com/issue/KT-40165
public /*fun*/ interface GRootResolver {

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


// Remove when fixed: https://youtrack.jetbrains.com/issue/KT-40165
@Suppress("ObjectLiteralToLambda")
public fun <Parent : Any> GRootResolver(resolveRoot: suspend GRootResolverContext.() -> Any): GRootResolver =
	object : GRootResolver {

		override suspend fun GRootResolverContext.resolveRoot(): Any =
			resolveRoot()
	}
