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

	// Each operation is checked against the fragments it reaches, whatever the document order. Verified against
	// graphql@17.0.2, as are the three tests below.
	@Test
	fun testRejectsNullableVariableOfOperationSharingFragmentWithLaterOperation() {
		assertValidationRule(
			rule = VariablesInAllowedPositionRule,
			errors = listOf(
				"""
				Variable '${'$'}v' of type 'Boolean' cannot be used as an argument of type 'Boolean!'.

				<document>:1:9
				1 | query B(${'$'}v: Boolean) { ...f }
				  |         ^
				2 | query A(${'$'}v: Boolean!) { ...f }

				<document>:3:42
				2 | query A(${'$'}v: Boolean!) { ...f }
				3 | fragment f on Query { field @include(if: ${'$'}v) }
				  |                                          ^
			""",
			),
			document = """
				|query B(${'$'}v: Boolean) { ...f }
				|query A(${'$'}v: Boolean!) { ...f }
				|fragment f on Query { field @include(if: ${'$'}v) }
			""",
			schema = """
				|type Query { field: String }
			""",
		)
	}

	@Test
	fun testRejectsNullableVariableUsedInFragmentDefinedBeforeOperation() {
		assertValidationRule(
			rule = VariablesInAllowedPositionRule,
			errors = listOf(
				"""
				Variable '${'$'}v' of type 'Boolean' cannot be used as an argument of type 'Boolean!'.

				<document>:2:9
				1 | fragment f on Query { field @include(if: ${'$'}v) }
				2 | query Q(${'$'}v: Boolean) { ...f }
				  |         ^

				<document>:1:42
				1 | fragment f on Query { field @include(if: ${'$'}v) }
				  |                                          ^
				2 | query Q(${'$'}v: Boolean) { ...f }
			""",
			),
			document = """
				|fragment f on Query { field @include(if: ${'$'}v) }
				|query Q(${'$'}v: Boolean) { ...f }
			""",
			schema = """
				|type Query { field: String }
			""",
		)
	}

	@Test
	fun testAcceptsAnyVariableUsedInUnreferencedFragment() {
		assertValidationRule(
			rule = VariablesInAllowedPositionRule,
			errors = emptyList(),
			document = """
				|query Q(${'$'}v: Boolean) { field }
				|fragment f on Query { field @include(if: ${'$'}v) }
			""",
			schema = """
				|type Query { field: String }
			""",
		)
	}

	@Test
	fun testRejectsVariableWithIncompatibleTypeInFragmentOnNonRootType() {
		assertValidationRule(
			rule = VariablesInAllowedPositionRule,
			errors = listOf(
				"""
				Variable '${'$'}x' of type 'String' cannot be used as an argument of type 'Int'.

				<document>:1:9
				1 | query Q(${'$'}x: String) { o { ...f } }
				  |         ^
				2 | fragment f on O { field(arg: ${'$'}x) }

				<document>:2:30
				1 | query Q(${'$'}x: String) { o { ...f } }
				2 | fragment f on O { field(arg: ${'$'}x) }
				  |                              ^
			""",
			),
			document = """
				|query Q(${'$'}x: String) { o { ...f } }
				|fragment f on O { field(arg: ${'$'}x) }
			""",
			schema = """
				|type Query { o: O }
				|type O { field(arg: Int): String }
			""",
		)
	}

	@Test
	fun testRejectsNullableVariableForIntrospectionFieldArgInFragment() {
		assertValidationRule(
			rule = VariablesInAllowedPositionRule,
			errors = listOf(
				"""
				Variable '${'$'}n' of type 'String' cannot be used as an argument of type 'String!'.

				<document>:1:9
				1 | query Q(${'$'}n: String) { ...f }
				  |         ^
				2 | fragment f on Query { __type(name: ${'$'}n) { name } }

				<document>:2:36
				1 | query Q(${'$'}n: String) { ...f }
				2 | fragment f on Query { __type(name: ${'$'}n) { name } }
				  |                                    ^
			""",
			),
			document = """
				|query Q(${'$'}n: String) { ...f }
				|fragment f on Query { __type(name: ${'$'}n) { name } }
			""",
			schema = """
				|type Query { field: String }
			""",
		)
	}

	@Test
	fun testRejectsNullableVariableOnceInCyclicFragments() {
		assertValidationRule(
			rule = VariablesInAllowedPositionRule,
			errors = listOf(
				"""
				Variable '${'$'}v' of type 'Boolean' cannot be used as an argument of type 'Boolean!'.

				<document>:1:9
				1 | query Q(${'$'}v: Boolean) { ...a }
				  |         ^
				2 | fragment a on Query { ...b field @include(if: ${'$'}v) }

				<document>:2:47
				1 | query Q(${'$'}v: Boolean) { ...a }
				2 | fragment a on Query { ...b field @include(if: ${'$'}v) }
				  |                                               ^
				3 | fragment b on Query { ...a }
			""",
			),
			document = """
				|query Q(${'$'}v: Boolean) { ...a }
				|fragment a on Query { ...b field @include(if: ${'$'}v) }
				|fragment b on Query { ...a }
			""",
			schema = """
				|type Query { field: String }
			""",
		)
	}

	// A list item takes the list's item type, and a list value where no list is expected takes that type itself, made
	// nullable. A list item never has a default value. Verified against graphql@17.0.2, as are the three tests below.
	@Test
	fun testAcceptsNonNullVariableForNonNullListItem() {
		assertValidationRule(
			rule = VariablesInAllowedPositionRule,
			errors = emptyList(),
			document = """
				|query Q(${'$'}v: Int!) { field(list: [${'$'}v]) }
			""",
			schema = """
				|type Query { field(list: [Int!]): String }
			""",
		)
	}

	@Test
	fun testRejectsNullableVariableForNonNullListItem() {
		assertValidationRule(
			rule = VariablesInAllowedPositionRule,
			errors = listOf(
				"""
				Variable '${'$'}v' of type 'Int' cannot be used as an argument of type 'Int!'.

				<document>:1:9
				1 | query Q(${'$'}v: Int) { field(list: [${'$'}v]) }
				  |         ^

				<document>:1:33
				1 | query Q(${'$'}v: Int) { field(list: [${'$'}v]) }
				  |                                 ^
			""",
			),
			document = """
				|query Q(${'$'}v: Int) { field(list: [${'$'}v]) }
			""",
			schema = """
				|type Query { field(list: [Int!]): String }
			""",
		)
	}

	@Test
	fun testRejectsNullableVariableForNonNullListItemOfArgWithDefault() {
		assertValidationRule(
			rule = VariablesInAllowedPositionRule,
			errors = listOf(
				"""
				Variable '${'$'}v' of type 'Int' cannot be used as an argument of type 'Int!'.

				<document>:1:9
				1 | query Q(${'$'}v: Int) { field(list: [${'$'}v]) }
				  |         ^

				<document>:1:33
				1 | query Q(${'$'}v: Int) { field(list: [${'$'}v]) }
				  |                                 ^
			""",
			),
			document = """
				|query Q(${'$'}v: Int) { field(list: [${'$'}v]) }
			""",
			schema = """
				|type Query { field(list: [Int!] = [1]): String }
			""",
		)
	}

	@Test
	fun testAcceptsNullableVariableInListForNonListArg() {
		assertValidationRule(
			rule = VariablesInAllowedPositionRule,
			errors = emptyList(),
			document = """
				|query Q(${'$'}v: Int) { field(arg: [${'$'}v]) }
			""",
			schema = """
				|type Query { field(arg: Int!): String }
			""",
		)
	}

	// graphql-js resolves a fragment name to the last definition carrying it. Verified against graphql@17.0.2.
	@Test
	fun testChecksLastOfDuplicateFragmentDefinitions() {
		assertValidationRule(
			rule = VariablesInAllowedPositionRule,
			errors = listOf(
				"""
				Variable '${'$'}v' of type 'Boolean' cannot be used as an argument of type 'Boolean!'.

				<document>:1:9
				1 | query Q(${'$'}v: Boolean) { ...f }
				  |         ^
				2 | fragment f on Query { field }

				<document>:3:42
				2 | fragment f on Query { field }
				3 | fragment f on Query { field @include(if: ${'$'}v) }
				  |                                          ^
			""",
			),
			document = """
				|query Q(${'$'}v: Boolean) { ...f }
				|fragment f on Query { field }
				|fragment f on Query { field @include(if: ${'$'}v) }
			""",
			schema = """
				|type Query { field: String }
			""",
		)
	}

	@Test
	fun testRejectsNullableVariableOfEachOperationSharingFragment() {
		assertValidationRule(
			rule = VariablesInAllowedPositionRule,
			errors = listOf(
				"""
				Variable '${'$'}v' of type 'Boolean' cannot be used as an argument of type 'Boolean!'.

				<document>:1:9
				1 | query A(${'$'}v: Boolean) { ...f }
				  |         ^
				2 | query B(${'$'}v: Boolean) { ...f }

				<document>:3:42
				2 | query B(${'$'}v: Boolean) { ...f }
				3 | fragment f on Query { field @include(if: ${'$'}v) }
				  |                                          ^
			""",
				"""
				Variable '${'$'}v' of type 'Boolean' cannot be used as an argument of type 'Boolean!'.

				<document>:2:9
				1 | query A(${'$'}v: Boolean) { ...f }
				2 | query B(${'$'}v: Boolean) { ...f }
				  |         ^
				3 | fragment f on Query { field @include(if: ${'$'}v) }

				<document>:3:42
				2 | query B(${'$'}v: Boolean) { ...f }
				3 | fragment f on Query { field @include(if: ${'$'}v) }
				  |                                          ^
			""",
			),
			document = """
				|query A(${'$'}v: Boolean) { ...f }
				|query B(${'$'}v: Boolean) { ...f }
				|fragment f on Query { field @include(if: ${'$'}v) }
			""",
			schema = """
				|type Query { field: String }
			""",
		)
	}
}
