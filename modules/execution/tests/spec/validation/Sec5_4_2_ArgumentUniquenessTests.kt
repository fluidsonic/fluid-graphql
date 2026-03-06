package testing

import io.fluidsonic.graphql.*
import kotlin.test.*


// GraphQL Spec §5.4.2 — Argument Uniqueness
class Sec5_4_2_ArgumentUniquenessTests {

	@Test
	fun testAcceptsUniqueArgs() {
		assertValidationRule(
			rule = ArgumentUniquenessRule,
			errors = emptyList(),
			document = """
				|{ field(a: 1, b: 2) }
			""",
			schema = """
				|type Query { field(a: Int, b: Int): String }
			"""
		)
	}


	@Test
	fun testRejectsDuplicateArgs() {
		assertValidationRule(
			rule = ArgumentUniquenessRule,
			errors = listOf("""
				Argument 'a' must not occur multiple times.

				<document>:1:9
				1 | { field(a: 1, a: 2) }
				  |         ^

				<document>:1:15
				1 | { field(a: 1, a: 2) }
				  |               ^
			"""),
			document = """
				|{ field(a: 1, a: 2) }
			""",
			schema = """
				|type Query { field(a: Int): String }
			"""
		)
	}
}
