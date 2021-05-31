package testing

import io.fluidsonic.graphql.*
import kotlin.test.*


class ArgumentExistenceRuleTest {

	@Test
	fun testAcceptsKnownArgument() {
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
	fun testAcceptsKnownInputField() {
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
	fun testIgnoresObjectValueFieldsOfScalar() {
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
	fun testRejectsUnknownArgument() {
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


	@Test
	fun testRejectsUnknownInputField() {
		assertValidationRule(
			rule = ArgumentExistenceRule,
			errors = listOf(
				"""
					Field 'noSuchArgument' is not defined by type 'Input'.

					<document>:2:16
					1 | {
					2 |    id(input: { noSuchArgument: "value" }) @foo(input: { bar: 2 })
					  |                ^
					3 | }
				""",
				"""
					Field 'bar' is not defined by type 'Input'.

					<document>:2:57
					1 | {
					2 |    id(input: { noSuchArgument: "value" }) @foo(input: { bar: 2 })
					  |                                                         ^
					3 | }
				"""
			),
			document = """
				|{
				|   id(input: { noSuchArgument: "value" }) @foo(input: { bar: 2 })
				|}
			""",
			schema = """
				|input Input { scalar: Scalar }
				|type Query {
				|   id(input: Input): ID
				|}
				|
				|directive @foo(input: Input) on FIELD
			"""
		)
	}
}
