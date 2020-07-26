package testing

import io.fluidsonic.graphql.*
import kotlin.test.*


class DirectiveExclusivityRuleTest {

	@Test
	fun `accepts directives that occur only once or that are repeatable`() {
		assertValidationRule(
			rule = DirectiveExclusivityRule,
			errors = emptyList(),
			document = """
				|query {
				|  field @foo @bar @baz @baz
				|}
			""",
			schema = """
				|type Query { id: ID }
				|
				|directive @foo on FIELD
				|directive @bar on FIELD
				|directive @baz repeatable on FIELD
			"""
		)
	}


	@Test
	fun `rejects repeating directives that are not defined to be repeatable`() {
		assertValidationRule(
			rule = DirectiveExclusivityRule,
			errors = listOf(
				"""
					Directive '@foo' must not occur multiple times.

					<document>:2:10
					1 | query {
					2 |   field @foo @foo @bar @bar @bar @baz @baz
					  |          ^
					3 | }

					<document>:2:15
					1 | query {
					2 |   field @foo @foo @bar @bar @bar @baz @baz
					  |               ^
					3 | }
				""",
				"""
					Directive '@bar' must not occur multiple times.

					<document>:2:20
					1 | query {
					2 |   field @foo @foo @bar @bar @bar @baz @baz
					  |                    ^
					3 | }

					<document>:2:25
					1 | query {
					2 |   field @foo @foo @bar @bar @bar @baz @baz
					  |                         ^
					3 | }

					<document>:2:30
					1 | query {
					2 |   field @foo @foo @bar @bar @bar @baz @baz
					  |                              ^
					3 | }
				"""
			),
			document = """
				|query {
				|  field @foo @foo @bar @bar @bar @baz @baz
				|}
			""",
			schema = """
				|type Query { id: ID }
				|
				|directive @foo on FIELD
				|directive @bar on FIELD
				|directive @baz repeatable on FIELD
			"""
		)
	}
}
