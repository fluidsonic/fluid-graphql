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
	fun testRejectsArgumentThatDontExist() {
		assertValidationRule(
			rule = ArgumentExistenceRule,
			errors = listOf(
				"""
					Unknown argument 'noSuchArgument' for field 'id'.

					<document>:2:7
					1 | {
					2 |    id(noSuchArgument: "value") @include(unless: false) @foo(bar: 2)
					  |       ^
					3 | }

					<document>:2:4
					1 | type Query {
					2 |    id(argument: String): ID
					  |    ^
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

					<document>:5:12
					4 | 
					5 | directive @foo(value: Int) on FIELD
					  |            ^
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
