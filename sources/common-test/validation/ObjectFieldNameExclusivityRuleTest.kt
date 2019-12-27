package io.fluidsonic.graphql

import tests.*
import kotlin.test.*


class ObjectFieldNameExclusivityRuleTest : ValidationRule {

	@Test
	fun `accepts unique object field names`() {
		assertValidationRule(
			rule = ObjectFieldNameExclusivityRule,
			errors = emptyList(),
			document = """
				|{
				|  fun(input: { a:1 b:2 c:3 })
				|}
			""",
			schema = "type Query { id: ID }"
		)
	}


	@Test
	fun `rejects duplicate object field names`() {
		assertValidationRule(
			rule = ObjectFieldNameExclusivityRule,
			errors = listOf(
				"""
					An input object can only have a single field named 'a'.

					<document>:2:16
					1 | {
					2 |   fun(input: { a:1 a:1 a:2 b:1 b:2 c:3 })
					  |                ^
					3 | }

					<document>:2:20
					1 | {
					2 |   fun(input: { a:1 a:1 a:2 b:1 b:2 c:3 })
					  |                    ^
					3 | }

					<document>:2:24
					1 | {
					2 |   fun(input: { a:1 a:1 a:2 b:1 b:2 c:3 })
					  |                        ^
					3 | }
				""",
				"""
					An input object can only have a single field named 'b'.

					<document>:2:28
					1 | {
					2 |   fun(input: { a:1 a:1 a:2 b:1 b:2 c:3 })
					  |                            ^
					3 | }

					<document>:2:32
					1 | {
					2 |   fun(input: { a:1 a:1 a:2 b:1 b:2 c:3 })
					  |                                ^
					3 | }
				"""
			),
			document = """
				|{
				|  fun(input: { a:1 a:1 a:2 b:1 b:2 c:3 })
				|}
			""",
			schema = "type Query { id: ID }"
		)
	}
}
