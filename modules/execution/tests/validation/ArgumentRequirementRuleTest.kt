package testing

import io.fluidsonic.graphql.*
import kotlin.test.*


class ArgumentRequirementRuleTest {

	@Test
	fun testAcceptsAbsenceOfArgumentsWithDefaultValue() {
		assertValidationRule(
			rule = ArgumentRequirementRule,
			errors = emptyList(),
			document = "{ id }",
			schema = """
				|type Query {
				|   id(arg1: String, arg2: String! = "default"): ID
				|}
			"""
		)
	}


	@Test
	fun testAcceptsAbsenceOfOptionalArguments() {
		assertValidationRule(
			rule = ArgumentRequirementRule,
			errors = emptyList(),
			document = """
				|{
				|   id @foo
				|   withInput: id(input: {}) @foo(input: {})
				|}
			""",
			schema = """
				|input Input { string: String! @optional, input: Input! @optional }
				|type Query {
				|   id(string: String! @optional, input: Input! @optional): ID
				|}
				|directive @foo(string: String! @optional, input: Input! @optional) on FIELD
			"""
		)
	}


	@Test
	fun testRejectsAbsenceOfRequiredArguments() {
		assertValidationRule(
			rule = ArgumentRequirementRule,
			errors = listOf(
				"""
					Selection of field 'id' is missing required argument 'arg1'.

					<document>:1:3
					1 | { id @foo }
					  |   ^

					<document>:2:7
					1 | type Query {
					2 |    id(arg1: String!): ID
					  |       ^
					3 | }
				""",
				"""
					Directive '@foo' is missing required argument 'arg1'.

					<document>:1:7
					1 | { id @foo }
					  |       ^

					<document>:5:16
					4 | 
					5 | directive @foo(arg1: String!) on FIELD
					  |                ^
				"""
			),
			document = """
				|{ id @foo }
			""",
			schema = """
				|type Query {
				|   id(arg1: String!): ID
				|}
				|
				|directive @foo(arg1: String!) on FIELD
			"""
		)
	}


	@Test
	fun testRejectsAbsenceOfMultipleRequiredArguments() {
		assertValidationRule(
			rule = ArgumentRequirementRule,
			errors = listOf(
				"""
					Selection of field 'id' is missing required arguments 'arg1' and 'arg3'.

					<document>:1:3
					1 | { id @foo }
					  |   ^

					<document>:2:7
					1 | type Query {
					2 |    id(arg1: String!, arg2: String, arg3: Int!): ID
					  |       ^
					3 | }

					<document>:2:36
					1 | type Query {
					2 |    id(arg1: String!, arg2: String, arg3: Int!): ID
					  |                                    ^
					3 | }
				""",
				"""
					Directive '@foo' is missing required arguments 'arg1' and 'arg3'.

					<document>:1:7
					1 | { id @foo }
					  |       ^

					<document>:5:16
					4 | 
					5 | directive @foo(arg1: String!, arg2: String, arg3: Int!) on FIELD
					  |                ^

					<document>:5:45
					4 | 
					5 | directive @foo(arg1: String!, arg2: String, arg3: Int!) on FIELD
					  |                                             ^
				"""
			),
			document = """
				|{ id @foo }
			""",
			schema = """
				|type Query {
				|   id(arg1: String!, arg2: String, arg3: Int!): ID
				|}
				|
				|directive @foo(arg1: String!, arg2: String, arg3: Int!) on FIELD
			"""
		)
	}
}
