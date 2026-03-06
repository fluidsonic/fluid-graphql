package testing

import io.fluidsonic.graphql.*
import kotlin.test.*


// GraphQL Spec §5.8.4 — All Variables Used
class Sec5_8_4_AllVariablesUsedTests {

	@Ignore("Known bug: AllVariablesUsedRule not implemented")
	@Test
	fun testAcceptsVariableUsed() {
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


	@Ignore("Known bug: AllVariablesUsedRule not implemented")
	@Test
	fun testRejectsVariableNotUsed() {
		assertValidationRule(
			rule = VariableDefinitionNameExclusivityRule,
			errors = listOf("Variable '\$x' is defined but never used."),
			document = """
				|query Q(${'$'}x: String) { field }
			""",
			schema = """
				|type Query { field: String }
			"""
		)
	}


	@Ignore("Known bug: AllVariablesUsedRule not implemented")
	@Test
	fun testAcceptsVariableUsedInFragment() {
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
