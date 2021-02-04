package io.fluidsonic.graphql


public sealed class GExceptionOrigin {

	public class FieldResolver(
		public val context: GFieldResolverContext,
		public val resolver: GFieldResolver<*>
	) : GExceptionOrigin()


	public class NodeInputCoercer(
		public val coercer: GNodeInputCoercer<*>,
		public val context: GNodeInputCoercerContext
	) : GExceptionOrigin()


	public class OutputCoercer(
		public val coercer: GOutputCoercer<*>,
		public val context: GOutputCoercerContext
	) : GExceptionOrigin()


	public class RootResolver(
		public val context: GRootResolverContext,
		public val resolver: GRootResolver
	) : GExceptionOrigin()


	public class VariableInputCoercer(
		public val coercer: GVariableInputCoercer<*>,
		public val context: GVariableInputCoercerContext
	) : GExceptionOrigin()
}
