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
	@SchemaBuilderKeywordB // FIXME ok?
	public val hasValue: Boolean

	/** The path identifying this variable in the variables map. */
	@SchemaBuilderKeywordB
	public val path: GPath

	@SchemaBuilderKeywordB // FIXME ok?
	public val variableDefinition: GVariableDefinition


	@SchemaBuilderKeywordB // FIXME
	public fun <Input> GVariableInputCoercer<Input>.coerceVariableInput(input: Input): Any? =
		with(this@GVariableInputCoercerContext) { coerceVariableInput(input) }
}
