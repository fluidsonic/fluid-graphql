package testing

import io.fluidsonic.graphql.FragmentTypeConditionValidityRule
import kotlin.test.Test

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
			""",
		)
	}

	@Test
	fun testRejectsInlineFragmentOnNonexistentType() {
		assertValidationRule(
			rule = FragmentTypeConditionValidityRule,
			errors = listOf(
				"""
					Fragment "fragOnScalar" cannot condition on non composite type "Date".

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
					Fragment cannot condition on non composite type "Date".

					<document>:6:10
					5 | fragment inlineFragOnScalar on Dog {
					6 |   ... on Date {
					  |          ^
					7 |     somethingElse

					<document>:6:8
					5 | union CatOrDog = Cat | Dog
					6 | scalar Date
					  |        ^
				""",
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
			""",
		)
	}

	@Test
	fun testRejectsFragmentOnInputObjectType() {
		assertValidationRule(
			rule = FragmentTypeConditionValidityRule,
			errors = listOf(
				"""
					Fragment "fragOnInputObject" cannot condition on non composite type "In".

					<document>:1:31
					1 | fragment fragOnInputObject on In {
					  |                               ^
					2 |   a

					<document>:3:7
					2 | type Dog { name: String }
					3 | input In { a: Int }
					  |       ^
				""",
				"""
					Fragment cannot condition on non composite type "In".

					<document>:6:10
					5 | fragment inlineFragOnInputObject on Dog {
					6 |   ... on In {
					  |          ^
					7 |     a

					<document>:3:7
					2 | type Dog { name: String }
					3 | input In { a: Int }
					  |       ^
				""",
			),
			document = """
				|fragment fragOnInputObject on In {
				|  a
				|}
				|
				|fragment inlineFragOnInputObject on Dog {
				|  ... on In {
				|    a
				|  }
				|}
			""",
			schema = """
				|type Query { id: ID }
				|type Dog { name: String }
				|input In { a: Int }
			""",
		)
	}
}
