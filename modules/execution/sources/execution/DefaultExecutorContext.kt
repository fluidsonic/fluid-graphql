package io.fluidsonic.graphql


internal data class DefaultExecutorContext(
	override val document: GDocument,
	val exceptionHandler: GExceptionHandler?,
	override val extensions: GExecutorContextExtensionSet,
	val fieldResolver: GFieldResolver<Any>?,
	val fieldSelectionExecutor: DefaultFieldSelectionExecutor,
	val nodeInputCoercer: GNodeInputCoercer<Any?>?,
	val nodeInputConverter: NodeInputConverter,
	override val operation: GOperationDefinition,
	val outputCoercer: GOutputCoercer<Any>?,
	val outputConverter: OutputConverter,
	override val root: Any,
	override val rootType: GObjectType,
	override val schema: GSchema,
	val selectionSetExecutor: DefaultSelectionSetExecutor,
	val variableInputCoercer: GVariableInputCoercer<Any?>?,
	val variableInputConverter: VariableInputConverter,
	override val variableValues: Map<String, Any?>,
) : GExecutorContext, GRootResolverContext {

	override val execution: GExecutorContext
		get() = this


	inline fun <Result> withExceptionHandler(origin: () -> GExceptionOrigin, action: () -> Result): Result {
		try {
			return action()
		}
		catch (exception: GErrorException) {
			throw exception
		}
		catch (exception: Throwable) {
			with(exceptionHandler ?: throw exception) {
				@Suppress("NAME_SHADOWING")
				val origin = origin()

				DefaultExceptionHandlerContext(origin = origin)
					.handleException(exception)
					.let { error ->
						when (error.path) {
							null -> origin.path?.let { error.copy(path = it) } ?: error
							else -> error
						}
					}
					.throwException()
			}
		}
	}
}
