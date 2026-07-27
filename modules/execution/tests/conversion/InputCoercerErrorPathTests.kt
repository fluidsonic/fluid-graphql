/*
 * Whether an input coercer's error carries a response `path` must follow from *which* coercer failed, not
 * from how it signalled the failure.
 *
 * The two input coercers hold different kinds of path. A literal coercer's is a response field path, so it
 * belongs on the error. A variable coercer's names a variable, and a variable failure is a request error
 * whose response has no data for a path to index into — so it must not appear.
 *
 * Verified against graphql@17.0.2 via `execute()` (its non-validating entry point):
 *   literal argument rejected  -> data {"f": null, "g": "fine"}, path ["f"]
 *   variable rejected          -> data absent, no path
 */
package testing

import io.fluidsonic.graphql.GDocument
import io.fluidsonic.graphql.GError
import io.fluidsonic.graphql.GExecutor
import io.fluidsonic.graphql.GSchema
import io.fluidsonic.graphql.GraphQL
import io.fluidsonic.graphql.coerceInputLiteral
import io.fluidsonic.graphql.coerceInputValue
import io.fluidsonic.graphql.resolve
import io.fluidsonic.graphql.schema
import io.fluidsonic.graphql.type
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull

private class RejectedByCoercer : RuntimeException("rejected")

/** Rejects everything, raising [GErrorException] on the literal path and a plain exception on the value path. */
private fun schemaRejectingEverything(): GSchema = GraphQL.schema {
	val Custom by type

	Scalar(Custom) {
		coerceInputLiteral { GError(message = "rejected").throwException() }
		coerceInputValue { throw RejectedByCoercer() }
	}

	Query {
		field("f" of String) {
			argument("a" of Custom)
			resolve { "ok" }
		}
		field("g" of String) { resolve { "fine" } }
	}
}

@Suppress("UNCHECKED_CAST")
private fun singleError(result: Map<String, Any?>): Map<String, Any?> {
	val errors = assertNotNull(result["errors"] as? List<*>, "expected errors but got: $result")

	return errors.single() as Map<String, Any?>
}

class InputCoercerErrorPathTests {

	// The literal coercer's path IS a response field path, so the error must carry it — regardless of the
	// coercer having signalled with a `GErrorException` rather than an arbitrary throwable.
	@Test
	fun literalCoercerError_carriesTheResponseFieldPath() = runTest {
		val executor = GExecutor.default(schema = schemaRejectingEverything())
		val document = GDocument.parse("{ f(a: 1) g }").valueOrThrow()
		val error = singleError(executor.serializeResult(executor.execute(document)))

		assertEquals(actual = error["path"], expected = listOf("f"))
	}

	// The variable coercer's path names a variable, not a response field. A variable failure is a request
	// error — the response carries no data — so there is nothing for a path to point into.
	@Test
	fun variableCoercerError_carriesNoPath() = runTest {
		val executor = GExecutor.default(
			schema = schemaRejectingEverything(),
			exceptionHandler = { GError(message = "rejected") },
		)
		val document = GDocument.parse("query(\$v: Custom) { f(a: \$v) g }").valueOrThrow()
		val result = executor.serializeResult(executor.execute(document, variableValues = mapOf("v" to "x")))
		val error = singleError(result)

		assertFalse(result.containsKey("data"), "a variable failure is a request error, so 'data' is absent: $result")
		assertFalse(error.containsKey("path"), "a variable name is not a response path: $error")
	}
}
