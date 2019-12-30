package io.fluidsonic.graphql

import tests.*
import kotlin.test.*


class VariableDefinitionTypeValidityRuleTest {

	@Test
	fun `accepts variables with input types`() {
		assertValidationRule(
			rule = VariableDefinitionTypeValidityRule,
			errors = emptyList(),
			document = """
				|query someQuery(${'$'}a: Boolean, ${'$'}b: Enum, ${'$'}c: Float, ${'$'}d: Input, ${'$'}e: Int, ${'$'}f: String) {
				|  id
				|}
				|
				|fragment frag(${'$'}a: Boolean, ${'$'}b: Enum, ${'$'}c: Float, ${'$'}d: Input, ${'$'}e: Int, ${'$'}f: String) on Query {
				|  id
				|}
			""",
			schema = """
				|type Query { id: ID }
				|input Input { id: ID }
			"""
		)
	}


	@Test
	fun `rejects duplicate variable names`() {
		assertValidationRule(
			rule = VariableDefinitionTypeValidityRule,
			errors = listOf(
				"""
					Variable '${'$'}a' cannot have output type 'Interface'.

					<document>:1:21
					1 | query someQuery(${'$'}a: Interface, ${'$'}b: Object, ${'$'}c: Union) {
					  |                     ^
					2 |   id

					<document>:2:11
					1 | type Query { id: ID }
					2 | interface Interface { id: ID }
					  |           ^
					3 | type Object { id: ID }
				""",
				"""
					Variable '${'$'}b' cannot have output type 'Object'.

					<document>:1:36
					1 | query someQuery(${'$'}a: Interface, ${'$'}b: Object, ${'$'}c: Union) {
					  |                                    ^
					2 |   id

					<document>:3:6
					2 | interface Interface { id: ID }
					3 | type Object { id: ID }
					  |      ^
					4 | type Union { id: ID }
				""",
				"""
					Variable '${'$'}c' cannot have output type 'Union'.

					<document>:1:48
					1 | query someQuery(${'$'}a: Interface, ${'$'}b: Object, ${'$'}c: Union) {
					  |                                                ^
					2 |   id

					<document>:4:6
					3 | type Object { id: ID }
					4 | type Union { id: ID }
					  |      ^
				""",
				"""
					Variable '${'$'}a' cannot have output type 'Interface'.

					<document>:5:19
					4 | 
					5 | fragment frag(${'$'}a: Interface, ${'$'}b: Object, ${'$'}c: Union) on Query {
					  |                   ^
					6 |   id

					<document>:2:11
					1 | type Query { id: ID }
					2 | interface Interface { id: ID }
					  |           ^
					3 | type Object { id: ID }
				""",
				"""
					Variable '${'$'}b' cannot have output type 'Object'.

					<document>:5:34
					4 | 
					5 | fragment frag(${'$'}a: Interface, ${'$'}b: Object, ${'$'}c: Union) on Query {
					  |                                  ^
					6 |   id

					<document>:3:6
					2 | interface Interface { id: ID }
					3 | type Object { id: ID }
					  |      ^
					4 | type Union { id: ID }
				""",
				"""
					Variable '${'$'}c' cannot have output type 'Union'.

					<document>:5:46
					4 | 
					5 | fragment frag(${'$'}a: Interface, ${'$'}b: Object, ${'$'}c: Union) on Query {
					  |                                              ^
					6 |   id

					<document>:4:6
					3 | type Object { id: ID }
					4 | type Union { id: ID }
					  |      ^
				"""
			),
			document = """
				|query someQuery(${'$'}a: Interface, ${'$'}b: Object, ${'$'}c: Union) {
				|  id
				|}
				|
				|fragment frag(${'$'}a: Interface, ${'$'}b: Object, ${'$'}c: Union) on Query {
				|  id
				|}
			""",
			schema = """
				|type Query { id: ID }
				|interface Interface { id: ID }
				|type Object { id: ID }
				|type Union { id: ID }
			"""
		)
	}
}
