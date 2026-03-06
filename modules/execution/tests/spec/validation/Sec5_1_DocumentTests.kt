package testing

import io.fluidsonic.graphql.*
import kotlin.test.*


// GraphQL Spec §5.1 — Executable Definitions
class Sec5_1_DocumentTests {

	@Test
	fun testAcceptsQueryOperation() {
		assertValidationRule(
			rule = DocumentExecutabilityRule,
			errors = emptyList(),
			document = """
				|{ field }
			""",
			schema = """
				|type Query { field: String }
			"""
		)
	}


	@Test
	fun testAcceptsFragmentAndOperation() {
		assertValidationRule(
			rule = DocumentExecutabilityRule,
			errors = emptyList(),
			document = """
				|query Q { field }
				|fragment f on Query { field }
			""",
			schema = """
				|type Query { field: String }
			"""
		)
	}


	@Test
	fun testRejectsSchemaDefinitionInDocument() {
		assertValidationRule(
			rule = DocumentExecutabilityRule,
			errors = listOf("""
				In order to be executable, the document must contain only executable definitions.

				<document>:2:1
				1 | { field }
				2 | type Foo { id: ID }
				  | ^
			"""),
			document = """
				|{ field }
				|type Foo { id: ID }
			""",
			schema = """
				|type Query { field: String }
			"""
		)
	}


	@Test
	fun testRejectsTypeExtensionInDocument() {
		assertValidationRule(
			rule = DocumentExecutabilityRule,
			errors = listOf("""
				In order to be executable, the document must contain only executable definitions.

				<document>:2:1
				1 | { field }
				2 | extend scalar S @foo
				  | ^
			"""),
			document = """
				|{ field }
				|extend scalar S @foo
			""",
			schema = """
				|type Query { field: String }
			"""
		)
	}


	@Test
	fun testAcceptsOnlyFragments() {
		assertValidationRule(
			rule = DocumentExecutabilityRule,
			errors = listOf("In order to be executable, the document must contain at least one operation definition."),
			document = """
				|fragment f on Query { field }
			""",
			schema = """
				|type Query { field: String }
			"""
		)
	}
}
