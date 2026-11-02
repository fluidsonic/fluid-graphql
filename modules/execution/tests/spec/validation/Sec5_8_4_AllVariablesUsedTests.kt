package testing

import io.fluidsonic.graphql.AllVariablesUsedRule
import kotlin.test.Test

// GraphQL Spec §5.8.4 — All Variables Used
class Sec5_8_4_AllVariablesUsedTests {

	@Test
	fun testAcceptsVariableUsed() {
		assertValidationRule(
			rule = AllVariablesUsedRule,
			errors = emptyList(),
			document = """
				|query Q(${'$'}x: String) { field(arg: ${'$'}x) }
			""",
			schema = """
				|type Query { field(arg: String): String }
			""",
		)
	}

	@Test
	fun testRejectsVariableNotUsed() {
		assertValidationRule(
			rule = AllVariablesUsedRule,
			errors = listOf(
				"""
				Variable '${'$'}x' is defined but never used.

				<document>:1:10
				1 | query Q(${'$'}x: String) { field }
				  |          ^
			""",
			),
			document = """
				|query Q(${'$'}x: String) { field }
			""",
			schema = """
				|type Query { field: String }
			""",
		)
	}

	@Test
	fun testAcceptsVariableUsedInFragment() {
		assertValidationRule(
			rule = AllVariablesUsedRule,
			errors = emptyList(),
			document = """
				|query Q(${'$'}x: String) { ...frag }
				|fragment frag on Query { field(arg: ${'$'}x) }
			""",
			schema = """
				|type Query { field(arg: String): String }
			""",
		)
	}

	@Test
	fun testAcceptsVariableUsedInIncludeDirective() {
		assertValidationRule(
			rule = AllVariablesUsedRule,
			errors = emptyList(),
			document = """
				|query Q(${'$'}v: Boolean!) { field @include(if: ${'$'}v) }
			""",
			schema = """
				|type Query { field: String }
			""",
		)
	}

	@Test
	fun testAcceptsVariableUsedInSkipDirective() {
		assertValidationRule(
			rule = AllVariablesUsedRule,
			errors = emptyList(),
			document = """
				|query Q(${'$'}v: Boolean!) { field @skip(if: ${'$'}v) }
			""",
			schema = """
				|type Query { field: String }
			""",
		)
	}

	@Test
	fun testAcceptsVariableUsedInFragmentSpreadDirective() {
		assertValidationRule(
			rule = AllVariablesUsedRule,
			errors = emptyList(),
			document = """
				|query Q(${'$'}v: Boolean!) { ...frag @include(if: ${'$'}v) }
				|fragment frag on Query { field }
			""",
			schema = """
				|type Query { field: String }
			""",
		)
	}

	@Test
	fun testAcceptsVariableUsedInInlineFragmentDirective() {
		assertValidationRule(
			rule = AllVariablesUsedRule,
			errors = emptyList(),
			document = """
				|query Q(${'$'}v: Boolean!) { ... @include(if: ${'$'}v) { field } }
			""",
			schema = """
				|type Query { field: String }
			""",
		)
	}

	@Test
	fun testAcceptsVariableUsedInDirectiveInFragment() {
		assertValidationRule(
			rule = AllVariablesUsedRule,
			errors = emptyList(),
			document = """
				|query Q(${'$'}v: Boolean!) { ...frag }
				|fragment frag on Query { field @skip(if: ${'$'}v) }
			""",
			schema = """
				|type Query { field: String }
			""",
		)
	}

	@Test
	fun testAcceptsVariableUsedInFragmentDefinitionDirective() {
		assertValidationRule(
			rule = AllVariablesUsedRule,
			errors = emptyList(),
			document = """
				|query Q(${'$'}v: Boolean) { ...frag }
				|fragment frag on Query @custom(arg: ${'$'}v) { field }
			""",
			schema = """
				|directive @custom(arg: Boolean) on FRAGMENT_DEFINITION
				|type Query { field: String }
			""",
		)
	}

	@Test
	fun testAcceptsVariableUsedInOperationDirective() {
		assertValidationRule(
			rule = AllVariablesUsedRule,
			errors = emptyList(),
			document = """
				|query Q(${'$'}v: Boolean) @custom(arg: ${'$'}v) { field }
			""",
			schema = """
				|directive @custom(arg: Boolean) on QUERY
				|type Query { field: String }
			""",
		)
	}

	@Test
	fun testAcceptsVariableUsedInCustomDirective() {
		assertValidationRule(
			rule = AllVariablesUsedRule,
			errors = emptyList(),
			document = """
				|query Q(${'$'}v: Boolean) { field @custom(arg: [${'$'}v]) }
			""",
			schema = """
				|directive @custom(arg: [Boolean]) on FIELD
				|type Query { field: String }
			""",
		)
	}

	@Test
	fun testAcceptsVariableUsedInDirectiveOfRepeatedFragmentSpread() {
		assertValidationRule(
			rule = AllVariablesUsedRule,
			errors = emptyList(),
			document = """
				|query Q(${'$'}v: Boolean!) { ...frag ...frag @include(if: ${'$'}v) }
				|fragment frag on Query { field }
			""",
			schema = """
				|type Query { field: String }
			""",
		)
	}

	@Test
	fun testAcceptsVariableUsedInDirectiveOfUndefinedFragmentSpread() {
		assertValidationRule(
			rule = AllVariablesUsedRule,
			errors = emptyList(),
			document = """
				|query Q(${'$'}v: Boolean!) { field ...missing @include(if: ${'$'}v) }
			""",
			schema = """
				|type Query { field: String }
			""",
		)
	}

	@Test
	fun testRejectsVariableUsedOnlyInDirectiveOfUnreachedFragment() {
		assertValidationRule(
			rule = AllVariablesUsedRule,
			errors = listOf(
				"""
				Variable '${'$'}v' is defined but never used.

				<document>:1:10
				1 | query Q(${'$'}v: Boolean!) { field }
				  |          ^
				2 | fragment frag on Query { field @include(if: ${'$'}v) }
			""",
			),
			document = """
				|query Q(${'$'}v: Boolean!) { field }
				|fragment frag on Query { field @include(if: ${'$'}v) }
			""",
			schema = """
				|type Query { field: String }
			""",
		)
	}
}
