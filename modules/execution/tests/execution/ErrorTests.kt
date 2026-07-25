package testing

import io.fluidsonic.graphql.GExecutor
import io.fluidsonic.graphql.GraphQL
import io.fluidsonic.graphql.default
import io.fluidsonic.graphql.document
import io.fluidsonic.graphql.schema
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class ErrorTests {

	@Test
	fun testErrorForInvalidFieldInNonValidatedQuery() = runTest {
		val schema = GraphQL.schema { Query {} }
		val document = "{ foo }"

		val executor = GExecutor.default(schema = schema)
		val result = executor.serializeResult(executor.execute(document))

		assertEquals(
			expected = mapOf(
				"data" to null,
				"errors" to listOf(
					mapOf(
						"message" to "There is no field named 'foo' on type 'Query'.",
						"locations" to listOf(
							mapOf(
								"line" to 1,
								"column" to 3,
							),
						),
						"path" to listOf("foo"),
					),
				),
			),
			actual = result,
		)
	}
}
