package testing

import io.fluidsonic.graphql.GDocument
import io.fluidsonic.graphql.GNode
import io.fluidsonic.graphql.GOperationDefinition
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

// Printer (roundtrip and output) — https://spec.graphql.org/draft/#sec-Language
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

	// https://spec.graphql.org/draft/#sec-Objects
	@Test
	fun testPrintObjectType_keepsDirectives() {
		val document = GDocument.parse("type AnnotatedObject @onObject(arg: \"value\") { field: Type }").valueWithoutErrorsOrThrow()
		val printed = GNode.print(document)

		assertTrue(
			printed.contains("@onObject(arg: \"value\")"),
			"Printed object type definition must keep its directives, got:\n$printed",
		)
	}

	// https://spec.graphql.org/draft/#sec-Object-Extensions
	@Test
	fun testPrintObjectTypeExtension_keepsDirectives() {
		val document = GDocument.parse("extend type Foo @onType").valueWithoutErrorsOrThrow()
		val printed = GNode.print(document)

		assertTrue(
			printed.contains("@onType"),
			"Printed object type extension must keep its directives, got:\n$printed",
		)
	}

	// https://spec.graphql.org/draft/#sec-Union-Extensions
	@Test
	fun testPrintUnionTypeExtension_keepsExtendKeyword() {
		val document = GDocument.parse("extend union Feed = Photo | Video").valueWithoutErrorsOrThrow()
		val printed = GNode.print(document)

		assertTrue(
			printed.trimStart().startsWith("extend union "),
			"Printed union type extension must start with 'extend union ', got:\n$printed",
		)
	}

	// https://spec.graphql.org/draft/#sec-Type-System.Directives
	@Test
	fun testPrintDirectiveDefinition_keepsRepeatable() {
		val document = GDocument.parse("directive @myRepeatableDir(name: String!) repeatable on OBJECT | INTERFACE").valueWithoutErrorsOrThrow()
		val printed = GNode.print(document)

		assertEquals(
			actual = printed,
			expected = "directive @myRepeatableDir(name: String!) repeatable on OBJECT | INTERFACE",
			message = "Printed directive definition must keep the 'repeatable' keyword.",
		)
	}

	// https://spec.graphql.org/draft/#sec-Schema
	// Whitespace-exact on purpose: GraphQL is whitespace-insensitive, so a stray space survives a parse/print roundtrip.
	@Test
	fun testPrintSchemaDefinition_spacesDirectivesCorrectly() {
		val document = GDocument.parse("schema @onSchema { query: QueryType }").valueWithoutErrorsOrThrow()
		val printed = GNode.print(document)

		assertEquals(
			actual = printed,
			expected = "schema @onSchema {\n\tquery: QueryType\n}",
			message = "Printed schema definition must have exactly one space around its directives.",
		)
	}

	// https://spec.graphql.org/draft/#sec-Schema-Extension
	@Test
	fun testPrintSchemaExtension_spacesDirectivesCorrectly() {
		val document = GDocument.parse("extend schema @onSchema").valueWithoutErrorsOrThrow()
		val printed = GNode.print(document)

		assertEquals(
			actual = printed,
			expected = "extend schema @onSchema",
			message = "Printed schema extension must have exactly one space before its directives.",
		)
	}

	// https://spec.graphql.org/draft/#sec-Schema-Extension
	@Test
	fun testPrintSchemaExtension_spacesOperationTypeDefinitions() {
		val document = GDocument.parse("extend schema @onSchema { subscription: SubscriptionType }").valueWithoutErrorsOrThrow()
		val printed = GNode.print(document)

		assertEquals(
			actual = printed,
			expected = "extend schema @onSchema {\n\tsubscription: SubscriptionType\n}",
			message = "Printed schema extension must separate its operation type definitions from what precedes them.",
		)
	}

	// Gate F: parse -> print -> parse must produce a structurally equal AST.
	@Test
	fun testRoundtripKitchenSinkSchema() {
		val document = GDocument.parse(kitchenSinkSchema.trimMargin()).valueWithoutErrorsOrThrow()
		val printed = GNode.print(document)
		val reparsedDocument = GDocument.parse(printed).valueWithoutErrorsOrThrow()

		assertEquals(
			actual = reparsedDocument.definitions.size,
			expected = document.definitions.size,
			message = "Re-parsing the printed kitchen sink schema must yield the same number of definitions.\nPrinted:\n$printed",
		)

		document.definitions.forEachIndexed { index, definition ->
			val reparsedDefinition = reparsedDocument.definitions[index]

			assertTrue(
				reparsedDefinition.equalsNode(definition),
				"Definition #$index must survive a print/parse roundtrip.\nOriginal:\n$definition\nRoundtripped:\n$reparsedDefinition",
			)
		}

		assertTrue(
			reparsedDocument.equalsNode(document),
			"Re-parsing the printed kitchen sink schema must yield a structurally equal AST.\nPrinted:\n$printed",
		)
	}
}
