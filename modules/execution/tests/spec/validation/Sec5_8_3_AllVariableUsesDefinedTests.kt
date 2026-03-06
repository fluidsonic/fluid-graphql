package testing

import io.fluidsonic.graphql.*
import kotlin.test.*


// GraphQL Spec §5.8.3 — All Variable Uses Defined
class Sec5_8_3_AllVariableUsesDefinedTests {

	@Ignore("Known bug: AllVariableUsesDefinedRule not implemented")
	@Test
	fun testAcceptsVariableUsedAfterDefined() {
		assertValidationRule(
			rule = VariableDefinitionNameExclusivityRule,
			errors = emptyList(),
			document = """
				|query Q(${'$'}x: String) { field(arg: ${'$'}x) }
			""",
			schema = """
				|type Query { field(arg: String): String }
			"""
		)
	}


	@Ignore("Known bug: AllVariableUsesDefinedRule not implemented")
	@Test
	fun testRejectsUndefinedVariableUsed() {
		assertValidationRule(
			rule = VariableDefinitionNameExclusivityRule,
			errors = listOf("Variable '\$x' is not defined."),
			document = """
				|query Q { field(arg: ${'$'}x) }
			""",
			schema = """
				|type Query { field(arg: String): String }
			"""
		)
	}


	@Ignore("Known bug: AllVariableUsesDefinedRule not implemented")
	@Test
	fun testAcceptsVariableInFragmentWhereDefinedInOperation() {
		assertValidationRule(
			rule = VariableDefinitionNameExclusivityRule,
			errors = emptyList(),
			document = """
				|query Q(${'$'}x: String) { ...frag }
				|fragment frag on Query { field(arg: ${'$'}x) }
			""",
			schema = """
				|type Query { field(arg: String): String }
			"""
		)
	}
}
