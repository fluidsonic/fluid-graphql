package io.fluidsonic.graphql


// FIXME do we need all the contexts to be generic?
interface GValueConversionContext<out Environment : Any> {

	val environment: Environment
	val schema: GSchema
}
