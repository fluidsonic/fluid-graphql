package io.fluidsonic.graphql


class GError(
	message: String,
	val positions: List<Int> = emptyList()
) : Exception(
	buildMessage(message = message, positions = positions)
) {

	companion object {

		private fun buildMessage(message: String, positions: List<Int>) =
			when (positions.size) {
				0 -> message
				1 -> "$message, at position ${positions.first()}"
				else -> "$message, at positions ${positions.joinToString()}"
			}


		internal fun syntax(description: String, position: Int) =
			GError(message = "Syntax error: $description", positions = listOf(position))
	}
}
