package testing

import io.fluidsonic.graphql.ScalarLeavesRule
import kotlin.test.Test

// GraphQL Spec §5.3.3 — Leaf Field Selections
class Sec5_3_3_LeafFieldSelectionsTests {

	@Test
	fun testAcceptsScalarLeaf() {
		assertValidationRule(
			rule = ScalarLeavesRule,
			errors = emptyList(),
			document = """
				|{ name }
			""",
			schema = """
				|type Query { name: String }
			""",
		)
	}

	@Test
	fun testAcceptsEnumLeaf() {
		assertValidationRule(
			rule = ScalarLeavesRule,
			errors = emptyList(),
			document = """
				|{ status }
			""",
			schema = """
				|type Query { status: Status }
				|enum Status { ACTIVE INACTIVE }
			""",
		)
	}

	@Test
	fun testAcceptsObjectWithSelection() {
		assertValidationRule(
			rule = ScalarLeavesRule,
			errors = emptyList(),
			document = """
				|{ dog { name } }
			""",
			schema = """
				|type Query { dog: Dog }
				|type Dog { name: String }
			""",
		)
	}

	@Test
	fun testAcceptsInterfaceWithSelection() {
		assertValidationRule(
			rule = ScalarLeavesRule,
			errors = emptyList(),
			document = """
				|{ pet { name } }
			""",
			schema = """
				|type Query { pet: Pet }
				|interface Pet { name: String }
				|type Dog implements Pet { name: String }
			""",
		)
	}

	@Test
	fun testAcceptsUnionWithSelection() {
		assertValidationRule(
			rule = ScalarLeavesRule,
			errors = emptyList(),
			document = """
				|{ catOrDog { __typename } }
			""",
			schema = """
				|type Query { catOrDog: CatOrDog }
				|type Cat { name: String }
				|type Dog { name: String }
				|union CatOrDog = Cat | Dog
			""",
		)
	}

	@Test
	fun testRejectsScalarWithSelection() {
		assertValidationRule(
			rule = ScalarLeavesRule,
			errors = listOf(
				"""
					Field "name" must not have a selection since type "String" has no subfields.

					<document>:1:8
					1 | { name { foo } }
					  |        ^
				""",
			),
			document = """
				|{ name { foo } }
			""",
			schema = """
				|type Query { name: String }
			""",
		)
	}

	@Test
	fun testRejectsEnumWithSelection() {
		assertValidationRule(
			rule = ScalarLeavesRule,
			errors = listOf(
				"""
					Field "status" must not have a selection since type "Status" has no subfields.

					<document>:1:10
					1 | { status { foo } }
					  |          ^
				""",
			),
			document = """
				|{ status { foo } }
			""",
			schema = """
				|type Query { status: Status }
				|enum Status { ACTIVE INACTIVE }
			""",
		)
	}

	@Test
	fun testRejectsObjectWithoutSelection() {
		assertValidationRule(
			rule = ScalarLeavesRule,
			errors = listOf(
				"""
					Field "dog" of type "Dog" must have a selection of subfields. Did you mean "dog { ... }"?

					<document>:1:3
					1 | { dog }
					  |   ^
				""",
			),
			document = """
				|{ dog }
			""",
			schema = """
				|type Query { dog: Dog }
				|type Dog { name: String }
			""",
		)
	}
}
