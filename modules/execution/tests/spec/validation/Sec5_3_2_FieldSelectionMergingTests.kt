package testing

import io.fluidsonic.graphql.SelectionUnambiguityRule
import io.fluidsonic.graphql.document
import io.fluidsonic.graphql.schema
import kotlin.test.Test

// GraphQL Spec §5.3.2 — Field Selection Merging
//
// Expected errors live in the file-level constants below the class. Each message line is hand-written from the
// upstream (graphql-js) catalogue; only the `<document>:line:column` blocks are taken from the test harness.
class Sec5_3_2_FieldSelectionMergingTests {

	@Test
	fun testAcceptsIdenticalFields() {
		assertValidationRule(
			rule = SelectionUnambiguityRule,
			errors = emptyList(),
			document = """
				|{ foo foo }
			""",
			schema = """
				|type Query { foo: String }
			""",
		)
	}

	@Test
	fun testAcceptsIdenticalAliasedFields() {
		assertValidationRule(
			rule = SelectionUnambiguityRule,
			errors = emptyList(),
			document = """
				|{ x: foo x: foo }
			""",
			schema = """
				|type Query { foo: String }
			""",
		)
	}

	@Test
	fun testAcceptsSameAliasOnFragments() {
		assertValidationRule(
			rule = SelectionUnambiguityRule,
			errors = emptyList(),
			document = """
				|{
				|  ...frag1
				|  ...frag2
				|}
				|fragment frag1 on Query { foo }
				|fragment frag2 on Query { foo }
			""",
			schema = """
				|type Query { foo: String }
			""",
		)
	}

	@Test
	fun testAcceptsIdenticalFieldArgs() {
		assertValidationRule(
			rule = SelectionUnambiguityRule,
			errors = emptyList(),
			document = """
				|{ foo(a: 1) foo(a: 1) }
			""",
			schema = """
				|type Query { foo(a: Int): String }
			""",
		)
	}

	// The fixture has different return types (ID vs Int) as well, but the differing field names are checked first.
	@Test
	fun testRejectsDifferentReturnTypeSameAlias() {
		assertValidationRule(
			rule = SelectionUnambiguityRule,
			errors = differentReturnTypeSameAliasErrors,
			document = """
				|{ id id: foo }
			""",
			schema = """
				|type Query { id: ID, foo: Int }
			""",
		)
	}

	@Test
	fun testRejectsConflictingArgsSameAlias() {
		assertValidationRule(
			rule = SelectionUnambiguityRule,
			errors = conflictingArgsSameAliasErrors,
			document = """
				|{ x: foo(a: 1) x: foo(a: 2) }
			""",
			schema = """
				|type Query { foo(a: Int): String }
			""",
		)
	}

	// One error per conflicting pair, so n conflicting selections yield n * (n - 1) / 2 errors.
	@Test
	fun testRejectsTwoConflictingSelectionsWithOneError() {
		assertValidationRule(
			rule = SelectionUnambiguityRule,
			errors = twoConflictingSelectionsErrors,
			document = """
				|{ foo(bar: 1) foo(bar: 2) }
			""",
			schema = """
				|type Query { foo(bar: Int): String }
			""",
		)
	}

	@Test
	fun testRejectsThreeConflictingSelectionsWithThreeErrors() {
		assertValidationRule(
			rule = SelectionUnambiguityRule,
			errors = threeConflictingSelectionsErrors,
			document = """
				|{ foo(bar: 1) foo(bar: 2) foo(bar: 3) }
			""",
			schema = """
				|type Query { foo(bar: Int): String }
			""",
		)
	}

	@Test
	fun testRejectsFourConflictingSelectionsWithSixErrors() {
		assertValidationRule(
			rule = SelectionUnambiguityRule,
			errors = fourConflictingSelectionsErrors,
			document = """
				|{ foo(bar: 1) foo(bar: 2) foo(bar: 3) foo(bar: 4) }
			""",
			schema = """
				|type Query { foo(bar: Int): String }
			""",
		)
	}

