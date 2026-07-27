/*
 * What a client is told when a value fails on the way *out*.
 *
 * An output coercion failure happens at a known response position, so unlike a validation error it must
 * carry a `path`. And an attached coercer's structured error must survive the trip, the same way it now
 * does on the input side.
 */
package testing

import io.fluidsonic.graphql.GError
import io.fluidsonic.graphql.GExecutor
import io.fluidsonic.graphql.GSchema
import io.fluidsonic.graphql.GraphQL
import io.fluidsonic.graphql.Object
import io.fluidsonic.graphql.coerceOutputValue
import io.fluidsonic.graphql.resolve
import io.fluidsonic.graphql.schema
import io.fluidsonic.graphql.type
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

private val outputExtensions = mapOf<String, Any?>("code" to "BAD_OUTPUT")

private data class WrapData(val dummy: String = "")

@Suppress("UNCHECKED_CAST")
private suspend fun singleError(schema: GSchema, source: String): Map<String, Any?> {
	val executor = GExecutor.default(schema = schema)
	val result = executor.serializeResult(executor.execute(source))
	val errors = assertNotNull(result["errors"] as? List<*>, "expected errors but got: $result")

	return errors.single() as Map<String, Any?>
}

class OutputCoercionErrorShapeTests {

	// A built-in scalar rejecting a resolved value: the converter enriches the scalar's bare message, and
	// the response position it was rejected at has to come along.
	@Test
	fun builtinOutputCoercionFailure_carriesThePath() = runTest {
		val schema = GraphQL.schema {
			val Wrap by type

			Object<WrapData>(Wrap) {
				field("nanFloat" of Float) { resolve { Double.NaN } }
			}

			Query {
				field("nested" of Wrap) { resolve { WrapData() } }
			}
		}

		val error = singleError(schema, "{ nested { nanFloat } }")

		assertEquals(actual = error["path"], expected = listOf("nested", "nanFloat"))
	}

	// An attached output coercer raising a structured error: both the path and the extensions must survive.
	@Test
	fun attachedOutputCoercerFailure_carriesThePathAndTheExtensions() = runTest {
		val schema = GraphQL.schema {
			val Custom by type
			val Wrap by type

			Scalar(Custom) {
				coerceOutputValue { GError(message = "cannot serialise", extensions = outputExtensions).throwException() }
			}

			Object<WrapData>(Wrap) {
				field("custom" of Custom) { resolve { "anything" } }
			}

			Query {
				field("nested" of Wrap) { resolve { WrapData() } }
			}
		}

		val error = singleError(schema, "{ nested { custom } }")

		assertEquals(actual = error["message"], expected = "cannot serialise")
		assertEquals(actual = error["path"], expected = listOf("nested", "custom"))
		assertEquals(actual = error["extensions"], expected = outputExtensions)
	}
}
