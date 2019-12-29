package io.fluidsonic.graphql


internal class Validator(
	private val document: GDocument,
	private val rules: List<ValidationRule>,
	schema: GSchema
) {

	private val context = ValidationContext(
		document = document,
		schema = schema
	)

	private val visitor = rules
		.map(::ValidationRuleVisitor)
		.parallelize()
		.contextualize()


	fun validate() {
		document.accept(visitor, data = context)

		for (rule in rules)
			rule.beforeTraversal(context)
	}
}
