package testing

import io.fluidsonic.graphql.*
import kotlin.test.*


// GraphQL Spec §5.5.4 — Fragments Must Be Used
class Sec5_5_4_FragmentsMustBeUsedTests {

	@Test
	fun testAcceptsUsedFragment() {
		assertValidationRule(
			rule = FragmentDefinitionUsageRule,
			errors = emptyList(),
			document = """
				|{ dog { ...dogFields } }
				|fragment dogFields on Dog { name }
			""",
			schema = """
				|type Query { dog: Dog }
				|type Dog { name: String }
			"""
		)
	}


	@Test
	fun testRejectsUnusedFragment() {
		assertValidationRule(
			rule = FragmentDefinitionUsageRule,
			errors = listOf("""
				Fragment 'dogFields' is not used by any operation.

				<document>:2:10
				1 | { dog { name } }
				2 | fragment dogFields on Dog { name }
				  |          ^
			"""),
			document = """
				|{ dog { name } }
				|fragment dogFields on Dog { name }
			""",
			schema = """
				|type Query { dog: Dog }
				|type Dog { name: String }
			"""
		)
	}


	@Test
	fun testAcceptsMultipleUsedFragments() {
		assertValidationRule(
			rule = FragmentDefinitionUsageRule,
			errors = emptyList(),
			document = """
				|{ dog { ...fragA } cat { ...fragB } }
				|fragment fragA on Dog { name }
				|fragment fragB on Cat { name }
			""",
			schema = """
				|type Query { dog: Dog cat: Cat }
				|type Dog { name: String }
				|type Cat { name: String }
			"""
		)
	}


	@Test
	fun testRejectsMultipleUnusedFragments() {
		assertValidationRule(
			rule = FragmentDefinitionUsageRule,
			errors = listOf(
				"""
					Fragment 'fragA' is not used by any operation.

					<document>:2:10
					1 | { id }
					2 | fragment fragA on Query { id }
					  |          ^
					3 | fragment fragB on Query { id }
				""",
				"""
					Fragment 'fragB' is not used by any operation.

					<document>:3:10
					2 | fragment fragA on Query { id }
					3 | fragment fragB on Query { id }
					  |          ^
				"""
			),
			document = """
				|{ id }
				|fragment fragA on Query { id }
				|fragment fragB on Query { id }
			""",
			schema = """
				|type Query { id: ID }
			"""
		)
	}
}
