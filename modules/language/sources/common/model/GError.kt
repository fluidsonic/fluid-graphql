package io.fluidsonic.graphql


public class GError(
	public val message: String,
	public val path: GPath? = null,
	public val nodes: List<GNode> = emptyList(),
	public val origins: List<GDocumentPosition> = emptyList(),
	public val extensions: Map<String, Any?> = emptyMap()
) {

	public fun describe(): String = buildString {
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


	public fun throwException(): Nothing {
		throw GErrorException(this)
	}


	override fun toString(): String =
		"GraphQL Error: ${describe()}"


	public companion object {

		internal fun syntax(details: String, origin: GDocumentPosition) =
			GError(message = "Syntax Error: $details", origins = listOf(origin))
	}
}
