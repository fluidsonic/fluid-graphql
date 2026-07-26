package io.fluidsonic.graphql

// FIXME error limit & abortion?
internal class ValidationContext(document: GDocument, schema: GSchema) :
	VisitorContext(
		document = document,
		schema = schema,
		fieldDefinition = schema::fieldDefinition,
	) {

	private val _errors = mutableListOf<GError>()

	internal val errors: List<GError>
		get() = _errors.toList()

	// A document that fails validation is never executed, so every violation reported here is a
	// request error: the response must omit the "data" key entirely.
	fun reportError(error: GError) {
		_errors += error.copy(isRequestError = true)
	}

	fun reportError(message: String, nodes: List<GNode> = emptyList()) = reportError(
		GError(
			message = message,
			nodes = nodes,
		),
	)
}