	// The next three pin how the per-selection-set checks and the pairwise sub-selection recursion add up. They
	// overlap without duplicating: a conflict inside one field's sub-selection is reported once by that selection
	// set, and once more per pair of enclosing fields that must merge.
	@Test
	fun testReportsANestedConflictOnceWhenTheEnclosingFieldIsSelectedOnce() {
		assertValidationRule(
			rule = SelectionUnambiguityRule,
			errors = nestedConflictUnderSingleFieldErrors,
			document = """
				|{ o { k: a k: b } }
			""",
			schema = """
				|type Query { o: O }
				|type O { a: String, b: Int }
			""",
		)
	}

	@Test
	fun testReportsThreeErrorsWhenBothTheEnclosingFieldsAndTheirSubfieldsConflict() {
		assertValidationRule(
			rule = SelectionUnambiguityRule,
			errors = enclosingAndSubfieldConflictErrors,
			document = """
				|{ o { k: a k: b } o { k: a k: b } }
			""",
			schema = """
				|type Query { o: O }
				|type O { a: String, b: Int }
			""",
		)
	}

	@Test
	fun testReportsTwoErrorsWhenOnlyOneSubSelectionConflictsInternally() {
		assertValidationRule(
			rule = SelectionUnambiguityRule,
			errors = oneSubSelectionConflictErrors,
			document = """
				|{ o { k: a k: b } o { k: b } }
			""",
			schema = """
				|type Query { o: O }
				|type O { a: String, b: Int }
			""",
		)
	}

	@Test
	fun testReportsDifferentFieldNamesAsReason() {
		assertValidationRule(
			rule = SelectionUnambiguityRule,
			errors = differentFieldNamesReasonErrors,
			document = """
				|{ x: foo x: bar }
			""",
			schema = """
				|type Query { foo: String, bar: String }
			""",
		)
	}

	@Test
	fun testReportsDifferingArgumentsAsReason() {
		assertValidationRule(
			rule = SelectionUnambiguityRule,
			errors = differingArgumentsReasonErrors,
			document = """
				|{ foo(a: 1) foo(a: 2) }
			""",
			schema = """
				|type Query { foo(a: Int): String }
			""",
		)
	}

	// Fields of two distinct object types can never be part of the same response, so their names and arguments
	// are not compared — but their return types still have to be mergeable.
	@Test
	fun testReportsConflictingTypesAsReason() {
		assertValidationRule(
			rule = SelectionUnambiguityRule,
			errors = conflictingTypesReasonErrors,
			document = """
				|fragment f on Pet {
				|   ... on Dog { name }
				|   ... on Cat { name }
				|}
			""",
			schema = """
				|interface Pet { id: ID }
				|type Dog implements Pet { id: ID, name: String }
				|type Cat implements Pet { id: ID, name: String! }
				|type Query { pet: Pet }
			""",
		)
	}

	@Test
	fun testReportsConflictingListTypesAsReason() {
		assertValidationRule(
			rule = SelectionUnambiguityRule,
			errors = conflictingListTypesReasonErrors,
			document = """
				|fragment f on Pet {
				|   ... on Dog { tags }
				|   ... on Cat { tags }
				|}
			""",
			schema = """
				|interface Pet { id: ID }
				|type Dog implements Pet { id: ID, tags: [String] }
				|type Cat implements Pet { id: ID, tags: [[String]] }
				|type Query { pet: Pet }
			""",
		)
	}

	@Test
	fun testReportsNestedSubfieldConflictAsReason() {
		assertValidationRule(
			rule = SelectionUnambiguityRule,
			errors = nestedSubfieldReasonErrors,
			document = """
				|{ o { o { k: a } } o { o { k: b } } }
			""",
			schema = """
				|type Query { o: O }
				|type O { o: O, k: String, a: String, b: String }
			""",
		)
	}

