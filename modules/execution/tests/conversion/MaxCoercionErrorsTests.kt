package testing

import io.fluidsonic.graphql.GExecutor
import io.fluidsonic.graphql.GSchema
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

// Step 12 — variable coercion stops after a fixed number of errors.
//
// Ported from graphql-js `ExecutionArgs.options.maxCoercionErrors`, which is marked @internal and
// defaults to 50, so no knob is exposed here.
//
// Verified against graphql@17.0.2 with `[Int]` and N bad list items:
//   N=49 -> 49 errors, N=50 -> 50 errors, N=51 -> 51 errors, N=120 -> 51 errors, N=200 -> 51 errors
//   errors[50].message === "Too many errors processing variables, error limit reached. Execution aborted."
// Proven independent of maxErrors: 200 bad items give 101 errors through validate but 51 here.
class MaxCoercionErrorsTests {

	private val schema = GSchema.parse("type Query { foo(arg: [Int]): String }").valueOrThrow()

	private suspend fun coercionErrorCount(badItemCount: Int): List<String> {
		val executor = GExecutor.default(schema = schema)
		val result = executor.execute(
			"query (\$v: [Int]) { foo(arg: \$v) }",
			variableValues = mapOf("v" to List(badItemCount) { "nope" }),
		)

		return result.errors.map { it.message }
	}

	@Test
	fun testEveryBadListItemProducesItsOwnError() = runTest {
		assertEquals(actual = coercionErrorCount(10).size, expected = 10)
	}

	@Test
	fun testErrorCountIsUncappedBelowTheLimit() = runTest {
		assertEquals(actual = coercionErrorCount(49).size, expected = 49)
		assertEquals(actual = coercionErrorCount(50).size, expected = 50)
	}

	@Test
	fun testErrorCountIsCappedAtTheLimitPlusTerminalEntry() = runTest {
		assertEquals(actual = coercionErrorCount(51).size, expected = 51)
		assertEquals(actual = coercionErrorCount(120).size, expected = 51)
		assertEquals(actual = coercionErrorCount(200).size, expected = 51)
	}

	@Test
	fun testTerminalEntryHasTheUpstreamMessage() = runTest {
		val messages = coercionErrorCount(120)

		assertEquals(
			actual = messages.last(),
			expected = "Too many errors processing variables, error limit reached. Execution aborted.",
		)
	}

	@Test
	fun testEntriesBeforeTheTerminalOneDescribeTheBadItems() = runTest {
		val messages = coercionErrorCount(120)

		assertEquals(actual = messages.size, expected = 51)
		for (message in messages.dropLast(1)) {
			assertEquals(
				actual = message.startsWith("String value is not valid for"),
				expected = true,
				message = "unexpected coercion error message: $message",
			)
		}
	}
}
