package io.fluidsonic.graphql


public fun GDocument.validate(schema: GSchema): List<GError> =
	Validator.default.validate(this, schema)
