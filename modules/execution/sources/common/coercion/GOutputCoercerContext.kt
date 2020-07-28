package io.fluidsonic.graphql


interface GOutputCoercerContext : GExecutorContext {

	val field: GFieldDefinition
	val parentType: GObjectType
	val type: GType

	fun invalidValueError(details: String? = null): Nothing
}
