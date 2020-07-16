package io.fluidsonic.graphql


// FIXME add proper toString()
internal inline class SourceCharacter(private val value: Int) {

	constructor(value: Char) :
		this(value.toInt())


	infix fun eq(other: Char) =
		value == other.toInt()


	fun isIn(range: CharRange) =
		value >= range.first.toInt() && value <= range.last.toInt()


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


	fun toChar() =
		if (value != endOfInputValue) value.toChar() else 0.toChar()


	fun toHexString() =
		value.toString(16)


	companion object {

		private const val endOfInputValue = Int.MIN_VALUE

		val endOfInput = SourceCharacter(endOfInputValue)
	}
}


internal operator fun CharRange.contains(sourceCharacter: SourceCharacter) =
	sourceCharacter.isIn(this)
