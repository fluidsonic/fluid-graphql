package io.fluidsonic.graphql

import tests.*
import kotlin.test.*


class ObjectFieldExistenceRuleTest : ValidationRule {

	@Test
	fun `accepts field names that exist`() {
		assertValidationRule(
			rule = ObjectFieldExistenceRule,
			errors = emptyList(),
			document = """
				|{
				|   id(input: { exists: true })
				|}
			""",
			schema = """
				|type Query {
				|   id(input: Input): ID
				|}
				|
				|input Input {
				|   exists: Boolean
				|}
			"""
		)
	}


	@Test
	fun `rejects field names that don't exist`() {
		assertValidationRule(
			rule = ObjectFieldExistenceRule,
			errors = listOf(
				"""
					Unknown field 'doesNotExist' for Input type 'Input'.

					<document>:4:10
					3 |       input: {
					4 |          doesNotExist: true
					  |          ^
					5 |          nested: { doesNotExist: true }

					<document>:5:7
					4 | 
					5 | input Input {
					  |       ^
					6 |    exists: Boolean
				""",
				"""
					Unknown field 'doesNotExist' for Input type 'OtherInput'.

					<document>:5:20
					4 |          doesNotExist: true
					5 |          nested: { doesNotExist: true }
					  |                    ^
					6 |       }

					<document>:10:7
					 9 | 
					10 | input OtherInput {
					   |       ^
					11 |    exists: Boolean
				"""
			),
			document = """
				|{
				|   id(
				|      input: {
				|         doesNotExist: true
				|         nested: { doesNotExist: true }
                |      }
				|   )
				|}
			""",
			schema = """
				|type Query {
				|   id(input: Input): ID
				|}
				|
				|input Input {
				|   exists: Boolean
				|   nested: OtherInput
				|}
				|
				|input OtherInput {
				|   exists: Boolean
				|}
			"""
		)
	}
}
