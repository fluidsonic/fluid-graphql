package io.fluidsonic.graphql

import kotlin.math.floor

/** Renders a value for a coercion failure message: strings are quoted, everything else uses `toString()`. */
private fun describeValue(value: Any?): String = when (value) {
	is String -> "\"$value\""
	else -> value.toString()
}

/**
 * The built-in GraphQL `Boolean` scalar type.
 *
 * Instantiate one per schema rather than sharing a single instance; see [GScalarType] for why.
 *
 * @param extensions Metadata attached to this instance only, e.g. a coercer replacing the coercion below.
 */
// https://graphql.github.io/graphql-spec/draft/#sec-Boolean.Input-Coercion
public class GBooleanType(extensions: GNodeExtensionSet<GBooleanType> = GNodeExtensionSet.empty()) : GScalarType(name = "Boolean", extensions = extensions) {

	override fun coerceOutputValue(value: Any): Any = when (value) {
		is Boolean -> value
		is Byte -> value != 0.toByte()
		is Short -> value != 0.toShort()
		is Int -> value != 0
		is Long -> value != 0L
		is UByte -> value != 0.toUByte()
		is UShort -> value != 0.toUShort()
		is UInt -> value != 0u
		is ULong -> value != 0uL
		is Float -> fromNumber(value.toDouble(), described = value)
		is Double -> fromNumber(value, described = value)
		else -> fail(value)
	}

	override fun coerceInputValue(value: Any): Any? = value as? Boolean ?: fail(value)

	override val coerceInputLiteral: (value: GValue) -> Any? = { literal ->
		(literal as? GBooleanValue)?.value ?: fail(literal)
	}

	private fun fail(value: Any?): Nothing = GError(message = "Boolean cannot represent a non boolean value: ${describeValue(value)}").throwException()

	private fun fromNumber(value: Double, described: Any?): Boolean {
		if (!value.isFinite()) {
			fail(described)
		}

		return value != 0.0
	}
}

/**
 * The built-in GraphQL `Float` scalar type.
 *
 * Instantiate one per schema rather than sharing a single instance; see [GScalarType] for why.
 *
 * @param extensions Metadata attached to this instance only, e.g. a coercer replacing the coercion below.
 */
// https://graphql.github.io/graphql-spec/draft/#sec-Float.Input-Coercion
public class GFloatType(extensions: GNodeExtensionSet<GFloatType> = GNodeExtensionSet.empty()) : GScalarType(name = "Float", extensions = extensions) {

	override fun coerceOutputValue(value: Any): Any = when (value) {
		is Boolean -> if (value) 1.0 else 0.0
		is String -> fromNumber(jsNumber(value) ?: fail(value), described = value)
		else -> fromValue(value)
	}

	override fun coerceInputValue(value: Any): Any? = fromValue(value)

	// A `GFloatValue` is finite by construction, so the non-finite literal graphql-js accepts here cannot
	// even be represented — fluid rejects non-finite values on all three paths.
	override val coerceInputLiteral: (value: GValue) -> Any? = { literal ->
		when (literal) {
			is GFloatValue -> literal.value
			is GIntValue -> literal.value.toDouble()
			else -> fail(literal)
		}
	}

	private fun fail(value: Any?): Nothing = GError(message = "Float cannot represent non numeric value: ${describeValue(value)}").throwException()

	private fun fromValue(value: Any): Double = when (value) {
		is Byte -> value.toDouble()
		is Short -> value.toDouble()
		is Int -> value.toDouble()
		is Long -> value.toDouble()
		is UByte -> value.toDouble()
		is UShort -> value.toDouble()
		is UInt -> value.toDouble()
		is ULong -> value.toDouble()
		is Float -> fromNumber(value.toDouble(), described = value)
		is Double -> fromNumber(value, described = value)
		else -> fail(value)
	}

	private fun fromNumber(value: Double, described: Any?): Double {
		if (!value.isFinite()) {
			fail(described)
		}

		return value
	}
}

private val integerLikeString = Regex("""^-?(?:0|[1-9][0-9]*)$""")

/**
 * The built-in GraphQL `ID` scalar type.
 *
 * Instantiate one per schema rather than sharing a single instance; see [GScalarType] for why.
 *
 * @param extensions Metadata attached to this instance only, e.g. a coercer replacing the coercion below.
 */
// https://graphql.github.io/graphql-spec/draft/#sec-ID.Input-Coercion
public class GIdType(extensions: GNodeExtensionSet<GIdType> = GNodeExtensionSet.empty()) : GScalarType(name = "ID", extensions = extensions) {

	override fun coerceOutputValue(value: Any): Any = fromValue(value)

	override fun coerceInputValue(value: Any): Any? = fromValue(value)

	// The literal path uses a second message, distinct from the one the value paths use.
	override val coerceInputLiteral: (value: GValue) -> Any? = { literal ->
		when (literal) {
			is GStringValue -> literal.value
			is GIntValue -> literal.value.toString()
			else -> GError(
				message = "ID cannot represent a non-string and non-integer value: ${describeValue(literal)}",
			).throwException()
		}
	}

	override fun valueToLiteral(value: Any?): GValue? = when (value) {
		is String -> literalOf(value)
		is Byte -> GIntValue(value.toInt())
		is Short -> GIntValue(value.toInt())
		is Int -> GIntValue(value)
		is UByte -> GIntValue(value.toInt())
		is UShort -> GIntValue(value.toInt())
		is Long -> literalOf(value.toString())
		is UInt -> literalOf(value.toString())
		is ULong -> literalOf(value.toString())
		is Float -> literalOf(fromNumber(value.toDouble(), described = value))
		is Double -> literalOf(fromNumber(value, described = value))
		else -> null
	}