	// Several conflicts below the same pair of fields are joined with " and ".
	@Test
	fun testReportsSeveralNestedSubfieldConflictsAsReason() {
		assertValidationRule(
			rule = SelectionUnambiguityRule,
			errors = severalNestedSubfieldReasonsErrors,
			document = """
				|{ o { p { k: m j: m } } o { p { k: n j: n } } }
			""",
			schema = """
				|type Query { o: O }
				|type O { o: O, p: P }
				|type P { k: String, j: String, m: String, n: String }
			""",
		)
	}
}

private val differentReturnTypeSameAliasErrors = listOf(
	"""
	Fields "id" conflict because "id" and "foo" are different fields. Use different aliases on the fields to fetch both if this was intentional.

	<document>:1:3
	1 | { id id: foo }
	  |   ^

	<document>:1:6
	1 | { id id: foo }
	  |      ^
""",
)

private val conflictingArgsSameAliasErrors = listOf(
	"""
	Fields "x" conflict because they have differing arguments. Use different aliases on the fields to fetch both if this was intentional.

	<document>:1:3
	1 | { x: foo(a: 1) x: foo(a: 2) }
	  |   ^

	<document>:1:16
	1 | { x: foo(a: 1) x: foo(a: 2) }
	  |                ^
""",
)

private val twoConflictingSelectionsErrors = listOf(
	"""
	Fields "foo" conflict because they have differing arguments. Use different aliases on the fields to fetch both if this was intentional.

	<document>:1:3
	1 | { foo(bar: 1) foo(bar: 2) }
	  |   ^

	<document>:1:15
	1 | { foo(bar: 1) foo(bar: 2) }
	  |               ^
""",
)

private val threeConflictingSelectionsErrors = listOf(
	"""
	Fields "foo" conflict because they have differing arguments. Use different aliases on the fields to fetch both if this was intentional.

	<document>:1:3
	1 | { foo(bar: 1) foo(bar: 2) foo(bar: 3) }
	  |   ^

	<document>:1:15
	1 | { foo(bar: 1) foo(bar: 2) foo(bar: 3) }
	  |               ^
""",
	"""
	Fields "foo" conflict because they have differing arguments. Use different aliases on the fields to fetch both if this was intentional.

	<document>:1:3
	1 | { foo(bar: 1) foo(bar: 2) foo(bar: 3) }
	  |   ^

	<document>:1:27
	1 | { foo(bar: 1) foo(bar: 2) foo(bar: 3) }
	  |                           ^
""",
	"""
	Fields "foo" conflict because they have differing arguments. Use different aliases on the fields to fetch both if this was intentional.

	<document>:1:15
	1 | { foo(bar: 1) foo(bar: 2) foo(bar: 3) }
	  |               ^

	<document>:1:27
	1 | { foo(bar: 1) foo(bar: 2) foo(bar: 3) }
	  |                           ^
""",
)

private val fourConflictingSelectionsErrors = listOf(
	"""
	Fields "foo" conflict because they have differing arguments. Use different aliases on the fields to fetch both if this was intentional.

	<document>:1:3
	1 | { foo(bar: 1) foo(bar: 2) foo(bar: 3) foo(bar: 4) }
	  |   ^

	<document>:1:15
	1 | { foo(bar: 1) foo(bar: 2) foo(bar: 3) foo(bar: 4) }
	  |               ^
""",
	"""
	Fields "foo" conflict because they have differing arguments. Use different aliases on the fields to fetch both if this was intentional.

	<document>:1:3
	1 | { foo(bar: 1) foo(bar: 2) foo(bar: 3) foo(bar: 4) }
	  |   ^

	<document>:1:27
	1 | { foo(bar: 1) foo(bar: 2) foo(bar: 3) foo(bar: 4) }
	  |                           ^
""",
	"""
	Fields "foo" conflict because they have differing arguments. Use different aliases on the fields to fetch both if this was intentional.

	<document>:1:3
	1 | { foo(bar: 1) foo(bar: 2) foo(bar: 3) foo(bar: 4) }
	  |   ^

	<document>:1:39
	1 | { foo(bar: 1) foo(bar: 2) foo(bar: 3) foo(bar: 4) }
	  |                                       ^
""",
	"""
	Fields "foo" conflict because they have differing arguments. Use different aliases on the fields to fetch both if this was intentional.

	<document>:1:15
	1 | { foo(bar: 1) foo(bar: 2) foo(bar: 3) foo(bar: 4) }
	  |               ^

	<document>:1:27
	1 | { foo(bar: 1) foo(bar: 2) foo(bar: 3) foo(bar: 4) }
	  |                           ^
""",
	"""
	Fields "foo" conflict because they have differing arguments. Use different aliases on the fields to fetch both if this was intentional.

	<document>:1:15
	1 | { foo(bar: 1) foo(bar: 2) foo(bar: 3) foo(bar: 4) }
	  |               ^

	<document>:1:39
	1 | { foo(bar: 1) foo(bar: 2) foo(bar: 3) foo(bar: 4) }
	  |                                       ^
""",
	"""
	Fields "foo" conflict because they have differing arguments. Use different aliases on the fields to fetch both if this was intentional.

	<document>:1:27
	1 | { foo(bar: 1) foo(bar: 2) foo(bar: 3) foo(bar: 4) }
	  |                           ^

	<document>:1:39
	1 | { foo(bar: 1) foo(bar: 2) foo(bar: 3) foo(bar: 4) }
	  |                                       ^
""",
)

