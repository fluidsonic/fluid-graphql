package io.fluidsonic.graphql

/**
 * Validates this document against [schema] using the default set of validation rules.
 *
 * Returns a list of [GError]s describing any violations found, or an empty list if the document is valid.
 *
 * Every returned error has [GError.isRequestError] set, because a document that fails validation is never executed.
 */
public fun GDocument.validate(schema: GSchema): List<GError> = Validator.default.validate(this, schema)
