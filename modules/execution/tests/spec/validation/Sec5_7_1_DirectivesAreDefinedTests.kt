package testing

import io.fluidsonic.graphql.*
import kotlin.test.*


// GraphQL Spec §5.7.1 — Directives Are Defined
class Sec5_7_1_DirectivesAreDefinedTests {

	@Test
	fun testAcceptsKnownDirective() {
		assertValidationRule(
			rule = DirectiveExistenceRule,
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
	fun testRejectsUnknownDirective() {
		assertValidationRule(
			rule = DirectiveExistenceRule,
			errors = listOf("""
				Unknown directive '@unknown'.

				<document>:1:10
				1 | { field @unknown }
				  |          ^
			"""),
			document = """
				|{ field @unknown }
			""",
			schema = """
				|type Query { field: String }
			"""
		)
	}


	@Test
	fun testAcceptsCustomDefinedDirective() {
		assertValidationRule(
			rule = DirectiveExistenceRule,
			errors = emptyList(),
			document = """
				|{ field @myDirective }
			""",
			schema = """
				|type Query { field: String }
				|directive @myDirective on FIELD
			"""
		)
	}


	@Test
	fun testAcceptsIncludeDirective() {
		assertValidationRule(
			rule = DirectiveExistenceRule,
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
	fun testRejectsMultipleUnknownDirectives() {
		assertValidationRule(
			rule = DirectiveExistenceRule,
			errors = listOf(
				"""
					Unknown directive '@unknown1'.

					<document>:1:10
					1 | { field @unknown1 @unknown2 }
					  |          ^
				""",
				"""
					Unknown directive '@unknown2'.

					<document>:1:20
					1 | { field @unknown1 @unknown2 }
					  |                    ^
				"""
			),
			document = """
				|{ field @unknown1 @unknown2 }
			""",
			schema = """
				|type Query { field: String }
			"""
		)
	}
}