private val nestedConflictUnderSingleFieldErrors = listOf(
	"""
	Fields "k" conflict because "a" and "b" are different fields. Use different aliases on the fields to fetch both if this was intentional.

	<document>:1:7
	1 | { o { k: a k: b } }
	  |       ^

	<document>:1:12
	1 | { o { k: a k: b } }
	  |            ^
""",
)

// The nested reason appears twice — once for the (a, b) pair and once for (b, a) — and the first error's last two
// locations are in comparison order, not document order.
private val enclosingAndSubfieldConflictErrors = listOf(
	"""
	Fields "o" conflict because subfields "k" conflict because "a" and "b" are different fields and subfields "k" conflict because "b" and "a" are different fields. Use different aliases on the fields to fetch both if this was intentional.

	<document>:1:3
	1 | { o { k: a k: b } o { k: a k: b } }
	  |   ^

	<document>:1:7
	1 | { o { k: a k: b } o { k: a k: b } }
	  |       ^

	<document>:1:12
	1 | { o { k: a k: b } o { k: a k: b } }
	  |            ^

	<document>:1:19
	1 | { o { k: a k: b } o { k: a k: b } }
	  |                   ^

	<document>:1:28
	1 | { o { k: a k: b } o { k: a k: b } }
	  |                            ^

	<document>:1:23
	1 | { o { k: a k: b } o { k: a k: b } }
	  |                       ^
""",
	"""
	Fields "k" conflict because "a" and "b" are different fields. Use different aliases on the fields to fetch both if this was intentional.

	<document>:1:7
	1 | { o { k: a k: b } o { k: a k: b } }
	  |       ^

	<document>:1:12
	1 | { o { k: a k: b } o { k: a k: b } }
	  |            ^
""",
	"""
	Fields "k" conflict because "a" and "b" are different fields. Use different aliases on the fields to fetch both if this was intentional.

	<document>:1:23
	1 | { o { k: a k: b } o { k: a k: b } }
	  |                       ^

	<document>:1:28
	1 | { o { k: a k: b } o { k: a k: b } }
	  |                            ^
""",
)

private val oneSubSelectionConflictErrors = listOf(
	"""
	Fields "o" conflict because subfields "k" conflict because "a" and "b" are different fields. Use different aliases on the fields to fetch both if this was intentional.

	<document>:1:3
	1 | { o { k: a k: b } o { k: b } }
	  |   ^

	<document>:1:7
	1 | { o { k: a k: b } o { k: b } }
	  |       ^

	<document>:1:19
	1 | { o { k: a k: b } o { k: b } }
	  |                   ^

	<document>:1:23
	1 | { o { k: a k: b } o { k: b } }
	  |                       ^
""",
	"""
	Fields "k" conflict because "a" and "b" are different fields. Use different aliases on the fields to fetch both if this was intentional.

	<document>:1:7
	1 | { o { k: a k: b } o { k: b } }
	  |       ^

	<document>:1:12
	1 | { o { k: a k: b } o { k: b } }
	  |            ^
""",
)

