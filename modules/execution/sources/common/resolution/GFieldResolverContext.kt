package io.fluidsonic.graphql


interface GFieldResolverContext : GExecutorContext {

	val arguments: Map<String, Any?>
	val field: GFieldDefinition
	val parentType: GNamedType
}
