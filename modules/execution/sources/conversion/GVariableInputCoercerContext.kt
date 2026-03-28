package io.fluidsonic.graphql


/**
 * Context provided to a [GVariableInputCoercer] when coercing a variable input value.
 *
 * Extends [GInputCoercerContext] with the variable definition, the variable path, and whether
 * a value was actually provided for the variable (as opposed to it being absent).
 */
public interface GVariableInputCoercerContext : GInputCoercerContext {

	/**
	 * Whether the variable was included in the request's variable map.
	 *
	 * `false` when the variable was declared but not supplied; in that case [next] will use
	 * the default value from the variable definition.
	 */
	public val hasValue: Boolean

	/** The path identifying this variable in the variables map. */
	public val path: GPath

	/** The definition of the variable being coerced. */
	public val variableDefinition: GVariableDefinition


	/** Convenience extension to invoke another [GVariableInputCoercer] within this context. */
	public fun <Input> GVariableInputCoercer<Input>.coerceVariableInput(input: Input): Any? =
		with(this@GVariableInputCoercerContext) { coerceVariableInput(input) }
}
