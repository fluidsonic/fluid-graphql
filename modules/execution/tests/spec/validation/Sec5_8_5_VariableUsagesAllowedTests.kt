package testing

import io.fluidsonic.graphql.VariablesInAllowedPositionRule
import kotlin.test.Test

// GraphQL Spec §5.8.5 — All Variable Usages Are Allowed
class Sec5_8_5_VariableUsagesAllowedTests {

	@Test
	fun testAcceptsVariableWithCompatibleType() {
		assertValidationRule(
			rule = VariablesInAllowedPositionRule,
			errors = emptyList(),
			document = """
				|query Q(${'$'}x: Int) { field(arg: ${'$'}x) }
			""",
			schema = """
				|type Query { field(arg: Int): String }
			""",
		)
	}

	@Test
	fun testRejectsVariableWithIncompatibleType() {
		assertValidationRule(
			rule = VariablesInAllowedPositionRule,
			errors = listOf(
				"""
				Variable '${'$'}x' of type 'String' cannot be used as an argument of type 'Int'.

				<document>:1:9
				1 | query Q(${'$'}x: String) { field(arg: ${'$'}x) }
				  |         ^

				<document>:1:34
				1 | query Q(${'$'}x: String) { field(arg: ${'$'}x) }
				  |                                  ^
			""",
			),
			document = """
				|query Q(${'$'}x: String) { field(arg: ${'$'}x) }
			""",
			schema = """
				|type Query { field(arg: Int): String }
			""",
		)
	}

	@Test
	fun testAcceptsNullableVariableForNullableArg() {
		assertValidationRule(
			rule = VariablesInAllowedPositionRule,
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
	fun testRejectsNullableVariableForNonNullArg() {
		assertValidationRule(
			rule = VariablesInAllowedPositionRule,
			errors = listOf(
				"""
				Variable '${'$'}x' of type 'String' cannot be used as an argument of type 'String!'.

				<document>:1:9
				1 | query Q(${'$'}x: String) { field(arg: ${'$'}x) }
				  |         ^

				<document>:1:34
				1 | query Q(${'$'}x: String) { field(arg: ${'$'}x) }
				  |                                  ^
			""",
			),
			document = """
				|query Q(${'$'}x: String) { field(arg: ${'$'}x) }
			""",
			schema = """
				|type Query { field(arg: String!): String }
			""",
		)
	}

	@Test
	fun testAcceptsNonNullVariableForNonNullDirectiveArg() {
		assertValidationRule(
			rule = VariablesInAllowedPositionRule,
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
	fun testRejectsNullableVariableForNonNullDirectiveArg() {
		assertValidationRule(
			rule = VariablesInAllowedPositionRule,
			errors = listOf(
				"""
				Variable '${'$'}v' of type 'Boolean' cannot be used as an argument of type 'Boolean!'.

				<document>:1:9
				1 | query Q(${'$'}v: Boolean) { field @include(if: ${'$'}v) }
				  |         ^

				<document>:1:43
				1 | query Q(${'$'}v: Boolean) { field @include(if: ${'$'}v) }
				  |                                           ^
			""",
			),
			document = """
				|query Q(${'$'}v: Boolean) { field @include(if: ${'$'}v) }
			""",
			schema = """
				|type Query { field: String }
			""",
		)
	}

	@Test
	fun testRejectsNullableVariableForNonNullDirectiveArgInFragment() {
		assertValidationRule(
			rule = VariablesInAllowedPositionRule,
			errors = listOf(
				"""
				Variable '${'$'}v' of type 'Boolean' cannot be used as an argument of type 'Boolean!'.

				<document>:1:9
				1 | query Q(${'$'}v: Boolean) { ...frag }
				  |         ^
				2 | fragment frag on Query { field @skip(if: ${'$'}v) }

				<document>:2:42
				1 | query Q(${'$'}v: Boolean) { ...frag }
				2 | fragment frag on Query { field @skip(if: ${'$'}v) }
				  |                                          ^
			""",
			),
			document = """
				|query Q(${'$'}v: Boolean) { ...frag }
				|fragment frag on Query { field @skip(if: ${'$'}v) }
			""",
			schema = """
				|type Query { field: String }
			""",
		)
	}
}
