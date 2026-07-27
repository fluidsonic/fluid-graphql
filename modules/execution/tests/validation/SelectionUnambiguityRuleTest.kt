package testing

import io.fluidsonic.graphql.SelectionUnambiguityRule
import kotlin.test.Test

// https://github.com/graphql/graphql-js/blob/main/src/validation/__tests__/OverlappingFieldsCanBeMergedRule-test.ts
//
// Expected errors live in the file-level constants below the class. Each message line is hand-written from the
// upstream (graphql-js) catalogue; only the `<document>:line:column` blocks are taken from the test harness.
class SelectionUnambiguityRuleTest {

	@Test
	fun testAcceptsDuplicateButIdenticalNameAndAliasSelection() {
		assertValidationRule(
			rule = SelectionUnambiguityRule,
			errors = emptyList(),
			document = """
				|{
				|   id
				|   id
				|   other: id
				|   other: id
				|}
			""",
			schema = "type Query { id: ID }",
		)
	}

	// Input object fields are unordered, so the same object value written in a different field order is the same value.
	@Test
	fun testAcceptsReorderedObjectArgumentFields() {
		assertValidationRule(
			rule = SelectionUnambiguityRule,
			errors = emptyList(),
			document = """
				|{ foo(bar: {a: null, b: null}) foo(bar: {b: null, a: null}) }
			""",
			schema = """
				|type Query { foo(bar: In): String }
				|input In { a: Int, b: Int }
			""",
		)
	}

	@Test
	fun testAcceptsConflictingFieldNamesAndArgumentsForDisjointObjectTypes() {
		assertValidationRule(
			rule = SelectionUnambiguityRule,
			errors = emptyList(),
			document = """
				|fragment conflictingDifferingResponses on Pet {
				|   ... on Dog {
				|       string: dogString
				|       stringWithArg: dogStringWithArg(foo: 2)
				|   }
				|   ... on Cat {
				|       string: catString
				|       stringWithArg: catStringWithArg(bar: "baz")
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
				|   dogString: String
				|   dogStringWithArg(foo: Int): String
				|}
				|
				|type Cat implements Pet {
				|   name: String!
				|   catString: String
				|   catStringWithArg(bar: String): String
				|}
			""",
		)
	}

	@Test
	fun testRejectsConflictingTypesSelection() {
		assertValidationRule(
			rule = SelectionUnambiguityRule,
			errors = conflictingTypesSelectionErrors,
			document = """
				|{
				|   id
				|   id: foo
				|}
			""",
			schema = "type Query { id: ID, foo: Int }",
		)
	}

	@Test
	fun testRejectsConflictingNullability() {
		assertValidationRule(
			rule = SelectionUnambiguityRule,
			errors = conflictingNullabilityErrors,
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
			""",
		)
	}

	@Test
	fun testRejectsConflictingListTypes() {
		assertValidationRule(
			rule = SelectionUnambiguityRule,
			errors = conflictingListTypesErrors,
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
			""",
		)
	}

	@Test
	fun testRejectsConflictingFieldNames() {
		assertValidationRule(
			rule = SelectionUnambiguityRule,
			errors = conflictingFieldNamesErrors,
			document = """
				|{
				|   foo
				|   foo: bar
				|}
			""",
			schema = """
				|type Query { foo: String, bar: String }
			""",
		)
	}

	@Test
	fun testRejectsConflictingArguments() {
		assertValidationRule(
			rule = SelectionUnambiguityRule,
			errors = conflictingArgumentsErrors,
			document = """
				|{
				|   foo(bar: 1)
				|   foo(bar: 2)
				|}
			""",
			schema = """
				|type Query { foo(bar: Int): String }
			""",
		)
	}

