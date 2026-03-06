package testing

import io.fluidsonic.graphql.*
import kotlin.test.*


// GraphQL Spec §2.10 — Input Values
class InputValueTests {

	// -- Int values --

	@Test
	fun testIntValueZero() {
		val arg = parseArgument("{ f(a: 0) }")
		val intVal = arg.value as GIntValue
		assertEquals(0, intVal.value)
	}


	@Test
	fun testIntValuePositive() {
		val arg = parseArgument("{ f(a: 42) }")
		val intVal = arg.value as GIntValue
		assertEquals(42, intVal.value)
	}


	@Test
	fun testIntValueNegative() {
		val arg = parseArgument("{ f(a: -1) }")
		val intVal = arg.value as GIntValue
		assertEquals(-1, intVal.value)
	}


	@Test
	fun testInvalidIntLeadingZeros() {
		val result = GDocument.parse("{ f(a: 00) }")
		assertTrue(result.errors.isNotEmpty(), "Expected parse error for leading zeros in integer")
	}


	@Test
	fun testInvalidIntWithDecimalIsFloat() {
		// 1.0 is a Float literal, not an invalid Int
		val arg = parseArgument("{ f(a: 1.0) }")
		assertTrue(arg.value is GFloatValue, "Expected GFloatValue for '1.0'")
	}


	// -- Float values --

	@Test
	fun testFloatWithDecimal() {
		val arg = parseArgument("{ f(a: 1.0) }")
		val floatVal = arg.value as GFloatValue
		assertEquals(1.0, floatVal.value)
	}


	@Test
	fun testFloatWithExponent() {
		val arg = parseArgument("{ f(a: 1e10) }")
		val floatVal = arg.value as GFloatValue
		assertEquals(1e10, floatVal.value)
	}


	@Test
	fun testFloatNegative() {
		val arg = parseArgument("{ f(a: -1.5) }")
		val floatVal = arg.value as GFloatValue
		assertEquals(-1.5, floatVal.value)
	}


	@Test
	fun testInvalidFloatLeadingDot() {
		val result = GDocument.parse("{ f(a: .5) }")
		assertTrue(result.errors.isNotEmpty(), "Expected parse error for float starting with dot")
	}


	// -- String values --

	@Test
	fun testStringValue() {
		val arg = parseArgument("""{ f(a: "hello") }""")
		val strVal = arg.value as GStringValue
		assertEquals("hello", strVal.value)
		assertFalse(strVal.isBlock)
	}


	@Test
	fun testStringEscapeNewline() {
		val arg = parseArgument("{ f(a: \"a\\nb\") }")
		val strVal = arg.value as GStringValue
		assertEquals("a\nb", strVal.value)
	}


	@Test
	fun testStringEscapeTab() {
		val arg = parseArgument("{ f(a: \"a\\tb\") }")
		val strVal = arg.value as GStringValue
		assertEquals("a\tb", strVal.value)
	}


	@Test
	fun testStringEscapeUnicode() {
		val arg = parseArgument("{ f(a: \"a\\u0041b\") }")
		val strVal = arg.value as GStringValue
		assertEquals("aAb", strVal.value)
	}


	@Test
	fun testBlockString() {
		val arg = parseArgument("{ f(a: \"\"\"hello\"\"\") }")
		val strVal = arg.value as GStringValue
		assertEquals("hello", strVal.value)
		assertTrue(strVal.isBlock)
	}


	// -- Boolean values --

	@Test
	fun testBooleanTrue() {
		val arg = parseArgument("{ f(a: true) }")
		val boolVal = arg.value as GBooleanValue
		assertTrue(boolVal.value)
	}


	@Test
	fun testBooleanFalse() {
		val arg = parseArgument("{ f(a: false) }")
		val boolVal = arg.value as GBooleanValue
		assertFalse(boolVal.value)
	}


	// -- Null value --

	@Test
	fun testNullValue() {
		val arg = parseArgument("{ f(a: null) }")
		assertTrue(arg.value is GNullValue)
	}


	// -- Enum value --

	@Test
	fun testEnumValue() {
		val arg = parseArgument("{ f(a: ACTIVE) }")
		val enumVal = arg.value as GEnumValue
		assertEquals("ACTIVE", enumVal.name)
	}


	// -- List values --

	@Test
	fun testEmptyList() {
		val arg = parseArgument("{ f(a: []) }")
		val listVal = arg.value as GListValue
		assertEquals(0, listVal.elements.size)
	}


	@Test
	fun testIntList() {
		val arg = parseArgument("{ f(a: [1, 2, 3]) }")
		val listVal = arg.value as GListValue
		assertEquals(3, listVal.elements.size)
		assertTrue(listVal.elements[0] is GIntValue)
		assertTrue(listVal.elements[1] is GIntValue)
		assertTrue(listVal.elements[2] is GIntValue)
		assertEquals(1, (listVal.elements[0] as GIntValue).value)
		assertEquals(2, (listVal.elements[1] as GIntValue).value)
		assertEquals(3, (listVal.elements[2] as GIntValue).value)
	}


	@Test
	fun testNestedList() {
		val arg = parseArgument("{ f(a: [[1]]) }")
		val outerList = arg.value as GListValue
		assertEquals(1, outerList.elements.size)
		val innerList = outerList.elements.single() as GListValue
		assertEquals(1, innerList.elements.size)
		assertEquals(1, (innerList.elements.single() as GIntValue).value)
	}


	// -- Input Object values --

	@Test
	fun testEmptyObject() {
		val arg = parseArgument("{ f(a: {}) }")
		val objVal = arg.value as GObjectValue
		assertEquals(0, objVal.arguments.size)
	}


	@Test
	fun testObjectWithField() {
		val arg = parseArgument("{ f(a: {key: 1}) }")
		val objVal = arg.value as GObjectValue
		assertEquals(1, objVal.arguments.size)
		val field = objVal.arguments.single()
		assertEquals("key", field.name)
		assertEquals(1, (field.value as GIntValue).value)
	}


	private fun parseArgument(source: String): GArgument {
		val doc = GDocument.parse(source).valueWithoutErrorsOrThrow()
		return doc.definitions.single()
			.let { it as GOperationDefinition }
			.selectionSet.selections.single()
			.let { it as GFieldSelection }
			.arguments.single()
	}
}
