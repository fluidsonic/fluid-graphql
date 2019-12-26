package io.fluidsonic.graphql

import tests.*
import kotlin.test.*


class OperationHasUniqueNameRuleTest : ValidationRule {

	@Test
	fun `accepts unique operation names`() {
		assertValidationRule(
			rule = OperationHasUniqueNameRule,
			errors = emptyList(),
			document = """
				|query q1 { id: ID }
				|query q2 { id: ID }
				|query q3 { id: ID }
			""",
			schema = "type Query { id: ID }"
		)
	}


	@Test
	fun `ignores anonymous operations`() {
		assertValidationRule(
			rule = OperationHasUniqueNameRule,
			errors = emptyList(),
			document = """
				|query q1 { id: ID }
				|query q2 { id: ID }
				|query q3 { id: ID }
				|{ id: ID }
				|{ id: ID }
				|{ id: ID }
			""",
			schema = "type Query { id: ID }"
		)
	}


	@Test
	fun `rejects duplicate operation names`() {
		assertValidationRule(
			rule = OperationHasUniqueNameRule,
			errors = listOf("""
				The document must not contain multiple operations with the same name 'q'.

				<document>:1:1
				1 | query q { id: ID }
				  | ^
				2 | query q { id: ID }

				<document>:2:1
				1 | query q { id: ID }
				2 | query q { id: ID }
				  | ^
				3 | query q { id: ID }

				<document>:3:1
				2 | query q { id: ID }
				3 | query q { id: ID }
				  | ^
			"""),
			document = """
				|query q { id: ID }
				|query q { id: ID }
				|query q { id: ID }
			""",
			schema = "type Query { id: ID }"
		)
	}


	@Test
	fun `reports all problematic operations`() {
		assertValidationRule(
			rule = OperationHasUniqueNameRule,
			errors = listOf(
				"""
					The document must not contain multiple operations with the same name 'q'.

					<document>:1:1
					1 | query q { id: ID }
					  | ^
					2 | query q { id: ID }

					<document>:2:1
					1 | query q { id: ID }
					2 | query q { id: ID }
					  | ^
					3 | query q { id: ID }

					<document>:3:1
					2 | query q { id: ID }
					3 | query q { id: ID }
					  | ^
					4 | query r { id: ID }
				""",
				"""
					The document must not contain multiple operations with the same name 'r'.

					<document>:4:1
					3 | query q { id: ID }
					4 | query r { id: ID }
					  | ^
					5 | query r { id: ID }

					<document>:5:1
					4 | query r { id: ID }
					5 | query r { id: ID }
					  | ^
					6 | query r { id: ID }

					<document>:6:1
					5 | query r { id: ID }
					6 | query r { id: ID }
					  | ^
				"""
			),
			document = """
				|query q { id: ID }
				|query q { id: ID }
				|query q { id: ID }
				|query r { id: ID }
				|query r { id: ID }
				|query r { id: ID }
			""",
			schema = "type Query { id: ID }"
		)
	}
}
