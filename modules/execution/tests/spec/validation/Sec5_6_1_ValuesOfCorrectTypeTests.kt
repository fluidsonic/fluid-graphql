package testing

import io.fluidsonic.graphql.*
import kotlin.test.*


// GraphQL Spec §5.6.1 — Values of Correct Type
class Sec5_6_1_ValuesOfCorrectTypeTests {

	@Test
	fun testAcceptsIntValue() {
		assertValidationRule(
			rule = ValueValidityRule,
			errors = emptyList(),
			document = """
				|{ field(arg: 42) }
			""",
			schema = """
				|type Query { field(arg: Int): String }
			"""
		)
	}


	@Test
	fun testAcceptsStringValue() {
		assertValidationRule(
			rule = ValueValidityRule,
			errors = emptyList(),
			document = """
				|{ field(arg: "hello") }
			""",
			schema = """
				|type Query { field(arg: String): String }
			"""
		)
	}


	@Test
	fun testAcceptsBooleanValue() {
		assertValidationRule(
			rule = ValueValidityRule,
			errors = emptyList(),
			document = """
				|{ field(arg: true) }
			""",
			schema = """
				|type Query { field(arg: Boolean): String }
			"""
		)
	}


	@Test
	fun testAcceptsNullForNullable() {
		assertValidationRule(
			rule = ValueValidityRule,
			errors = emptyList(),
			document = """
				|{ field(arg: null) }
			""",
			schema = """
				|type Query { field(arg: String): String }
			"""
		)
	}


	@Test
	fun testRejectsStringForInt() {
		assertValidationRule(
			rule = ValueValidityRule,
			errors = listOf("""
				Type 'Int' does not allow value '"hello"'.

				<document>:1:14
				1 | { field(arg: "hello") }
				  |              ^

				<document>:1:25
				1 | type Query { field(arg: Int): String }
				  |                         ^
			"""),
			document = """
				|{ field(arg: "hello") }
			""",
			schema = """
				|type Query { field(arg: Int): String }
			"""
		)
	}


	@Test
	fun testRejectsIntForString() {
		assertValidationRule(
			rule = ValueValidityRule,
			errors = listOf("""
				Type 'String' does not allow value '42'.

				<document>:1:14
				1 | { field(arg: 42) }
				  |              ^

				<document>:1:25
				1 | type Query { field(arg: String): String }
				  |                         ^
			"""),
			document = """
				|{ field(arg: 42) }
			""",
			schema = """
				|type Query { field(arg: String): String }
			"""
		)
	}


	@Test
	fun testRejectsNullForNonNull() {
		assertValidationRule(
			rule = ValueValidityRule,
			errors = listOf("""
				Type 'String' does not allow value 'null'.

				<document>:1:14
				1 | { field(arg: null) }
				  |              ^

				<document>:1:25
				1 | type Query { field(arg: String!): String }
				  |                         ^
			"""),
			document = """
				|{ field(arg: null) }
			""",
			schema = """
				|type Query { field(arg: String!): String }
			"""
		)
	}


	@Test
	fun testAcceptsEnumValue() {
		assertValidationRule(
			rule = ValueValidityRule,
			errors = emptyList(),
			document = """
				|{ field(arg: ACTIVE) }
			""",
			schema = """
				|type Query { field(arg: Status): String }
				|enum Status { ACTIVE INACTIVE }
			"""
		)
	}


	@Test
	fun testRejectsStringForEnum() {
		assertValidationRule(
			rule = ValueValidityRule,
			errors = listOf("""
				Type 'Status' does not allow value '"ACTIVE"'.

				<document>:1:14
				1 | { field(arg: "ACTIVE") }
				  |              ^

				<document>:1:25
				1 | type Query { field(arg: Status): String }
				  |                         ^
				2 | enum Status { ACTIVE INACTIVE }
			"""),
			document = """
				|{ field(arg: "ACTIVE") }
			""",
			schema = """
				|type Query { field(arg: Status): String }
				|enum Status { ACTIVE INACTIVE }
			"""
		)
	}


	@Test
	fun testAcceptsListValue() {
		assertValidationRule(
			rule = ValueValidityRule,
			errors = emptyList(),
			document = """
				|{ field(arg: [1, 2]) }
			""",
			schema = """
				|type Query { field(arg: [Int]): String }
			"""
		)
	}
}
