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
	override val variableValues: Map<String, Any?>
) : GExecutorContext, GRootResolverContext {

	override val execution: GExecutorContext
		get() = this
}
