package testing

import io.fluidsonic.graphql.GDocument
import io.fluidsonic.graphql.GFieldSelection
import io.fluidsonic.graphql.GOperationDefinition
import io.fluidsonic.graphql.GStringValue
import kotlin.test.Test
import kotlin.test.assertEquals

// Lexer-level conformance with https://spec.graphql.org/draft/#sec-String-Value
// Behaviour pinned against graphql-js 17.0.2.
class LexerTests {

	// -- Gate AA: Unicode escapes, both syntaxes named --

	@Test
	fun testUnicodeEscape_bracedFixedAndLiteralFormsAllYieldSameCharacter() {
		val emoji = "\uD83D\uDE00"

		assertEquals(actual = parseStringArgument("\"\\u{1F600}\""), expected = emoji)
		assertEquals(actual = parseStringArgument("\"\\uD83D\\uDE00\""), expected = emoji)
		assertEquals(actual = parseStringArgument("\"$emoji\""), expected = emoji)
	}

	@Test
	fun testUnicodeEscape_bracedFormAcceptsAsciiAndLeadingZeros() {
		assertEquals(actual = parseStringArgument("\"\\u{41}\""), expected = "A")
		assertEquals(actual = parseStringArgument("\"\\u{0041}\""), expected = "A")
		assertEquals(actual = parseStringArgument("\"\\u{00000041}\""), expected = "A")
		assertEquals(actual = parseStringArgument("\"\\u{1f600}\""), expected = "\uD83D\uDE00")
		assertEquals(actual = parseStringArgument("\"\\u{10FFFF}\""), expected = "\uDBFF\uDFFF")
	}

	@Test
	fun testUnicodeEscape_rejectsCodePointAboveMaximum() {
		assertEquals(
			actual = parseError("{ f(x: \"\\u{110000}\") }"),
			expected = "Syntax Error: Invalid Unicode escape sequence: \"\\u{110000}\".",
		)
	}

	@Test
	fun testUnicodeEscape_rejectsEmptyBraces() {
		assertEquals(
			actual = parseError("{ f(x: \"\\u{}\") }"),
			expected = "Syntax Error: Invalid Unicode escape sequence: \"\\u{}\".",
		)
	}

	@Test
	fun testUnicodeEscape_rejectsOverlongHexRun() {
		assertEquals(
			actual = parseError("{ f(x: \"\\u{FFFFFFFFFFFF}\") }"),
			expected = "Syntax Error: Invalid Unicode escape sequence: \"\\u{FFFFFFFF\".",
		)
	}

	@Test
	fun testUnicodeEscape_rejectsNonHexDigits() {
		assertEquals(
			actual = parseError("{ f(x: \"\\u{ZZ}\") }"),
			expected = "Syntax Error: Invalid Unicode escape sequence: \"\\u{Z\".",
		)
	}

	@Test
	fun testUnicodeEscape_rejectsUnterminatedBraces() {
		assertEquals(
			actual = parseError("{ f(x: \"\\u{41\") }"),
			expected = "Syntax Error: Invalid Unicode escape sequence: \"\\u{41\"\".",
		)
	}

	@Test
	fun testUnicodeEscape_rejectsLoneSurrogateInFixedForm() {
		assertEquals(
			actual = parseError("{ f(x: \"\\uD83D\") }"),
			expected = "Syntax Error: Invalid Unicode escape sequence: \"\\uD83D\".",
		)
	}

	@Test
	fun testUnicodeEscape_rejectsLoneSurrogateInBracedForm() {
		assertEquals(
			actual = parseError("{ f(x: \"\\u{D83D}\") }"),
			expected = "Syntax Error: Invalid Unicode escape sequence: \"\\u{D83D}\".",
		)
	}

	@Test
	fun testUnicodeEscape_rejectsTrailingSurrogateWithoutLeadingSurrogate() {
		assertEquals(
			actual = parseError("{ f(x: \"\\uDE00\\uD83D\") }"),
			expected = "Syntax Error: Invalid Unicode escape sequence: \"\\uDE00\".",
		)
	}

	// The SurrogatePair production is defined over the fixed-width form only, so a pair may
	// never be assembled from a braced half. All three mixed spellings must be rejected.
	@Test
	fun testUnicodeEscape_rejectsSurrogatePairingInvolvingBracedForm() {
		assertEquals(
			actual = parseError("{ f(x: \"\\u{D83D}\\u{DE00}\") }"),
			expected = "Syntax Error: Invalid Unicode escape sequence: \"\\u{D83D}\".",
		)
		assertEquals(
			actual = parseError("{ f(x: \"\\u{D83D}\\uDE00\") }"),
			expected = "Syntax Error: Invalid Unicode escape sequence: \"\\u{D83D}\".",
		)
		assertEquals(
			actual = parseError("{ f(x: \"\\uD83D\\u{DE00}\") }"),
			expected = "Syntax Error: Invalid Unicode escape sequence: \"\\uD83D\".",
		)
	}

