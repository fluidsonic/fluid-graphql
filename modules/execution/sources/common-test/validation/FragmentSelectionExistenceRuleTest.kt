package testing

import io.fluidsonic.graphql.*
import kotlin.test.*


class FragmentSelectionExistenceRuleTest {

	@Test
	fun `accepts selections of existing fragments`() {
		assertValidationRule(
			rule = FragmentSelectionExistenceRule,
			errors = emptyList(),
			document = """
				|{
				|  dog {
				|    ...nameFragment
				|  }
				|}
				|
				|fragment nameFragment on Dog {
				|  name
				|}
			""",
			schema = """
				|type Query { dog: Dog }
				|type Dog { name: String }
			"""
		)
	}


	@Test
	fun `rejects selections of nonexistent fragments`() {
		assertValidationRule(
			rule = FragmentSelectionExistenceRule,
			errors = listOf(
				"""
					Fragment 'undefined' does not exist.

					<document>:2:6
					1 | {
					2 |   ...undefined
					  |      ^
					3 | }
				"""
			),
			document = """
				|{
				|  ...undefined
				|}
			""",
			schema = """
				|type Query { id: ID }
			"""
		)
	}
}
