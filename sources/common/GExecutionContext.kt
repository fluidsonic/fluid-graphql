package io.fluidsonic.graphql


class GExecutionContext(
	val document: GDocument,
	val externalContext: Any? = null,
	val errors: MutableList<GError> = mutableListOf(),
	val fragmentsByName: Map<String, GFragmentDefinition>,
	val operation: GOperationDefinition,
	val rootValue: GObjectValue,
	val schema: GSchema,
	val variableValues: GVariableValues
)
