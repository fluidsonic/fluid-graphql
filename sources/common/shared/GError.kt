package io.fluidsonic.graphql


class GError(
	message: String,
	val nodes: List<GAst> = emptyList(),
	val origins: List<GOrigin> = nodes.ifEmpty { null }?.map { it.origin }.orEmpty()
) : Exception(
	message
) {

	fun describe() = buildString {
		append(message)

		for (node in nodes) {
			append("\n\n")
			append(node.origin.describe())
		}

		val origins = origins.filter { origin -> nodes.none { it.origin === origin } }
		for (origin in origins) {
			append("\n\n")
			append(origin.describe())
		}
	}


	override fun toString() =
		"GraphQL Error: ${describe()}"


	companion object {

		internal fun syntax(description: String, origin: GOrigin) =
			GError(message = "Syntax Error: $description", origins = listOf(origin))
	}
}
