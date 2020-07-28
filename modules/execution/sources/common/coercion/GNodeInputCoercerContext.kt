package io.fluidsonic.graphql


interface GNodeInputCoercerContext : GExecutorContext {

	val argumentDefinition: GArgumentDefinition?
	val type: GNamedType

	fun invalidValueError(details: String? = null): Nothing
}
