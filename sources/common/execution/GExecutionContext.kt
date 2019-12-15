package io.fluidsonic.graphql


class GExecutionContext(
	val defaultResolver: GFieldResolver<*>? = null,
	val document: GDocument,
	val externalContext: Any? = null,
	val errors: MutableList<GError> = mutableListOf(),
	val operation: GOperationDefinition,
	val rootValue: Any,
	val schema: GSchema,
	val variableValues: GVariableValues
)
