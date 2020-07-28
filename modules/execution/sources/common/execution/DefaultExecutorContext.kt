package io.fluidsonic.graphql


internal data class DefaultExecutorContext(
	override val defaultFieldResolver: GFieldResolver<Any>?,
	override val document: GDocument,
	val fieldSelectionExecutor: DefaultFieldSelectionExecutor,
	val nodeInputCoercer: GenericNodeInputCoercer,
	override val operation: GOperationDefinition,
	val outputCoercer: GenericOutputCoercer,
	override val root: Any,
	override val rootType: GObjectType,
	override val schema: GSchema,
	val selectionSetExecutor: DefaultSelectionSetExecutor,
	val variableInputCoercer: GenericVariableInputCoercer,
	override val variableValues: Map<String, Any?>
) : GRootResolverContext