	@Test
	fun testRejectsConflictingSubSelections() {
		assertValidationRule(
			rule = SelectionUnambiguityRule,
			errors = conflictingSubSelectionsErrors,
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
			""",
		)
	}

	// The rule must reach every definition, not just the first. This is the shape that silently regresses if
	// anyone reinstates a "top-level selection sets only" gate on the hook.
	@Test
	fun testRejectsConflictInASecondOperation() {
		assertValidationRule(
			rule = SelectionUnambiguityRule,
			errors = conflictInASecondOperationErrors,
			document = """
				|query A { x }
				|query B { id id: foo }
			""",
			schema = "type Query { x: String, id: ID, foo: Int }",
		)
	}

	@Test
	fun testRejectsConflictInAFragmentDefinitionAfterAnOperation() {
		assertValidationRule(
			rule = SelectionUnambiguityRule,
			errors = conflictInAFragmentAfterAnOperationErrors,
			document = """
				|{ x }
				|fragment f on Query { id id: foo }
			""",
			schema = "type Query { x: String, id: ID, foo: Int }",
		)
	}

	// An unresolvable fragment type condition must not suppress the name and argument checks — only the return-type
	// check needs the field definition.
	@Test
	fun testRejectsConflictUnderUnresolvableFragmentTypeCondition() {
		assertValidationRule(
			rule = SelectionUnambiguityRule,
			errors = conflictUnderUnresolvableFragmentTypeErrors,
			document = """
				|{ ...f } fragment f on Undefined { z: q(x:1) z: q(x:2) }
			""",
			schema = """
				|type Query { o: O }
				|type O { a: String }
			""",
		)
	}

	// The enclosing set inlines the inline fragment's fields, so the outer and the inner selection set each report.
	@Test
	fun testRejectsConflictUnderUnresolvableInlineFragmentTypeCondition() {
		assertValidationRule(
			rule = SelectionUnambiguityRule,
			errors = conflictUnderUnresolvableInlineFragmentTypeErrors,
			document = """
				|{ ... on Undefined { z: q(x:1) z: q(x:2) } }
			""",
			schema = """
				|type Query { o: O }
				|type O { a: String }
			""",
		)
	}

	@Test
	fun testRejectsConflictBetweenFieldsWithoutDefinition() {
		assertValidationRule(
			rule = SelectionUnambiguityRule,
			errors = conflictBetweenFieldsWithoutDefinitionErrors,
			document = """
				|{ o { z: nope(x:1) z: nope(x:2) } }
			""",
			schema = """
				|type Query { o: O }
				|type O { a: String }
			""",
		)
	}

	// Fields of two distinct object types are never part of the same response, so their names and arguments are
	// not compared — but their return types still have to be mergeable.
	@Test
	fun testRejectsDifferentTypesAlsoForDisjointObjectTypes() {
		assertValidationRule(
			rule = SelectionUnambiguityRule,
			errors = differentTypesForDisjointObjectTypesErrors,
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
			""",
		)
	}

	// Mutual exclusivity is decided per pair: Dog vs Cat is exclusive so their differing arguments are fine,
	// while the two Cat selections can both be part of the same response and therefore conflict.
	@Test
	fun testRejectsDifferingArgumentsOnlyBetweenNonExclusiveObjectTypes() {
		assertValidationRule(
			rule = SelectionUnambiguityRule,
			errors = differingArgumentsBetweenNonExclusiveTypesErrors,
			document = """
				|fragment f on Pet {
				|   ... on Dog { s(x: "a") }
				|   ... on Cat { s(x: "b") }
				|   ... on Cat { s(x: "c") }
				|}
			""",
			schema = """
				|interface Pet { name: String! }
				|type Dog implements Pet { name: String!, s(x: String): String }
				|type Cat implements Pet { name: String!, s(x: String): String }
				|type Query { pet: Pet }
			""",
		)
	}

	// Two different composite types are not a conflict by themselves, but their sub-selections are still merged.
	@Test
	fun testRejectsConflictingSubfieldsOfDistinctCompositeTypes() {
		assertValidationRule(
			rule = SelectionUnambiguityRule,
			errors = conflictingSubfieldsOfDistinctCompositeTypesErrors,
			document = """
				|fragment f on Pet {
				|   ... on Dog { sub { x } }
				|   ... on Cat { sub { x } }
				|}
			""",
			schema = """
				|interface Pet { name: String! }
				|type O { x: String }
				|type P { x: Int }
				|type Dog implements Pet { name: String!, sub: O }
				|type Cat implements Pet { name: String!, sub: P }
				|type Query { pet: Pet }
			""",
		)
	}
}

private val conflictingTypesSelectionErrors = listOf(
	"""
	Fields "id" conflict because "id" and "foo" are different fields. Use different aliases on the fields to fetch both if this was intentional.

	<document>:2:4
	1 | {
	2 |    id
	  |    ^
	3 |    id: foo

	<document>:3:4
	2 |    id
	3 |    id: foo
	  |    ^
	4 | }
""",
)

private val conflictingNullabilityErrors = listOf(
	"""
	Fields "foo" conflict because they return conflicting types "String" and "String!". Use different aliases on the fields to fetch both if this was intentional.

	<document>:3:17
	2 |    bar {
	3 |       ...on A { foo }
	  |                 ^
	4 |       ...on B { foo }

	<document>:4:17
	3 |       ...on A { foo }
	4 |       ...on B { foo }
	  |                 ^
	5 |    }
""",
)

private val conflictingListTypesErrors = listOf(
	"""
	Fields "foo" conflict because they return conflicting types "String" and "[[String]]". Use different aliases on the fields to fetch both if this was intentional.

	<document>:3:17
	2 |    bar {
	3 |       ...on A { foo }
	  |                 ^
	4 |       ...on B { foo }

	<document>:4:17
	3 |       ...on A { foo }
	4 |       ...on B { foo }
	  |                 ^
	5 |    }
""",
)

private val conflictingFieldNamesErrors = listOf(
	"""
	Fields "foo" conflict because "foo" and "bar" are different fields. Use different aliases on the fields to fetch both if this was intentional.

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
""",
)

private val conflictingArgumentsErrors = listOf(
	"""
	Fields "foo" conflict because they have differing arguments. Use different aliases on the fields to fetch both if this was intentional.

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
""",
)

