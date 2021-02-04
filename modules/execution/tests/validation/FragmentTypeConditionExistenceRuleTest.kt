package testing

import io.fluidsonic.graphql.*
import kotlin.test.*


class FragmentTypeConditionExistenceRuleTest {

	@Test
	fun testAcceptsInlineFragmentOnExistingType() {
		assertValidationRule(
			rule = FragmentTypeConditionExistenceRule,
			errors = emptyList(),
			document = """
				|fragment inlineFragment on Dog {
				|  ... on Dog {
				|    name
				|  }
				|}
				|
				|fragment inlineFragment2 on Dog {
				|  ... @include(if: true) {
				|    name
				|  }
				|}
			""",
			schema = """
				|type Query { id: ID }
				|type Dog { name: String }
			"""
		)
	}


	@Test
	fun testAcceptsFragmentsDefinitionOnExistingType() {
		assertValidationRule(
			rule = FragmentTypeConditionExistenceRule,
			errors = emptyList(),
			document = """
				|fragment correctType on Dog {
				|  name
				|}
			""",
			schema = """
				|type Query { id: ID }
				|type Dog { name: String }
			"""
		)
	}


	@Test
	fun testRejectsInlineFragmentOnNonexistentType() {
		assertValidationRule(
			rule = FragmentTypeConditionExistenceRule,
			errors = listOf(
				"""
					A fragment spread must be specified on a type that exist in the schema.

					<document>:2:10
					1 | fragment inlineNotExistingType on Dog {
					2 |   ... on NotInSchema {
					  |          ^
					3 |     name
				"""
			),
			document = """
				|fragment inlineNotExistingType on Dog {
				|  ... on NotInSchema {
				|    name
				|  }
				|}
			""",
			schema = """
				|type Query { id: ID }
				|type Dog { name: String }
			"""
		)
	}


	@Test
	fun testRejectsFragmentDefinitionOnNonexistentType() {
		assertValidationRule(
			rule = FragmentTypeConditionExistenceRule,
			errors = listOf(
				"""
					A fragment must be specified on a type that exist in the schema.

					<document>:1:31
					1 | fragment notOnExistingType on NotInSchema {
					  |                               ^
					2 |   name
				"""
			),
			document = """
				|fragment notOnExistingType on NotInSchema {
				|  name
				|}
			""",
			schema = """
				|type Query { id: ID }
				|type Dog { name: String }
			"""
		)
	}
}
