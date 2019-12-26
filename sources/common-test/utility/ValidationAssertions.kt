package tests

import io.fluidsonic.graphql.*
import kotlin.test.*


@Suppress("NAME_SHADOWING")
internal fun assertValidationRule(
	rule: ValidationRule,
	errors: List<String>,
	document: String,
	schema: String
) {
	val document = GDocument.parse(document.trimMargin())
	val schema = GSchema.parse(schema.trimMargin())!!

	val context = ValidationContext(
		document = document,
		schema = schema
	)

	document.accept(
		visitor = ValidationRuleVisitor(rule).contextualize(context),
		data = context
	)

	assertEquals(
		expected = errors.map { it.trimIndent() },
		actual = context.errors.map { it.describe() },
		message = "Validation errors for rule ${rule::class.simpleName}"
	)
}
