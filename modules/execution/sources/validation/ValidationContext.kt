package io.fluidsonic.graphql

/**
 * Unwinds the validation traversal once the error limit has been reached.
 *
 * Deliberately not a [GErrorException]: rules and `GSchema.validateValue` raise that for genuine validation
 * failures, so catching it here would swallow real errors.
 */
private class ValidationErrorLimitReachedException : RuntimeException("Validation error limit reached.")

private val validationErrorLimitReachedError = GError(
	message = "Too many validation errors, error limit reached. Validation aborted.",
	isRequestError = true,
)

/**
 * Collects the errors reported by validation rules, at most [maxErrors] of them.
 *
 * @param maxErrors How many errors may be reported before validation is abandoned. Must not be negative.
 * Reporting one error beyond that limit stops the traversal — see [collectingErrors].
 */
internal class ValidationContext(document: GDocument, schema: GSchema, private val maxErrors: Int) :
	VisitorContext(
		document = document,
		schema = schema,
		fieldDefinition = schema::fieldDefinition,
	) {

	init {
		require(maxErrors >= 0) { "'maxErrors' must not be negative." }
	}

	private val _errors = mutableListOf<GError>()

	internal val errors: List<GError>
		get() = _errors.toList()

	/**
	 * Runs [validate] and returns the errors reported during it.
	 *
	 * If [validate] reaches the error limit it is abandoned mid-traversal and a terminal error is appended to the
	 * result. That error is appended rather than reported, so it cannot itself exceed the limit.
	 *
	 * The catch lives here rather than in [Validator] because Kotlin's `private` is file-scoped and the exception
	 * that signals the limit must not be visible beyond this file. The exception carries no information beyond the
	 * signal itself, hence the caught value is named as unused.
	 */
	fun collectingErrors(validate: () -> Unit): List<GError> = try {
		validate()

		errors
	} catch (ignored: ValidationErrorLimitReachedException) {
		errors + validationErrorLimitReachedError
	}

	/**
	 * Reports [error] as a request error.
	 *
	 * A document that fails validation is never executed, so every violation reported here is a request error: the
	 * response must omit the "data" key entirely.
	 *
	 * Aborts the surrounding [collectingErrors] once [maxErrors] errors have already been reported, so that no
	 * further validation work is done.
	 */
	fun reportError(error: GError) {
		if (_errors.size >= maxErrors) {
			throw ValidationErrorLimitReachedException()
		}

		_errors += error.copy(isRequestError = true)
	}

	fun reportError(message: String, nodes: List<GNode> = emptyList()) = reportError(
		GError(
			message = message,
			nodes = nodes,
		),
	)
}