	private fun fail(value: Any?): Nothing = GError(message = "ID cannot represent value: ${describeValue(value)}").throwException()

	private fun fromValue(value: Any): String = when (value) {
		is String -> value
		is Byte -> value.toString()
		is Short -> value.toString()
		is Int -> value.toString()
		is Long -> value.toString()
		is UByte -> value.toString()
		is UShort -> value.toString()
		is UInt -> value.toString()
		is ULong -> value.toString()
		is Float -> fromNumber(value.toDouble(), described = value)
		is Double -> fromNumber(value, described = value)
		else -> fail(value)
	}

	// The range check is not redundant with the integrality check: `toLong()` saturates at `Long.MIN_VALUE`
	// and `Long.MAX_VALUE`, so without it an integral `1e30` would silently become a *different* identifier.
	// graphql-js renders such a value as "1e+30"; Kotlin's `toString` would spell it "1.0E30", and an ID that
	// two systems spell differently is worse than one that is refused. Deliberate divergence.
	private fun fromNumber(value: Double, described: Any?): String {
		if (!value.isFinite() || value != floor(value)) {
			fail(described)
		}
		if (value < Long.MIN_VALUE.toDouble() || value > Long.MAX_VALUE.toDouble()) {
			fail(described)
		}

		return value.toLong().toString()
	}

	// An integer-looking string becomes an `IntValue`, matching graphql-js — a genuine round-trip hazard
	// for identifiers such as "1". A value too large for `GIntValue`'s `Int` stays a `StringValue`.
	private fun literalOf(value: String): GValue = when {
		integerLikeString.matches(value) -> value.toIntOrNull()?.let(::GIntValue) ?: GStringValue(value)
		else -> GStringValue(value)
	}
}

/**
 * The built-in GraphQL `Int` scalar type.
 *
 * Instantiate one per schema rather than sharing a single instance; see [GScalarType] for why.
 *
 * @param extensions Metadata attached to this instance only, e.g. a coercer replacing the coercion below.
 */
// https://graphql.github.io/graphql-spec/draft/#sec-Int.Input-Coercion
public class GIntType(extensions: GNodeExtensionSet<GIntType> = GNodeExtensionSet.empty()) : GScalarType(name = "Int", extensions = extensions) {

	override fun coerceOutputValue(value: Any): Any = when (value) {
		is Boolean -> if (value) 1 else 0
		is String -> fromNumber(jsNumber(value) ?: failNonInteger(value), described = value)
		else -> fromValue(value)
	}

	override fun coerceInputValue(value: Any): Any? = fromValue(value)

	// `GIntValue` already holds an `Int`, so the 32-bit range check graphql-js performs here is implicit.
	override val coerceInputLiteral: (value: GValue) -> Any? = { literal ->
		(literal as? GIntValue)?.value ?: failNonInteger(literal)
	}

	private fun failNonInteger(value: Any?): Nothing = GError(message = "Int cannot represent non-integer value: ${describeValue(value)}").throwException()

	private fun failOutOfRange(value: Any?): Nothing =
		GError(message = "Int cannot represent non 32-bit signed integer value: ${describeValue(value)}").throwException()

	private fun fromValue(value: Any): Int = when (value) {
		is Byte -> value.toInt()
		is Short -> value.toInt()
		is Int -> value
		is Long -> if (value in Int.MIN_VALUE.toLong()..Int.MAX_VALUE.toLong()) value.toInt() else failOutOfRange(value)
		is UByte -> value.toInt()
		is UShort -> value.toInt()
		is UInt -> if (value <= Int.MAX_VALUE.toUInt()) value.toInt() else failOutOfRange(value)
		is ULong -> if (value <= Int.MAX_VALUE.toULong()) value.toInt() else failOutOfRange(value)
		is Float -> fromNumber(value.toDouble(), described = value)
		is Double -> fromNumber(value, described = value)
		else -> failNonInteger(value)
	}

	private fun fromNumber(value: Double, described: Any?): Int {
		if (!value.isFinite() || value != floor(value)) {
			failNonInteger(described)
		}
		if (value < Int.MIN_VALUE.toDouble() || value > Int.MAX_VALUE.toDouble()) {
			failOutOfRange(described)
		}

		return value.toInt()
	}
}

/**
 * The built-in GraphQL `String` scalar type.
 *
 * Instantiate one per schema rather than sharing a single instance; see [GScalarType] for why.
 *
 * @param extensions Metadata attached to this instance only, e.g. a coercer replacing the coercion below.
 */
// https://graphql.github.io/graphql-spec/draft/#sec-String.Input-Coercion
public class GStringType(extensions: GNodeExtensionSet<GStringType> = GNodeExtensionSet.empty()) : GScalarType(name = "String", extensions = extensions) {

	override fun coerceOutputValue(value: Any): Any = when (value) {
		is String -> value
		is Boolean -> value.toString()
		is Byte, is Short, is Int, is Long, is UByte, is UShort, is UInt, is ULong -> value.toString()
		is Float -> if (value.isFinite()) value.toString() else failOutput(value)
		is Double -> if (value.isFinite()) value.toString() else failOutput(value)
		else -> failOutput(value)
	}

	override fun coerceInputValue(value: Any): Any? = value as? String ?: failInput(value)

	override val coerceInputLiteral: (value: GValue) -> Any? = { literal ->
		(literal as? GStringValue)?.value ?: failInput(literal)
	}

	// The two directions deliberately use different wording, mirroring graphql-js.
	private fun failOutput(value: Any?): Nothing = GError(message = "String cannot represent value: ${describeValue(value)}").throwException()

	private fun failInput(value: Any?): Nothing = GError(message = "String cannot represent a non string value: ${describeValue(value)}").throwException()
}
