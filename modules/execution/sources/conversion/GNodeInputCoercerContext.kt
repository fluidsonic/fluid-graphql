package io.fluidsonic.graphql


/**
 * Context provided to a [GNodeInputCoercer] when coercing an inline input value (AST node).
 *
 * Extends [GInputCoercerContext] with the response path of the field selection that triggered
 * coercion, and a convenience extension for invoking another coercer within the same context.
 */
public interface GNodeInputCoercerContext : GInputCoercerContext {

	/** The response path of the field selection that triggered this coercion, or `null` if not within a field. */
	@SchemaBuilderKeywordB
	public val fieldSelectionPath: GPath?


	@SchemaBuilderKeywordB // FIXME
	public fun <Input> GNodeInputCoercer<Input>.coerceNodeInput(input: Input): Any? =
		with(this@GNodeInputCoercerContext) { coerceNodeInput(input) }
}
