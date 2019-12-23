package tests

import kotlin.test.*


// https://github.com/graphql/graphql-js/blob/master/src/language/__tests__/schema-parser-test.js
class AstSchemaParsingErrorTest {

	@Test
	fun `rejects directive with incorrect location`() {
		assertSyntaxError(
			content = "directive @foo on FIELD | INCORRECT_LOCATION",
			message = """Syntax Error: Unexpected Name "INCORRECT_LOCATION".""",
			line = 1, column = 27
		)
	}


	@Test
	fun `rejects input object with argument`() {
		assertSyntaxError(
			content = """
				|input Hello {
				|  world(foo: Int): String
				|}
			""",
			message = """Syntax Error: Expected ":", found "(".""",
			line = 2, column = 8
		)
	}


	@Test
	fun `rejects union without types`() {
		assertSyntaxError(
			content = "union Hello = |",
			message = """Syntax Error: Expected Name, found <end of input>.""",
			line = 1, column = 16
		)
	}


	@Test
	fun `rejects union with leading double pipe`() {
		assertSyntaxError(
			content = "union Hello = || Wo | Rld",
			message = """Syntax Error: Expected Name, found "|".""",
			line = 1, column = 16
		)
	}


	@Test
	fun `rejects union with double pipe`() {
		assertSyntaxError(
			content = "union Hello = | Wo | Rld |",
			message = """Syntax Error: Expected Name, found <end of input>.""",
			line = 1, column = 27
		)
	}
}
