package io.fluidsonic.graphql


internal fun invalidOperationError(
	document: GDocument,
	schema: GSchema,
	message: String,
	errors: List<GError> = emptyList()
): Nothing =
	error(buildString {
		append("The operation is invalid: $message\n")
		append("Validate each operation before executing it.\n\n")

		if (errors.isNotEmpty()) {
			append("Errors:\n")
			append(errors.joinToString("\n\n"))
			append("\n\n")
		}

		append("Document:\n$document\n\n")

		append("Schema:\n$schema")
	})
