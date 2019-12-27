package io.fluidsonic.graphql

import tests.*
import kotlin.test.*


class ObjectFieldRequirementRuleTest : ValidationRule {

	@Test
	fun `accepts absence of optional fields`() {
		assertValidationRule(
			rule = ObjectFieldRequirementRule,
			errors = emptyList(),
			document = "{ id(input: { required: true }) }",
			schema = """
				|type Query {
				|   id(input: Input): ID
				|}
				|
				|input Input {
				|   optional: Boolean! = true
				|   required: Boolean!
				|}
			"""
		)
	}


	@Test
	fun `rejects absence of required fields`() {
		assertValidationRule(
			rule = ObjectFieldRequirementRule,
			errors = listOf(
				"""
					Value for Input type 'Input' is missing required fields 'required1' and 'required2'.

					<document>:1:13
					1 | { id(input: {}) }
					  |             ^

					<document>:7:4
					6 |    optional: Boolean! = true
					7 |    required1: Boolean!
					  |    ^
					8 |    required2: Boolean!

					<document>:8:4
					7 |    required1: Boolean!
					8 |    required2: Boolean!
					  |    ^
					9 | }
				"""
			),
			document = "{ id(input: {}) }",
			schema = """
				|type Query {
				|   id(input: Input): ID
				|}
				|
				|input Input {
				|   optional: Boolean! = true
				|   required1: Boolean!
				|   required2: Boolean!
				|}
			"""
		)
	}
}
