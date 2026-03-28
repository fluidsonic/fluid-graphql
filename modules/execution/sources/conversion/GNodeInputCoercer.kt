package io.fluidsonic.graphql


// FIXME call for List & null too
/**
 * Converts an inline GraphQL input value (an AST [GValue] node) into a Kotlin value.
 *
 * Used during argument coercion when input values are provided inline in a document
 * (as opposed to via variables). The [GNodeInputCoercerContext] provides the expected type,
 * argument definition, and access to the next coercer in the chain.
 *
 * Attach to a scalar or input object type via [GLeafType.nodeInputCoercer] or
 * [GInputObjectType.nodeInputCoercer], or provide a fallback via [GExecutor.default].
 */
public fun interface GNodeInputCoercer<in Input> {

	/** Coerces the given inline [input] value into a Kotlin value. */
	public fun GNodeInputCoercerContext.coerceNodeInput(input: Input): Any?
}
