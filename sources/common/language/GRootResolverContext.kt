package io.fluidsonic.graphql


interface GRootResolverContext<out Environment : Any> {

	val environment: Environment
	val operationType: GOperationType
	val operationTypeDefinition: GNamedType
	val schema: GSchema
}
