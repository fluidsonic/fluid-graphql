package io.fluidsonic.graphql


public interface GVariableInputCoercerContext {

	@SchemaBuilderKeywordB // FIXME ok?
	public val argumentDefinition: GArgumentDefinition?

	@SchemaBuilderKeywordB // FIXME ok?
	public val execution: GExecutorContext

	@SchemaBuilderKeywordB // FIXME ok?
	public val hasValue: Boolean

	@SchemaBuilderKeywordB // FIXME ok?
	public val type: GType // FIXME make all generic?

	@SchemaBuilderKeywordB // FIXME ok?
	public val variableDefinition: GVariableDefinition

	@SchemaBuilderKeywordB // FIXME ok?
	public fun invalidValueError(details: String? = null): Nothing

	@SchemaBuilderKeywordB // FIXME
	public fun next(): Any?


	@SchemaBuilderKeywordB // FIXME
	public fun <Input> GVariableInputCoercer<Input>.coerceVariableInput(input: Input): Any? =
		with(this@GVariableInputCoercerContext) { coerceVariableInput(input) }
}
