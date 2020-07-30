package io.fluidsonic.graphql


public interface GOutputCoercerContext : GExecutorContext {

	public val field: GFieldDefinition
	public val parentType: GObjectType
	public val type: GType

	public fun invalidValueError(details: String? = null): Nothing
}
