package io.fluidsonic.graphql

internal class Lexer(val source: GDocumentSource.Parsable, private val maxTokens: Int? = null) {

	private val content = source.content
	private var tokenCount = 0

	var currentToken = Token(
		kind = Token.Kind.START_OF_INPUT,
		startPosition = 0,
		endPosition = 0,
		lineNumber = 1,
		linePosition = 0,
		previousToken = null,
	)
		private set

	var previousToken = currentToken
		private set

	private var lookaheadLineNumber = currentToken.lineNumber
	private var lookaheadLinePosition = currentToken.linePosition
	private var lookaheadPosition = currentToken.endPosition
	private var lookaheadToken = currentToken

	fun advance(): Token {
		previousToken = currentToken
		currentToken = lookahead()

		return currentToken
	}

	fun lookahead(): Token {
		var token = currentToken
		if (token.kind !== Token.Kind.END_OF_INPUT) {
			do {
				token = token.nextToken ?: run {
					lookaheadLineNumber = token.lineNumber
					lookaheadLinePosition = token.linePosition
					lookaheadPosition = token.endPosition
					lookaheadToken = token

					readCountedToken().also { nextToken ->
						token.nextToken = nextToken
					}
				}
			}
			while (token.kind === Token.Kind.COMMENT)
		}

		return token
	}

	private fun readBlockString(): Token {
		var position = lookaheadPosition + 3
		var chunkStart = position
		val rawValue = StringBuilder()

		var char = readChar(position)
		while (char.isValid()) { // FIXME handle invalid chars
			if (char eq '"' && readChar(position + 1) eq '"' && readChar(position + 2) eq '"') {
				rawValue.appendRange(content, startIndex = chunkStart, endIndex = position)

				return makeToken(
					kind = Token.Kind.BLOCK_STRING,
					endPosition = position + 3,
					value = normalizeBlockString(rawValue.toString()),
				)
			}

			if (char eq '\n' || char eq '\r') {
				++position

				if (char eq '\r' && readChar(position) eq '\n') {
					++position
				}

				++lookaheadLineNumber
				lookaheadLinePosition = position
			} else if (
				char eq '\\' &&
				readChar(position + 1) eq '"' &&
				readChar(position + 2) eq '"' &&
				readChar(position + 3) eq '"'
			) {
				rawValue.appendRange(content, startIndex = chunkStart, endIndex = position).append("\"\"\"")

				position += 4
				chunkStart = position
			} else {
				++position
			}

			char = readChar(position)
		}

		syntaxError(description = "Unterminated string.", position = position)
	}

	private fun readChar(position: Int) = if (position >= content.length) {
		SourceCharacter.endOfInput
	} else {
		SourceCharacter(content[position])
	}

	private fun readComment(): Token {
		var position = lookaheadPosition

		var character: SourceCharacter
		do {
			character = readChar(++position)
		} while (character.isValid() && !character.isLineBreak())

		return makeToken(
			kind = Token.Kind.COMMENT,
			endPosition = position,
			value = content.substring(startIndex = lookaheadPosition + 1, endIndex = position),
		)
	}

	private fun readDigits(position: Int): Int {
		@Suppress("NAME_SHADOWING")
		var position = position

		var character = readChar(position)
		if (character in '0'..'9') {
			do {
				character = readChar(++position)
			} while (character in '0'..'9')

			return position
		}

		unexpectedCharacterError(character = character, position = position)
	}

	private fun readName(): Token {
		var position = lookaheadPosition + 1

		var character = readChar(position)
		while (character.isValidForName()) {
			character = readChar(++position)
		}

		return makeToken(
			kind = Token.Kind.NAME,
			endPosition = position,
			value = content.substring(startIndex = lookaheadPosition, endIndex = position),
		)
	}

