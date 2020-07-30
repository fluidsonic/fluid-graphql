package io.fluidsonic.graphql


public interface GNodeInputCoercerContext : GExecutorContext {

	public val argumentDefinition: GArgumentDefinition?
	public val type: GNamedType

	public fun invalidValueError(details: String? = null): Nothing
}
