package io.fluidsonic.graphql


// FIXME check all call-sites for whether they can provide more context than they currently do
class GError(
	message: String,
	val path: GPath? = null, // FIXME set path only for RESULT fields & consider field aliases
	val nodes: List<GNode> = emptyList(),
	val origins: List<GDocumentPosition> = emptyList(),
	cause: Throwable? = null
) : Exception(
	message,
	cause
) {

	fun copy(
		message: String = this.message.orEmpty(),
		path: GPath? = this.path,
		nodes: List<GNode> = this.nodes,
		origins: List<GDocumentPosition> = this.origins,
		cause: Throwable? = this.cause
	): GError =
		GError(
			message = message,
			path = path,
			nodes = nodes,
			origins = origins,
			cause = cause
		)


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


	override fun toString(): String =
		"GraphQL Error: ${describe()}"


	companion object {

		internal fun syntax(description: String, origin: GDocumentPosition) =
			GError(message = "Syntax Error: $description", origins = listOf(origin))
	}
}
