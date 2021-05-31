package io.fluidsonic.graphql


internal class DefaultFieldResolverContext(
	override val arguments: Map<String, Any?>,
	override val execution: DefaultExecutorContext,
	override val fieldDefinition: GFieldDefinition,
	private val parent: Any,
	override val parentType: GObjectType,
) : GFieldResolverContext {

	@Suppress("UNCHECKED_CAST")
	override suspend fun next(): Any? =
		when (val resolver = fieldDefinition.resolver as GFieldResolver<Any>?) {
			null -> error("No resolver is set for field '${parentType.name}.${fieldDefinition.name}'.")
			else -> execution.withExceptionHandler(origin = { GExceptionOrigin.FieldResolver(resolver = resolver, context = this) }) {
				resolver.resolveField(parent, context = Next())
			}
		}


	private inner class Next : GFieldResolverContext by this {

		override suspend fun next() =
			error("Resolver of field '${parentType.name}.${fieldDefinition.name}' cannot delegate resolution any further.")
	}
}
