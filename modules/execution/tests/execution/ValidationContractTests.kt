package testing

import io.fluidsonic.graphql.GDocument
import io.fluidsonic.graphql.GDocumentSource
import io.fluidsonic.graphql.GExecutor
import io.fluidsonic.graphql.GraphQL
import io.fluidsonic.graphql.default
import io.fluidsonic.graphql.resolve
import io.fluidsonic.graphql.schema
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

// Gate D — the two `execute(documentSource…)` overloads validate the document before executing it,
// mirroring upstream's `graphql()`. The `execute(document: GDocument)` overload does not, mirroring
// upstream's `execute()`.
//
// Verified against graphql@17.0.2 for `{ invalidField }` against `type Query { id: String }`:
//   graphql({schema, source})            -> {"errors":[{"message":"Cannot query field \"invalidField\" on type \"Query\"."}]}
//                                           (no "data" key)
//   execute({schema, document: parse(…)}) -> {"data":{}}
//
// The two channels are told apart by the error message: only `FieldSelectionExistenceRule` says
// "Cannot select nonexistent field". The executor no longer produces a competing message at all —
// it skips unknown fields — so the absence assertion below also pins that.
class ValidationContractTests {

	private val schema = GraphQL.schema {
		Query {
			field("hello" of String) { resolve { "world" } }
		}
	}

	private val invalidFieldRuleMessage = "Cannot select nonexistent field 'invalidField' on type 'Query'."

	@Test
	fun testStringOverload_validatesBeforeExecuting() = runTest {
		val executor = GExecutor.default(schema = schema)
		val messages = executor.execute("{ invalidField }").errors.map { it.message }

		assertEquals(actual = messages, expected = listOf(invalidFieldRuleMessage))
		assertFalse(
			messages.any { it.contains("There is no field named") },
			"the executor must not report unknown fields; validation owns that. Got: $messages",
		)
	}

	@Test
	fun testParsableOverload_validatesBeforeExecuting() = runTest {
		val executor = GExecutor.default(schema = schema)
		val messages = executor.execute(GDocumentSource.of("{ invalidField }")).errors.map { it.message }

		assertEquals(actual = messages, expected = listOf(invalidFieldRuleMessage))
		assertFalse(
			messages.any { it.contains("There is no field named") },
			"the executor must not report unknown fields; validation owns that. Got: $messages",
		)
	}

	// A document that fails validation is never executed, so the failure is a request error and the
	// response must omit the "data" key entirely.
	// https://spec.graphql.org/draft/#sec-Response
	@Test
	fun testStringOverload_reportsValidationFailureAsRequestError() = runTest {
		val executor = GExecutor.default(schema = schema)
		val result = executor.serializeResult(executor.execute("{ invalidField }"))

		assertFalse(result.containsKey("data"), "expected the 'data' key to be absent but got: $result")
		assertTrue(result.containsKey("errors"), "expected an 'errors' key but got: $result")
	}

	// The counterpart: the pre-parsed overload skips validation, so the unknown field is simply
	// skipped and the "data" key stays present.
	@Test
	fun testDocumentOverload_skipsValidation() = runTest {
		val executor = GExecutor.default(schema = schema)
		val document = GDocument.parse("{ invalidField }").valueOrThrow()
		val result = executor.serializeResult(executor.execute(document))

		assertEquals(actual = result, expected = mapOf("data" to emptyMap<String, Any?>()))
	}

	// A valid document must still execute normally through the validating channel.
	@Test
	fun testStringOverload_executesValidDocument() = runTest {
		val executor = GExecutor.default(schema = schema)
		val result = executor.serializeResult(executor.execute("{ hello }"))

		assertEquals(actual = result, expected = mapOf("data" to mapOf("hello" to "world")))
	}
}
