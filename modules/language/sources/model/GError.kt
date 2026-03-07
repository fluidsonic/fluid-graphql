package io.fluidsonic.graphql


/**
 * A GraphQL error, matching the error format defined by the GraphQL specification.
 *
 * @property message Human-readable description of the error.
 * @property path The field path in the response where the error occurred, if applicable.
 * @property nodes AST nodes associated with this error; their [GNode.origin] is used when describing the error.
 * @property origins Explicit source locations to include in the error description.
 * @property extensions Arbitrary additional metadata attached to this error.
 */
public data class GError(
	public val message: String,
	public val path: GPath? = null,
	public val nodes: List<GNode> = emptyList(),
	public val origins: List<GDocumentPosition> = emptyList(),
	public val extensions: Map<String, Any?> = emptyMap(),
) {

	/**
	 * Returns a human-readable description of this error, including [GDocumentPosition] context
	 * with a code excerpt and caret pointer for every associated [GNode] or explicit origin.
	 */
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


	/** Throws a [GErrorException] wrapping this error. */
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
