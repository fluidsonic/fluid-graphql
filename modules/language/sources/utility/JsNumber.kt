package io.fluidsonic.graphql

/**
 * Matches the decimal literals JavaScript's `Number(string)` accepts.
 *
 * The `Infinity` forms are deliberately absent: `Number` accepts them but no GraphQL scalar may represent
 * a non-finite value.
 */
private val jsDecimalLiteral = Regex("""^[+-]?(?:\d+(?:\.\d*)?|\.\d+)(?:[eE][+-]?\d+)?$""")

private const val BINARY_RADIX = 2
private const val OCTAL_RADIX = 8
private const val HEXADECIMAL_RADIX = 16

/** The radix a `0x`, `0o` or `0b` prefix selects, or `null` if [text] carries no such prefix. */
private fun radixPrefixOf(text: String): Int? = when {
	text.length <= 2 || text[0] != '0' -> null
	else -> when (text[1]) {
		'b', 'B' -> BINARY_RADIX
		'o', 'O' -> OCTAL_RADIX
		'x', 'X' -> HEXADECIMAL_RADIX
		else -> null
	}
}

/**
 * Parses [value] the way JavaScript's `Number(string)` does, or returns `null` where GraphQL rejects the
 * result.
 *
 * This is a deliberate parity port of `graphql-js`, which coerces a `String` output value for `Int` and
 * `Float` with `Number(value)` — not with a strict parse and not with a trim-then-parse. The differences
 * matter and are load-bearing:
 *
 * - an empty string is rejected, but a blank one yields `0.0`, because the upstream guard is `value !== ''`
 *   rather than a trim;
 * - the `0x`, `0o` and `0b` radix prefixes are accepted (`"0x10"` is `16.0`);
 * - a leading `+` and a trailing `.` are accepted (`"+5"`, `"5."`);
 * - `"Infinity"` and `"-Infinity"` are rejected even though `Number` accepts them.
 *
 * A radix-prefixed literal wider than 64 bits is rejected rather than rounded, which is the one place this
 * port is narrower than `Number`.
 *
 * @param value The string to parse.
 * @return The parsed number, or `null` if the string is not one this port accepts.
 */
internal fun jsNumber(value: String): Double? {
	val text = value.trim()

	return when {
		value.isEmpty() -> null
		text.isEmpty() -> 0.0
		else -> radixPrefixOf(text)?.let { radix -> text.substring(startIndex = 2).toULongOrNull(radix)?.toDouble() }
			?: text.takeIf(jsDecimalLiteral::matches)?.toDouble()
	}
}
