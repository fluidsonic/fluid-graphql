package io.fluidsonic.graphql


public interface GVariableInputCoercerContext : GInputCoercerContext {

	@SchemaBuilderKeywordB // FIXME ok?
	public val hasValue: Boolean

	@SchemaBuilderKeywordB
	public val path: GPath

	@SchemaBuilderKeywordB // FIXME ok?
	public val variableDefinition: GVariableDefinition


	@SchemaBuilderKeywordB // FIXME
	public fun <Input> GVariableInputCoercer<Input>.coerceVariableInput(input: Input): Any? =
		with(this@GVariableInputCoercerContext) { coerceVariableInput(input) }
}
