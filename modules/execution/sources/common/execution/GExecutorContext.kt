package io.fluidsonic.graphql


public interface GExecutorContext {

	public val defaultFieldResolver: GFieldResolver<Any>?
	public val document: GDocument
	public val operation: GOperationDefinition
	public val root: Any
	public val rootType: GObjectType
	public val schema: GSchema
	public val variableValues: Map<String, Any?>
}
