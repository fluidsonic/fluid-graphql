package testing

import io.fluidsonic.graphql.*
import kotlin.test.*


// GraphQL Spec §5.7.3 — Directives Are Unique Per Location
class Sec5_7_3_DirectivesUniquePerLocationTests {

	@Test
	fun testAcceptsSingleDirective() {
		assertValidationRule(
			rule = DirectiveExclusivityRule,
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
	fun testAcceptsTwoDifferentDirectives() {
		assertValidationRule(
			rule = DirectiveExclusivityRule,
			errors = emptyList(),
			document = """
				|{ field @skip(if: false) @include(if: true) }
			""",
			schema = """
				|type Query { field: String }
			"""
		)
	}


	@Test
	fun testRejectsDuplicateNonRepeatableDirective() {
		assertValidationRule(
			rule = DirectiveExclusivityRule,
			errors = listOf("""
				Directive '@skip' must not occur multiple times.

				<document>:1:10
				1 | { field @skip(if: false) @skip(if: true) }
				  |          ^

				<document>:1:27
				1 | { field @skip(if: false) @skip(if: true) }
				  |                           ^
			"""),
			document = """
				|{ field @skip(if: false) @skip(if: true) }
			""",
			schema = """
				|type Query { field: String }
			"""
		)
	}


	@Test
	fun testAcceptsRepeatableDirective() {
		assertValidationRule(
			rule = DirectiveExclusivityRule,
			errors = emptyList(),
			document = """
				|{ field @repeatable @repeatable }
			""",
			schema = """
				|type Query { field: String }
				|directive @repeatable repeatable on FIELD
			"""
		)
	}
}
