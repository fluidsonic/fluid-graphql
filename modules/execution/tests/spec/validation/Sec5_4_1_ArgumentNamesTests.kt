package testing

import io.fluidsonic.graphql.*
import kotlin.test.*


// GraphQL Spec §5.4.1 — Argument Names
class Sec5_4_1_ArgumentNamesTests {

	@Test
	fun testAcceptsKnownArgOnField() {
		assertValidationRule(
			rule = ArgumentExistenceRule,
			errors = emptyList(),
			document = """
				|{ field(arg: "value") }
			""",
			schema = """
				|type Query { field(arg: String): String }
			"""
		)
	}


	@Test
	fun testAcceptsKnownArgOnDirective() {
		assertValidationRule(
			rule = ArgumentExistenceRule,
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
	fun testRejectsUnknownArgOnField() {
		assertValidationRule(
			rule = ArgumentExistenceRule,
			errors = listOf("""
				Unknown argument 'unknown' for field 'Query.field'.

				<document>:1:9
				1 | { field(unknown: "value") }
				  |         ^
			"""),
			document = """
				|{ field(unknown: "value") }
			""",
			schema = """
				|type Query { field(arg: String): String }
			"""
		)
	}


	@Test
	fun testRejectsUnknownArgOnDirective() {
		assertValidationRule(
			rule = ArgumentExistenceRule,
			errors = listOf("""
				Unknown argument 'unknown' for directive 'skip'.

				<document>:1:15
				1 | { field @skip(unknown: false) }
				  |               ^
			"""),
			document = """
				|{ field @skip(unknown: false) }
			""",
			schema = """
				|type Query { field: String }
			"""
		)
	}


	@Test
	fun testAcceptsMultipleValidArgs() {
		assertValidationRule(
			rule = ArgumentExistenceRule,
			errors = emptyList(),
			document = """
				|{ field(arg1: "a", arg2: 1) }
			""",
			schema = """
				|type Query { field(arg1: String, arg2: Int): String }
			"""
		)
	}
}
