package io.fluidsonic.graphql


class GExecutionContext(
	val defaultResolver: GFieldResolver<*>? = null,
	val document: GDocument,
	val externalContext: Any? = null,
	val operation: GOperationDefinition,
	val rootValue: Any,
	val schema: GSchema,
	val variableValues: Map<String, Any?>
)
