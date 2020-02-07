package io.fluidsonic.graphql


interface GFieldResolverContext<out Environment : Any> {

	val arguments: Map<String, Any?>
	val fieldDefinition: GFieldDefinition
	val environment: Environment
	val parentTypeDefinition: GNamedType
	val schema: GSchema
}
