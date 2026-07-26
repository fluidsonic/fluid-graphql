package testing

import io.fluidsonic.graphql.GDocument
import io.fluidsonic.graphql.GExecutor
import io.fluidsonic.graphql.GraphQL
import io.fluidsonic.graphql.default
import io.fluidsonic.graphql.document
import io.fluidsonic.graphql.schema
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class ErrorTests {

	// A field the type does not define is skipped, so its response key is absent and no error is
	// reported. Verified against graphql@17.0.2, which returns from `executeField` when
	// `schema.getField` finds no definition:
	//   execute({schema, document: parse('{ known unknownField }')}) -> {"data":{"known":"K"}}
	// Detecting the unknown field is validation's job, not execution's.
	//
	// This uses the `execute(document: GDocument)` overload on purpose — it is the only one that does
	// not validate, which is exactly the contract under test here. The `documentSource` overloads turn
	// the same document into a request error.
	@Test
	fun testInvalidFieldInNonValidatedQueryIsSkipped() = runTest {
		val schema = GraphQL.schema { Query {} }
		val document = GDocument.parse("{ foo }").valueOrThrow()

		val executor = GExecutor.default(schema = schema)
		val result = executor.serializeResult(executor.execute(document))

		assertEquals(
			expected = mapOf("data" to emptyMap<String, Any?>()),
			actual = result,
		)
	}
}
