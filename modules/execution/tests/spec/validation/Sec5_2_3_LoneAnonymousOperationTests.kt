package testing

import io.fluidsonic.graphql.*
import kotlin.test.*


// GraphQL Spec §5.2.3 — Lone Anonymous Operation
class Sec5_2_3_LoneAnonymousOperationTests {

	@Test
	fun testAcceptsZeroOperations() {
		assertValidationRule(
			rule = AnonymousOperationExclusivityRule,
			errors = emptyList(),
			document = """
				|fragment f on Query { id }
			""",
			schema = """
				|type Query { id: ID }
			"""
		)
	}


	@Test
	fun testAcceptsOneAnonymousOperation() {
		assertValidationRule(
			rule = AnonymousOperationExclusivityRule,
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
	fun testAcceptsTwoNamedOperations() {
		assertValidationRule(
			rule = AnonymousOperationExclusivityRule,
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
	fun testRejectsAnonymousWithNamed() {
		assertValidationRule(
			rule = AnonymousOperationExclusivityRule,
			errors = listOf("""
				The document must not contain more than one operation if it contains an anonymous operation.

				<document>:1:1
				1 | { id }
				  | ^
				2 | query Q { id }
			"""),
			document = """
				|{ id }
				|query Q { id }
			""",
			schema = """
				|type Query { id: ID }
			"""
		)
	}


	@Test
	fun testRejectsTwoAnonymousOperations() {
		assertValidationRule(
			rule = AnonymousOperationExclusivityRule,
			errors = listOf("""
				The document must not contain more than one operation if it contains an anonymous operation.

				<document>:1:1
				1 | { id }
				  | ^
				2 | { id }

				<document>:2:1
				1 | { id }
				2 | { id }
				  | ^
			"""),
			document = """
				|{ id }
				|{ id }
			""",
			schema = """
				|type Query { id: ID }
			"""
		)
	}


	@Test
	fun testRejectsMixedAnonymousAndNamed() {
		assertValidationRule(
			rule = AnonymousOperationExclusivityRule,
			errors = listOf("""
				The document must not contain more than one operation if it contains an anonymous operation.

				<document>:1:1
				1 | { id }
				  | ^
				2 | query q1 { id }
			"""),
			document = """
				|{ id }
				|query q1 { id }
				|query q2 { id }
			""",
			schema = """
				|type Query { id: ID }
			"""
		)
	}
}
