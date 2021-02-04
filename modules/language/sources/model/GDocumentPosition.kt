package io.fluidsonic.graphql


public interface GDocumentPosition {

	public val column: Int
		get() = -1


	public val endPosition: Int
		get() = startPosition


	public val line: Int
		get() = -1


	public val source: GDocumentSource


	public val startPosition: Int
		get() = -1


	public fun describe(): String = buildString {
		append(source.name)

		val lineNumber = line
		val columnNumber = column

		if (lineNumber > 0) {
			append(":")
			append(lineNumber)

			if (columnNumber > 0) {
				append(":")
				append(columnNumber)
			}
		}

		val content = source.content
		if (content != null && lineNumber > 0 && columnNumber > 0) {
			val lines = content.lines()

			val relevantLine = lines.getOrNull(lineNumber - 1)
			if (relevantLine != null) {
				if (relevantLine.length > 120) {
					val sublineIndex = ((columnNumber - 1) / 80)
					val sublineColumnNumber = columnNumber % 80;
					val sublines = relevantLine.windowed(80)

					val relevantSubline = sublines.getOrNull(sublineIndex)
					if (relevantSubline != null) {
						appendPrefixedLines(
							"$lineNumber" to sublines[0],
							*sublines
								.subList(1, (sublineIndex + 1).coerceAtMost(sublines.lastIndex + 1))
								.map { "" to it }
								.toTypedArray(),
							"" to (" ".repeat(sublineColumnNumber - 1) + "^"), // FIXME account for leading tabs in previous line
							"" to lines.getOrNull(sublineIndex + 1)
						)
					}
				}
				else
					appendPrefixedLines(
						"${lineNumber - 1}" to lines.getOrNull(lineNumber - 2),
						"$lineNumber" to relevantLine,
						"" to (" ".repeat(columnNumber - 1) + "^"), // FIXME account for leading tabs in previous line
						"${lineNumber + 1}" to lines.getOrNull(lineNumber)
					)
			}
		}
	}


	public companion object {

		private fun StringBuilder.appendPrefixedLines(vararg lines: Pair<String, String?>) {
			@Suppress("NAME_SHADOWING", "UNCHECKED_CAST")
			val lines = lines.filter { it.second != null } as List<Pair<String, String>>
			val prefixLength = lines.fold(0) { length, (prefix) -> length.coerceAtLeast(prefix.length) }

			for ((prefix, line) in lines) {
				append("\n")

				repeat(prefixLength - prefix.length) {
					append(" ")
				}

				append(prefix)
				append(" | ")
				append(line)
			}
		}
	}
}
