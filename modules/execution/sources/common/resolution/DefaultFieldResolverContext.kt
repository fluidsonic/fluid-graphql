package io.fluidsonic.graphql


internal class DefaultFieldResolverContext(
	override val arguments: Map<String, Any?>,
	override val execution: GExecutorContext,
	override val fieldDefinition: GFieldDefinition,
	private val parent: Any,
	override val parentType: GObjectType
) : GFieldResolverContext {

	@Suppress("UNCHECKED_CAST")
	override suspend fun next(): Any? =
		when (val resolver = fieldDefinition.resolver as GFieldResolver<Any>?) {
			null -> error("No resolver is set for field '${parentType.name}.${fieldDefinition.name}'.")
			else -> resolver.resolveField(parent)
		}
}
