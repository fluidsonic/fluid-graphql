package testing

import io.fluidsonic.graphql.*
import kotlin.test.*


// GraphQL Spec §5.3.2 — Field Selection Merging
class Sec5_3_2_FieldSelectionMergingTests {

	@Test
	fun testAcceptsIdenticalFields() {
		assertValidationRule(
			rule = SelectionUnambiguityRule,
			errors = emptyList(),
			document = """
				|{ foo foo }
			""",
			schema = """
				|type Query { foo: String }
			"""
		)
	}


	@Test
	fun testAcceptsIdenticalAliasedFields() {
		assertValidationRule(
			rule = SelectionUnambiguityRule,
			errors = emptyList(),
			document = """
				|{ x: foo x: foo }
			""",
			schema = """
				|type Query { foo: String }
			"""
		)
	}


	@Test
	fun testRejectsDifferentReturnTypeSameAlias() {
		assertValidationRule(
			rule = SelectionUnambiguityRule,
			errors = listOf("""
				Field 'id' in 'Query' is selected in multiple locations but with incompatible types.

				<document>:1:3
				1 | { id id: foo }
				  |   ^

				<document>:1:18
				1 | type Query { id: ID, foo: Int }
				  |                  ^

				<document>:1:6
				1 | { id id: foo }
				  |      ^

				<document>:1:27
				1 | type Query { id: ID, foo: Int }
				  |                           ^
			"""),
			document = """
				|{ id id: foo }
			""",
			schema = """
				|type Query { id: ID, foo: Int }
			"""
		)
	}


	@Test
	fun testAcceptsSameAliasOnFragments() {
		assertValidationRule(
			rule = SelectionUnambiguityRule,
			errors = emptyList(),
			document = """
				|{
				|  ...frag1
				|  ...frag2
				|}
				|fragment frag1 on Query { foo }
				|fragment frag2 on Query { foo }
			""",
			schema = """
				|type Query { foo: String }
			"""
		)
	}


	@Test
	fun testRejectsConflictingArgsSameAlias() {
		assertValidationRule(
			rule = SelectionUnambiguityRule,
			errors = listOf("""
				Field 'x' in 'Query' is selected in multiple locations but selects different fields or with different arguments.

				<document>:1:13
				1 | { x: foo(a: 1) x: foo(a: 2) }
				  |             ^

				<document>:1:26
				1 | { x: foo(a: 1) x: foo(a: 2) }
				  |                          ^
			"""),
			document = """
				|{ x: foo(a: 1) x: foo(a: 2) }
			""",
			schema = """
				|type Query { foo(a: Int): String }
			"""
		)
	}


	@Test
	fun testAcceptsIdenticalFieldArgs() {
		assertValidationRule(
			rule = SelectionUnambiguityRule,
			errors = emptyList(),
			document = """
				|{ foo(a: 1) foo(a: 1) }
			""",
			schema = """
				|type Query { foo(a: Int): String }
			"""
		)
	}
}