	private fun readNumber(): Token {
		var position = lookaheadPosition
		var character = readChar(position)
		var isFloat = false

		if (character eq '-') {
			character = readChar(++position)
		}

		if (character eq '0') {
			character = readChar(++position)

			if (character in '0'..'9') {
				unexpectedCharacterError(character = character, position = position)
			}
		} else {
			position = readDigits(position = position)
			character = readChar(position)
		}

		if (character eq '.') {
			isFloat = true
			++position

			position = readDigits(position = position)
			character = readChar(position)
		}

		if (character eq 'E' || character eq 'e') {
			isFloat = true

			character = readChar(++position)
			if (character eq '+' || character eq '-') {
				++position
			}

			position = readDigits(position = position)
			character = readChar(position)
		}

		if (character eq '.' || character.isValidForNameStart()) {
			unexpectedCharacterError(character = character, position = position)
		}

		return makeToken(
			kind = if (isFloat) Token.Kind.FLOAT else Token.Kind.INT,
			endPosition = position,
			value = content.substring(startIndex = lookaheadPosition, endIndex = position),
		)
	}

	private fun readString(): Token {
		var position = lookaheadPosition + 1
		var chunkStart = position
		val value = StringBuilder()

		var char = readChar(position)
		while (char.isValid() && !char.isLineBreak()) { // FIXME throw if invalid char
			if (char eq '"') {
				value.appendRange(content, startIndex = chunkStart, endIndex = position)

				return makeToken(
					kind = Token.Kind.STRING,
					endPosition = position + 1,
					value = value.toString(),
				)
			}

			if (char eq '\\') {
				value.appendRange(content, startIndex = chunkStart, endIndex = position)

				position = readEscapeSequence(position = position, value = value)
				chunkStart = position
			} else {
				++position
			}

			char = readChar(position)
		}

		syntaxError(description = "Unterminated string.", position = position)
	}

	/**
	 * Reads the *EscapedCharacter* or *EscapedUnicode* sequence whose backslash sits at [position], appends the
	 * character it denotes to [value] and returns the position just past the sequence.
	 *
	 * Fails with a syntax error if the sequence is unknown or truncated by the end of input.
	 *
	 * https://spec.graphql.org/draft/#sec-String-Value
	 */
	private fun readEscapeSequence(position: Int, value: StringBuilder): Int {
		val escaped = readChar(position + 1)
		if (escaped == SourceCharacter.endOfInput) {
			invalidCharacterEscapeError(position = position, size = 1)
		}

		val unescaped = when (escaped.toChar()) {
			'"' -> '"'
			'\\' -> '\\'
			'/' -> '/'
			'b' -> '\b'
			'f' -> '\u000C'
			'n' -> '\n'
			'r' -> '\r'
			't' -> '\t'
			'u' -> return readEscapedUnicode(position = position, value = value)
			else -> invalidCharacterEscapeError(position = position, size = 2)
		}

		value.append(unescaped)

		return position + 2
	}

	/**
	 * Reads the *EscapedUnicode* sequence whose backslash sits at [position], dispatching between the braced
	 * variable-width form `\u{…}` and the fixed-width form `\uXXXX`.
	 */
	private fun readEscapedUnicode(position: Int, value: StringBuilder): Int = if (readChar(position + 2) eq '{') {
		readEscapedUnicodeVariableWidth(position = position, value = value)
	} else {
		readEscapedUnicodeFixedWidth(position = position, value = value)
	}

	/**
	 * Reads the braced *EscapedUnicode* form `\u{…}` whose backslash sits at [position].
	 *
	 * The braced form denotes a single *Unicode scalar value* directly, so — unlike the fixed-width form — it can
	 * never take part in a *SurrogatePair*: a braced escape denoting a lone surrogate is always a syntax error.
	 *
	 * https://spec.graphql.org/draft/#sec-String-Value
	 */
	private fun readEscapedUnicodeVariableWidth(position: Int, value: StringBuilder): Int {
		var codePoint = 0
		var size = bracedEscapePrefixLength

		while (size < bracedEscapeMaximumLength) {
			val char = readChar(position + size++)
			if (char eq '}') {
				if (size >= bracedEscapeMinimumLength && isUnicodeScalarValue(codePoint)) {
					value.appendCodePoint(codePoint)

					return position + size
				}

				break
			}

			// A non-hex digit — including the end of input — yields -1 and therefore a negative accumulator, as does
			// an accumulator overflowing 32 bits.
			codePoint = (codePoint shl 4) or char.parseHexDigit()
			if (codePoint < 0) {
				break
			}
		}

		invalidUnicodeEscapeError(position = position, size = size)
	}

