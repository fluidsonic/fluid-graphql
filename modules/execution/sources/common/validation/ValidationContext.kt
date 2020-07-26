package io.fluidsonic.graphql


// FIXME error limit & abortion?
@Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")
internal class ValidationContext(
	document: GDocument,
	schema: GSchema
) : VisitorContext(document = document, schema = schema) {

	private val _errors = mutableListOf<GError>()

	internal val errors: List<GError>
		get() = _errors.toList()


	fun reportError(error: GError) {
		_errors += error
	}


	fun reportError(
		message: String,
		nodes: List<GNode> = emptyList()
	) =
		reportError(GError(
			message = message,
			nodes = nodes
		))
}
