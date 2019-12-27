package io.fluidsonic.graphql

import tests.*
import kotlin.test.*


class VariableDefinitionNameExclusivityRuleTest : ValidationRule {

	@Test
	fun `accepts unique variable names`() {
		assertValidationRule(
			rule = VariableDefinitionNameExclusivityRule,
			errors = emptyList(),
			document = """
				|query someQuery(${'$'}a: Int, ${'$'}b: Int) {
				|  id
				|}
				|
				|fragment frag(${'$'}a: Int, ${'$'}b: Int) on Query {
				|  id
				|}
			""",
			schema = "type Query { id: ID }"
		)
	}


	@Test
	fun `rejects duplicate variable names`() {
		assertValidationRule(
			rule = VariableDefinitionNameExclusivityRule,
			errors = listOf(
				"""
					Operation 'someQuery' must not contain multiple variables with the same name '${'$'}b'.

					<document>:1:26
					1 | query someQuery(${'$'}a: Int, ${'$'}b: Int, ${'$'}b: Int, ${'$'}c: Int, ${'$'}c: Int, ${'$'}c: Int) {
					  |                          ^
					2 |   id

					<document>:1:35
					1 | query someQuery(${'$'}a: Int, ${'$'}b: Int, ${'$'}b: Int, ${'$'}c: Int, ${'$'}c: Int, ${'$'}c: Int) {
					  |                                   ^
					2 |   id
				""",
				"""
					Operation 'someQuery' must not contain multiple variables with the same name '${'$'}c'.

					<document>:1:44
					1 | query someQuery(${'$'}a: Int, ${'$'}b: Int, ${'$'}b: Int, ${'$'}c: Int, ${'$'}c: Int, ${'$'}c: Int) {
					  |                                            ^
					2 |   id

					<document>:1:53
					1 | query someQuery(${'$'}a: Int, ${'$'}b: Int, ${'$'}b: Int, ${'$'}c: Int, ${'$'}c: Int, ${'$'}c: Int) {
					  |                                                     ^
					2 |   id

					<document>:1:62
					1 | query someQuery(${'$'}a: Int, ${'$'}b: Int, ${'$'}b: Int, ${'$'}c: Int, ${'$'}c: Int, ${'$'}c: Int) {
					  |                                                              ^
					2 |   id
				""",
				"""
					Fragment 'frag' must not contain multiple variables with the same name '${'$'}b'.

					<document>:5:24
					4 | 
					5 | fragment frag(${'$'}a: Int, ${'$'}b: Int, ${'$'}b: Int, ${'$'}c: Int, ${'$'}c: Int, ${'$'}c: Int) on Query {
					  |                        ^
					6 |   id

					<document>:5:33
					4 | 
					5 | fragment frag(${'$'}a: Int, ${'$'}b: Int, ${'$'}b: Int, ${'$'}c: Int, ${'$'}c: Int, ${'$'}c: Int) on Query {
					  |                                 ^
					6 |   id
				""",
				"""
					Fragment 'frag' must not contain multiple variables with the same name '${'$'}c'.

					<document>:5:42
					4 | 
					5 | fragment frag(${'$'}a: Int, ${'$'}b: Int, ${'$'}b: Int, ${'$'}c: Int, ${'$'}c: Int, ${'$'}c: Int) on Query {
					  |                                          ^
					6 |   id

					<document>:5:51
					4 | 
					5 | fragment frag(${'$'}a: Int, ${'$'}b: Int, ${'$'}b: Int, ${'$'}c: Int, ${'$'}c: Int, ${'$'}c: Int) on Query {
					  |                                                   ^
					6 |   id

					<document>:5:60
					4 | 
					5 | fragment frag(${'$'}a: Int, ${'$'}b: Int, ${'$'}b: Int, ${'$'}c: Int, ${'$'}c: Int, ${'$'}c: Int) on Query {
					  |                                                            ^
					6 |   id
				"""
			),
			document = """
				|query someQuery(${'$'}a: Int, ${'$'}b: Int, ${'$'}b: Int, ${'$'}c: Int, ${'$'}c: Int, ${'$'}c: Int) {
				|  id
				|}
				|
				|fragment frag(${'$'}a: Int, ${'$'}b: Int, ${'$'}b: Int, ${'$'}c: Int, ${'$'}c: Int, ${'$'}c: Int) on Query {
				|  id
				|}
			""",
			schema = "type Query { id: ID }"
		)
	}
}
