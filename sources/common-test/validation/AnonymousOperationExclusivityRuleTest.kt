package io.fluidsonic.graphql

import tests.*
import kotlin.test.*


class AnonymousOperationExclusivityRuleTest : ValidationRule {

	@Test
	fun `accepts zero operations`() {
		assertValidationRule(
			rule = AnonymousOperationExclusivityRule,
			errors = emptyList(),
			document = "fragment f on Query { id: ID }",
			schema = "type Query { id: ID }"
		)
	}


	@Test
	fun `accepts one named operations`() {
		assertValidationRule(
			rule = AnonymousOperationExclusivityRule,
			errors = emptyList(),
			document = """
				|query q { id: ID }
				|fragment f on Query { id: ID }
			""",
			schema = "type Query { id: ID }"
		)
	}


	@Test
	fun `accepts one anonymous operations`() {
		assertValidationRule(
			rule = AnonymousOperationExclusivityRule,
			errors = emptyList(),
			document = """
				|{ id: ID }
				|fragment f on Query { id: ID }
			""",
			schema = "type Query { id: ID }"
		)
	}


	@Test
	fun `accepts two named operations`() {
		assertValidationRule(
			rule = AnonymousOperationExclusivityRule,
			errors = emptyList(),
			document = """
				|query q1 { id: ID }
				|query q2 { id: ID }
				|fragment f on Query { id: ID }
			""",
			schema = "type Query { id: ID }"
		)
	}


	@Test
	fun `rejects two anonymous operations`() {
		assertValidationRule(
			rule = AnonymousOperationExclusivityRule,
			errors = listOf("""
				The document must not contain more than one operation if it contains an anonymous operation.

				<document>:1:1
				1 | { id: ID }
				  | ^
				2 | { id: ID }

				<document>:2:1
				1 | { id: ID }
				2 | { id: ID }
				  | ^
				3 | fragment f on Query { id: ID }
			"""),
			document = """
				|{ id: ID }
				|{ id: ID }
				|fragment f on Query { id: ID }
			""",
			schema = "type Query { id: ID }"
		)
	}


	@Test
	fun `rejects one anonymous and one named operations`() {
		assertValidationRule(
			rule = AnonymousOperationExclusivityRule,
			errors = listOf("""
				The document must not contain more than one operation if it contains an anonymous operation.

				<document>:1:1
				1 | { id: ID }
				  | ^
				2 | query q { id: ID }
			"""),
			document = """
				|{ id: ID }
				|query q { id: ID }
				|fragment f on Query { id: ID }
			""",
			schema = "type Query { id: ID }"
		)
	}
}
