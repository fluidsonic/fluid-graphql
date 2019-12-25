package io.fluidsonic.graphql


// FIXME get rid of this
class GWriter(
	val indent: String = "\t"
) {

	private val builder = StringBuilder()
	private var currentLineIsIndented = false

	@PublishedApi
	internal var indentationLevel = 0


	fun clear() {
		builder.clear()

		currentLineIsIndented = false
		indentationLevel = 0
	}


	inline fun <R> indented(block: () -> R) =
		try {
			indentationLevel += 1
			block()
		}
		finally {
			indentationLevel -= 1
		}


	fun isEmpty() =
		builder.isEmpty()


	fun isNotEmpty() =
		builder.isNotEmpty()


	val length
		get() = builder.length


	override fun toString() =
		builder.toString()


	private fun writeIndentationIfNeeded() {
		if (currentLineIsIndented) return
		repeat(indentationLevel) { builder.append(indent) }
		currentLineIsIndented = true
	}


	fun writeLinebreak() {
		builder.append('\n')
		currentLineIsIndented = false
	}


	fun writeRaw(char: Char) {
		if (char == '\n')
			writeLinebreak()
		else {
			writeIndentationIfNeeded()
			builder.append(char)
		}
	}


	fun writeRaw(string: String) {
		var startIndex = 0
		while (startIndex < string.length) {
			val newlineIndex = string.indexOf('\n', startIndex = startIndex)
			val endIndex = if (newlineIndex >= 0) newlineIndex else string.length
			if (endIndex > startIndex) {
				writeIndentationIfNeeded()
				builder.append(string, startIndex, endIndex)
			}
			if (newlineIndex >= 0) {
				writeLinebreak()
			}

			startIndex = endIndex + 1
		}
	}


	companion object {

		inline operator fun invoke(indent: String = "\t", block: GWriter.() -> Unit) =
			GWriter(indent = indent).apply(block).toString()
	}
}
