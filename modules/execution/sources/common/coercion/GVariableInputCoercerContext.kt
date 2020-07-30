package io.fluidsonic.graphql


public interface GVariableInputCoercerContext : GExecutorContext {

	public val argumentDefinition: GArgumentDefinition?
	public val type: GNamedType
	public val variableDefinition: GVariableDefinition

	public fun invalidValueError(details: String? = null): Nothing
}
