package testing

import io.fluidsonic.graphql.*
import kotlin.test.*


class ArgumentExistenceRuleTest {

	@Test
	fun testAcceptsArgumentThatExist() {
		assertValidationRule(
			rule = ArgumentExistenceRule,
			errors = emptyList(),
			document = """
				|{
				|   id(argument: "value") @include(if: true)
				|}
			""",
			schema = """
				|type Query {
				|   id(argument: String): ID
				|}
			"""
		)
	}


	@Test
	fun testAcceptsNestedArgumentThatExist() {
		assertValidationRule(
			rule = ArgumentExistenceRule,
			errors = emptyList(),
			document = """
				|{
				|   id(input: { argument: "value" }) @include(if: true)
				|}
			""",
			schema = """
				|input Input { argument: String } 
				|type Query {
				|   id(input: Input): ID
				|}
			"""
		)
	}


	@Test
	fun testIgnoresNestedObjectValuesWithoutInputType() {
		assertValidationRule(
			rule = ArgumentExistenceRule,
			errors = emptyList(),
			document = """
				|{
				|   id(input: { scalar: { doesNotExist: true } })
				|}
			""",
			schema = """
				|input Input { scalar: Scalar }
				|scalar Scalar
				|type Query {
				|   id(input: Input): ID
				|}
			"""
		)
	}


	@Test
	fun testRejectsArgumentThatDontExist() {
		assertValidationRule(
			rule = ArgumentExistenceRule,
			errors = listOf(
				"""
					Unknown argument 'noSuchArgument' for field 'Query.id'.

					<document>:2:7
					1 | {
					2 |    id(noSuchArgument: "value") @include(unless: false) @foo(bar: 2)
					  |       ^
					3 | }
				""",
				"""
					Unknown argument 'unless' for directive 'include'.

					<document>:2:41
					1 | {
					2 |    id(noSuchArgument: "value") @include(unless: false) @foo(bar: 2)
					  |                                         ^
					3 | }
				""",
				"""
					Unknown argument 'bar' for directive 'foo'.

					<document>:2:61
					1 | {
					2 |    id(noSuchArgument: "value") @include(unless: false) @foo(bar: 2)
					  |                                                             ^
					3 | }
				"""
			),
			document = """
				|{
				|   id(noSuchArgument: "value") @include(unless: false) @foo(bar: 2)
				|}
			""",
			schema = """
				|type Query {
				|   id(argument: String): ID
				|}
				|
				|directive @foo(value: Int) on FIELD
			"""
		)
	}
}
