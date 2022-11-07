package io.fluidsonic.graphql

import io.fluidsonic.graphql.GExceptionOrigin.*


public sealed interface GExceptionOrigin {

	public val context: GExecutorContext.Child


	public class FieldResolver(
		public override val context: GFieldResolverContext,
		public val resolver: GFieldResolver<*>,
	) : GExceptionOrigin


	public class NodeInputCoercer(
		public val coercer: GNodeInputCoercer<*>,
		public override val context: GNodeInputCoercerContext,
	) : GExceptionOrigin


	public class OutputCoercer(
		public val coercer: GOutputCoercer<*>,
		public override val context: GOutputCoercerContext,
	) : GExceptionOrigin


	public class RootResolver(
		public override val context: GRootResolverContext,
		public val resolver: GRootResolver,
	) : GExceptionOrigin


	public class VariableInputCoercer(
		public val coercer: GVariableInputCoercer<*>,
		public override val context: GVariableInputCoercerContext,
	) : GExceptionOrigin
}


public val GExceptionOrigin.path: GPath?
	get() = when (this) {
		is FieldResolver -> context.path
		is NodeInputCoercer -> context.fieldSelectionPath
		is OutputCoercer -> context.path
		is RootResolver -> GPath.root
		is VariableInputCoercer -> context.path
	}
