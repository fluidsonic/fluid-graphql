package io.fluidsonic.graphql


@Suppress("NON_PUBLIC_PRIMARY_CONSTRUCTOR_OF_INLINE_CLASS")
internal inline class SourceCharacter private constructor(val value: Int) {

	constructor(value: Char) :
		this(value.toInt())


	infix fun eq(other: Char) =
		value == other.toInt()


	inline fun ifInvalid(block: () -> Nothing) =
		if (isValid()) value.toChar() else block()


	fun isLineBreak() =
		this eq '\n' || this eq '\r'


	fun isValid() =
		value >= 0x20 || this eq '\t' || isLineBreak()


	fun isValidForName() =
		isValidForNameStart() || this in '0' .. '9'


	fun isValidForNameStart() =
		this eq '_' || this in 'A' .. 'Z' || this in 'a' .. 'z'


	fun toChar() = when (value) {
		endOfInputValue -> error("Cannot convert $this to Char.")
		else -> value.toChar()
	}


	override fun toString() = when (value) {
		endOfInputValue -> "<end of input>"
		else -> when (val char = value.toChar()) {
			'\'' -> "\"'\""

			in 0x21.toChar() .. 0x26.toChar(),
			in 0x28.toChar() .. 0x7E.toChar()
			-> "'$char'"

			else -> "\\u${value.toString(16).padStart(length = 4, padChar = '0')}"
		}
	}


	companion object {

		private const val endOfInputValue = Int.MIN_VALUE

		val endOfInput = SourceCharacter(endOfInputValue)
	}
}


internal operator fun CharRange.contains(sourceCharacter: SourceCharacter) =
	sourceCharacter.value >= first.toInt() && sourceCharacter.value <= last.toInt()
