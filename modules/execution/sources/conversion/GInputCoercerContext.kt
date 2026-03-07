package io.fluidsonic.graphql


/**
 * Base context for input coercers, shared by [GNodeInputCoercerContext] and [GVariableInputCoercerContext].
 *
 * Provides the expected GraphQL type, the argument definition (if applicable), a way to report
 * invalid input via [invalid], and access to the next coercer in the chain via [next].
 */
public interface GInputCoercerContext : GExecutorContext.Child {

	/** The argument definition for the value being coerced, or `null` if not within an argument. */
	@SchemaBuilderKeywordB // FIXME ok?
	public val argumentDefinition: GArgumentDefinition?

	/** The expected GraphQL type for the value being coerced. */
	@SchemaBuilderKeywordB // FIXME ok?
	public val type: GType // FIXME make all generic?

	/**
	 * Reports that the input value is invalid and aborts coercion.
	 *
	 * @param details Optional human-readable description of why the value is invalid.
	 */
	@SchemaBuilderKeywordB // FIXME ok?
	public fun invalid(details: String? = null): Nothing

	/**
	 * Delegates to the next input coercer in the chain.
	 *
	 * Returns `null` if there is no further coercer or the next coercer returns `null`.
	 */
	@SchemaBuilderKeywordB // FIXME
	public fun next(): Any?
}
