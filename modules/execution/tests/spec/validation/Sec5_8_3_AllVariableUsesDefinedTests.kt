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

	// Verified against graphql@17.0.2: an operation's own usages come first, each spread's directives included, and
	// then those of every fragment it reaches, once per fragment however often it is spread.
	@Test
	fun testRejectsUndefinedVariablesOfOperationBeforeThoseOfItsFragments() {
		assertValidationRule(
			rule = AllVariableUsesDefinedRule,
			errors = listOf(
				"""
				Variable '${'$'}a' is not defined.

				<document>:1:22
				1 | query Q @custom(arg: ${'$'}a) { ...f @skip(if: ${'$'}b) field ...f @include(if: ${'$'}c) }
				  |                      ^
				2 | fragment f on Query @custom(arg: ${'$'}d) { field(arg: ${'$'}e) }
			""",
				"""
				Variable '${'$'}b' is not defined.

				<document>:1:43
				1 | query Q @custom(arg: ${'$'}a) { ...f @skip(if: ${'$'}b) field ...f @include(if: ${'$'}c) }
				  |                                           ^
				2 | fragment f on Query @custom(arg: ${'$'}d) { field(arg: ${'$'}e) }
			""",
				"""
				Variable '${'$'}c' is not defined.

				<document>:1:71
				1 | query Q @custom(arg: ${'$'}a) { ...f @skip(if: ${'$'}b) field ...f @include(if: ${'$'}c) }
				  |                                                                       ^
				2 | fragment f on Query @custom(arg: ${'$'}d) { field(arg: ${'$'}e) }
			""",
				"""
				Variable '${'$'}d' is not defined.

				<document>:2:34
				1 | query Q @custom(arg: ${'$'}a) { ...f @skip(if: ${'$'}b) field ...f @include(if: ${'$'}c) }
				2 | fragment f on Query @custom(arg: ${'$'}d) { field(arg: ${'$'}e) }
				  |                                  ^
			""",
				"""
				Variable '${'$'}e' is not defined.

				<document>:2:51
				1 | query Q @custom(arg: ${'$'}a) { ...f @skip(if: ${'$'}b) field ...f @include(if: ${'$'}c) }
				2 | fragment f on Query @custom(arg: ${'$'}d) { field(arg: ${'$'}e) }
				  |                                                   ^
			""",
			),
			document = """
				|query Q @custom(arg: ${'$'}a) { ...f @skip(if: ${'$'}b) field ...f @include(if: ${'$'}c) }
				|fragment f on Query @custom(arg: ${'$'}d) { field(arg: ${'$'}e) }
			""",
			schema = """
				|directive @custom(arg: Boolean) on QUERY | FRAGMENT_DEFINITION
				|type Query { field(arg: String): String }
			""",
		)
	}

	// graphql-js `getRecursivelyReferencedFragments` takes a selection set's own spreads before those nested in it,
	// and visits nested selection sets last in, first out. Verified against graphql@17.0.2: `$c`, `$b`, then `$a`.
	@Test
	fun testRejectsUndefinedVariablesOfFragmentsInUpstreamOrder() {
		assertValidationRule(
			rule = AllVariableUsesDefinedRule,
			errors = listOf(
				"""
				Variable '${'$'}c' is not defined.

				<document>:4:35
				3 | fragment f2 on O { field(arg: ${'$'}b) }
				4 | fragment f3 on Query { field(arg: ${'$'}c) }
				  |                                   ^
			""",
				"""
				Variable '${'$'}b' is not defined.

				<document>:3:31
				2 | fragment f1 on Query { field(arg: ${'$'}a) }
				3 | fragment f2 on O { field(arg: ${'$'}b) }
				  |                               ^
				4 | fragment f3 on Query { field(arg: ${'$'}c) }
			""",
				"""
				Variable '${'$'}a' is not defined.

				<document>:2:35
				1 | query Q { ... { ...f1 } o { ...f2 } ...f3 }
				2 | fragment f1 on Query { field(arg: ${'$'}a) }
				  |                                   ^
				3 | fragment f2 on O { field(arg: ${'$'}b) }
			""",
			),
			document = """
				|query Q { ... { ...f1 } o { ...f2 } ...f3 }
				|fragment f1 on Query { field(arg: ${'$'}a) }
				|fragment f2 on O { field(arg: ${'$'}b) }
				|fragment f3 on Query { field(arg: ${'$'}c) }
			""",
			schema = """
				|type Query { field(arg: String): String o: O }
				|type O { field(arg: String): String }
			""",
		)
	}
}
