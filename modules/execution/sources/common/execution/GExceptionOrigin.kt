package io.fluidsonic.graphql


public sealed class GExceptionOrigin {

	public class NodeInputCoercer(
		public val coercer: GNodeInputCoercer<*>,
		public val context: GNodeInputCoercerContext
	) : GExceptionOrigin()


	public class VariableInputCoercer(
		public val coercer: GVariableInputCoercer<*>,
		public val context: GVariableInputCoercerContext
	) : GExceptionOrigin()


	public class OutputCoercer(
		public val coercer: GOutputCoercer<*>,
		public val context: GOutputCoercerContext
	) : GExceptionOrigin()


	public class FieldResolver(
		public val context: GFieldResolverContext,
		public val resolver: GFieldResolver<*>
	) : GExceptionOrigin()


	public class RootResolver(
		public val context: GNodeInputCoercerContext,
		public val rootResolver: GRootResolver
	) : GExceptionOrigin()
}
