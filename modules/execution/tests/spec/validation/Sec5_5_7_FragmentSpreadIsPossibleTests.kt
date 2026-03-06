package testing

import io.fluidsonic.graphql.*
import kotlin.test.*


// GraphQL Spec §5.5.7 — Fragment Spread Is Possible
class Sec5_5_7_FragmentSpreadIsPossibleTests {

	@Test
	fun testAcceptsSpreadOnSameObject() {
		assertValidationRule(
			rule = FragmentSelectionPossibilityRule,
			errors = emptyList(),
			document = """
				|{ dog { ...dogFrag } }
				|fragment dogFrag on Dog { name }
			""",
			schema = """
				|type Query { dog: Dog }
				|type Dog { name: String }
			"""
		)
	}


	@Test
	fun testAcceptsSpreadOnImplementedInterface() {
		assertValidationRule(
			rule = FragmentSelectionPossibilityRule,
			errors = emptyList(),
			document = """
				|{ pet { ...petFrag } }
				|fragment petFrag on Pet { name }
			""",
			schema = """
				|type Query { pet: Pet }
				|interface Pet { name: String }
				|type Dog implements Pet { name: String }
			"""
		)
	}


	@Test
	fun testAcceptsObjectSpreadOnUnion() {
		assertValidationRule(
			rule = FragmentSelectionPossibilityRule,
			errors = emptyList(),
			document = """
				|{ catOrDog { ...dogFrag } }
				|fragment dogFrag on Dog { name }
			""",
			schema = """
				|type Query { catOrDog: CatOrDog }
				|type Cat { name: String }
				|type Dog { name: String }
				|union CatOrDog = Cat | Dog
			"""
		)
	}


	@Test
	fun testRejectsSpreadOnDifferentObject() {
		assertValidationRule(
			rule = FragmentSelectionPossibilityRule,
			errors = listOf("""
				Fragment 'catFrag' on 'Cat' will never match the unrelated type 'Dog'.

				<document>:1:12
				1 | { dog { ...catFrag } }
				  |            ^
				2 | fragment catFrag on Cat { name }

				<document>:2:21
				1 | { dog { ...catFrag } }
				2 | fragment catFrag on Cat { name }
				  |                     ^

				<document>:3:6
				2 | interface Pet { name: String }
				3 | type Cat { name: String }
				  |      ^
				4 | type Dog { name: String }

				<document>:4:6
				3 | type Cat { name: String }
				4 | type Dog { name: String }
				  |      ^
				5 | union CatOrDog = Cat | Dog
			"""),
			document = """
				|{ dog { ...catFrag } }
				|fragment catFrag on Cat { name }
			""",
			schema = """
				|type Query { dog: Dog }
				|interface Pet { name: String }
				|type Cat { name: String }
				|type Dog { name: String }
				|union CatOrDog = Cat | Dog
			"""
		)
	}


	@Test
	fun testAcceptsInlineFragmentOnSameObject() {
		assertValidationRule(
			rule = FragmentSelectionPossibilityRule,
			errors = emptyList(),
			document = """
				|{ dog { ... on Dog { name } } }
			""",
			schema = """
				|type Query { dog: Dog }
				|type Dog { name: String }
			"""
		)
	}
}
