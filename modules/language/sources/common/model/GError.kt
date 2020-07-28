package io.fluidsonic.graphql


// FIXME check all call-sites for whether they can provide more context than they currently do
class GError(
	message: String,
	val path: GPath? = null, // FIXME set path only for RESULT fields & consider field aliases
	val nodes: List<GNode> = emptyList(),
	val origins: List<GDocumentPosition> = emptyList(),
	val extensions: Map<String, Any?> = emptyMap(),
	cause: Throwable? = null
) : Exception( // FIXME Don't make exception. Add fn to throw it wrapped.
	message,
	cause
) {

	fun copy(
		message: String = this.message.orEmpty(),
		path: GPath? = this.path,
		nodes: List<GNode> = this.nodes,
		origins: List<GDocumentPosition> = this.origins,
		extensions: Map<String, Any?> = emptyMap(),
		cause: Throwable? = this.cause
	): GError =
		GError(
			message = message,
			path = path,
			nodes = nodes,
			origins = origins,
			cause = cause,
			extensions = extensions
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

		internal fun syntax(details: String, origin: GDocumentPosition) =
			GError(message = "Syntax Error: $details", origins = listOf(origin))
	}
}