	/**
	 * Reads the fixed-width *EscapedUnicode* form `\uXXXX` whose backslash sits at [position], including the
	 * *SurrogatePair* production that joins a leading and a trailing surrogate escape into one code point.
	 *
	 * Both halves of a pair must use the fixed-width form; a braced escape is never accepted as either half.
	 *
	 * https://spec.graphql.org/draft/#sec-String-Value
	 */
	private fun readEscapedUnicodeFixedWidth(position: Int, value: StringBuilder): Int {
		val code = read16BitHexCode(position + 2)

		if (isUnicodeScalarValue(code)) {
			value.append(code.toChar())

			return position + fixedWidthEscapeLength
		}

		val secondEscapePosition = position + fixedWidthEscapeLength
		if (isLeadingSurrogate(code) && readChar(secondEscapePosition) eq '\\' && readChar(secondEscapePosition + 1) eq 'u') {
			val trailingCode = read16BitHexCode(secondEscapePosition + 2)
			if (isTrailingSurrogate(trailingCode)) {
				value.append(code.toChar()).append(trailingCode.toChar())

				return position + surrogatePairEscapeLength
			}
		}

		invalidUnicodeEscapeError(position = position, size = fixedWidthEscapeLength)
	}

	/** Reads exactly four hex digits starting at [position], or returns `-1` if any of them is not a hex digit. */
	private fun read16BitHexCode(position: Int): Int {
		var code = 0

		for (offset in 0 until fixedWidthHexDigitCount) {
			val digit = readChar(position + offset).parseHexDigit()
			if (digit < 0) {
				return -1
			}

			code = (code shl hexDigitBits) or digit
		}

		return code
	}

	private fun invalidCharacterEscapeError(position: Int, size: Int): Nothing = syntaxError(
		description = "Invalid character escape sequence: \"${escapeSequenceText(position = position, size = size)}\".",
		position = position,
	)

	private fun invalidUnicodeEscapeError(position: Int, size: Int): Nothing = syntaxError(
		description = "Invalid Unicode escape sequence: \"${escapeSequenceText(position = position, size = size)}\".",
		position = position,
	)

	/** Returns the up to [size] source characters starting at [position], clamped to the end of the document. */
	private fun escapeSequenceText(position: Int, size: Int) = content.substring(startIndex = position, endIndex = (position + size).coerceAtMost(content.length))

	private fun makeToken(kind: Token.Kind, startPosition: Int = this.lookaheadPosition, endPosition: Int, value: String? = null) = Token(
		kind = kind,
		startPosition = startPosition,
		endPosition = endPosition,
		lineNumber = lookaheadLineNumber,
		linePosition = lookaheadLinePosition,
		previousToken = lookaheadToken,
		value = value,
	)

	/**
	 * Reads the next token and charges it against [maxTokens].
	 *
	 * Ignored characters — whitespace, commas and comments — never reach the counter because they either produce no
	 * token at all or produce a [Token.Kind.COMMENT] that [lookahead] skips. The end of input is not counted either,
	 * so a document of exactly `maxTokens` tokens still parses.
	 */
	private fun readCountedToken(): Token {
		val token = readToken()

		if (maxTokens != null && token.kind !== Token.Kind.COMMENT && token.kind !== Token.Kind.END_OF_INPUT) {
			tokenCount += 1

			if (tokenCount > maxTokens) {
				syntaxError(
					description = "Document contains more than $maxTokens tokens. Parsing aborted.",
					position = token.startPosition,
				)
			}
		}

		return token
	}

