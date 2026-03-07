package io.fluidsonic.graphql


// FIXME call for List & null too
/**
 * Converts a GraphQL variable value (already parsed from JSON/map) into a Kotlin value.
 *
 * Used during variable coercion when input values are provided as variables rather than inline.
 * The [GVariableInputCoercerContext] provides the variable definition, expected type, and
 * whether a value was actually supplied for the variable.
 *
 * Attach to a scalar or input object type via [GLeafType.variableInputCoercer] or
 * [GInputObjectType.variableInputCoercer], or provide a fallback via [GExecutor.default].
 */
public fun interface GVariableInputCoercer<in Input> {

	public fun GVariableInputCoercerContext.coerceVariableInput(input: Input): Any?
}
