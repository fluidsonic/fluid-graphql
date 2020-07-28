package io.fluidsonic.graphql


interface GVariableInputCoercerContext : GExecutorContext {

	val argumentDefinition: GArgumentDefinition?
	val type: GNamedType
	val variableDefinition: GVariableDefinition

	fun invalidValueError(details: String? = null): Nothing
}
