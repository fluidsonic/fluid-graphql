package io.fluidsonic.graphql

// FIXME call for List & null too
/**
 * Converts a GraphQL variable value (already parsed from JSON/map) into a Kotlin value.
 *
 * Used during variable coercion when input values are provided as variables rather than inline. A coercer
 * attached to a type takes precedence over the coercion the type performs itself
 * ([GScalarType.coerceInputValue]).
 *
 * Attach to a leaf type via [GLeafType.inputValueCoercer] or to an input object type via
 * [GInputObjectType.inputValueCoercer].
 */
public fun interface GInputValueCoercer<in Input> {

	/**
	 * Coerces the given variable [value] into a Kotlin value.
	 *
	 * @param value The supplied value, or — for an input object type — the map of already coerced fields.
	 * @return The coerced value, which may be `null`.
	 * @throws GErrorException To report that [value] is not valid. The message should be bare and free of
	 *   positional context; the executor enriches it with the variable the value was supplied for.
	 */
	public fun coerceInputValue(value: Input): Any?
}
