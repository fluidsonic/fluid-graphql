package io.fluidsonic.graphql

import io.fluidsonic.graphql.GExceptionOrigin.*


/**
 * Identifies where in the execution pipeline an exception was thrown.
 *
 * Passed to a [GExceptionHandler] via [GExceptionHandlerContext.origin] so the handler can
 * inspect the resolver or coercer that threw, along with the associated context.
 */
public sealed interface GExceptionOrigin {

	/** The executor child context active at the point where the exception was thrown. */
	public val context: GExecutorContext.Child


	/** An exception thrown by a [GFieldResolver]. */
	public class FieldResolver(
		override val context: GFieldResolverContext,
		/** The resolver that threw the exception. */
		public val resolver: GFieldResolver<*>,
	) : GExceptionOrigin


	/** An exception thrown by a [GNodeInputCoercer]. */
	public class NodeInputCoercer(
		/** The coercer that threw the exception. */
		public val coercer: GNodeInputCoercer<*>,
		override val context: GNodeInputCoercerContext,
	) : GExceptionOrigin


	/** An exception thrown by a [GOutputCoercer]. */
	public class OutputCoercer(
		/** The coercer that threw the exception. */
		public val coercer: GOutputCoercer<*>,
		override val context: GOutputCoercerContext,
	) : GExceptionOrigin


	/** An exception thrown by a [GRootResolver]. */
	public class RootResolver(
		override val context: GRootResolverContext,
		/** The resolver that threw the exception. */
		public val resolver: GRootResolver,
	) : GExceptionOrigin


	/** An exception thrown by a [GVariableInputCoercer]. */
	public class VariableInputCoercer(
		/** The coercer that threw the exception. */
		public val coercer: GVariableInputCoercer<*>,
		override val context: GVariableInputCoercerContext,
	) : GExceptionOrigin
}


/** The response path at the point where the exception occurred, or `null` if unavailable. */
public val GExceptionOrigin.path: GPath?
	get() = when (this) {
		is FieldResolver -> context.path
		is NodeInputCoercer -> context.fieldSelectionPath
		is OutputCoercer -> context.path
		is RootResolver -> GPath.root
		is VariableInputCoercer -> context.path
	}
