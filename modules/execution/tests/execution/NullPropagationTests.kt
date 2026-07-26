package testing

import io.fluidsonic.graphql.GError
import io.fluidsonic.graphql.GExecutor
import io.fluidsonic.graphql.GSchema
import io.fluidsonic.graphql.GraphQL
import io.fluidsonic.graphql.Object
import io.fluidsonic.graphql.default
import io.fluidsonic.graphql.resolve
import io.fluidsonic.graphql.schema
import io.fluidsonic.graphql.type
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

// Gate AJ — a field error on a non-null field nullifies the nearest nullable ancestor.
// https://spec.graphql.org/draft/#sec-Handling-Field-Errors
// Shapes ported from graphql-js `nonnull-test.ts` and re-verified against graphql@17.0.2.
class NullPropagationTests {

	private data class ParentData(val dummy: String = "")

	private fun schemaWith(nn: () -> String?, nestedNn: () -> String?, listOfNonNull: () -> List<String?>) = GraphQL.schema {
		val Parent by type

		Object<ParentData>(Parent) {
			field("nn" of !String) { resolve { nestedNn() } }
		}

		Query {
			field("nn" of !String) { resolve { nn() } }
			field("nullableParent" of Parent) { resolve { ParentData() } }
			field("listOfNN" of List(!String)) { resolve { listOfNonNull() } }
		}
	}

	private val returningNull = schemaWith(
		nn = { null },
		nestedNn = { null },
		listOfNonNull = { listOf("a", null, "c") },
	)

	private val throwing = schemaWith(
		nn = { GError(message = "boom").throwException() },
		nestedNn = { GError(message = "boom").throwException() },
		listOfNonNull = { listOf("a", GError(message = "boom").throwException(), "c") },
	)

	private suspend fun serialize(schema: GSchema, document: String): Map<String, Any?> {
		val executor = GExecutor.default(schema = schema)

		return executor.serializeResult(executor.execute(document))
	}

	private fun assertPaths(result: Map<String, Any?>, expected: List<Any>) {
		val errors = result["errors"] as List<*>
		assertTrue(errors.isNotEmpty(), "expected at least one error but got: $result")
		val firstError = errors.first() as Map<*, *>
		assertEquals(actual = firstError["path"], expected = expected)
	}

	// --- `{ nn }` where nn is String! and resolves to null -> data present and null.

	@Test
	fun testRootNonNullReturningNull_nullifiesData() = runTest {
		val result = serialize(returningNull, "{ nn }")

		assertTrue(result.containsKey("data"), "expected the 'data' key to be present but got: $result")
		assertEquals(actual = result["data"], expected = null)
		assertPaths(result, listOf("nn"))
	}

	@Test
	fun testRootNonNullThrowing_nullifiesData() = runTest {
		val result = serialize(throwing, "{ nn }")

		assertTrue(result.containsKey("data"), "expected the 'data' key to be present but got: $result")
		assertEquals(actual = result["data"], expected = null)
		assertPaths(result, listOf("nn"))
	}

	// --- `{ nullableParent { nn } }` -> the nullable parent absorbs the null.

	@Test
	fun testNestedNonNullReturningNull_nullifiesNullableParent() = runTest {
		val result = serialize(returningNull, "{ nullableParent { nn } }")

		assertEquals(actual = result["data"], expected = mapOf("nullableParent" to null))
		assertPaths(result, listOf("nullableParent", "nn"))
	}

	@Test
	fun testNestedNonNullThrowing_nullifiesNullableParent() = runTest {
		val result = serialize(throwing, "{ nullableParent { nn } }")

		assertEquals(actual = result["data"], expected = mapOf("nullableParent" to null))
		assertPaths(result, listOf("nullableParent", "nn"))
	}

	// --- `[String!]` with a null at index 1 -> the whole list becomes null, path carries the index.

	@Test
	fun testNullInListOfNonNull_nullifiesWholeList() = runTest {
		val result = serialize(returningNull, "{ listOfNN }")

		assertEquals(actual = result["data"], expected = mapOf("listOfNN" to null))
		assertPaths(result, listOf("listOfNN", 1))
	}

	// The list resolver itself throwing nullifies the same (nullable) list field.
	// A per-element throw is not representable here: fluid resolves a list field to a materialized
	// Kotlin list, so there is no per-element resolver that could fail independently.
	@Test
	fun testThrowingListResolver_nullifiesWholeList() = runTest {
		val result = serialize(throwing, "{ listOfNN }")

		assertEquals(actual = result["data"], expected = mapOf("listOfNN" to null))
		assertPaths(result, listOf("listOfNN"))
	}
}
