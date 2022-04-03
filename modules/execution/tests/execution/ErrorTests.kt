@file:Suppress("LocalVariableName")

package testing

import io.fluidsonic.graphql.*
import kotlin.test.*


class ErrorTests {

	@Test
	fun testErrorForInvalidFieldInNonValidatedQuery() = runBlockingTest {
		val schema = GraphQL.schema { Query {} }
		val document = "{ foo }"

		val executor = GExecutor.default(schema = schema)
		val result = executor.serializeResult(executor.execute(document))

		assertEquals(
			expected = mapOf(
				"data" to null,
				"errors" to listOf(mapOf(
					"message" to "There is no field named 'foo' on type 'Query'.",
					"locations" to listOf(mapOf(
						"line" to 1,
						"column" to 3
					)),
					"path" to listOf("foo")
				))
			),
			actual = result
		)
	}
}
