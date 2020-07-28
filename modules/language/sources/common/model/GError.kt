package io.fluidsonic.graphql


class GError(
	val message: String,
	val path: GPath? = null,
	val nodes: List<GNode> = emptyList(),
	val origins: List<GDocumentPosition> = emptyList(),
	val extensions: Map<String, Any?> = emptyMap()
) {

	fun describe(): String = buildString {
		append(message)

		for (node in nodes) {
			val origin = node.origin ?: continue

			append("\n\n")
			append(origin.describe())
		}

		for (origin in origins) {
			append("\n\n")
			append(origin.describe())
		}
	}


	fun throwException(): Nothing {
		throw GErrorException(this)
	}


	override fun toString(): String =
		"GraphQL Error: ${describe()}"


	companion object {

		internal fun syntax(details: String, origin: GDocumentPosition) =
			GError(message = "Syntax Error: $details", origins = listOf(origin))
	}
}
