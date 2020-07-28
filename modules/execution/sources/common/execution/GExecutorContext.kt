package io.fluidsonic.graphql


interface GExecutorContext {

	val defaultFieldResolver: GFieldResolver<Any>?
	val document: GDocument
	val operation: GOperationDefinition
	val root: Any
	val rootType: GObjectType
	val schema: GSchema
	val variableValues: Map<String, Any?>
}
