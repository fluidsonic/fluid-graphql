package testing

import io.fluidsonic.graphql.*
import kotlin.test.*


// GraphQL Spec §5.7.2 — Directives In Valid Locations
class Sec5_7_2_DirectivesInValidLocationsTests {

	@Test
	fun testAcceptsSkipOnField() {
		assertValidationRule(
			rule = DirectiveLocationValidityRule,
			errors = emptyList(),
			document = """
				|{ field @skip(if: false) }
			""",
			schema = """
				|type Query { field: String }
			"""
		)
	}


	@Test
	fun testAcceptsIncludeOnField() {
		assertValidationRule(
			rule = DirectiveLocationValidityRule,
			errors = emptyList(),
			document = """
				|{ field @include(if: true) }
			""",
			schema = """
				|type Query { field: String }
			"""
		)
	}


	@Test
	fun testAcceptsSkipOnFragmentSpread() {
		assertValidationRule(
			rule = DirectiveLocationValidityRule,
			errors = emptyList(),
			document = """
				|{ ...frag @skip(if: false) }
				|fragment frag on Query { field }
			""",
			schema = """
				|type Query { field: String }
			"""
		)
	}


	@Test
	fun testAcceptsSkipOnInlineFragment() {
		assertValidationRule(
			rule = DirectiveLocationValidityRule,
			errors = emptyList(),
			document = """
				|{ ... on Query @skip(if: false) { field } }
			""",
			schema = """
				|type Query { field: String }
			"""
		)
	}


	@Test
	fun testRejectsFieldDirectiveOnOperation() {
		assertValidationRule(
			rule = DirectiveLocationValidityRule,
			errors = listOf("""
				Directive '@onField' is not valid on QUERY but only on FIELD.

				<document>:1:8
				1 | query @onField { field }
				  |        ^

				<document>:2:23
				1 | type Query { field: String }
				2 | directive @onField on FIELD
				  |                       ^
			"""),
			document = """
				|query @onField { field }
			""",
			schema = """
				|type Query { field: String }
				|directive @onField on FIELD
			"""
		)
	}
}
