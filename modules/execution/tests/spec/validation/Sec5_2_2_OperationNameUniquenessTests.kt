package testing

import io.fluidsonic.graphql.*
import kotlin.test.*


// GraphQL Spec §5.2.2 — Operation Name Uniqueness
class Sec5_2_2_OperationNameUniquenessTests {

	@Test
	fun testAcceptsTwoUniqueNames() {
		assertValidationRule(
			rule = OperationDefinitionNameExclusivityRule,
			errors = emptyList(),
			document = """
				|query q1 { id }
				|query q2 { id }
			""",
			schema = """
				|type Query { id: ID }
			"""
		)
	}


	@Test
	fun testAcceptsSingleAnonymousOperation() {
		assertValidationRule(
			rule = OperationDefinitionNameExclusivityRule,
			errors = emptyList(),
			document = """
				|{ id }
			""",
			schema = """
				|type Query { id: ID }
			"""
		)
	}


	@Test
	fun testRejectsDuplicateNames() {
		assertValidationRule(
			rule = OperationDefinitionNameExclusivityRule,
			errors = listOf("""
				The document must not contain multiple operations with the same name 'q'.

				<document>:1:7
				1 | query q { id }
				  |       ^
				2 | query q { id }

				<document>:2:7
				1 | query q { id }
				2 | query q { id }
				  |       ^
			"""),
			document = """
				|query q { id }
				|query q { id }
			""",
			schema = """
				|type Query { id: ID }
			"""
		)
	}


	@Test
	fun testRejectsThreeSameNames() {
		assertValidationRule(
			rule = OperationDefinitionNameExclusivityRule,
			errors = listOf("""
				The document must not contain multiple operations with the same name 'q'.

				<document>:1:7
				1 | query q { id }
				  |       ^
				2 | query q { id }

				<document>:2:7
				1 | query q { id }
				2 | query q { id }
				  |       ^
				3 | query q { id }

				<document>:3:7
				2 | query q { id }
				3 | query q { id }
				  |       ^
			"""),
			document = """
				|query q { id }
				|query q { id }
				|query q { id }
			""",
			schema = """
				|type Query { id: ID }
			"""
		)
	}


	@Test
	fun testAcceptsDifferentOperationTypes() {
		assertValidationRule(
			rule = OperationDefinitionNameExclusivityRule,
			errors = emptyList(),
			document = """
				|query GetData { id }
				|mutation UpdateData { id }
			""",
			schema = """
				|type Query { id: ID }
				|type Mutation { id: ID }
			"""
		)
	}
}
