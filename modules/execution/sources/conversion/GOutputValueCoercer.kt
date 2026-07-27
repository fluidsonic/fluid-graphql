package io.fluidsonic.graphql

/**
 * Converts a Kotlin value returned by a field resolver into a GraphQL-serializable value.
 *
 * A coercer attached to a type takes precedence over the coercion the type performs itself
 * ([GScalarType.coerceOutputValue]).
 *
 * Attach to a leaf type via [GLeafType.outputValueCoercer] or to an object type via
 * [GObjectType.outputValueCoercer].
 */
public fun interface GOutputValueCoercer<in Output : Any> {

	/**
	 * Coerces the given resolved [value] into a GraphQL-serializable form.
	 *
	 * @param value The resolved value, or — for an object type — the map of already serialized fields.
	 * @return The serializable representation of [value]. Never `null`.
	 * @throws GErrorException To report that [value] is not valid. The message should be bare and free of
	 *   positional context; the executor enriches it with the field the value was resolved for.
	 */
	public fun coerceOutputValue(value: Output): Any
}