private val conflictingSubSelectionsErrors = listOf(
	"""
	Fields "bar" conflict because "bar" and "baz" are different fields. Use different aliases on the fields to fetch both if this was intentional.

	<document>:3:7
	2 |    foo {
	3 |       bar
	  |       ^
	4 |       bar: baz

	<document>:4:7
	3 |       bar
	4 |       bar: baz
	  |       ^
	5 |    }
""",
)

private val conflictInASecondOperationErrors = listOf(
	"""
	Fields "id" conflict because "id" and "foo" are different fields. Use different aliases on the fields to fetch both if this was intentional.

	<document>:2:11
	1 | query A { x }
	2 | query B { id id: foo }
	  |           ^

	<document>:2:14
	1 | query A { x }
	2 | query B { id id: foo }
	  |              ^
""",
)

private val conflictInAFragmentAfterAnOperationErrors = listOf(
	"""
	Fields "id" conflict because "id" and "foo" are different fields. Use different aliases on the fields to fetch both if this was intentional.

	<document>:2:23
	1 | { x }
	2 | fragment f on Query { id id: foo }
	  |                       ^

	<document>:2:26
	1 | { x }
	2 | fragment f on Query { id id: foo }
	  |                          ^
""",
)

private val conflictUnderUnresolvableFragmentTypeErrors = listOf(
	"""
	Fields "z" conflict because they have differing arguments. Use different aliases on the fields to fetch both if this was intentional.

	<document>:1:36
	1 | { ...f } fragment f on Undefined { z: q(x:1) z: q(x:2) }
	  |                                    ^

	<document>:1:46
	1 | { ...f } fragment f on Undefined { z: q(x:1) z: q(x:2) }
	  |                                              ^
""",
)

private val conflictUnderUnresolvableInlineFragmentTypeErrors = listOf(
	"""
	Fields "z" conflict because they have differing arguments. Use different aliases on the fields to fetch both if this was intentional.

	<document>:1:22
	1 | { ... on Undefined { z: q(x:1) z: q(x:2) } }
	  |                      ^

	<document>:1:32
	1 | { ... on Undefined { z: q(x:1) z: q(x:2) } }
	  |                                ^
""",
	"""
	Fields "z" conflict because they have differing arguments. Use different aliases on the fields to fetch both if this was intentional.

	<document>:1:22
	1 | { ... on Undefined { z: q(x:1) z: q(x:2) } }
	  |                      ^

	<document>:1:32
	1 | { ... on Undefined { z: q(x:1) z: q(x:2) } }
	  |                                ^
""",
)

private val conflictBetweenFieldsWithoutDefinitionErrors = listOf(
	"""
	Fields "z" conflict because they have differing arguments. Use different aliases on the fields to fetch both if this was intentional.

	<document>:1:7
	1 | { o { z: nope(x:1) z: nope(x:2) } }
	  |       ^

	<document>:1:20
	1 | { o { z: nope(x:1) z: nope(x:2) } }
	  |                    ^
""",
)

private val differentTypesForDisjointObjectTypesErrors = listOf(
	"""
	Fields "someValue" conflict because they return conflicting types "String" and "Int". Use different aliases on the fields to fetch both if this was intentional.

	<document>:3:8
	2 |    ... on Dog {
	3 |        someValue: nickname
	  |        ^
	4 |    }

	<document>:6:8
	5 |    ... on Cat {
	6 |        someValue: meowVolume
	  |        ^
	7 |    }
""",
)

private val differingArgumentsBetweenNonExclusiveTypesErrors = listOf(
	"""
	Fields "s" conflict because they have differing arguments. Use different aliases on the fields to fetch both if this was intentional.

	<document>:3:17
	2 |    ... on Dog { s(x: "a") }
	3 |    ... on Cat { s(x: "b") }
	  |                 ^
	4 |    ... on Cat { s(x: "c") }

	<document>:4:17
	3 |    ... on Cat { s(x: "b") }
	4 |    ... on Cat { s(x: "c") }
	  |                 ^
	5 | }
""",
)

private val conflictingSubfieldsOfDistinctCompositeTypesErrors = listOf(
	"""
	Fields "sub" conflict because subfields "x" conflict because they return conflicting types "String" and "Int". Use different aliases on the fields to fetch both if this was intentional.

	<document>:2:17
	1 | fragment f on Pet {
	2 |    ... on Dog { sub { x } }
	  |                 ^
	3 |    ... on Cat { sub { x } }

	<document>:2:23
	1 | fragment f on Pet {
	2 |    ... on Dog { sub { x } }
	  |                       ^
	3 |    ... on Cat { sub { x } }

	<document>:3:17
	2 |    ... on Dog { sub { x } }
	3 |    ... on Cat { sub { x } }
	  |                 ^
	4 | }

	<document>:3:23
	2 |    ... on Dog { sub { x } }
	3 |    ... on Cat { sub { x } }
	  |                       ^
	4 | }
""",
)
