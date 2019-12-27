package tests

import io.fluidsonic.graphql.*
import kotlin.test.*


// We print errors as Kotlin raw strings so they can easily be copied to test code if needed.
@Suppress("NAME_SHADOWING")
fun assertError(expected: String, actual: GError, message: String? = null) {
	val expected = expected.trimIndent()
	val actual = actual.describe()

	if (actual != expected)
		assertEquals(
			message = buildString {
				if (message !== null) {
					append(message)
					append(": ")
				}

				append("Error is different than expected.")
			},
			expected = expected.toKotlinRawString("\t\t\t\t"),
			actual = actual.toKotlinRawString("\t\t\t\t")
		)

	// FIXME remove
	/*
	buildString {
				if (message !== null) {
					append(message)
					append(": ")
				}

				append("Error is different than expected.")
				append("\n\n--- EXPECTED ---\n")
				append(expected)
				append("\n\n--- ACTUAL ---\n")
				append(actual)
				append("\n\n")
			}
	 */
}


// We print errors as Kotlin raw strings so they can easily be copied to test code if needed.
fun assertErrors(expected: List<String>, actual: List<GError>) {
	if (expected.size == actual.size)
		expected.forEachIndexed { index, error ->
			assertError(expected = error, actual = actual[index], message = "#${index + 1}")
		}
	else {
		asserter.fail(buildString {
			if (actual.isEmpty())
				append("Expected ${expected.size} error(s) but got none.")
			else {
				if (expected.isNotEmpty())
					append("Expected ${expected.size} error(s) but got ${actual.size}.")
				else
					append("Expected success but got ${actual.size} errors.")

				append("\n\n")

				actual.forEachIndexed { index, error ->
					if (index >= 1)
						append(",\n")

					append(error.describe().toKotlinRawString("\t\t\t\t"))
				}

				append("\n\n")
			}
		})
	}
}


@Suppress("NAME_SHADOWING")
internal fun assertValidationRule(
	rule: ValidationRule,
	errors: List<String>,
	document: String,
	schema: String? = null
) =
	if (schema !== null)
		assertValidationRule(
			rule = rule,
			errors = errors,
			document = GDocument.parse(document.trimMargin()),
			schema = GSchema.parse(schema.trimMargin())!!
		)
	else
		assertValidationRule(
			rule = rule,
			errors = errors,
			document = GDocument.parse(document.trimMargin())
		)


@Suppress("NAME_SHADOWING")
internal fun assertValidationRule(
	rule: ValidationRule,
	errors: List<String>,
	document: GDocument,
	schema: GSchema = document.schema ?: GSchema.parse("schema {}")!!
) {
	val context = ValidationContext(
		document = document,
		schema = schema
	)

	document.accept(
		visitor = ValidationRuleVisitor(rule).contextualize(context),
		data = context
	)

	rule.beforeTraversal(context)

	assertErrors(
		expected = errors,
		actual = context.errors
	)
}
