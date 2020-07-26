package io.fluidsonic.graphql


interface GVariableInputCoercerContext {

	val argumentDefinition: GArgumentDefinition?
	val schema: GSchema // FIXME add to a shared execution context
	val type: GNamedType
	val variableDefinition: GVariableDefinition

	fun invalidValueError(details: String? = null): Nothing
}
