package io.fluidsonic.graphql


/**
 * Context provided to a [GOutputCoercer] when serializing a field resolver's return value.
 *
 * Provides the field definition, parent type, response path, and the expected output type.
 * Call [next] to delegate to the next output coercer in the chain.
 */
public interface GOutputCoercerContext : GExecutorContext.Child {

	@SchemaBuilderKeywordB // FIXME ok?
	public val fieldDefinition: GFieldDefinition

	@SchemaBuilderKeywordB // FIXME ok?
	public val parentType: GObjectType

	/** The response path to the field currently being coerced. */
	@SchemaBuilderKeywordB
	public val path: GPath

	/** The expected GraphQL output type for the value being coerced. */
	@SchemaBuilderKeywordB // FIXME ok?
	public val type: GType

	/**
	 * Reports that the output value is invalid and aborts coercion.
	 *
	 * @param details Optional human-readable description of why the value is invalid.
	 */
	@SchemaBuilderKeywordB // FIXME ok?
	public fun invalid(details: String? = null): Nothing // FIXME do we need this on the output side?

	/** Delegates to the next output coercer in the chain. */
	@SchemaBuilderKeywordB // FIXME
	public fun next(): Any // TODO Should this be nullable?


	@SchemaBuilderKeywordB // FIXME
	public fun <Output : Any> GOutputCoercer<Output>.coerceOutput(output: Output): Any? =
		with(this@GOutputCoercerContext) { coerceOutput(output) }
}