	private fun readToken(): Token {
		skipIgnoredCharacters()

		val character = readChar(lookaheadPosition)
		if (character == SourceCharacter.endOfInput) {
			return makeToken(kind = Token.Kind.END_OF_INPUT, endPosition = lookaheadPosition)
		}

		return when (character.toChar()) {
			'!' -> makeToken(kind = Token.Kind.BANG, endPosition = lookaheadPosition + 1)
			'$' -> makeToken(kind = Token.Kind.DOLLAR, endPosition = lookaheadPosition + 1)
			'&' -> makeToken(kind = Token.Kind.AMP, endPosition = lookaheadPosition + 1)
			'(' -> makeToken(kind = Token.Kind.PAREN_L, endPosition = lookaheadPosition + 1)
			')' -> makeToken(kind = Token.Kind.PAREN_R, endPosition = lookaheadPosition + 1)
			':' -> makeToken(kind = Token.Kind.COLON, endPosition = lookaheadPosition + 1)
			'=' -> makeToken(kind = Token.Kind.EQUALS, endPosition = lookaheadPosition + 1)
			'@' -> makeToken(kind = Token.Kind.AT, endPosition = lookaheadPosition + 1)
			'[' -> makeToken(kind = Token.Kind.BRACKET_L, endPosition = lookaheadPosition + 1)
			']' -> makeToken(kind = Token.Kind.BRACKET_R, endPosition = lookaheadPosition + 1)
			'{' -> makeToken(kind = Token.Kind.BRACE_L, endPosition = lookaheadPosition + 1)
			'|' -> makeToken(kind = Token.Kind.PIPE, endPosition = lookaheadPosition + 1)
			'}' -> makeToken(kind = Token.Kind.BRACE_R, endPosition = lookaheadPosition + 1)
			'#' -> readComment()

			'_', in 'A'..'Z', in 'a'..'z' ->
				readName()

			'-', in '0'..'9' ->
				readNumber()

			'.' ->
				if (readChar(lookaheadPosition + 1) eq '.' && readChar(lookaheadPosition + 2) eq '.') {
					makeToken(kind = Token.Kind.SPREAD, endPosition = lookaheadPosition + 3)
				} else {
					unexpectedCharacterError(character = character, position = lookaheadPosition)
				}

			'"' ->
				if (readChar(lookaheadPosition + 1) eq '"' && readChar(lookaheadPosition + 2) eq '"') {
					readBlockString()
				} else {
					readString()
				}

			else ->
				unexpectedCharacterError(character = character, position = lookaheadPosition)
		}
	}

	private fun skipIgnoredCharacters() {
		var position = lookaheadPosition

		loop@ while (true) {
			when (val sourceCharacter = readChar(position)) {
				SourceCharacter.endOfInput ->
					break@loop

				else -> when (val char = sourceCharacter.toChar()) {
					'\t', ' ', ',', 0xFEFF.toChar() ->
						++position

					'\r', '\n' -> {
						++position
						++lookaheadLineNumber

						if (char == '\r' && readChar(position) eq '\n') {
							++position
						}

						lookaheadLinePosition = position
					}

					else ->
						break@loop
				}
			}
		}

		lookaheadPosition = position
	}

	private fun syntaxError(description: String, position: Int): Nothing = GError.syntax(
		details = description,
		origin = DocumentPosition(
			column = position - lookaheadLinePosition + 1,
			line = lookaheadLineNumber,
			position = position,
			source = source,
		),
	).throwException()

	private fun unexpectedCharacterError(character: SourceCharacter, position: Int): Nothing = // FIXME add expected
		syntaxError(
			description = when {
				character.isValid() -> "Unexpected character $character."
				else -> "Character $character is not allowed in a GraphQL document."
			},
			position = position,
		)

