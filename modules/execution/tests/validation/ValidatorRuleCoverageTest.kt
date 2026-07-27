package testing

import io.fluidsonic.graphql.AllVariableUsesDefinedRule
import io.fluidsonic.graphql.AllVariablesUsedRule
import io.fluidsonic.graphql.AnonymousOperationExclusivityRule
import io.fluidsonic.graphql.ArgumentExistenceRule
import io.fluidsonic.graphql.ArgumentRequirementRule
import io.fluidsonic.graphql.ArgumentUniquenessRule
import io.fluidsonic.graphql.DirectiveExclusivityRule
import io.fluidsonic.graphql.DirectiveExistenceRule
import io.fluidsonic.graphql.DirectiveLocationValidityRule
import io.fluidsonic.graphql.DocumentExecutabilityRule
import io.fluidsonic.graphql.FieldSelectionExistenceRule
import io.fluidsonic.graphql.FragmentCycleDetectionRule
import io.fluidsonic.graphql.FragmentDefinitionNameExclusivityRule
import io.fluidsonic.graphql.FragmentDefinitionUsageRule
import io.fluidsonic.graphql.FragmentSelectionExistenceRule
import io.fluidsonic.graphql.FragmentSelectionPossibilityRule
import io.fluidsonic.graphql.FragmentTypeConditionExistenceRule
import io.fluidsonic.graphql.FragmentTypeConditionValidityRule
import io.fluidsonic.graphql.GDocument
import io.fluidsonic.graphql.GSchema
import io.fluidsonic.graphql.ObjectFieldExistenceRule
import io.fluidsonic.graphql.ObjectFieldNameExclusivityRule
import io.fluidsonic.graphql.ObjectFieldRequirementRule
import io.fluidsonic.graphql.OperationDefinitionNameExclusivityRule
import io.fluidsonic.graphql.ScalarLeavesRule
import io.fluidsonic.graphql.SelectionUnambiguityRule
import io.fluidsonic.graphql.SubscriptionRootFieldExclusivityRule
import io.fluidsonic.graphql.ValidationRule
import io.fluidsonic.graphql.Validator
import io.fluidsonic.graphql.ValueValidityRule
import io.fluidsonic.graphql.VariableDefinitionNameExclusivityRule
import io.fluidsonic.graphql.VariableDefinitionTypeValidityRule
import io.fluidsonic.graphql.VariablesInAllowedPositionRule
import io.fluidsonic.graphql.validate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

// Every rule registered in `Validator.default` must demonstrably fire *through* `Validator`, both alone and while
// sharing one traversal with the other 28 rules.
//
// The ~220 per-rule tests all go through `assertValidationRule`, which runs a single rule against a fresh
// `ValidationContext` and never touches `Validator`. A rule that the shared traversal fails to dispatch to would
// therefore be completely dead in production while its own tests stay green. That has happened before in this
// codebase and went unnoticed for a long time. `testEachRuleFiresThroughTheDefaultValidator` is the assertion that
// closes the hole; `testEachRuleFiresInIsolation` exists to prove each fixture really targets the rule it claims,
// by pinning that the rule alone reports exactly the one expected message on it.
class ValidatorRuleCoverageTest {

	@Test
	fun testEachRuleFiresInIsolation() {
		for (case in ruleCases) {
			val messages = Validator(rules = listOf(case.rule))
				.validate(document = case.parseDocument(), schema = case.parseSchema())
				.map { it.message }

			assertEquals(
				actual = messages,
				expected = listOf(case.expectedMessage),
				message = "${case.name} alone must report exactly its expected message on its own fixture.",
			)
		}
	}

	@Test
	fun testEachRuleFiresThroughTheDefaultValidator() {
		for (case in ruleCases) {
			val messages = case.parseDocument().validate(case.parseSchema()).map { it.message }

			assertTrue(
				messages.contains(case.expectedMessage),
				"${case.name} went inert in the shared traversal: `document.validate(schema)` did not report " +
					"\"${case.expectedMessage}\". Reported instead: $messages",
			)
		}
	}

	// Without this, a rule added to `Validator.default` would silently escape the two tests above.
	@Test
	fun testEveryRegisteredRuleHasACase() {
		assertEquals(
			actual = ruleCases.map { it.name }.sorted(),
			expected = Validator.default.rules.map { it.ruleName }.sorted(),
			message = "Every rule registered in `Validator.default` needs a case in `ruleCases`, and vice versa.",
		)
	}

