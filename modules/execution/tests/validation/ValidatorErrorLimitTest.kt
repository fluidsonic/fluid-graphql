package testing

import io.fluidsonic.graphql.GDocument
import io.fluidsonic.graphql.GFieldSelection
import io.fluidsonic.graphql.GSchema
import io.fluidsonic.graphql.ValidationContext
import io.fluidsonic.graphql.ValidationRule
import io.fluidsonic.graphql.Validator
import io.fluidsonic.graphql.Visit
import io.fluidsonic.graphql.validate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

// The error limit must stop the traversal rather than truncate the result, so the primary assertion here counts
// how often a rule is invoked — a pure `errors.take(maxErrors)` would satisfy every error count below while
// still visiting the entire document.
class ValidatorErrorLimitTest {

	private val terminalMessage = "Too many validation errors, error limit reached. Validation aborted."

	private val schema = GSchema.parse("type Query { id: ID }").valueOrThrow()

	@Test
	fun testStopsVisitingNodesOnceLimitIsReached() {
		val rule = CountingFieldSelectionRule()
		val errors = Validator(rules = listOf(rule)).validate(
			document = documentWithBadRootFields(count = 100),
			schema = schema,
			maxErrors = 3,
		)

		assertEquals(actual = errors.map { it.message }, expected = listOf("bad0", "bad1", "bad2", terminalMessage))
		assertEquals(actual = rule.visitedFieldSelectionCount, expected = 4)
	}

	@Test
	fun testDefaultLimitIsOneHundred() {
		val errors = documentWithBadRootFields(count = 250).validate(schema)

		assertEquals(actual = errors.size, expected = 101)
		assertEquals(actual = errors.last().message, expected = terminalMessage)
	}

	@Test
	fun testExplicitLimitOfOneHundredMatchesTheDefault() {
		val errors = Validator.default.validate(
			document = documentWithBadRootFields(count = 250),
			schema = schema,
			maxErrors = 100,
		)

		assertEquals(actual = errors.size, expected = 101)
		assertEquals(actual = errors.last().message, expected = terminalMessage)
	}

	@Test
	fun testLimitOfZeroReportsOnlyTheTerminalError() {
		val errors = Validator.default.validate(
			document = documentWithBadRootFields(count = 250),
			schema = schema,
			maxErrors = 0,
		)

		assertEquals(actual = errors.map { it.message }, expected = listOf(terminalMessage))
	}

	@Test
	fun testLimitAboveTheErrorCountReportsNoTerminalError() {
		val errors = Validator.default.validate(
			document = documentWithBadRootFields(count = 250),
			schema = schema,
			maxErrors = 1000,
		)

		assertEquals(actual = errors.size, expected = 250)
		assertFalse(errors.any { it.message == terminalMessage }, message = "Expected no terminal error.")
	}

	private fun documentWithBadRootFields(count: Int) = GDocument
		.parse((0 until count).joinToString(separator = " ", prefix = "{ ", postfix = " }") { "bad$it" })
		.valueOrThrow()
}

// Reports one error per field selection and records how often it was invoked, so that a test can tell a stopped
// traversal from a truncated error list.
private class CountingFieldSelectionRule : ValidationRule.Singleton() {

	var visitedFieldSelectionCount = 0
		private set

	override fun onFieldSelection(selection: GFieldSelection, data: ValidationContext, visit: Visit) {
		visitedFieldSelectionCount += 1

		data.reportError(message = selection.name, nodes = listOf(selection.nameNode))
	}
}
