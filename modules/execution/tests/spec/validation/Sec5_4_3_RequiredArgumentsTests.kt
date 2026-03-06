package testing

import io.fluidsonic.graphql.*
import kotlin.test.*


// GraphQL Spec §5.4.3 — Required Arguments
class Sec5_4_3_RequiredArgumentsTests {

	@Test
	fun testAcceptsAllRequiredArgsProvided() {
		assertValidationRule(
			rule = ArgumentRequirementRule,
			errors = emptyList(),
			document = """
				|{ field(arg: "value") }
			""",
			schema = """
				|type Query { field(arg: String!): String }
			"""
		)
	}


	@Test
	fun testAcceptsOptionalArgOmitted() {
		assertValidationRule(
			rule = ArgumentRequirementRule,
			errors = emptyList(),
			document = """
				|{ field }
			""",
			schema = """
				|type Query { field(arg: String): String }
			"""
		)
	}


	@Test
	fun testRejectsMissingRequiredArg() {
		assertValidationRule(
			rule = ArgumentRequirementRule,
			errors = listOf("""
				Selection of field 'field' is missing required argument 'arg'.

				<document>:1:3
				1 | { field }
				  |   ^

				<document>:1:20
				1 | type Query { field(arg: String!): String }
				  |                    ^
			"""),
			document = """
				|{ field }
			""",
			schema = """
				|type Query { field(arg: String!): String }
			"""
		)
	}


	@Test
	fun testAcceptsNullableArgOmitted() {
		assertValidationRule(
			rule = ArgumentRequirementRule,
			errors = emptyList(),
			document = """
				|{ field }
			""",
			schema = """
				|type Query { field(arg: String): String }
			"""
		)
	}


	@Test
	fun testAcceptsArgWithDefaultValueOmitted() {
		assertValidationRule(
			rule = ArgumentRequirementRule,
			errors = emptyList(),
			document = """
				|{ field }
			""",
			schema = """
				|type Query { field(arg: String! = "default"): String }
			"""
		)
	}
}
