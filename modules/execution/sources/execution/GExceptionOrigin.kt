package io.fluidsonic.graphql

/**
 * Identifies where in the execution pipeline an exception was thrown.
 *
 * Passed to a [GExceptionHandler] via [GExceptionHandlerContext.origin] so the handler can
 * inspect the resolver or coercer that threw, along with the associated context.
 */
public sealed interface GExceptionOrigin {

	/** The executor child context active at the point where the exception was thrown. */
	public val context: GExecutorContext.Child

	/**
	 * The path at the point where the exception occurred, or `null` if unavailable.
	 *
	 * Not necessarily a *response* path: [InputValueCoercer] names a variable here, because that is the only
	 * position a variable failure has. Use [responseFieldPath] when deciding what to put on a [GError].
	 */
	public val path: GPath?

	/** An exception thrown by a [GFieldResolver]. */
	public class FieldResolver(
		override val context: GFieldResolverContext,
		/** The resolver that threw the exception. */
		public val resolver: GFieldResolver<*>,
	) : GExceptionOrigin {

		override val path: GPath? get() = context.path
	}

	/** An exception thrown by a [GInputLiteralCoercer]. */
	public class InputLiteralCoercer(
		/** The coercer that threw the exception. */
		public val coercer: GInputLiteralCoercer<*>,
		override val context: GExecutorContext.Child,
		override val path: GPath?,
	) : GExceptionOrigin

	/** An exception thrown by a [GInputValueCoercer]. */
	public class InputValueCoercer(
		/** The coercer that threw the exception. */
		public val coercer: GInputValueCoercer<*>,
		override val context: GExecutorContext.Child,
		override val path: GPath?,
	) : GExceptionOrigin

	/** An exception thrown by a [GOutputValueCoercer]. */
	public class OutputValueCoercer(
		/** The coercer that threw the exception. */
		public val coercer: GOutputValueCoercer<*>,
		override val context: GExecutorContext.Child,
		override val path: GPath?,
	) : GExceptionOrigin

	/** An exception thrown by a [GRootResolver]. */
	public class RootResolver(
		override val context: GRootResolverContext,
		/** The resolver that threw the exception. */
		public val resolver: GRootResolver,
	) : GExceptionOrigin {

		override val path: GPath get() = GPath.root
	}
}

/**
 * The path of the response field this origin failed at, or `null` if it has none.
 *
 * `null` for [GExceptionOrigin.InputValueCoercer] even though it carries a [GExceptionOrigin.path]: that
 * path names a *variable*, and a variable failure is a request error whose response has no `data` for a path
 * to index into. The specification defines `path` as "the path of the response field which experienced the
 * error", so reporting a variable name there would be a category error.
 *
 * `null` too whenever the path is empty, since the root position is not a field.
 */
// https://spec.graphql.org/draft/#sec-Errors
internal val GExceptionOrigin.responseFieldPath: GPath?
	get() = when (this) {
		is GExceptionOrigin.InputValueCoercer -> null
		else -> path?.takeIf { it.elements.isNotEmpty() }
	}
