package io.fluidsonic.graphql


public interface GFieldResolverContext : GExecutorContext {

	public val arguments: Map<String, Any?>
	public val field: GFieldDefinition
	public val parentType: GObjectType
}
