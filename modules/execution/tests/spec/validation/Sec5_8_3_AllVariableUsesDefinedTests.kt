package testing

import io.fluidsonic.graphql.*
import kotlin.test.*


// GraphQL Spec §5.8.3 — All Variable Uses Defined
class Sec5_8_3_AllVariableUsesDefinedTests {

	@Test
	fun testAcceptsVariableUsedAfterDefined() {
		assertValidationRule(
			rule = AllVariableUsesDefinedRule,
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
	fun testRejectsUndefinedVariableUsed() {
		assertValidationRule(
			rule = AllVariableUsesDefinedRule,
			errors = listOf("""
				Variable '${'$'}x' is not defined.

				<document>:1:22
				1 | query Q { field(arg: ${'$'}x) }
				  |                      ^
			"""),
			document = """
				|query Q { field(arg: ${'$'}x) }
			""",
			schema = """
				|type Query { field(arg: String): String }
			"""
		)
	}


	@Test
	fun testAcceptsVariableInFragmentWhereDefinedInOperation() {
		assertValidationRule(
			rule = AllVariableUsesDefinedRule,
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
