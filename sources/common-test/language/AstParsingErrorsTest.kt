package tests

import io.fluidsonic.graphql.*
import kotlin.test.*


// https://github.com/graphql/graphql-js/blob/master/src/language/__tests__/parser-test.js
class AstParsingErrorsTest {

	@Test
	fun `provides useful origin`() {
		val error = assertFailsWith<GError> { GDocument.parse(content = "{", name = "<test>") }
		assertEquals(expected = "Syntax Error: Expected Name, found <end of input>.", actual = error.message)
		assertEquals(expected = 1, actual = error.origins.size)

		val origin = error.origins.first()
		assertEquals(expected = 2, actual = origin.column)
		assertEquals(expected = 1, actual = origin.line)
		assertEquals(expected = 1, actual = origin.startPosition)
		assertEquals(expected = 1, actual = origin.endPosition)
		assertEquals(expected = "{", actual = origin.source.content)

		assertEquals(
			expected = """
				|Syntax Error: Expected Name, found <end of input>.
				|
                |<test>:1:2
                |1 | {
                |  |  ^
			""".trimMargin(),
			actual = error.describe()
		)
	}


	@Test
	fun `provides useful error when using source`() {
		val error = assertFailsWith<GError> { GDocument.parse(GSource.of(content = "query", name = "MyQuery.graphql")) }

		assertEquals(
			expected = """
				|Syntax Error: Expected "{", found <end of input>.
				|
                |MyQuery.graphql:1:6
                |1 | query
                |  |      ^
			""".trimMargin(),
			actual = error.describe()
		)
	}


	@Test
	fun `rejects fragments named "on"`() {
		assertSyntaxError(
			content = "fragment on on on { on }",
			message = """Syntax Error: Unexpected Name "on".""",
			line = 1, column = 10
		)
	}


	@Test
	fun `rejects fragment spreads of "on"`() {
		assertSyntaxError(
			content = "{ ...on }",
			message = """Syntax Error: Expected Name, found "}".""",
			line = 1, column = 9
		)
	}


	@Test
	fun `rejects missing 'on' in fragment definition`() {
		assertSyntaxError(
			content = """
				|
				|{ ...MissingOn }
				|fragment MissingOn Type
				|
			""",
			message = """Syntax Error: Expected "on", found Name "Type".""",
			line = 3, column = 20
		)
	}


	@Test
	fun `rejects missing field name after alias`() {
		assertSyntaxError(
			content = "{ field: {} }",
			message = """Syntax Error: Expected Name, found "{".""",
			line = 1, column = 10
		)
	}


	@Test
	fun `rejects nonexistent operation name`() {
		assertSyntaxError(
			content = "notanoperation Foo { field }",
			message = """Syntax Error: Unexpected Name "notanoperation".""",
			line = 1, column = 1
		)
	}


	@Test
	fun `rejects top-level spread`() {
		assertSyntaxError(
			content = "...",
			message = """Syntax Error: Unexpected "...".""",
			line = 1, column = 1
		)
	}


	@Test
	fun `rejects string as field key`() {
		assertSyntaxError(
			content = """
				|{ ""
			""",
			message = """Syntax Error: Expected Name, found String "".""",
			line = 1, column = 3
		)
	}


	@Test
	fun `rejects non-constant default values`() {
		assertSyntaxError(
			content = "query Foo(\$x: Complex = { a: { b: [ \$var ] } }) { field }",
			message = """Syntax Error: Unexpected "$".""",
			line = 1, column = 37
		)
	}
}
