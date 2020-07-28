package testing

import io.fluidsonic.graphql.*
import kotlin.test.*


class VariableDefinitionNameExclusivityRuleTest {

	@Test
	fun testAcceptsUniqueVariableNames() {
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
	fun testRejectsDuplicateVariableNames() {
		assertValidationRule(
			rule = VariableDefinitionNameExclusivityRule,
			errors = listOf(
				"""
					Operation 'someQuery' must not contain multiple variables with the same name '${'$'}b'.

					<document>:1:27
					1 | query someQuery(${'$'}a: Int, ${'$'}b: Int, ${'$'}b: Int, ${'$'}c: Int, ${'$'}c: Int, ${'$'}c: Int) {
					  |                           ^
					2 |   id

					<document>:1:36
					1 | query someQuery(${'$'}a: Int, ${'$'}b: Int, ${'$'}b: Int, ${'$'}c: Int, ${'$'}c: Int, ${'$'}c: Int) {
					  |                                    ^
					2 |   id
				""",
				"""
					Operation 'someQuery' must not contain multiple variables with the same name '${'$'}c'.

					<document>:1:45
					1 | query someQuery(${'$'}a: Int, ${'$'}b: Int, ${'$'}b: Int, ${'$'}c: Int, ${'$'}c: Int, ${'$'}c: Int) {
					  |                                             ^
					2 |   id

					<document>:1:54
					1 | query someQuery(${'$'}a: Int, ${'$'}b: Int, ${'$'}b: Int, ${'$'}c: Int, ${'$'}c: Int, ${'$'}c: Int) {
					  |                                                      ^
					2 |   id

					<document>:1:63
					1 | query someQuery(${'$'}a: Int, ${'$'}b: Int, ${'$'}b: Int, ${'$'}c: Int, ${'$'}c: Int, ${'$'}c: Int) {
					  |                                                               ^
					2 |   id
				""",
				"""
					Fragment 'frag' must not contain multiple variables with the same name '${'$'}b'.

					<document>:5:25
					4 | 
					5 | fragment frag(${'$'}a: Int, ${'$'}b: Int, ${'$'}b: Int, ${'$'}c: Int, ${'$'}c: Int, ${'$'}c: Int) on Query {
					  |                         ^
					6 |   id

					<document>:5:34
					4 | 
					5 | fragment frag(${'$'}a: Int, ${'$'}b: Int, ${'$'}b: Int, ${'$'}c: Int, ${'$'}c: Int, ${'$'}c: Int) on Query {
					  |                                  ^
					6 |   id
				""",
				"""
					Fragment 'frag' must not contain multiple variables with the same name '${'$'}c'.

					<document>:5:43
					4 | 
					5 | fragment frag(${'$'}a: Int, ${'$'}b: Int, ${'$'}b: Int, ${'$'}c: Int, ${'$'}c: Int, ${'$'}c: Int) on Query {
					  |                                           ^
					6 |   id

					<document>:5:52
					4 | 
					5 | fragment frag(${'$'}a: Int, ${'$'}b: Int, ${'$'}b: Int, ${'$'}c: Int, ${'$'}c: Int, ${'$'}c: Int) on Query {
					  |                                                    ^
					6 |   id

					<document>:5:61
					4 | 
					5 | fragment frag(${'$'}a: Int, ${'$'}b: Int, ${'$'}b: Int, ${'$'}c: Int, ${'$'}c: Int, ${'$'}c: Int) on Query {
					  |                                                             ^
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
