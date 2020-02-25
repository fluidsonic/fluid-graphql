package io.fluidsonic.graphql


interface GCoercionContext<out Environment : Any> {

	val environment: Environment
	val schema: GSchema
}
