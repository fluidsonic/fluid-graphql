package io.fluidsonic.graphql


internal class Lexer(
	private val source: String
) {

	var currentToken = Token(
		kind = Token.Kind.SOF,
		startPosition = 0,
		endPosition = 0,
		lineNumber = 1,
		linePosition = 0,
		previousToken = null
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
		if (token.kind !== Token.Kind.EOF) {
			do {
				token = token.nextToken ?: run {
					lookaheadLineNumber = token.lineNumber
					lookaheadLinePosition = token.linePosition
					lookaheadPosition = token.endPosition
					lookaheadToken = token

					readToken().also { nextToken ->
						println(nextToken)
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
		var rawValue = ""

		var char = readChar(position)
		while (char.isValid()) { // FIXME invalid chars
			if (char eq '"' && readChar(position + 1) eq '"' && readChar(position + 2) eq '"') {
				rawValue += source.substring(startIndex = chunkStart, endIndex = position)

				return makeToken(
					kind = Token.Kind.BLOCK_STRING,
					endPosition = position + 3,
					value = normalizeBlockString(rawValue)
				)
			}

			if (char eq '\n' || char eq '\r') {
				++position

				if (char eq '\r' && readChar(position) eq '\n')
					++position

				++lookaheadLineNumber
				lookaheadPosition = position
			}
			else if (
				char eq '\\' &&
				readChar(position + 1) eq '"' &&
				readChar(position + 2) eq '"' &&
				readChar(position + 3) eq '"'
			) {
				rawValue += source.substring(startIndex = chunkStart, endIndex = position) + "\"\"\""

				position += 4
				chunkStart = position
			}
			else
				++position

			char = readChar(position)
		}

		syntaxError(description = "Unterminated string.", position = position)
	}


	private fun readChar(position: Int) =
		if (position >= source.length)
			SourceCharacter.endOfInput
		else
			SourceCharacter(source[position].toInt())


	private fun readComment(): Token {
		var position = lookaheadPosition

		var character: SourceCharacter
		do character = readChar(++position)
		while (character.isValid() && !character.isLineBreak())

		return makeToken(
			kind = Token.Kind.COMMENT,
			endPosition = position,
			value = source.substring(startIndex = lookaheadPosition + 1, endIndex = position)
		)
	}


	private fun readDigits(position: Int): Int {
		@Suppress("NAME_SHADOWING")
		var position = position

		var character = readChar(position)
		if (character in '0' .. '9') {
			do character = readChar(++position)
			while (character in '0' .. '9')

			return position
		}

		unexpectedCharacterError(character = character, position = position)
	}


	private fun readName(): Token {
		var position = lookaheadPosition + 1

		var character = readChar(position)
		while (character.isValidForName())
			character = readChar(++position)

		return makeToken(
			kind = Token.Kind.NAME,
			endPosition = position,
			value = source.substring(startIndex = lookaheadPosition, endIndex = position)
		)
	}


	private fun readNumber(): Token {
		var position = lookaheadPosition
		var character = readChar(position)
		var isFloat = false

		if (character eq '-')
			character = readChar(++position)

		if (character eq '0') {
			character = readChar(++position)

			if (character in '0' .. '9')
				unexpectedCharacterError(character = character, position = position)
		}
		else {
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
			if (character eq '+' || character eq '-')
				++position

			position = readDigits(position = position)
			character = readChar(position)
		}

		if (character eq '.' || character.isValidForNameStart())
			unexpectedCharacterError(character = character, position = position)

		return makeToken(
			kind = if (isFloat) Token.Kind.FLOAT else Token.Kind.INT,
			endPosition = position,
			value = source.substring(startIndex = lookaheadPosition, endIndex = position)
		)
	}


	private fun readString(): Token {
		var position = lookaheadPosition + 1
		var chunkStart = position
		var value = ""

		var char = readChar(position)
		while (char.isValid() && !char.isLineBreak()) { // FIXME throw in invalid char
			if (char eq '"') {
				value += source.substring(startIndex = chunkStart, endIndex = position)

				return makeToken(
					kind = Token.Kind.STRING,
					endPosition = position + 1,
					value = value
				)
			}

			++position

			if (char eq '\\') {
				value += source.substring(startIndex = chunkStart, endIndex = position - 1)

				char = readChar(position)
				when (char.toChar()) {
					'"' -> value += "\""
					'/' -> value += "/"
					'\\' -> value += "\\"
					'b' -> value += "\b"
					'f' -> value += "\u000C"
					'n' -> value += "\n"
					'r' -> value += "\r"
					't' -> value += "\t"
					'u' -> {
						// FIXME throw if EOF
						val charCode = makeCharacterFromHex(
							readChar(position + 1).toChar(),
							readChar(position + 2).toChar(),
							readChar(position + 3).toChar(),
							readChar(position + 4).toChar()
						)

						if (charCode < 0) {
							val invalidSequence = source.substring(startIndex = position + 1, endIndex = position + 5)

							syntaxError(
								description = "Invalid character escape sequence: \\u${invalidSequence}.",
								position = position
							)
						}

						value += charCode.toChar()
						position += 4
					}
					else ->
						syntaxError(
							description = "Invalid character escape sequence: \\$char.",
							position = position
						)
				}

				++position
				chunkStart = position
			}

			char = readChar(position)
		}

		syntaxError(description = "Unterminated string.", position = position)
	}


	private fun makeToken(
		kind: Token.Kind,
		startPosition: Int = this.lookaheadPosition,
		endPosition: Int,
		value: String? = null
	) = Token(
		kind = kind,
		startPosition = startPosition,
		endPosition = endPosition,
		lineNumber = lookaheadLineNumber,
		linePosition = lookaheadLinePosition,
		previousToken = lookaheadToken,
		value = value
	)


	private fun readToken(): Token {
		skipIgnoredCharacters()

		val character = readChar(lookaheadPosition)
		if (character == SourceCharacter.endOfInput)
			return makeToken(kind = Token.Kind.EOF, endPosition = lookaheadPosition)

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

			'_', in 'A' .. 'Z', in 'a' .. 'z' ->
				readName()

			'-', in '0' .. '9' ->
				readNumber()

			'.' ->
				if (readChar(lookaheadPosition + 1) eq '.' && readChar(lookaheadPosition + 2) eq '.')
					makeToken(kind = Token.Kind.SPREAD, endPosition = lookaheadPosition + 3)
				else
					unexpectedCharacterError(character = character, position = lookaheadPosition)

			'"' ->
				if (readChar(lookaheadPosition + 1) eq '"' && readChar(lookaheadPosition + 2) eq '"')
					readBlockString()
				else
					readString()

			else ->
				unexpectedCharacterError(character = character, position = lookaheadPosition)
		}
	}


	private fun skipIgnoredCharacters() {
		var position = lookaheadPosition

		loop@ while (true)
			when (val char = readChar(position).toChar()) {
				'\t', ' ', ',', 0xFEFF.toChar() ->
					++position

				'\r', '\n' -> {
					++position
					++lookaheadLineNumber

					if (char == '\r' && readChar(position) eq '\n')
						++position

					lookaheadLinePosition = position
				}

				else ->
					break@loop
			}

		lookaheadPosition = position
	}


	companion object {

		private fun Char.parseHex() =
			when (this) {
				in '0' .. '9' -> this - '0'
				in 'a' .. 'f' -> this - 'a' + 10
				in 'A' .. 'F' -> this - 'A' + 10
				else -> -1
			}


		private fun computeBlockStringIndentation(lines: List<String>): Int {
			var commonIndentation = -1

			for (index in lines.indices) {
				val line = lines[index]

				val indentation = line.indexOfFirst { it != ' ' && it != '\t' }
				if (indentation < 0)
					continue

				if (commonIndentation < 0 || indentation < commonIndentation) {
					commonIndentation = indentation

					if (commonIndentation == 0)
						break
				}
			}

			return commonIndentation.coerceAtLeast(0)
		}


		private fun makeCharacterFromHex(a: Char, b: Char, c: Char, d: Char) =
			(a.parseHex() shl 12) or (b.parseHex() shl 8) or (c.parseHex() shl 4) or d.parseHex()


		private fun normalizeBlockString(value: String): String {
			if (value.indexOfFirst { it == '\n' || it == '\r' } < 0)
				return value

			val lines = value.lineSequence().toMutableList()

			val commonIndent = computeBlockStringIndentation(lines)
			if (commonIndent != 0)
				lines.forEachIndexed { index, line ->
					lines[index] = line.substring(startIndex = commonIndent.coerceAtMost(line.length))
				}

			while (lines.isNotEmpty() && lines.last().isBlank())
				lines.removeAt(lines.size - 1)

			while (lines.isNotEmpty() && lines.first().isBlank())
				lines.removeAt(0)

			return lines.joinToString("\n")
		}


		private fun unexpectedCharacterError(character: SourceCharacter, position: Int): Nothing = // FIXME add expected
			syntaxError(
				description = when {
					character == SourceCharacter.endOfInput -> "Unexpected end of input"
					!character.isValid() -> "Cannot contain the invalid character 0x${character.toHexString()}"
					else -> "Unexpected character '${character.toChar()}'"
				},
				position = position
			)


		private fun syntaxError(description: String, position: Int): Nothing =
			throw GError.syntax(description = description, position = position)
	}
}
