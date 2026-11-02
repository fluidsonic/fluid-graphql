package testing

import io.fluidsonic.graphql.AllVariableUsesDefinedRule
import kotlin.test.Test

// GraphQL Spec §5.8.3 — All Variable Uses Defined
class Sec5_8_3_AllVariableUsesDefinedTests {

	@Test
	fun testAcceptsVariableUsedAfterDefined() {
		assertValidationRule(
			rule = AllVariableUsesDefinedRule,
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
	fun testRejectsUndefinedVariableUsed() {
		assertValidationRule(
			rule = AllVariableUsesDefinedRule,
			errors = listOf(
				"""
				Variable '${'$'}x' is not defined.

				<document>:1:22
				1 | query Q { field(arg: ${'$'}x) }
				  |                      ^
			""",
			),
			document = """
				|query Q { field(arg: ${'$'}x) }
			""",
			schema = """
				|type Query { field(arg: String): String }
			""",
		)
	}

	@Test
	fun testAcceptsVariableInFragmentWhereDefinedInOperation() {
		assertValidationRule(
			rule = AllVariableUsesDefinedRule,
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
	fun testRejectsUndefinedVariablesUsedInDirectivesInDocumentOrder() {
		assertValidationRule(
			rule = AllVariableUsesDefinedRule,
			errors = listOf(
				"""
				Variable '${'$'}a' is not defined.

				<document>:1:22
				1 | query Q @custom(arg: ${'$'}a) { field(arg: ${'$'}b) @include(if: ${'$'}c) ...frag @skip(if: ${'$'}d) }
				  |                      ^
				2 | fragment frag on Query @custom(arg: ${'$'}e) { field }
			""",
				"""
				Variable '${'$'}b' is not defined.

				<document>:1:39
				1 | query Q @custom(arg: ${'$'}a) { field(arg: ${'$'}b) @include(if: ${'$'}c) ...frag @skip(if: ${'$'}d) }
				  |                                       ^
				2 | fragment frag on Query @custom(arg: ${'$'}e) { field }
			""",
				"""
				Variable '${'$'}c' is not defined.

				<document>:1:56
				1 | query Q @custom(arg: ${'$'}a) { field(arg: ${'$'}b) @include(if: ${'$'}c) ...frag @skip(if: ${'$'}d) }
				  |                                                        ^
				2 | fragment frag on Query @custom(arg: ${'$'}e) { field }
			""",
				"""
				Variable '${'$'}d' is not defined.

				<document>:1:78
				1 | query Q @custom(arg: ${'$'}a) { field(arg: ${'$'}b) @include(if: ${'$'}c) ...frag @skip(if: ${'$'}d) }
				  |                                                                              ^
				2 | fragment frag on Query @custom(arg: ${'$'}e) { field }
			""",
				"""
				Variable '${'$'}e' is not defined.

				<document>:2:37
				1 | query Q @custom(arg: ${'$'}a) { field(arg: ${'$'}b) @include(if: ${'$'}c) ...frag @skip(if: ${'$'}d) }
				2 | fragment frag on Query @custom(arg: ${'$'}e) { field }
				  |                                     ^
			""",
			),
			document = """
				|query Q @custom(arg: ${'$'}a) { field(arg: ${'$'}b) @include(if: ${'$'}c) ...frag @skip(if: ${'$'}d) }
				|fragment frag on Query @custom(arg: ${'$'}e) { field }
			""",
			schema = """
				|directive @custom(arg: Boolean) on QUERY | FRAGMENT_DEFINITION
				|type Query { field(arg: String): String }
			""",
		)
	}
}
