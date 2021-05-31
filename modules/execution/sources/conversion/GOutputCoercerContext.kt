package io.fluidsonic.graphql


public interface GOutputCoercerContext {

	@SchemaBuilderKeywordB // FIXME ok?
	public val execution: GExecutorContext

	@SchemaBuilderKeywordB // FIXME ok?
	public val fieldDefinition: GFieldDefinition

	@SchemaBuilderKeywordB // FIXME ok?
	public val parentType: GObjectType

	@SchemaBuilderKeywordB // FIXME ok?
	public val type: GType

	@SchemaBuilderKeywordB // FIXME ok?
	public fun invalid(details: String? = null): Nothing // FIXME do we need this on the output side?

	@SchemaBuilderKeywordB // FIXME
	public fun next(): Any // TODO Should this be nullable?


	@SchemaBuilderKeywordB // FIXME
	public fun <Output : Any> GOutputCoercer<Output>.coerceOutput(output: Output): Any? =
		with(this@GOutputCoercerContext) { coerceOutput(output) }
}
