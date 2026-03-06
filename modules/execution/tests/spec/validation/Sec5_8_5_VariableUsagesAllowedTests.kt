package testing

import io.fluidsonic.graphql.*
import kotlin.test.*


// GraphQL Spec §5.8.5 — All Variable Usages Are Allowed
class Sec5_8_5_VariableUsagesAllowedTests {

	@Test
	fun testAcceptsVariableWithCompatibleType() {
		assertValidationRule(
			rule = VariablesInAllowedPositionRule,
			errors = emptyList(),
			document = """
				|query Q(${'$'}x: Int) { field(arg: ${'$'}x) }
			""",
			schema = """
				|type Query { field(arg: Int): String }
			"""
		)
	}


	@Test
	fun testRejectsVariableWithIncompatibleType() {
		assertValidationRule(
			rule = VariablesInAllowedPositionRule,
			errors = listOf("""
				Variable '${'$'}x' of type 'String' cannot be used as an argument of type 'Int'.

				<document>:1:9
				1 | query Q(${'$'}x: String) { field(arg: ${'$'}x) }
				  |         ^

				<document>:1:34
				1 | query Q(${'$'}x: String) { field(arg: ${'$'}x) }
				  |                                  ^
			"""),
			document = """
				|query Q(${'$'}x: String) { field(arg: ${'$'}x) }
			""",
			schema = """
				|type Query { field(arg: Int): String }
			"""
		)
	}


	@Test
	fun testAcceptsNullableVariableForNullableArg() {
		assertValidationRule(
			rule = VariablesInAllowedPositionRule,
			errors = emptyList(),
			document = """
				|query Q(${'$'}x: String) { field(arg: ${'$'}x) }
			""",
			schema = """
				|type Query { field(arg: String): String }
			"""
		)
	}


	@Test
	fun testRejectsNullableVariableForNonNullArg() {
		assertValidationRule(
			rule = VariablesInAllowedPositionRule,
			errors = listOf("""
				Variable '${'$'}x' of type 'String' cannot be used as an argument of type 'String!'.

				<document>:1:9
				1 | query Q(${'$'}x: String) { field(arg: ${'$'}x) }
				  |         ^

				<document>:1:34
				1 | query Q(${'$'}x: String) { field(arg: ${'$'}x) }
				  |                                  ^
			"""),
			document = """
				|query Q(${'$'}x: String) { field(arg: ${'$'}x) }
			""",
			schema = """
				|type Query { field(arg: String!): String }
			"""
		)
	}
}
