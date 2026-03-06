package testing

import io.fluidsonic.graphql.*
import kotlin.test.*


// GraphQL Spec §5.3.3 — Leaf Field Selections
class Sec5_3_3_LeafFieldSelectionsTests {

	@Test
	fun testAcceptsScalarLeaf() {
		assertValidationRule(
			rule = FieldSubselectionRule,
			errors = emptyList(),
			document = """
				|{ name }
			""",
			schema = """
				|type Query { name: String }
			"""
		)
	}


	@Test
	fun testAcceptsEnumLeaf() {
		assertValidationRule(
			rule = FieldSubselectionRule,
			errors = emptyList(),
			document = """
				|{ status }
			""",
			schema = """
				|type Query { status: Status }
				|enum Status { ACTIVE INACTIVE }
			"""
		)
	}


	@Test
	fun testAcceptsObjectWithSelection() {
		assertValidationRule(
			rule = FieldSubselectionRule,
			errors = emptyList(),
			document = """
				|{ dog { name } }
			""",
			schema = """
				|type Query { dog: Dog }
				|type Dog { name: String }
			"""
		)
	}


	@Test
	fun testAcceptsInterfaceWithSelection() {
		assertValidationRule(
			rule = FieldSubselectionRule,
			errors = emptyList(),
			document = """
				|{ pet { name } }
			""",
			schema = """
				|type Query { pet: Pet }
				|interface Pet { name: String }
				|type Dog implements Pet { name: String }
			"""
		)
	}


	@Test
	fun testAcceptsUnionWithSelection() {
		assertValidationRule(
			rule = FieldSubselectionRule,
			errors = emptyList(),
			document = """
				|{ catOrDog { __typename } }
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
	fun testRejectsScalarWithSelection() {
		assertValidationRule(
			rule = FieldSubselectionRule,
			errors = listOf("""
				Cannot select children of 'String' field 'name'.

				<document>:1:8
				1 | { name { foo } }
				  |        ^

				<document>:1:14
				1 | type Query { name: String }
				  |              ^
			"""),
			document = """
				|{ name { foo } }
			""",
			schema = """
				|type Query { name: String }
			"""
		)
	}


	@Test
	fun testRejectsEnumWithSelection() {
		assertValidationRule(
			rule = FieldSubselectionRule,
			errors = listOf("""
				Cannot select children of 'Status' field 'status'.

				<document>:1:10
				1 | { status { foo } }
				  |          ^

				<document>:1:14
				1 | type Query { status: Status }
				  |              ^
				2 | enum Status { ACTIVE INACTIVE }
			"""),
			document = """
				|{ status { foo } }
			""",
			schema = """
				|type Query { status: Status }
				|enum Status { ACTIVE INACTIVE }
			"""
		)
	}


	@Test
	fun testRejectsObjectWithoutSelection() {
		assertValidationRule(
			rule = FieldSubselectionRule,
			errors = listOf("""
				Must select children of 'Dog' field 'dog'.

				<document>:1:3
				1 | { dog }
				  |   ^

				<document>:1:14
				1 | type Query { dog: Dog }
				  |              ^
				2 | type Dog { name: String }
			"""),
			document = """
				|{ dog }
			""",
			schema = """
				|type Query { dog: Dog }
				|type Dog { name: String }
			"""
		)
	}
}
