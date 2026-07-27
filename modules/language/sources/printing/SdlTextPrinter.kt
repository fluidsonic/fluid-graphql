package io.fluidsonic.graphql

/**
 * The text-level building blocks [SchemaPrinter] assembles its definitions from — blocks, descriptions and
 * values — each matching graphql-js's schema printer down to its whitespace.
 */
internal object SdlTextPrinter {

	/** The value length above which graphql-js prints a single-line block string across several lines. */
	private const val maximumSingleLineDescriptionLength = 70

	private const val tripleQuote = "\"\"\""
	private const val escapedTripleQuote = "\\\"\"\""

	/** Joins [items] into a `{ … }` block, or into nothing at all when there are none — as graphql-js does. */
	fun printBlock(items: List<String>): String = if (items.isEmpty()) "" else items.joinToString(prefix = " {\n", separator = "\n", postfix = "\n}")

	/**
	 * Prints [description] as a block string followed by a line break, indented by [indentation] and preceded
	 * by a blank line unless it is the first entry of its block ([isFirst]). Prints nothing when there is no
	 * description.
	 */
	fun printDescription(description: String?, indentation: String = "", isFirst: Boolean = true): String {
		if (description === null) {
			return ""
		}

		val prefix = if (indentation.isNotEmpty() && !isFirst) "\n$indentation" else indentation

		return prefix + printBlockString(description).replace("\n", "\n$indentation") + "\n"
	}

	/** Prints [value] as a GraphQL literal on a single line, as graphql-js prints a default value. */
	fun printValue(value: GValue): String = when (value) {
		is GListValue -> value.elements.joinToString(prefix = "[", separator = ", ", postfix = "]", transform = ::printValue)

		is GObjectValue -> value.arguments.joinToString(prefix = "{ ", separator = ", ", postfix = " }") {
			"${it.name}: ${printValue(it.value)}"
		}

		is GStringValue -> if (value.isBlock) printBlockString(value.value) else Printer.print(value)

		else -> Printer.print(value)
	}

	/**
	 * Renders [value] as a GraphQL block string, using the compact `"""…"""` form only where graphql-js's
	 * `printBlockString()` does — that is, for a short single-line value that survives the round-trip without
	 * a leading or trailing line break.
	 */
	private fun printBlockString(value: String): String {
		val escaped = value.replace(tripleQuote, escapedTripleQuote)
		val lines = escaped.split("\r\n", "\n", "\r")
		val forcesLeadingLineBreak = lines.size > 1 && lines.drop(1).all { it.isEmpty() || it.first().isInlineWhitespace() }
		val forcesTrailingLineBreak = value.endsWith("\\") || (value.endsWith("\"") && !escaped.endsWith(escapedTripleQuote))
		val printsAsMultipleLines = printsAsMultipleLines(
			value = value,
			escaped = escaped,
			lineCount = lines.size,
			forcesLeadingLineBreak = forcesLeadingLineBreak,
			forcesTrailingLineBreak = forcesTrailingLineBreak,
		)

		// A single-line value that starts with a space or tab would lose that whitespace to the block string's
		// own indentation stripping, so it must stay glued to the opening quotes.
		val startsIndented = lines.size == 1 && value.firstOrNull()?.isInlineWhitespace() == true
		val leadingLineBreak = if (forcesLeadingLineBreak || (printsAsMultipleLines && !startsIndented)) "\n" else ""
		val trailingLineBreak = if (printsAsMultipleLines || forcesTrailingLineBreak) "\n" else ""

		return tripleQuote + leadingLineBreak + escaped + trailingLineBreak + tripleQuote
	}

	private fun printsAsMultipleLines(
		value: String,
		escaped: String,
		lineCount: Int,
		forcesLeadingLineBreak: Boolean,
		forcesTrailingLineBreak: Boolean,
	): Boolean = lineCount > 1 ||
		value.length > maximumSingleLineDescriptionLength ||
		forcesLeadingLineBreak ||
		forcesTrailingLineBreak ||
		escaped.endsWith(escapedTripleQuote)

	private fun Char.isInlineWhitespace() = this == ' ' || this == '\t'
}
