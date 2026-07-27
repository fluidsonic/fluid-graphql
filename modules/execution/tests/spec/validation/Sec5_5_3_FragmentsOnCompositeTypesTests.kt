package testing

import io.fluidsonic.graphql.FragmentTypeConditionValidityRule
import io.fluidsonic.graphql.document
import io.fluidsonic.graphql.schema
import kotlin.test.Test

// GraphQL Spec §5.5.3 — Fragments On Composite Types
class Sec5_5_3_FragmentsOnCompositeTypesTests {

	@Test
	fun testAcceptsFragmentOnObject() {
		assertValidationRule(
			rule = FragmentTypeConditionValidityRule,
			errors = emptyList(),
			document = """
				|fragment f on Dog { name }
			""",
			schema = """
				|type Query { dog: Dog }
				|type Dog { name: String }
			""",
		)
	}

	@Test
	fun testAcceptsFragmentOnInterface() {
		assertValidationRule(
			rule = FragmentTypeConditionValidityRule,
			errors = emptyList(),
			document = """
				|fragment f on Pet { name }
			""",
			schema = """
				|type Query { pet: Pet }
				|interface Pet { name: String }
				|type Dog implements Pet { name: String }
			""",
		)
	}

	@Test
	fun testAcceptsFragmentOnUnion() {
		assertValidationRule(
			rule = FragmentTypeConditionValidityRule,
			errors = emptyList(),
			document = """
				|fragment f on CatOrDog { __typename }
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
	fun testRejectsFragmentOnScalar() {
		assertValidationRule(
			rule = FragmentTypeConditionValidityRule,
			errors = listOf(
				"""
				Fragment "f" cannot condition on non composite type "String".

				<document>:1:15
				1 | fragment f on String { something }
				  |               ^
			""",
			),
			document = """
				|fragment f on String { something }
			""",
			// `scalar String` used to be declared here; redefining a built-in scalar is now rejected, and the
			// field's own reference is what registers `String` anyway.
			schema = """
				|type Query { name: String }
			""",
		)
	}

	@Test
	fun testRejectsInlineFragmentOnScalar() {
		assertValidationRule(
			rule = FragmentTypeConditionValidityRule,
			errors = listOf(
				"""
				Fragment cannot condition on non composite type "String".

				<document>:1:17
				1 | { name { ... on String { foo } } }
				  |                 ^
			""",
			),
			document = """
				|{ name { ... on String { foo } } }
			""",
			// `scalar String` used to be declared here; redefining a built-in scalar is now rejected, and the
			// built-in directives keep `String` registered anyway.
			schema = """
				|type Query { name: ScalarType }
				|scalar ScalarType
			""",
		)
	}
}
