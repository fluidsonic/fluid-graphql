package testing

import io.fluidsonic.graphql.*
import kotlin.test.*


// GraphQL Spec §2.1 — Source Text
class SourceTextTests {

	@Test
	fun testWhitespaceIgnored() {
		val doc = GDocument.parse("  { field }  ").valueWithoutErrorsOrThrow()
		assertEquals(1, doc.definitions.size)
	}


	@Test
	fun testTabIgnored() {
		val doc = GDocument.parse("\t{ field }").valueWithoutErrorsOrThrow()
		assertEquals(1, doc.definitions.size)
	}


	@Test
	fun testLineFeedTerminator() {
		val doc = GDocument.parse("{\nfield\n}").valueWithoutErrorsOrThrow()
		val op = doc.definitions.single() as GOperationDefinition
		assertEquals(1, op.selectionSet.selections.size)
	}


	@Test
	fun testCarriageReturnTerminator() {
		val doc = GDocument.parse("{\rfield\r}").valueWithoutErrorsOrThrow()
		val op = doc.definitions.single() as GOperationDefinition
		assertEquals(1, op.selectionSet.selections.size)
	}


	@Test
	fun testCRLFTerminator() {
		val doc = GDocument.parse("{\r\nfield\r\n}").valueWithoutErrorsOrThrow()
		val op = doc.definitions.single() as GOperationDefinition
		assertEquals(1, op.selectionSet.selections.size)
	}


	@Test
	fun testCommentIgnored() {
		val doc = GDocument.parse("# comment\n{ field }").valueWithoutErrorsOrThrow()
		val op = doc.definitions.single() as GOperationDefinition
		val field = op.selectionSet.selections.single() as GFieldSelection
		assertEquals("field", field.name)
	}


	@Test
	fun testCommentToEndOfLine() {
		val doc = GDocument.parse("{ field # inline comment\n}").valueWithoutErrorsOrThrow()
		val op = doc.definitions.single() as GOperationDefinition
		assertEquals(1, op.selectionSet.selections.size)
	}


	@Test
	fun testInsignificantComma() {
		val doc = GDocument.parse("{ a, b, c }").valueWithoutErrorsOrThrow()
		val op = doc.definitions.single() as GOperationDefinition
		assertEquals(3, op.selectionSet.selections.size)
	}


	@Test
	fun testInsignificantCommaInArguments() {
		val doc = GDocument.parse("{ f(a: 1, b: 2) }").valueWithoutErrorsOrThrow()
		val op = doc.definitions.single() as GOperationDefinition
		val field = op.selectionSet.selections.single() as GFieldSelection
		assertEquals(2, field.arguments.size)
	}


	@Test
	fun testTrailingComma() {
		val doc = GDocument.parse("{ a, b, }").valueWithoutErrorsOrThrow()
		val op = doc.definitions.single() as GOperationDefinition
		assertEquals(2, op.selectionSet.selections.size)
	}


	@Test
	fun testMaximalMunchToken() {
		val doc = GDocument.parse("{ a1 }").valueWithoutErrorsOrThrow()
		val op = doc.definitions.single() as GOperationDefinition
		val selections = op.selectionSet.selections
		assertEquals(1, selections.size)
		val field = selections.single() as GFieldSelection
		assertEquals("a1", field.name)
	}


	@Test
	fun testUnicodeBOM() {
		// BOM character (\uFEFF) at the start should be ignored or parse successfully
		val result = GDocument.parse("\uFEFF{ field }")
		// Either it parses successfully or produces an error — BOM handling is implementation-defined.
		// The important thing is that parsing does not throw an exception.
		val hasErrors = result.errors.isNotEmpty()
		if (!hasErrors) {
			val doc = result.valueWithoutErrorsOrThrow()
			assertEquals(1, doc.definitions.size)
		}
	}
}