	@Test
	fun testNoRuleIsCoveredTwice() {
		val duplicates = ruleCases.groupBy { it.rule }.filterValues { it.size > 1 }.keys

		assertTrue(duplicates.isEmpty(), "Rules covered by more than one case: $duplicates")
	}
}

// Six of the 29 rules are registered through a companion object, whose `simpleName` is just "Companion" — which
// would collapse them into one indistinguishable name and blunt `testEveryRegisteredRuleHasACase`. The enclosing
// class name is the stable identity.
private val ValidationRule.Provider.ruleName: String
	get() = (this::class.qualifiedName ?: this::class.simpleName ?: toString())
		.removeSuffix(".Companion")
		.substringAfterLast('.')

private class RuleCase(val rule: ValidationRule.Provider, val document: String, val expectedMessage: String, val schema: String = RULE_SCHEMA) {

	val name: String get() = rule.ruleName

	fun parseDocument() = GDocument.parse(document.trimIndent()).valueOrThrow()

	fun parseSchema() = GSchema.parse(schema.trimIndent()).valueOrThrow()
}

// One schema serves every case that does not need an unusual shape: it carries an optional and a required field
// argument, an optional and a required input object, two unrelated object types, a non-repeatable directive, and a
// subscription root with two fields.
private const val RULE_SCHEMA = """
	schema {
		query: Query
		subscription: Subscription
	}

	type Query {
		id: ID
		int: Int
		obj: Obj
		withArg(a: Int): String
		withRequiredArg(a: Int!): String
		withInput(input: In): String
		withRequiredInput(input: RequiredIn): String
	}

	type Obj {
		x: String
	}

	type Other {
		x: String
	}

	type Subscription {
		first: Int
		second: Int
	}

	input In {
		a: Int
	}

	input RequiredIn {
		a: Int!
	}

	directive @once on FIELD
"""

private const val DOLLAR = "$"

