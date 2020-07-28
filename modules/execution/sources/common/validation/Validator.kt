package io.fluidsonic.graphql


internal class Validator(
	private val document: GDocument,
	rules: List<ValidationRule.Provider>,
	schema: GSchema
) {

	private val context = ValidationContext(
		document = document,
		schema = schema
	)

	private val visitor = rules
		.map(ValidationRule.Provider::provide)
		.parallelize()
		.contextualize(context)


	fun validate() {
		document.accept(visitor)
	}
}
