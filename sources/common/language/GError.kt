package io.fluidsonic.graphql


// FIXME check all call-sites for whether they can provide more context than they currently do
class GError(
	message: String,
	val path: GPath? = null, // FIXME set path only for RESULT fields & consider aliases
	val nodes: List<GAst> = emptyList(),
	val origins: List<GOrigin> = emptyList(),
	cause: Throwable? = null
) : Exception(
	message,
	cause
) {

	fun copy(
		message: String = this.message.orEmpty(),
		path: GPath? = this.path,
		nodes: List<GAst> = this.nodes,
		origins: List<GOrigin> = this.origins,
		cause: Throwable? = this.cause
	) =
		GError(
			message = message,
			path = path,
			nodes = nodes,
			origins = origins,
			cause = cause
		)


	fun describe() = buildString {
		append(message)

		for (node in nodes) {
			node.origin?.let { origin ->
				append("\n\n")
				append(node.origin.describe())
			}
		}

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