	companion object {

		private const val maximumCodePoint = 0x10FFFF
		private const val maximumBasicPlaneCodePoint = 0xFFFF
		private const val supplementaryPlaneStart = 0x10000
		private const val leadingSurrogateStart = 0xD800
		private const val leadingSurrogateEnd = 0xDBFF
		private const val trailingSurrogateStart = 0xDC00
		private const val trailingSurrogateEnd = 0xDFFF
		private const val surrogateHalfBits = 10
		private const val surrogateHalfMask = 0x3FF
		private const val hexDigitBits = 4

		/** Number of hex digits in the fixed-width `\uXXXX` form. */
		private const val fixedWidthHexDigitCount = 4

		/** Length of `\uXXXX`. */
		private const val fixedWidthEscapeLength = 6

		/** Length of `\uXXXX\uXXXX`. */
		private const val surrogatePairEscapeLength = 12

		/** Length of the `\u{` prefix. */
		private const val bracedEscapePrefixLength = 3

		/** Length of the shortest well-formed braced escape, `\u{0}`. */
		private const val bracedEscapeMinimumLength = 5

		/** Length of the longest braced escape worth reading, `\u{00000000}`. */
		private const val bracedEscapeMaximumLength = 12

		private fun SourceCharacter.parseHexDigit() = when {
			this in '0'..'9' -> value - '0'.code
			this in 'a'..'f' -> value - 'a'.code + 10
			this in 'A'..'F' -> value - 'A'.code + 10
			else -> -1
		}

		/** Whether [codePoint] is a *Unicode scalar value*, i.e. in range but not one half of a surrogate pair. */
		private fun isUnicodeScalarValue(codePoint: Int) = codePoint in 0 until leadingSurrogateStart || codePoint in (trailingSurrogateEnd + 1)..maximumCodePoint

		private fun isLeadingSurrogate(codePoint: Int) = codePoint in leadingSurrogateStart..leadingSurrogateEnd

		private fun isTrailingSurrogate(codePoint: Int) = codePoint in trailingSurrogateStart..trailingSurrogateEnd

		/** Appends [codePoint] to this builder, encoding it as a surrogate pair if it lies outside the basic plane. */
		private fun StringBuilder.appendCodePoint(codePoint: Int) {
			if (codePoint <= maximumBasicPlaneCodePoint) {
				append(codePoint.toChar())
			} else {
				val offset = codePoint - supplementaryPlaneStart

				append((leadingSurrogateStart + (offset shr surrogateHalfBits)).toChar())
				append((trailingSurrogateStart + (offset and surrogateHalfMask)).toChar())
			}
		}

		private fun computeBlockStringIndentation(lines: List<String>): Int {
			var commonIndentation = -1

			for (index in lines.indices) {
				val line = lines[index]

				val indentation = line.indexOfFirst { it != ' ' && it != '\t' }
				if (indentation < 0) {
					continue
				}

				if (commonIndentation < 0 || indentation < commonIndentation) {
					commonIndentation = indentation

					if (commonIndentation == 0) {
						break
					}
				}
			}

			return commonIndentation.coerceAtLeast(0)
		}

		private fun normalizeBlockString(value: String): String {
			if (value.indexOfFirst { it == '\n' || it == '\r' } < 0) {
				return value.trimStart { it == ' ' || it == '\t' }
			}

			val lines = value.lineSequence().toMutableList()

			val commonIndent = computeBlockStringIndentation(lines)
			if (commonIndent != 0) {
				lines.forEachIndexed { index, line ->
					lines[index] = line.substring(startIndex = commonIndent.coerceAtMost(line.length))
				}
			}

			while (lines.isNotEmpty() && lines.last().isBlank()) {
				lines.removeAt(lines.size - 1)
			}

			while (lines.isNotEmpty() && lines.first().isBlank()) {
				lines.removeAt(0)
			}

			return lines.joinToString("\n")
		}
	}

	private class DocumentPosition(override val column: Int, override val line: Int, val position: Int, override val source: GDocumentSource.Parsable) :
		GDocumentPosition {

		override val startPosition
			get() = position
	}
}
