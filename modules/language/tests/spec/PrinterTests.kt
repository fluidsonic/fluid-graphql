package testing

import io.fluidsonic.graphql.*
import kotlin.test.*


// GraphQL Spec §2 — Printer (roundtrip and output)
class PrinterTests {

	@Test
	fun testPrintSimpleQuery() {
		val doc = GDocument.parse("{ foo }").valueWithoutErrorsOrThrow()
		val printed = GNode.print(doc)
		// Anonymous query shorthand: no "query" keyword, just the selection set
		assertTrue(printed.contains("foo"), "Printed output should contain field name 'foo'")
		assertTrue(printed.contains("{"), "Printed output should contain '{'")
		assertTrue(printed.contains("}"), "Printed output should contain '}'")
	}


	@Test
	fun testPrintQueryWithArgs() {
		val doc = GDocument.parse("{ foo(bar: 42) }").valueWithoutErrorsOrThrow()
		val printed = GNode.print(doc)
		assertTrue(printed.contains("foo"), "Printed output should contain field name")
		assertTrue(printed.contains("bar"), "Printed output should contain argument name")
		assertTrue(printed.contains("42"), "Printed output should contain argument value")
	}


	@Test
	fun testPrintFragment() {
		val doc = GDocument.parse("{ ...F } fragment F on Query { field }").valueWithoutErrorsOrThrow()
		val printed = GNode.print(doc)
		assertTrue(printed.contains("fragment"), "Printed output should contain 'fragment' keyword")
		assertTrue(printed.contains("F"), "Printed output should contain fragment name")
		assertTrue(printed.contains("on"), "Printed output should contain 'on' keyword")
		assertTrue(printed.contains("Query"), "Printed output should contain type condition")
	}


	@Test
	fun testPrintMutation() {
		val doc = GDocument.parse("mutation M { field }").valueWithoutErrorsOrThrow()
		val printed = GNode.print(doc)
		assertTrue(printed.contains("mutation"), "Printed output should contain 'mutation'")
		assertTrue(printed.contains("M"), "Printed output should contain operation name")
	}


	@Test
	fun testPrintSubscription() {
		val doc = GDocument.parse("subscription S { field }").valueWithoutErrorsOrThrow()
		val printed = GNode.print(doc)
		assertTrue(printed.contains("subscription"), "Printed output should contain 'subscription'")
		assertTrue(printed.contains("S"), "Printed output should contain operation name")
	}


	@Test
	fun testRoundtripSimple() {
		val source = "query MyQuery {\n\tfield\n}\n"
		val doc1 = GDocument.parse(source).valueWithoutErrorsOrThrow()
		val printed = GNode.print(doc1)
		val doc2 = GDocument.parse(printed).valueWithoutErrorsOrThrow()
		// Both ASTs should have the same structure
		assertEquals(doc1.definitions.size, doc2.definitions.size)
		val op1 = doc1.definitions.single() as GOperationDefinition
		val op2 = doc2.definitions.single() as GOperationDefinition
		assertEquals(op1.name, op2.name)
		assertEquals(op1.type, op2.type)
		assertEquals(op1.selectionSet.selections.size, op2.selectionSet.selections.size)
	}


	@Test
	fun testPrintNullValue() {
		val doc = GDocument.parse("{ f(a: null) }").valueWithoutErrorsOrThrow()
		val printed = GNode.print(doc)
		assertTrue(printed.contains("null"), "Printed output should contain 'null'")
	}


	@Test
	fun testPrintListValue() {
		val doc = GDocument.parse("{ f(a: [1, 2, 3]) }").valueWithoutErrorsOrThrow()
		val printed = GNode.print(doc)
		assertTrue(printed.contains("["), "Printed output should contain '['")
		assertTrue(printed.contains("]"), "Printed output should contain ']'")
		assertTrue(printed.contains("1"), "Printed output should contain '1'")
		assertTrue(printed.contains("2"), "Printed output should contain '2'")
		assertTrue(printed.contains("3"), "Printed output should contain '3'")
	}


	@Test
	fun testPrintObjectValue() {
		val doc = GDocument.parse("{ f(a: {key: 1}) }").valueWithoutErrorsOrThrow()
		val printed = GNode.print(doc)
		assertTrue(printed.contains("key"), "Printed output should contain object field name")
		assertTrue(printed.contains("1"), "Printed output should contain object field value")
	}


	@Test
	fun testPrintBooleanTrue() {
		val doc = GDocument.parse("{ f(a: true) }").valueWithoutErrorsOrThrow()
		val printed = GNode.print(doc)
		assertTrue(printed.contains("true"), "Printed output should contain 'true'")
	}


	@Test
	fun testPrintBooleanFalse() {
		val doc = GDocument.parse("{ f(a: false) }").valueWithoutErrorsOrThrow()
		val printed = GNode.print(doc)
		assertTrue(printed.contains("false"), "Printed output should contain 'false'")
	}


	@Test
	fun testPrintNonNullType() {
		val doc = GDocument.parse("query Q(\$x: String!) { f }").valueWithoutErrorsOrThrow()
		val printed = GNode.print(doc)
		assertTrue(printed.contains("String!"), "Printed output should contain 'String!'")
	}


	@Test
	fun testPrintListType() {
		val doc = GDocument.parse("query Q(\$x: [String]) { f }").valueWithoutErrorsOrThrow()
		val printed = GNode.print(doc)
		assertTrue(printed.contains("[String]"), "Printed output should contain '[String]'")
	}


	@Test
	fun testPrintDirective() {
		val doc = GDocument.parse("{ field @skip(if: false) }").valueWithoutErrorsOrThrow()
		val printed = GNode.print(doc)
		assertTrue(printed.contains("@skip"), "Printed output should contain '@skip'")
		assertTrue(printed.contains("if"), "Printed output should contain directive argument name")
		assertTrue(printed.contains("false"), "Printed output should contain directive argument value")
	}


	@Test
	fun testPrintVariableDefinition() {
		val doc = GDocument.parse("query Q(\$x: Int = 5) { f }").valueWithoutErrorsOrThrow()
		val printed = GNode.print(doc)
		assertTrue(printed.contains("\$x"), "Printed output should contain variable '\$x'")
		assertTrue(printed.contains("Int"), "Printed output should contain variable type")
		assertTrue(printed.contains("= 5"), "Printed output should contain default value")
	}


	@Test
	fun testToStringDelegatesToPrint() {
		val doc = GDocument.parse("{ foo }").valueWithoutErrorsOrThrow()
		val printed = GNode.print(doc)
		val toStringResult = doc.toString()
		assertEquals(printed, toStringResult, "toString() should produce the same output as GNode.print()")
	}
}
