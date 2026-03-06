package testing

import io.fluidsonic.graphql.*
import kotlin.test.*


// GraphQL Spec §5.8.4 — All Variables Used
class Sec5_8_4_AllVariablesUsedTests {

	@Test
	fun testAcceptsVariableUsed() {
		assertValidationRule(
			rule = AllVariablesUsedRule,
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
	fun testRejectsVariableNotUsed() {
		assertValidationRule(
			rule = AllVariablesUsedRule,
			errors = listOf("""
				Variable '${'$'}x' is defined but never used.

				<document>:1:10
				1 | query Q(${'$'}x: String) { field }
				  |          ^
			"""),
			document = """
				|query Q(${'$'}x: String) { field }
			""",
			schema = """
				|type Query { field: String }
			"""
		)
	}


	@Test
	fun testAcceptsVariableUsedInFragment() {
		assertValidationRule(
			rule = AllVariablesUsedRule,
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
