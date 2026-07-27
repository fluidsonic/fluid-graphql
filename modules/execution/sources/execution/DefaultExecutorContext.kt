package io.fluidsonic.graphql

import kotlin.coroutines.cancellation.CancellationException

internal data class DefaultExecutorContext(
	override val document: GDocument,
	val exceptionHandler: GExceptionHandler?,
	override val extensions: GExecutorContextExtensionSet,
	val fieldResolver: GFieldResolver<Any>?,
	val fieldSelectionExecutor: DefaultFieldSelectionExecutor,
	val nodeInputConverter: NodeInputConverter,
	override val operation: GOperationDefinition,
	val outputConverter: OutputConverter,
	override val root: Any,
	override val rootType: GObjectType,
	override val schema: GSchema,
	val selectionSetExecutor: DefaultSelectionSetExecutor,
	val variableInputConverter: VariableInputConverter,
	override val variableValues: Map<String, Any?>,
) : GExecutorContext,
	GRootResolverContext {

	override val execution: GExecutorContext
		get() = this

	inline fun <Result> withExceptionHandler(origin: () -> GExceptionOrigin, action: () -> Result): Result {
		try {
			return action()
		} catch (exception: GErrorException) {
			val resolvedOrigin = origin()
			val path = resolvedOrigin.responseFieldPath
			if (path != null) {
				throw GErrorException(
					exception.errors.map { error ->
						if (error.path == null) error.copy(path = path) else error
					},
				)
			}
			throw exception
		} catch (exception: CancellationException) {
			// Cancellation is not a failure of the resolver or coercer. Field resolvers suspend, so a cancelled
			// request unwinds through them as this exception; handing it to the exception handler would turn it
			// into a GraphQL error and leave the caller's coroutine believing it is still running.
			throw exception
		} catch (exception: Throwable) {
			with(exceptionHandler ?: throw exception) {
				@Suppress("NAME_SHADOWING")
				val origin = origin()

				DefaultExceptionHandlerContext(origin = origin)
					.handleException(exception)
					.let { error ->
						when (error.path) {
							null -> origin.responseFieldPath?.let { error.copy(path = it) } ?: error
							else -> error
						}
					}
					.throwException()
			}
		}
	}
}
