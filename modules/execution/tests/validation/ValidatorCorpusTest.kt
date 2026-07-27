package testing

import io.fluidsonic.graphql.GDocument
import io.fluidsonic.graphql.GSchema
import io.fluidsonic.graphql.validate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

// A corpus run through the real `Validator` via the public `GDocument.validate(schema)`, rather than through
// `assertValidationRule`, which runs a single rule against a fresh context and never touches `Validator` at all.
//
// This exists to catch a refactor of the shared traversal silently disabling validation. Almost every other
// validation test would stay green in that case: the per-rule tests bypass `Validator`, and the one test that
// does use it asserts an empty error list, so it passes when nothing runs.
//
// Each case pins the exact set of messages the whole rule set produces, compared as a sorted set — the order in
// which rules fire is deliberately not pinned, because reorganising the traversal is expected to change it.
// Message text is enough of a detector: the type and field names in these messages come from precisely the
// `VisitorContext` fields a broken traversal would leave null, and a rule that cannot resolve them returns
// early and contributes no message at all.
class ValidatorCorpusTest {

	@Test
	fun testCorpusProducesExactlyTheExpectedErrors() {
		for (case in corpus) {
			val schema = GSchema.parse(case.schema.trimIndent()).valueOrThrow()
			val document = GDocument.parse(case.document.trimIndent()).valueOrThrow()

			assertEquals(
				actual = document.validate(schema).map { it.message }.sorted(),
				expected = case.expectedMessages.sorted(),
				message = "Unexpected validation errors for corpus case '${case.name}'.",
			)
		}
	}

	// A floor on the whole corpus, so that a change which empties several cases at once fails loudly here even
	// if someone "fixes" an individual case's expectations.
	@Test
	fun testCorpusTripsAtLeastFifteenErrors() {
		val total = corpus.sumOf { case ->
			val schema = GSchema.parse(case.schema.trimIndent()).valueOrThrow()
			val document = GDocument.parse(case.document.trimIndent()).valueOrThrow()

			document.validate(schema).size
		}

		assertTrue(total >= 15, "Expected the corpus to trip at least 15 errors but it tripped $total.")
	}

	// The observable signature of a single interleaved traversal. While each rule got its own walk, every error
	// from an earlier-registered rule preceded every error from a later one regardless of where in the document
	// they occurred; now errors come out in document order, with the rules interleaved per node.
	//
	// Here `ScalarLeavesRule` fires on `int` (early in the document) and `FieldSelectionExistenceRule` on `sub`
	// and `nope` (later), while the registry lists `FieldSelectionExistenceRule` first — so grouped-by-rule and
	// document order disagree, and this pins the latter.
	@Test
	fun testErrorsAreReportedInDocumentOrderRatherThanGroupedByRule() {
		val schema = GSchema.parse(CORPUS_SCHEMA.trimIndent()).valueOrThrow()
		val document = GDocument.parse("{ int { sub } nope }").valueOrThrow()

		assertEquals(
			actual = document.validate(schema).map { it.message },
			expected = listOf(
				"""Field "int" must not have a selection since type "Int" has no subfields.""",
				"Cannot select nonexistent field 'sub' on type 'Int'.",
				"Cannot select nonexistent field 'nope' on type 'Query'.",
			),
		)
	}

	// The corpus is only a meaningful guard if it exercises rules that depend on the traversal threading context
	// correctly. Roughly a third of the rules read nothing from `VisitorContext` and would keep working even if
	// every context field were left null, so a corpus made only of those could pass while validation is broken.
	//
	// `ArgumentExistenceRule` and `DirectiveLocationValidityRule` are load-bearing beyond that: they are the only
	// two rules that branch on `data.parentNode`, and so the only ones that can detect a traversal which advances
	// the context more than once per node and thereby makes a node its own parent.
	@Test
	fun testCorpusCoversContextDependentRules() {
		val allMessages = corpus.flatMap { case ->
			val schema = GSchema.parse(case.schema.trimIndent()).valueOrThrow()
			val document = GDocument.parse(case.document.trimIndent()).valueOrThrow()

			document.validate(schema).map { it.message }
		}

		for ((rule, fragment) in contextDependentRuleMarkers) {
			assertTrue(
				allMessages.any { it.contains(fragment) },
				"The corpus must trip $rule — no message contained \"$fragment\". Messages: $allMessages",
			)
		}
	}
}

// Message fragments unique to each rule whose output depends on `VisitorContext` resolving a type, a field
// definition, an argument definition or a parent node.
//
// Verified to bite: replacing `Validator`'s per-rule `contextualize(context)` with the plausible-looking
// `map { it.provide() }.parallelize().contextualize(context)` — which typechecks, and advances the context only
// for the root node — silences every rule below except `SelectionUnambiguityRule`, while twelve
// context-independent rules keep firing and one starts reporting phantom errors. A corpus made only of those
// twelve would have stayed green.
//
// `SelectionUnambiguityRule` survives that trap and is therefore NOT a canary: it resolves types itself through
// `data.schema`, and since it tolerates an unresolvable parent type (matching upstream) it still finds argument
// conflicts with no context at all. It is listed to assert the rule runs, not to detect context breakage.
private val contextDependentRuleMarkers = listOf(
	"FieldSelectionExistenceRule" to "Cannot select nonexistent field",
	"ScalarLeavesRule" to "has no subfields",
	"ArgumentExistenceRule" to "Unknown argument",
	"SelectionUnambiguityRule" to "conflict because",
	"DirectiveLocationValidityRule" to "is not valid on",
	"ValueValidityRule" to "cannot represent",
	"ObjectFieldExistenceRule" to "is not defined by type",
)

