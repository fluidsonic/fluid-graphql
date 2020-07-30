package io.fluidsonic.graphql


public interface GNodeInputCoercerContext {

	@SchemaBuilderKeywordB // FIXME ok?
	public val argumentDefinition: GArgumentDefinition?

	@SchemaBuilderKeywordB // FIXME
	public val execution: GExecutorContext

	@SchemaBuilderKeywordB // FIXME ok?
	public val type: GType

	@SchemaBuilderKeywordB // FIXME ok?
	public fun invalidValueError(details: String? = null): Nothing

	@SchemaBuilderKeywordB // FIXME
	public fun next(): Any?


	@SchemaBuilderKeywordB // FIXME
	public fun <Input> GNodeInputCoercer<Input>.coerceNodeInput(input: Input): Any? =
		with(this@GNodeInputCoercerContext) { coerceNodeInput(input) }
}
