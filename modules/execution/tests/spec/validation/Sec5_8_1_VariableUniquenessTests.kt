package testing

import io.fluidsonic.graphql.*
import kotlin.test.*


// GraphQL Spec §5.8.1 — Variable Uniqueness
class Sec5_8_1_VariableUniquenessTests {

	@Test
	fun testAcceptsUniqueVariables() {
		assertValidationRule(
			rule = VariableDefinitionNameExclusivityRule,
			errors = emptyList(),
			document = """
				|query Q(${'$'}a: Int, ${'$'}b: String) { id }
			""",
			schema = """
				|type Query { id: ID }
			"""
		)
	}


	@Test
	fun testRejectsDuplicateVariables() {
		assertValidationRule(
			rule = VariableDefinitionNameExclusivityRule,
			errors = listOf("""
				Operation 'Q' must not contain multiple variables with the same name '${'$'}a'.

				<document>:1:10
				1 | query Q(${'$'}a: Int, ${'$'}a: String) { id }
				  |          ^

				<document>:1:19
				1 | query Q(${'$'}a: Int, ${'$'}a: String) { id }
				  |                   ^
			"""),
			document = """
				|query Q(${'$'}a: Int, ${'$'}a: String) { id }
			""",
			schema = """
				|type Query { id: ID }
			"""
		)
	}


	@Test
	fun testAcceptsUniqueAcrossOperations() {
		assertValidationRule(
			rule = VariableDefinitionNameExclusivityRule,
			errors = emptyList(),
			document = """
				|query Q1(${'$'}a: Int) { id }
				|query Q2(${'$'}a: Int) { id }
			""",
			schema = """
				|type Query { id: ID }
			"""
		)
	}
}
