package testing

import io.fluidsonic.graphql.*
import kotlin.test.*


class DirectiveExistenceRuleTest {

	@Test
	fun `accepts directive that exist`() {
		assertValidationRule(
			rule = DirectiveExistenceRule,
			errors = emptyList(),
			document = """
				|{
				|   id @foo
				|}
			""",
			schema = """
				|type Query {
				|   id: ID
				|}
				|
				|directive @foo on FIELD
			"""
		)
	}


	@Test
	fun `rejects directive that don't exist`() {
		assertValidationRule(
			rule = DirectiveExistenceRule,
			errors = listOf(
				"""
					Unknown directive '@bar'.

					<document>:2:8
					1 | {
					2 |    id @bar @baz
					  |        ^
					3 | }
				""",
				"""
					Unknown directive '@baz'.

					<document>:2:13
					1 | {
					2 |    id @bar @baz
					  |             ^
					3 | }
				"""
			),
			document = """
				|{
				|   id @bar @baz
				|}
			""",
			schema = """
				|type Query {
				|   id: ID
				|}
				|
				|directive @foo on FIELD
			"""
		)
	}
}
