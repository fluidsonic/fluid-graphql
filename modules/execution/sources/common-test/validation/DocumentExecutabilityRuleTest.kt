package testing

import io.fluidsonic.graphql.*
import kotlin.test.*


class DocumentExecutabilityRuleTest {

	@Test
	fun `accepts single operation`() {
		assertValidationRule(
			rule = DocumentExecutabilityRule,
			errors = emptyList(),
			document = "{ id: ID }",
			schema = "type Query { id: ID }"
		)
	}


	@Test
	fun `rejects zero operations`() {
		assertValidationRule(
			rule = DocumentExecutabilityRule,
			errors = listOf("In order to be executable, the document must contain at least one operation definition."),
			document = """
				|fragment f on Query { id: ID }
			""",
			schema = "type Query { id: ID }"
		)
	}


	@Test
	fun `rejects type definitions`() {
		assertValidationRule(
			rule = DocumentExecutabilityRule,
			errors = listOf(
				"""
					In order to be executable, the document must contain only executable definitions.

					<document>:2:1
					1 | { id: ID }
					2 | scalar S
					  | ^
				"""
			),
			document = """
				|{ id: ID }
				|scalar S
			""",
			schema = "type Query { id: ID }"
		)
	}


	@Test
	fun `rejects type extensions`() {
		assertValidationRule(
			rule = DocumentExecutabilityRule,
			errors = listOf("""
				In order to be executable, the document must contain only executable definitions.

				<document>:2:1
				1 | { id: ID }
				2 | extend scalar S @foo
				  | ^
			"""),
			document = """
				|{ id: ID }
				|extend scalar S @foo
			""",
			schema = "type Query { id: ID }"
		)
	}
}
