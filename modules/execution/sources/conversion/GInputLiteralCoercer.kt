package io.fluidsonic.graphql

// FIXME call for List & null too
/**
 * Converts an inline GraphQL input value (an AST [GValue] node) into a Kotlin value.
 *
 * Used during argument coercion when input values are provided inline in a document, as opposed to via
 * variables. A coercer attached to a type takes precedence over the coercion the type performs itself
 * ([GScalarType.coerceInputLiteral]).
 *
 * Attach to a leaf type via [GLeafType.inputLiteralCoercer] or to an input object type via
 * [GInputObjectType.inputLiteralCoercer].
 */
public fun interface GInputLiteralCoercer<in Input> {

	/**
	 * Coerces the given inline [value] into a Kotlin value.
	 *
	 * @param value The literal to coerce, or — for an input object type — the map of already coerced fields.
	 * @return The coerced value, which may be `null`.
	 * @throws GErrorException To report that [value] is not valid. The message should be bare and free of
	 *   positional context; the executor enriches it with the argument the value was written for.
	 */
	public fun coerceInputLiteral(value: Input): Any?
}