private val ruleCases = listOf(
	RuleCase(
		rule = AllVariableUsesDefinedRule,
		document = "query Named { withArg(a: ${DOLLAR}undefined) }",
		expectedMessage = "Variable '${DOLLAR}undefined' is not defined.",
	),
	RuleCase(
		rule = AllVariablesUsedRule,
		document = "query Named(${DOLLAR}unused: Int) { id }",
		expectedMessage = "Variable '${DOLLAR}unused' is defined but never used.",
	),
	RuleCase(
		rule = AnonymousOperationExclusivityRule,
		document = """
			{ id }
			query Named { id }
		""",
		expectedMessage = "The document must not contain more than one operation if it contains an anonymous operation.",
	),
	RuleCase(
		rule = ArgumentExistenceRule,
		document = "{ withArg(a: 1, bogus: 2) }",
		expectedMessage = "Unknown argument 'bogus' for field 'Query.withArg'.",
	),
	RuleCase(
		rule = ArgumentRequirementRule,
		document = "{ withRequiredArg }",
		expectedMessage = "Selection of field 'withRequiredArg' is missing required argument 'a'.",
	),
	RuleCase(
		rule = ArgumentUniquenessRule,
		document = "{ withArg(a: 1, a: 2) }",
		expectedMessage = "Argument 'a' must not occur multiple times.",
	),
	RuleCase(
		rule = DirectiveExclusivityRule,
		document = "{ id @once @once }",
		expectedMessage = "Directive '@once' must not occur multiple times.",
	),
	RuleCase(
		rule = DirectiveExistenceRule,
		document = "{ id @nope }",
		expectedMessage = "Unknown directive '@nope'.",
	),
	RuleCase(
		rule = DirectiveLocationValidityRule,
		document = "query Named @skip(if: true) { id }",
		expectedMessage = "Directive '@skip' is not valid on QUERY but only on FIELD, FRAGMENT_SPREAD or INLINE_FRAGMENT.",
	),
	// The only case whose document is not purely executable — the rule exists precisely to reject that.
	RuleCase(
		rule = DocumentExecutabilityRule,
		document = """
			{ id }
			scalar Extra
		""",
		expectedMessage = "In order to be executable, the document must contain only executable definitions.",
	),
	RuleCase(
		rule = FieldSelectionExistenceRule,
		document = "{ nope }",
		expectedMessage = "Cannot select nonexistent field 'nope' on type 'Query'.",
	),
	RuleCase(
		rule = FragmentCycleDetectionRule,
		document = """
			{ ...cycle }
			fragment cycle on Query { id ...cycle }
		""",
		expectedMessage = "Fragment 'cycle' cannot recursively reference itself.",
	),
	RuleCase(
		rule = FragmentDefinitionNameExclusivityRule,
		document = """
			{ ...duplicate }
			fragment duplicate on Query { id }
			fragment duplicate on Query { int }
		""",
		expectedMessage = "The document must not contain multiple fragments with the same name 'duplicate'.",
	),
	RuleCase(
		rule = FragmentDefinitionUsageRule,
		document = """
			{ id }
			fragment unused on Query { id }
		""",
		expectedMessage = "Fragment 'unused' is not used by any operation.",
	),
	RuleCase(
		rule = FragmentSelectionExistenceRule,
		document = "{ ...missing }",
		expectedMessage = "Fragment 'missing' does not exist.",
	),
	RuleCase(
		rule = FragmentSelectionPossibilityRule,
		document = """
			{ obj { ...onOther } }
			fragment onOther on Other { x }
		""",
		expectedMessage = "Fragment 'onOther' on 'Other' will never match the unrelated type 'Obj'.",
	),
	RuleCase(
		rule = FragmentTypeConditionExistenceRule,
		document = """
			{ ...onMissing }
			fragment onMissing on Missing { x }
		""",
		expectedMessage = "A fragment must be specified on a type that exist in the schema.",
	),
	RuleCase(
		rule = FragmentTypeConditionValidityRule,
		document = """
			{ ...onInput }
			fragment onInput on In { a }
		""",
		expectedMessage = "Fragment \"onInput\" cannot condition on non composite type \"In\".",
	),
	RuleCase(
		rule = ObjectFieldExistenceRule,
		document = "{ withInput(input: { nope: 1 }) }",
		expectedMessage = "Unknown field 'nope' for input object type 'In'.",
	),
	RuleCase(
		rule = ObjectFieldNameExclusivityRule,
		document = "{ withInput(input: { a: 1, a: 2 }) }",
		expectedMessage = "An input object can only have a single field named 'a'.",
	),
	RuleCase(
		rule = ObjectFieldRequirementRule,
		document = "{ withRequiredInput(input: { }) }",
		expectedMessage = "Value for Input type 'RequiredIn' is missing required field 'a'.",
	),
	RuleCase(
		rule = OperationDefinitionNameExclusivityRule,
		document = """
			query Named { id }
			query Named { int }
		""",
		expectedMessage = "The document must not contain multiple operations with the same name 'Named'.",
	),
	RuleCase(
		rule = ScalarLeavesRule,
		document = "{ obj }",
		expectedMessage = """Field "obj" of type "Obj" must have a selection of subfields. Did you mean "obj { ... }"?""",
	),
	RuleCase(
		rule = SelectionUnambiguityRule,
		document = "{ withArg(a: 1) withArg(a: 2) }",
		expectedMessage = "Fields \"withArg\" conflict because they have differing arguments. " +
			"Use different aliases on the fields to fetch both if this was intentional.",
	),
	// The only case that needs a subscription operation, and therefore a schema with a subscription root type.
	RuleCase(
		rule = SubscriptionRootFieldExclusivityRule,
		document = "subscription Named { first second }",
		expectedMessage = "Subscription operations must have exactly one root field.",
	),
	RuleCase(
		rule = ValueValidityRule,
		document = """{ withArg(a: "not an int") }""",
		expectedMessage = "Int cannot represent non-integer value: \"not an int\"",
	),
	RuleCase(
		rule = VariableDefinitionNameExclusivityRule,
		document = "query Named(${DOLLAR}duplicate: Int, ${DOLLAR}duplicate: Int) { withArg(a: ${DOLLAR}duplicate) }",
		expectedMessage = "Operation 'Named' must not contain multiple variables with the same name '${DOLLAR}duplicate'.",
	),
	RuleCase(
		rule = VariableDefinitionTypeValidityRule,
		document = "query Named(${DOLLAR}output: Obj) { id }",
		expectedMessage = "Variable '${DOLLAR}output' cannot have output type 'Obj'.",
	),
	RuleCase(
		rule = VariablesInAllowedPositionRule,
		document = "query Named(${DOLLAR}text: String) { withArg(a: ${DOLLAR}text) }",
		expectedMessage = "Variable '${DOLLAR}text' of type 'String' cannot be used as an argument of type 'Int'.",
	),
)
