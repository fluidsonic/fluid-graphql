package testing

import io.fluidsonic.graphql.*
import kotlin.test.*


class OperationDefinitionNameExclusivityRuleTest {

	@Test
	fun testAcceptsUniqueOperationNames() {
		assertValidationRule(
			rule = OperationDefinitionNameExclusivityRule,
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
	fun testIgnoresAnonymousOperations() {
		assertValidationRule(
			rule = OperationDefinitionNameExclusivityRule,
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
	fun testRejectsDuplicateOperationNames() {
		assertValidationRule(
			rule = OperationDefinitionNameExclusivityRule,
			errors = listOf(
				"""
					The document must not contain multiple operations with the same name 'q'.

					<document>:1:7
					1 | query q { id: ID }
					  |       ^
					2 | query q { id: ID }

					<document>:2:7
					1 | query q { id: ID }
					2 | query q { id: ID }
					  |       ^
					3 | query q { id: ID }

					<document>:3:7
					2 | query q { id: ID }
					3 | query q { id: ID }
					  |       ^
				"""
			),
			document = """
				|query q { id: ID }
				|query q { id: ID }
				|query q { id: ID }
			""",
			schema = "type Query { id: ID }"
		)
	}


	@Test
	fun testReportsAllProblematicOperations() {
		assertValidationRule(
			rule = OperationDefinitionNameExclusivityRule,
			errors = listOf(
				"""
					The document must not contain multiple operations with the same name 'q'.

					<document>:1:7
					1 | query q { id: ID }
					  |       ^
					2 | query q { id: ID }

					<document>:2:7
					1 | query q { id: ID }
					2 | query q { id: ID }
					  |       ^
					3 | query q { id: ID }

					<document>:3:7
					2 | query q { id: ID }
					3 | query q { id: ID }
					  |       ^
					4 | query r { id: ID }
				""",
				"""
					The document must not contain multiple operations with the same name 'r'.

					<document>:4:7
					3 | query q { id: ID }
					4 | query r { id: ID }
					  |       ^
					5 | query r { id: ID }

					<document>:5:7
					4 | query r { id: ID }
					5 | query r { id: ID }
					  |       ^
					6 | query r { id: ID }

					<document>:6:7
					5 | query r { id: ID }
					6 | query r { id: ID }
					  |       ^
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
