package io.fluidsonic.graphql


interface GNodeInputCoercerContext {

	val argumentDefinition: GArgumentDefinition?
	val schema: GSchema // FIXME add to a shared execution context
	val type: GNamedType
	val variableValues: Map<String, Any?> // FIXME add to a shared execution context

	fun invalidValueError(details: String? = null): Nothing
}
