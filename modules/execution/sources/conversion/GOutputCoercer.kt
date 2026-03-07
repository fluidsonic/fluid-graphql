package io.fluidsonic.graphql


/**
 * Converts a Kotlin value returned by a field resolver into a GraphQL-serializable value.
 *
 * The [GOutputCoercerContext] provides the field definition, parent type, response path, and
 * access to the next coercer in the chain via [GOutputCoercerContext.next].
 *
 * Attach to a scalar or object type via [GLeafType.outputCoercer] or [GObjectType.outputCoercer],
 * or provide a fallback via [GExecutor.default].
 */
public fun interface GOutputCoercer<in Output : Any> {

	public fun GOutputCoercerContext.coerceOutput(output: Output): Any
}
