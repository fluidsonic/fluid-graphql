package io.fluidsonic.graphql


public interface GNodeInputCoercerContext : GInputCoercerContext {

	@SchemaBuilderKeywordB // FIXME
	public fun <Input> GNodeInputCoercer<Input>.coerceNodeInput(input: Input): Any? =
		with(this@GNodeInputCoercerContext) { coerceNodeInput(input) }
}
