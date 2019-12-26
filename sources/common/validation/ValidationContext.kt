package io.fluidsonic.graphql


// FIXME error limit & abortion?
internal class ValidationContext(
	document: GDocument,
	schema: GSchema
) : GAstVisitorContext(document = document, schema = schema) {

	private val _errors = mutableListOf<GError>()

	internal val errors: List<GError>
		get() = _errors.toList()


	fun reportError(
		message: String,
		nodes: List<GAst> = emptyList()
	) {
		_errors += GError(
			message = message,
			nodes = nodes
		)
	}
}
