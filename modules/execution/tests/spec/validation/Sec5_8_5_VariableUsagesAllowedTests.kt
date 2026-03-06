package testing

import io.fluidsonic.graphql.*
import kotlin.test.*


// GraphQL Spec §5.8.5 — All Variable Usages Are Allowed
class Sec5_8_5_VariableUsagesAllowedTests {

	@Ignore("Known bug: VariablesInAllowedPositionRule not implemented")
	@Test
	fun testAcceptsVariableWithCompatibleType() {
		assertValidationRule(
			rule = VariableDefinitionTypeValidityRule,
			errors = emptyList(),
			document = """
				|query Q(${'$'}x: Int) { field(arg: ${'$'}x) }
			""",
			schema = """
				|type Query { field(arg: Int): String }
			"""
		)
	}


	@Ignore("Known bug: VariablesInAllowedPositionRule not implemented")
	@Test
	fun testRejectsVariableWithIncompatibleType() {
		assertValidationRule(
			rule = VariableDefinitionTypeValidityRule,
			errors = listOf("Variable '\$x' of type 'String' cannot be used as an argument of type 'Int'."),
			document = """
				|query Q(${'$'}x: String) { field(arg: ${'$'}x) }
			""",
			schema = """
				|type Query { field(arg: Int): String }
			"""
		)
	}


	@Ignore("Known bug: VariablesInAllowedPositionRule not implemented")
	@Test
	fun testAcceptsNullableVariableForNullableArg() {
		assertValidationRule(
			rule = VariableDefinitionTypeValidityRule,
			errors = emptyList(),
			document = """
				|query Q(${'$'}x: String) { field(arg: ${'$'}x) }
			""",
			schema = """
				|type Query { field(arg: String): String }
			"""
		)
	}


	@Ignore("Known bug: VariablesInAllowedPositionRule not implemented")
	@Test
	fun testRejectsNullableVariableForNonNullArg() {
		assertValidationRule(
			rule = VariableDefinitionTypeValidityRule,
			errors = listOf("Variable '\$x' of type 'String' cannot be used as an argument of type 'String!'."),
			document = """
				|query Q(${'$'}x: String) { field(arg: ${'$'}x) }
			""",
			schema = """
				|type Query { field(arg: String!): String }
			"""
		)
	}
}