private val differentFieldNamesReasonErrors = listOf(
	"""
	Fields "x" conflict because "foo" and "bar" are different fields. Use different aliases on the fields to fetch both if this was intentional.

	<document>:1:3
	1 | { x: foo x: bar }
	  |   ^

	<document>:1:10
	1 | { x: foo x: bar }
	  |          ^
""",
)

private val differingArgumentsReasonErrors = listOf(
	"""
	Fields "foo" conflict because they have differing arguments. Use different aliases on the fields to fetch both if this was intentional.

	<document>:1:3
	1 | { foo(a: 1) foo(a: 2) }
	  |   ^

	<document>:1:13
	1 | { foo(a: 1) foo(a: 2) }
	  |             ^
""",
)

private val conflictingTypesReasonErrors = listOf(
	"""
	Fields "name" conflict because they return conflicting types "String" and "String!". Use different aliases on the fields to fetch both if this was intentional.

	<document>:2:17
	1 | fragment f on Pet {
	2 |    ... on Dog { name }
	  |                 ^
	3 |    ... on Cat { name }

	<document>:3:17
	2 |    ... on Dog { name }
	3 |    ... on Cat { name }
	  |                 ^
	4 | }
""",
)

private val conflictingListTypesReasonErrors = listOf(
	"""
	Fields "tags" conflict because they return conflicting types "[String]" and "[[String]]". Use different aliases on the fields to fetch both if this was intentional.

	<document>:2:17
	1 | fragment f on Pet {
	2 |    ... on Dog { tags }
	  |                 ^
	3 |    ... on Cat { tags }

	<document>:3:17
	2 |    ... on Dog { tags }
	3 |    ... on Cat { tags }
	  |                 ^
	4 | }
""",
)

private val nestedSubfieldReasonErrors = listOf(
	"""
	Fields "o" conflict because subfields "o" conflict because subfields "k" conflict because "a" and "b" are different fields. Use different aliases on the fields to fetch both if this was intentional.

	<document>:1:3
	1 | { o { o { k: a } } o { o { k: b } } }
	  |   ^

	<document>:1:7
	1 | { o { o { k: a } } o { o { k: b } } }
	  |       ^

	<document>:1:11
	1 | { o { o { k: a } } o { o { k: b } } }
	  |           ^

	<document>:1:20
	1 | { o { o { k: a } } o { o { k: b } } }
	  |                    ^

	<document>:1:24
	1 | { o { o { k: a } } o { o { k: b } } }
	  |                        ^

	<document>:1:28
	1 | { o { o { k: a } } o { o { k: b } } }
	  |                            ^
""",
)

private val severalNestedSubfieldReasonsErrors = listOf(
	"""
	Fields "o" conflict because subfields "p" conflict because subfields "k" conflict because "m" and "n" are different fields and subfields "j" conflict because "m" and "n" are different fields. Use different aliases on the fields to fetch both if this was intentional.

	<document>:1:3
	1 | { o { p { k: m j: m } } o { p { k: n j: n } } }
	  |   ^

	<document>:1:7
	1 | { o { p { k: m j: m } } o { p { k: n j: n } } }
	  |       ^

	<document>:1:11
	1 | { o { p { k: m j: m } } o { p { k: n j: n } } }
	  |           ^

	<document>:1:16
	1 | { o { p { k: m j: m } } o { p { k: n j: n } } }
	  |                ^

	<document>:1:25
	1 | { o { p { k: m j: m } } o { p { k: n j: n } } }
	  |                         ^

	<document>:1:29
	1 | { o { p { k: m j: m } } o { p { k: n j: n } } }
	  |                             ^

	<document>:1:33
	1 | { o { p { k: m j: m } } o { p { k: n j: n } } }
	  |                                 ^

	<document>:1:38
	1 | { o { p { k: m j: m } } o { p { k: n j: n } } }
	  |                                      ^
""",
)
