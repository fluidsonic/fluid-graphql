package io.fluidsonic.graphql


interface GRootResolverContext {

	val operationType: GOperationType
	val operationTypeDefinition: GNamedType
	val schema: GSchema
}
