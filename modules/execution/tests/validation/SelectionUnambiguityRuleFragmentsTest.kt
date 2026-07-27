package testing

import io.fluidsonic.graphql.SelectionUnambiguityRule
import kotlin.test.Test

// §5.3.2 cases that turn on fragment structure — cycles, repeated spreads, and conflicts reached only by following a
// spread. Split from SelectionUnambiguityRuleTest to keep both files readable.
//
// https://github.com/graphql/graphql-js/blob/main/src/validation/__tests__/OverlappingFieldsCanBeMergedRule-test.ts
//
// Expected errors live in the file-level constants below the class. Each message line is hand-written from the
// upstream (graphql-js) catalogue; only the `<document>:line:column` blocks are taken from the test harness.
class SelectionUnambiguityRuleFragmentsTest {

	// A fragment cycle is FragmentCycleDetectionRule's concern. This rule must terminate and stay silent.
	@Test
	fun testAcceptsDirectlyCyclicFragment() {
		assertValidationRule(
			rule = SelectionUnambiguityRule,
			errors = emptyList(),
			document = """
				|{ ...f }
				|fragment f on Query { ...f }
			""",
			schema = "type Query { foo: String }",
		)
	}

	@Test
	fun testAcceptsTransitivelyCyclicFragments() {
		assertValidationRule(
			rule = SelectionUnambiguityRule,
			errors = emptyList(),
			document = """
				|{ ...a }
				|fragment a on Query { ...b }
				|fragment b on Query { ...a }
			""",
			schema = "type Query { foo: String }",
		)
	}

	// Spreads are keyed by fragment name, so spreading the same fragment twice collapses to a single spread.
	@Test
	fun testAcceptsDuplicateFragmentSpread() {
		assertValidationRule(
			rule = SelectionUnambiguityRule,
			errors = emptyList(),
			document = """
				|{ ...a ...a }
				|fragment a on Query { foo }
			""",
			schema = "type Query { foo: String, bar: String }",
		)
	}

	@Test
	fun testRejectsConflictBetweenTwoFragmentSpreads() {
		assertValidationRule(
			rule = SelectionUnambiguityRule,
			errors = conflictBetweenTwoFragmentSpreadsErrors,
			document = TWO_CONFLICTING_FRAGMENT_SPREADS_DOCUMENT,
			schema = "type Query { foo: String, bar: String }",
		)
	}

	// The rule memoizes which pairs it already compared, so it must be a fresh instance per validation rather than
	// a singleton — a shared `comparedFragmentPairs` is keyed on fragment names and would swallow this conflict on
	// every validation after the first.
	@Test
	fun testReportsTheSameErrorsOnEveryValidation() {
		repeat(2) {
			assertValidationRule(
				rule = SelectionUnambiguityRule,
				errors = conflictBetweenTwoFragmentSpreadsErrors,
				document = TWO_CONFLICTING_FRAGMENT_SPREADS_DOCUMENT,
				schema = "type Query { foo: String, bar: String }",
			)
		}
	}

	// Exercises the recursion into the fragments a spread references. Note that the reported locations are in the
	// order the algorithm compares them, which here is not document order.
	@Test
	fun testRejectsConflictBetweenTransitivelyReferencedFragments() {
		assertValidationRule(
			rule = SelectionUnambiguityRule,
			errors = conflictBetweenTransitivelyReferencedFragmentsErrors,
			document = """
				|fragment f on Query {
				|   ...a
				|   ...b
				|}
				|fragment a on Query { ...c }
				|fragment b on Query { x: bar }
				|fragment c on Query { x: foo }
			""",
			schema = "type Query { foo: String, bar: String }",
		)
	}

	// A self-spreading fragment must neither loop nor hide the conflict its own fields have with the spreading set.
	@Test
	fun testRejectsConflictWithSelfSpreadingFragment() {
		assertValidationRule(
			rule = SelectionUnambiguityRule,
			errors = conflictWithSelfSpreadingFragmentErrors,
			document = """
				|fragment f on Query {
				|   x: foo
				|   ...a
				|}
				|fragment a on Query { ...a x: bar }
			""",
			schema = "type Query { foo: String, bar: String }",
		)
	}

	// The same fragment spread under two different type conditions must not hide a conflict with a sibling field.
	@Test
	fun testRejectsConflictWithFragmentSpreadUnderTwoTypeConditions() {
		assertValidationRule(
			rule = SelectionUnambiguityRule,
			errors = conflictWithSpreadUnderTwoTypeConditionsErrors,
			document = """
				|fragment f on Pet {
				|   ... on Dog { ...g }
				|   ... on Cat { ...g }
				|   ... on Cat { name: meowVolume }
				|}
				|fragment g on Pet { name }
			""",
			schema = """
				|interface Pet { name: String! }
				|type Dog implements Pet { name: String!, nickname: String }
				|type Cat implements Pet { name: String!, meowVolume: Int }
				|type Query { pet: Pet }
			""",
		)
	}
}

private const val TWO_CONFLICTING_FRAGMENT_SPREADS_DOCUMENT = """
	|fragment f on Query {
	|   ...a
	|   ...b
	|}
	|fragment a on Query { x: foo }
	|fragment b on Query { x: bar }
"""

private val conflictBetweenTwoFragmentSpreadsErrors = listOf(
	"""
	Fields "x" conflict because "foo" and "bar" are different fields. Use different aliases on the fields to fetch both if this was intentional.

	<document>:5:23
	4 | }
	5 | fragment a on Query { x: foo }
	  |                       ^
	6 | fragment b on Query { x: bar }

	<document>:6:23
	5 | fragment a on Query { x: foo }
	6 | fragment b on Query { x: bar }
	  |                       ^
""",
)

private val conflictBetweenTransitivelyReferencedFragmentsErrors = listOf(
	"""
	Fields "x" conflict because "foo" and "bar" are different fields. Use different aliases on the fields to fetch both if this was intentional.

	<document>:7:23
	6 | fragment b on Query { x: bar }
	7 | fragment c on Query { x: foo }
	  |                       ^

	<document>:6:23
	5 | fragment a on Query { ...c }
	6 | fragment b on Query { x: bar }
	  |                       ^
	7 | fragment c on Query { x: foo }
""",
)

private val conflictWithSelfSpreadingFragmentErrors = listOf(
	"""
	Fields "x" conflict because "foo" and "bar" are different fields. Use different aliases on the fields to fetch both if this was intentional.

	<document>:2:4
	1 | fragment f on Query {
	2 |    x: foo
	  |    ^
	3 |    ...a

	<document>:5:28
	4 | }
	5 | fragment a on Query { ...a x: bar }
	  |                            ^
""",
)

private val conflictWithSpreadUnderTwoTypeConditionsErrors = listOf(
	"""
	Fields "name" conflict because "meowVolume" and "name" are different fields. Use different aliases on the fields to fetch both if this was intentional.

	<document>:4:17
	3 |    ... on Cat { ...g }
	4 |    ... on Cat { name: meowVolume }
	  |                 ^
	5 | }

	<document>:6:21
	5 | }
	6 | fragment g on Pet { name }
	  |                     ^
""",
)
