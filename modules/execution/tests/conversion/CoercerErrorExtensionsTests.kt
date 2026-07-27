/*
 * A coercer's structured error must reach the client whole, on whichever path rejects the value.
 *
 * The same literal is rejected by validation on the `documentSource` overload and by coercion on the
 * `GDocument` overload, so the two paths must agree about what the client is told — otherwise the
 * machine-readable half of the error depends on which entry point the server happens to call.
 */
package testing

import io.fluidsonic.graphql.GDocument
import io.fluidsonic.graphql.GError
import io.fluidsonic.graphql.GExecutor
import io.fluidsonic.graphql.GSchema
import io.fluidsonic.graphql.GraphQL
import io.fluidsonic.graphql.coerceInputLiteral
import io.fluidsonic.graphql.coerceInputValue
import io.fluidsonic.graphql.schema
import io.fluidsonic.graphql.type
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

private val extensions = mapOf<String, Any?>("code" to "BAD_CUSTOM", "retryable" to false)

private fun schemaWithRaisingCoercer(): GSchema = GraphQL.schema {
	val Custom by type

	Scalar(Custom) {
		coerceInputLiteral { GError(message = "no such thing", extensions = extensions).throwException() }
		coerceInputValue { GError(message = "no such thing", extensions = extensions).throwException() }
	}

	Query {
		field("custom" of String) { argument("a" of Custom) }
	}
}

@Suppress("UNCHECKED_CAST")
private fun singleErrorOf(result: Map<String, Any?>): Map<String, Any?> {
	val errors = assertNotNull(result["errors"] as? List<*>, "expected the result to carry errors but got: $result")

	return errors.single() as Map<String, Any?>
}

class CoercerErrorExtensionsTests {

	// Validation rejects the literal before an executor sees it, so the error travels the validation path.
	@Test
	fun validatingOverload_keepsTheCoercersExtensions() = runTest {
		val executor = GExecutor.default(schema = schemaWithRaisingCoercer())
		val error = singleErrorOf(executor.serializeResult(executor.execute("{ custom(a: 1) }")))

		assertEquals(actual = error["message"], expected = "no such thing")
		assertEquals(actual = error["extensions"], expected = extensions)
	}

	// The non-validating overload reaches coercion instead, which is a different code path to the client.
	@Test
	fun nonValidatingOverload_keepsTheCoercersExtensions() = runTest {
		val executor = GExecutor.default(schema = schemaWithRaisingCoercer())
		val document = GDocument.parse("{ custom(a: 1) }").valueOrThrow()
		val error = singleErrorOf(executor.serializeResult(executor.execute(document)))

		assertEquals(actual = error["message"], expected = "no such thing")
		assertEquals(actual = error["extensions"], expected = extensions)
	}

	// The third input path. A variable is coerced before execution begins, so this failure is a request
	// error rather than a field error — but what the client learns about *why* must not depend on that.
	@Test
	fun variablePath_keepsTheCoercersExtensions() = runTest {
		val executor = GExecutor.default(schema = schemaWithRaisingCoercer())
		val error = singleErrorOf(
			executor.serializeResult(
				executor.execute("""query(${'$'}v: Custom) { custom(a: ${'$'}v) }""", variableValues = mapOf("v" to "anything")),
			),
		)

		assertEquals(actual = error["message"], expected = "no such thing")
		assertEquals(actual = error["extensions"], expected = extensions)
	}
}
