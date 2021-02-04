package testing

import io.fluidsonic.graphql.*
import kotlin.test.*


class FragmentTypeConditionValidityRuleTest {

	@Test
	fun testAcceptsFragmentsOnCompositeTypes() {
		assertValidationRule(
			rule = FragmentTypeConditionValidityRule,
			errors = emptyList(),
			document = """
				|fragment fragOnObject on Dog {
				|  name
				|}
				|
				|fragment fragOnInterface on Pet {
				|  name
				|}
				|
				|fragment fragOnUnion on CatOrDog {
				|  ... on Dog {
				|    name
				|  }
				|}
			""",
			schema = """
				|type Query { id: ID }
				|interface Pet { name: String }
				|type Cat implements Pet { name: String }
				|type Dog implements Pet { name: String }
				|union CatOrDog = Cat | Dog
			"""
		)
	}


	@Test
	fun testRejectsInlineFragmentOnNonexistentType() {
		assertValidationRule(
			rule = FragmentTypeConditionValidityRule,
			errors = listOf(
				"""
					Fragment 'fragOnScalar' is specified on scalar 'Date' but must be specified on an interface, object or union type.

					<document>:1:26
					1 | fragment fragOnScalar on Date {
					  |                          ^
					2 |   something

					<document>:6:8
					5 | union CatOrDog = Cat | Dog
					6 | scalar Date
					  |        ^
				""",
				"""
					Inline fragment is specified on scalar 'Date' but must be specified on an interface, object or union type.

					<document>:6:10
					5 | fragment inlineFragOnScalar on Dog {
					6 |   ... on Date {
					  |          ^
					7 |     somethingElse

					<document>:6:8
					5 | union CatOrDog = Cat | Dog
					6 | scalar Date
					  |        ^
				"""
			),
			document = """
				|fragment fragOnScalar on Date {
				|  something
				|}
				|
				|fragment inlineFragOnScalar on Dog {
				|  ... on Date {
				|    somethingElse
				|  }
				|}
			""",
			schema = """
				|type Query { id: ID }
				|interface Pet { name: String }
				|type Cat implements Pet { name: String }
				|type Dog implements Pet { name: String }
				|union CatOrDog = Cat | Dog
				|scalar Date
			"""
		)
	}
}
