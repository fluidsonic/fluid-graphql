package testing

import io.fluidsonic.graphql.*
import kotlin.test.*


// GraphQL Spec §5.4.2 — Argument Uniqueness
class Sec5_4_2_ArgumentUniquenessTests {

	@Ignore("Known bug: ArgumentUniquenessRule not implemented")
	@Test
	fun testAcceptsUniqueArgs() {
		assertValidationRule(
			rule = ArgumentExistenceRule,
			errors = emptyList(),
			document = """
				|{ field(a: 1, b: 2) }
			""",
			schema = """
				|type Query { field(a: Int, b: Int): String }
			"""
		)
	}


	@Ignore("Known bug: ArgumentUniquenessRule not implemented")
	@Test
	fun testRejectsDuplicateArgs() {
		assertValidationRule(
			rule = ArgumentExistenceRule,
			errors = listOf("Argument 'a' must not occur multiple times."),
			document = """
				|{ field(a: 1, a: 2) }
			""",
			schema = """
				|type Query { field(a: Int): String }
			"""
		)
	}
}
