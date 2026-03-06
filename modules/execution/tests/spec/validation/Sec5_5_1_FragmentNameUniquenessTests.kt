package testing

import io.fluidsonic.graphql.*
import kotlin.test.*


// GraphQL Spec §5.5.1 — Fragment Name Uniqueness
class Sec5_5_1_FragmentNameUniquenessTests {

	@Test
	fun testAcceptsUniqueFragments() {
		assertValidationRule(
			rule = FragmentDefinitionNameExclusivityRule,
			errors = emptyList(),
			document = """
				|{ dog { ...fragA ...fragB } }
				|fragment fragA on Dog { name }
				|fragment fragB on Dog { name }
			""",
			schema = """
				|type Query { dog: Dog }
				|type Dog { name: String }
			"""
		)
	}


	@Test
	fun testRejectsDuplicateFragmentNames() {
		assertValidationRule(
			rule = FragmentDefinitionNameExclusivityRule,
			errors = listOf("""
				The document must not contain multiple fragments with the same name 'frag'.

				<document>:2:10
				1 | { dog { ...frag } }
				2 | fragment frag on Dog { name }
				  |          ^
				3 | fragment frag on Dog { name }

				<document>:3:10
				2 | fragment frag on Dog { name }
				3 | fragment frag on Dog { name }
				  |          ^
			"""),
			document = """
				|{ dog { ...frag } }
				|fragment frag on Dog { name }
				|fragment frag on Dog { name }
			""",
			schema = """
				|type Query { dog: Dog }
				|type Dog { name: String }
			"""
		)
	}


	@Test
	fun testRejectsThreeFragmentsWithTwoSameNames() {
		assertValidationRule(
			rule = FragmentDefinitionNameExclusivityRule,
			errors = listOf("""
				The document must not contain multiple fragments with the same name 'frag'.

				<document>:2:10
				1 | { dog { ...frag ...other } }
				2 | fragment frag on Dog { name }
				  |          ^
				3 | fragment other on Dog { name }

				<document>:4:10
				3 | fragment other on Dog { name }
				4 | fragment frag on Dog { name }
				  |          ^
			"""),
			document = """
				|{ dog { ...frag ...other } }
				|fragment frag on Dog { name }
				|fragment other on Dog { name }
				|fragment frag on Dog { name }
			""",
			schema = """
				|type Query { dog: Dog }
				|type Dog { name: String }
			"""
		)
	}
}
