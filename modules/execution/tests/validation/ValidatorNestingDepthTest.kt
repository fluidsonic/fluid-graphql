package testing

import io.fluidsonic.graphql.GDocument
import io.fluidsonic.graphql.GSchema
import io.fluidsonic.graphql.validate
import kotlin.test.Test
import kotlin.test.assertEquals

// Validation used to descend by mutual recursion through every rule, so one level of selection nesting cost one
// stack frame per rule and a document nested a few dozen levels deep overflowed the stack. The traversal is now
// iterative, so nesting costs heap rather than stack.
class ValidatorNestingDepthTest {

	private val schema = GSchema.parse("type Query { deeper: Query, id: ID }").valueOrThrow()

	@Test
	fun testValidatesDeeplyNestedSelections() {
		val document = GDocument.parse(nestedDocument(depth = 1000)).valueOrThrow()

		assertEquals(actual = document.validate(schema), expected = emptyList())
	}

	private fun nestedDocument(depth: Int) = buildString {
		append("{ ")
		repeat(depth) { append("deeper { ") }
		append("id")
		repeat(depth) { append("} ") }
		append("}")
	}
}