private class Case(val name: String, val schema: String, val document: String, val expectedMessages: List<String>)

private const val CORPUS_SCHEMA = """
	type Query {
		id: ID
		int: Int
		obj: Obj
		withArg(a: Int): String
		withInput(input: In): String
	}

	type Obj {
		x: String
	}

	input In {
		a: Int
	}
"""

private val corpus = listOf(
	Case(
		name = "unknown field",
		schema = CORPUS_SCHEMA,
		document = "{ id nope }",
		expectedMessages = listOf("Cannot select nonexistent field 'nope' on type 'Query'."),
	),
	Case(
		name = "leaf with a selection, composite without one",
		schema = CORPUS_SCHEMA,
		document = "{ int { sub } obj }",
		expectedMessages = listOf(
			"""Field "int" must not have a selection since type "Int" has no subfields.""",
			"""Field "obj" of type "Obj" must have a selection of subfields. Did you mean "obj { ... }"?""",
			// The sub-selection is still walked, and `sub` resolves against `Int`, which has no fields.
			"Cannot select nonexistent field 'sub' on type 'Int'.",
		),
	),
	Case(
		name = "unknown and duplicated field arguments",
		schema = CORPUS_SCHEMA,
		document = "{ withArg(a: 1, bogus: 2) }",
		expectedMessages = listOf("Unknown argument 'bogus' for field 'Query.withArg'."),
	),
	Case(
		name = "conflicting field arguments",
		schema = CORPUS_SCHEMA,
		document = "{ withArg(a: 1) withArg(a: 2) }",
		expectedMessages = listOf(
			"Fields \"withArg\" conflict because they have differing arguments. " +
				"Use different aliases on the fields to fetch both if this was intentional.",
		),
	),
	// The rule must reach every definition, not just the first. Covered at rule level too, but it belongs here as
	// well: the leaked `relatedSelectionSet` that used to hide this was a property of the shared traversal, so only
	// a run through the real Validator can prove it stays fixed.
	Case(
		name = "conflict in the second of two operations",
		schema = CORPUS_SCHEMA,
		document = """
			query First { id }
			query Second { withArg(a: 1) withArg(a: 2) }
		""",
		expectedMessages = listOf(
			"Fields \"withArg\" conflict because they have differing arguments. " +
				"Use different aliases on the fields to fetch both if this was intentional.",
		),
	),
	// The sharpest shape: with the fragment first, it is the *operation's* selection set that used to be skipped.
	Case(
		name = "conflict in an operation preceded by a fragment definition",
		schema = CORPUS_SCHEMA,
		document = """
			fragment onlyId on Query { id }
			query Second { ...onlyId withArg(a: 1) withArg(a: 2) }
		""",
		expectedMessages = listOf(
			"Fields \"withArg\" conflict because they have differing arguments. " +
				"Use different aliases on the fields to fetch both if this was intentional.",
		),
	),
	Case(
		name = "directive in an invalid location",
		schema = CORPUS_SCHEMA,
		document = "query Named @skip(if: true) { id }",
		expectedMessages = listOf(
			"Directive '@skip' is not valid on QUERY but only on FIELD, FRAGMENT_SPREAD or INLINE_FRAGMENT.",
		),
	),
	Case(
		name = "invalid literal value",
		schema = CORPUS_SCHEMA,
		document = """{ withArg(a: "not an int") }""",
		expectedMessages = listOf("Int cannot represent non-integer value: \"not an int\""),
	),
	// Three rules fire on one bad input field, and the third is wrong: `In` is an input object type, so an input
	// object value *is* allowed there — `GSchema.validateValue` reports the container as invalid because one of
	// its fields was. Recorded as-is rather than fixed; `validateValue` is rewritten in the next release, and
	// this corpus exists to prove the traversal refactor changes nothing, not to change behaviour itself.
	Case(
		name = "unknown input object field",
		schema = CORPUS_SCHEMA,
		document = "{ withInput(input: { nope: 1 }) }",
		expectedMessages = listOf(
			"Field 'nope' is not defined by type 'In'.",
			"Unknown field 'nope' for input object type 'In'.",
			"Type 'In' does not allow an input object value.",
		),
	),
	Case(
		name = "variables defined but unused, and used but undefined",
		schema = CORPUS_SCHEMA,
		document = "query Named(${'$'}unused: Int) { withArg(a: ${'$'}undefined) }",
		expectedMessages = listOf(
			"Variable '${'$'}unused' is defined but never used.",
			"Variable '${'$'}undefined' is not defined.",
		),
	),
	Case(
		name = "duplicate operation names and an anonymous operation alongside them",
		schema = CORPUS_SCHEMA,
		document = """
			query Named { id }
			query Named { id }
			{ id }
		""",
		expectedMessages = listOf(
			"The document must not contain multiple operations with the same name 'Named'.",
			"The document must not contain more than one operation if it contains an anonymous operation.",
		),
	),
	Case(
		name = "unknown directive",
		schema = CORPUS_SCHEMA,
		document = "{ id @nope }",
		expectedMessages = listOf("Unknown directive '@nope'."),
	),
	Case(
		name = "unused fragment and a spread of a missing one",
		schema = CORPUS_SCHEMA,
		document = """
			{ ...missing }
			fragment unused on Query { id }
		""",
		expectedMessages = listOf(
			"Fragment 'missing' does not exist.",
			"Fragment 'unused' is not used by any operation.",
		),
	),
	Case(
		name = "cyclic fragment",
		schema = CORPUS_SCHEMA,
		document = """
			{ ...cycle }
			fragment cycle on Query { id ...cycle }
		""",
		expectedMessages = listOf("Fragment 'cycle' cannot recursively reference itself."),
	),
)
