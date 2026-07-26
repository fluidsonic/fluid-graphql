package testing

import io.fluidsonic.graphql.VariablesInAllowedPositionRule
import kotlin.test.Test

// https://spec.graphql.org/draft/#sec-All-Variable-Usages-Are-Allowed
class VariablesInAllowedPositionRuleTest {

	private val oneOfSchema = """
		|input Input @oneOf { a: Int b: Int }
		|type Query { field(input: Input): String }
	"""

	// The two nullability rules for OneOf input objects point in opposite directions, which is easy to get
	// backwards: the *fields* must be nullable — upstream rejects `a: Int!` with "OneOf input field In.a
	// must be nullable." — because all but one are absent on any given request. A *variable* supplying such
	// a field must nevertheless be non-nullable, since a nullable one could carry `null` at runtime and
	// break the "exactly one field, non-null" invariant that validation cannot otherwise see.
	//
	// So validation rejects the variable's nullability independently of whether a value is supplied.
	//
	// Verified against graphql@17.0.2, which reports this from its own
	// `VariablesInAllowedPositionRule` and reports it for a value, for an explicit null, and for an
	// omitted variable alike:
	//   query ($v: Int)  { f(in: { a: $v }) }  -> Variable "$v" is of type "Int" but must be
	//                                             non-nullable to be used for OneOf Input Object "In".
	//   query ($v: Int!) { f(in: { a: $v }) }  -> no errors
	@Test
	fun testRejectsNullableVariableForOneOfInputObjectField() {
		assertValidationRule(
			rule = VariablesInAllowedPositionRule,
			errors = listOf(
				"""
					Variable "${'$'}a" is of type "Int" but must be non-nullable to be used for OneOf Input Object "Input".

					<document>:1:17
					1 | query someQuery(${'$'}a: Int) { field(input: { a: ${'$'}a }) }
					  |                 ^

					<document>:1:46
					1 | query someQuery(${'$'}a: Int) { field(input: { a: ${'$'}a }) }
					  |                                              ^
				""",
			),
			document = "query someQuery(${'$'}a: Int) { field(input: { a: ${'$'}a }) }",
			schema = oneOfSchema,
		)
	}

	@Test
	fun testAcceptsNonNullVariableForOneOfInputObjectField() {
		assertValidationRule(
			rule = VariablesInAllowedPositionRule,
			errors = emptyList(),
			document = "query someQuery(${'$'}a: Int!) { field(input: { a: ${'$'}a }) }",
			schema = oneOfSchema,
		)
	}

	// A nullable variable in an ordinary nullable position stays legal — the rule above must not
	// generalise to every input object.
	@Test
	fun testAcceptsNullableVariableForOrdinaryInputObjectField() {
		assertValidationRule(
			rule = VariablesInAllowedPositionRule,
			errors = emptyList(),
			document = "query someQuery(${'$'}a: Int) { field(input: { a: ${'$'}a }) }",
			schema = """
				|input Input { a: Int b: Int }
				|type Query { field(input: Input): String }
			""",
		)
	}
}
