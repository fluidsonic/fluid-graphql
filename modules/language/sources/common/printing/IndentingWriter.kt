package io.fluidsonic.graphql


internal class IndentingWriter(
	val indent: String = "\t"
) {

	private val builder = StringBuilder()
	private var currentLineIsIndented = false
	private var indentationLevel = 0


	inline fun <Result> indented(block: () -> Result) =
		try {
			indentationLevel += 1
			block()
		}
		finally {
			indentationLevel -= 1
		}


	val length
		get() = builder.length


	override fun toString() =
		builder.toString()


	private fun writeIndentationIfNeeded() {
		if (!currentLineIsIndented) {
			repeat(indentationLevel) { builder.append(indent) }
			currentLineIsIndented = true
		}
	}


	fun writeLinebreak() {
		builder.append('\n')
		currentLineIsIndented = false
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
			if (newlineIndex >= 0)
				writeLinebreak()

			startIndex = endIndex + 1
		}
	}
}
