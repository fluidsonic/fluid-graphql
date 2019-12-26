package io.fluidsonic.graphql

import tests.*
import kotlin.test.*


class SelectionSetHasNoConflictsRuleTest : ValidationRule {

	@Test
	fun `accepts duplicate but identical name and alias selection`() {
		assertValidationRule(
			rule = SelectionSetHasNoConflictsRule,
			errors = emptyList(),
			document = """
				|{
				|   id
				|   id
				|   other: id
				|   other: id
				|}
			""",
			schema = "type Query { id: ID }"
		)
	}


	@Test
	fun `rejects conflicting types selection`() {
		assertValidationRule(
			rule = SelectionSetHasNoConflictsRule,
			errors = listOf("""
				Field 'id' in 'Query' is selected in multiple locations but with incompatible types.

				<document>:2:4
				1 | {
				2 |    id
				  |    ^
				3 |    id: foo

				<document>:1:18
				1 | type Query { id: ID, foo: Int }
				  |                  ^

				<document>:3:4
				2 |    id
				3 |    id: foo
				  |    ^
				4 | }

				<document>:1:27
				1 | type Query { id: ID, foo: Int }
				  |                           ^
			"""),
			document = """
				|{
				|   id
				|   id: foo
				|}
			""",
			schema = "type Query { id: ID, foo: Int }"
		)
	}


	@Test
	fun `rejects conflicting nullability`() {
		assertValidationRule(
			rule = SelectionSetHasNoConflictsRule,
			errors = listOf("""
				Field 'foo' in 'A' is selected in multiple locations but with incompatible types.

				<document>:3:17
				2 |    bar {
				3 |       ...on A { foo }
				  |                 ^
				4 |       ...on B { foo }

				<document>:3:15
				2 | union AorB = A | B
				3 | type A { foo: String }
				  |               ^
				4 | type B { foo: String! }

				<document>:4:17
				3 |       ...on A { foo }
				4 |       ...on B { foo }
				  |                 ^
				5 |    }

				<document>:4:15
				3 | type A { foo: String }
				4 | type B { foo: String! }
				  |               ^
			"""),
			document = """
				|{
				|   bar {
				|      ...on A { foo }
				|      ...on B { foo }
				|   }
				|}
			""",
			schema = """
				|type Query { bar: AorB }
				|union AorB = A | B
				|type A { foo: String }
				|type B { foo: String! }
			"""
		)
	}


	@Test
	fun `rejects conflicting list types`() {
		assertValidationRule(
			rule = SelectionSetHasNoConflictsRule,
			errors = listOf("""
				Field 'foo' in 'A' is selected in multiple locations but with incompatible types.

				<document>:3:17
				2 |    bar {
				3 |       ...on A { foo }
				  |                 ^
				4 |       ...on B { foo }

				<document>:3:15
				2 | union AorB = A | B
				3 | type A { foo: String }
				  |               ^
				4 | type B { foo: [[String]] }

				<document>:4:17
				3 |       ...on A { foo }
				4 |       ...on B { foo }
				  |                 ^
				5 |    }

				<document>:4:15
				3 | type A { foo: String }
				4 | type B { foo: [[String]] }
				  |               ^
			"""),
			document = """
				|{
				|   bar {
				|      ...on A { foo }
				|      ...on B { foo }
				|   }
				|}
			""",
			schema = """
				|type Query { bar: AorB }
				|union AorB = A | B
				|type A { foo: String }
				|type B { foo: [[String]] }
			"""
		)
	}


	@Test
	fun `rejects conflicting field names`() {
		assertValidationRule(
			rule = SelectionSetHasNoConflictsRule,
			errors = listOf("""
				Field 'foo' in 'Query' is selected in multiple locations but selects different fields or with different arguments.

				<document>:2:4
				1 | {
				2 |    foo
				  |    ^
				3 |    foo: bar

				<document>:3:4
				2 |    foo
				3 |    foo: bar
				  |    ^
				4 | }
			"""),
			document = """
				|{
				|   foo
				|   foo: bar
				|}
			""",
			schema = """
				|type Query { foo: String, bar: String }
			"""
		)
	}


	@Test
	fun `rejects conflicting arguments`() {
		assertValidationRule(
			rule = SelectionSetHasNoConflictsRule,
			errors = listOf("""
				Field 'foo' in 'Query' is selected in multiple locations but selects different fields or with different arguments.

				<document>:2:4
				1 | {
				2 |    foo(bar: 1)
				  |    ^
				3 |    foo(bar: 2)

				<document>:3:4
				2 |    foo(bar: 1)
				3 |    foo(bar: 2)
				  |    ^
				4 | }
			"""),
			document = """
				|{
				|   foo(bar: 1)
				|   foo(bar: 2)
				|}
			""",
			schema = """
				|type Query { foo(bar: Int): String }
			"""
		)
	}


	@Test
	fun `rejects conflicting sub-selections`() {
		assertValidationRule(
			rule = SelectionSetHasNoConflictsRule,
			errors = listOf("""
				Field 'bar' in 'Foo' is selected in multiple locations but with incompatible types.

				<document>:3:7
				2 |    foo {
				3 |       bar
				  |       ^
				4 |       bar: baz

				<document>:2:17
				1 | type Query { foo: Foo }
				2 | type Foo { bar: String, baz: String! }
				  |                 ^

				<document>:4:7
				3 |       bar
				4 |       bar: baz
				  |       ^
				5 |    }

				<document>:2:30
				1 | type Query { foo: Foo }
				2 | type Foo { bar: String, baz: String! }
				  |                              ^
			"""),
			document = """
				|{
				|   foo {
				|      bar
				|      bar: baz
				|   }
				|}
			""",
			schema = """
				|type Query { foo: Foo }
				|type Foo { bar: String, baz: String! }
			"""
		)
	}


	@Test
	fun `rejects different types also for disjoint object types`() {
		assertValidationRule(
			rule = SelectionSetHasNoConflictsRule,
			errors = listOf("""
				Field 'someValue' in 'Dog' is selected in multiple locations but with incompatible types.

				<document>:3:8
				2 |    ... on Dog {
				3 |        someValue: nickname
				  |        ^
				4 |    }

				<document>:7:14
				6 |    name: String!
				7 |    nickname: String
				  |              ^
				8 | }

				<document>:6:8
				5 |    ... on Cat {
				6 |        someValue: meowVolume
				  |        ^
				7 |    }

				<document>:12:16
				11 |    name: String!
				12 |    meowVolume: Int
				   |                ^
				13 | }
			"""),
			document = """
				|fragment conflictingDifferingResponses on Pet {
				|   ... on Dog {
				|       someValue: nickname
				|   }
				|   ... on Cat {
				|       someValue: meowVolume
				|   }
				|}
			""",
			schema = """
				|interface Pet {
				|   name: String!
				|}
				|
				|type Dog implements Pet {
				|   name: String!
				|   nickname: String
				|}
				|
				|type Cat implements Pet {
				|   name: String!
				|   meowVolume: Int
				|}
			"""
		)
	}
}