	@Test
	fun testBlockString_doesNotProcessEscapeSequences() {
		val value = parseStringArgument("\"\"\"\\u{1F600}\"\"\"")

		assertEquals(actual = value, expected = "\\u{1F600}")
		assertEquals(actual = value.length, expected = 9)
	}

	// -- Step 10a: truncated escapes are syntax errors, not crashes --

	@Test
	fun testStringEscape_rejectsTruncatedBackslashAtEndOfInput() {
		assertEquals(
			actual = parseError("{ f(x: \"\\"),
			expected = "Syntax Error: Invalid character escape sequence: \"\\\".",
		)
	}

	@Test
	fun testUnicodeEscape_rejectsTruncatedSequencesAtEndOfInput() {
		assertEquals(
			actual = parseError("{ f(x: \"\\u"),
			expected = "Syntax Error: Invalid Unicode escape sequence: \"\\u\".",
		)
		assertEquals(
			actual = parseError("{ f(x: \"\\u{"),
			expected = "Syntax Error: Invalid Unicode escape sequence: \"\\u{\".",
		)
		assertEquals(
			actual = parseError("{ f(x: \"\\u{41"),
			expected = "Syntax Error: Invalid Unicode escape sequence: \"\\u{41\".",
		)
		assertEquals(
			actual = parseError("{ f(x: \"\\uD8"),
			expected = "Syntax Error: Invalid Unicode escape sequence: \"\\uD8\".",
		)
	}

	@Test
	fun testStringEscape_rejectsUnknownEscapeCharacter() {
		assertEquals(
			actual = parseError("{ f(x: \"\\q\") }"),
			expected = "Syntax Error: Invalid character escape sequence: \"\\q\".",
		)
	}

	// -- Step 10c / gate B: a non-finite float is a syntax error rather than a crash --

	@Test
	fun testFloatValue_rejectsMagnitudeExceedingDoublePrecisionRange() {
		assertEquals(
			actual = parseError("{ f(x: 1e400) }"),
			expected = "Syntax Error: Invalid Float value '1e400'.",
		)
		assertEquals(
			actual = parseError("{ f(x: -1e400) }"),
			expected = "Syntax Error: Invalid Float value '-1e400'.",
		)
	}

	// -- Gate AB: token limit --

	@Test
	fun testMaxTokens_abortsOnTheTokenAfterTheLimit() {
		// `{ f }` is exactly three tokens: `{`, `f`, `}`.
		assertEquals(actual = parseErrorWithTokenLimit("{ f }", maxTokens = 3), expected = null)
		assertEquals(
			actual = parseErrorWithTokenLimit("{ f }", maxTokens = 2),
			expected = "Syntax Error: Document contains more than 2 tokens. Parsing aborted.",
		)
	}

	@Test
	fun testMaxTokens_zeroAbortsImmediately() {
		assertEquals(
			actual = parseErrorWithTokenLimit("{ f }", maxTokens = 0),
			expected = "Syntax Error: Document contains more than 0 tokens. Parsing aborted.",
		)
	}

	@Test
	fun testMaxTokens_doesNotCountWhitespaceCommasCommentsOrEndOfInput() {
		// Still exactly the same three tokens as `{ f }`.
		val content = "# leading\n{ f }  ,,, \n# trailing"

		assertEquals(actual = parseErrorWithTokenLimit(content, maxTokens = 3), expected = null)
		assertEquals(
			actual = parseErrorWithTokenLimit(content, maxTokens = 2),
			expected = "Syntax Error: Document contains more than 2 tokens. Parsing aborted.",
		)
	}

	@Test
	fun testMaxTokens_countsBlockStringAsSingleToken() {
		// `{`, `f`, `(`, `x`, `:`, the block string, `)` and `}` — eight tokens.
		val content = "{ f(x: \"\"\"a\nb\"\"\") }"

		assertEquals(actual = parseErrorWithTokenLimit(content, maxTokens = 8), expected = null)
		assertEquals(
			actual = parseErrorWithTokenLimit(content, maxTokens = 7),
			expected = "Syntax Error: Document contains more than 7 tokens. Parsing aborted.",
		)
	}

	@Test
	fun testMaxTokens_unsetPermitsLargeDocument() {
		val content = (1..5_000).joinToString(separator = " ", prefix = "{ ", postfix = " }") { "f$it" }
		val document = GDocument.parse(content).valueWithoutErrorsOrThrow()

		assertEquals(actual = document.definitions.size, expected = 1)
	}

	// -- Helpers --

	private fun parseError(content: String): String = GDocument.parse(content).errors.single().message

	private fun parseErrorWithTokenLimit(content: String, maxTokens: Int): String? = GDocument.parse(content, maxTokens = maxTokens).errors.firstOrNull()?.message

	private fun parseStringArgument(literal: String): String = GDocument.parse("{ f(x: $literal) }")
		.valueWithoutErrorsOrThrow()
		.definitions.single()
		.let { it as GOperationDefinition }
		.selectionSet.selections.single()
		.let { it as GFieldSelection }
		.arguments.single()
		.value
		.let { it as GStringValue }
		.value
}
