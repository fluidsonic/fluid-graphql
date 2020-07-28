package io.fluidsonic.graphql


internal class DefaultFieldResolverContext(
	override val arguments: Map<String, Any?>,
	override val field: GFieldDefinition,
	override val parentType: GObjectType,
	executorContext: GExecutorContext
) : GFieldResolverContext, GExecutorContext by executorContext
