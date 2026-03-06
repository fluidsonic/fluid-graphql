package testing

import io.fluidsonic.graphql.*
import kotlin.test.*


// GraphQL Spec §5.8.2 — Variables Are Input Types
class Sec5_8_2_VariablesAreInputTypesTests {

	@Test
	fun testAcceptsIntVariableType() {
		assertValidationRule(
			rule = VariableDefinitionTypeValidityRule,
			errors = emptyList(),
			document = """
				|query Q(${'$'}x: Int) { id }
			""",
			schema = """
				|type Query { id: ID }
			"""
		)
	}


	@Test
	fun testAcceptsStringVariableType() {
		assertValidationRule(
			rule = VariableDefinitionTypeValidityRule,
			errors = emptyList(),
			document = """
				|query Q(${'$'}x: String) { id }
			""",
			schema = """
				|type Query { id: ID }
			"""
		)
	}


	@Test
	fun testAcceptsListVariableType() {
		assertValidationRule(
			rule = VariableDefinitionTypeValidityRule,
			errors = emptyList(),
			document = """
				|query Q(${'$'}x: [Int]) { id }
			""",
			schema = """
				|type Query { id: ID }
			"""
		)
	}


	@Test
	fun testAcceptsInputObjectVariableType() {
		assertValidationRule(
			rule = VariableDefinitionTypeValidityRule,
			errors = emptyList(),
			document = """
				|query Q(${'$'}x: MyInput) { id }
			""",
			schema = """
				|type Query { id: ID }
				|input MyInput { name: String }
			"""
		)
	}


	@Test
	fun testRejectsObjectVariableType() {
		assertValidationRule(
			rule = VariableDefinitionTypeValidityRule,
			errors = listOf("""
				Variable '${'$'}x' cannot have output type 'MyObject'.

				<document>:1:13
				1 | query Q(${'$'}x: MyObject) { id }
				  |             ^

				<document>:2:6
				1 | type Query { id: ID }
				2 | type MyObject { name: String }
				  |      ^
			"""),
			document = """
				|query Q(${'$'}x: MyObject) { id }
			""",
			schema = """
				|type Query { id: ID }
				|type MyObject { name: String }
			"""
		)
	}


	@Test
	fun testRejectsInterfaceVariableType() {
		assertValidationRule(
			rule = VariableDefinitionTypeValidityRule,
			errors = listOf("""
				Variable '${'$'}x' cannot have output type 'MyInterface'.

				<document>:1:13
				1 | query Q(${'$'}x: MyInterface) { id }
				  |             ^

				<document>:2:11
				1 | type Query { id: ID }
				2 | interface MyInterface { name: String }
				  |           ^
			"""),
			document = """
				|query Q(${'$'}x: MyInterface) { id }
			""",
			schema = """
				|type Query { id: ID }
				|interface MyInterface { name: String }
			"""
		)
	}


	@Test
	fun testRejectsUnionVariableType() {
		assertValidationRule(
			rule = VariableDefinitionTypeValidityRule,
			errors = listOf("""
				Variable '${'$'}x' cannot have output type 'MyUnion'.

				<document>:1:13
				1 | query Q(${'$'}x: MyUnion) { id }
				  |             ^

				<document>:3:7
				2 | type MyTypeA { name: String }
				3 | union MyUnion = MyTypeA
				  |       ^
			"""),
			document = """
				|query Q(${'$'}x: MyUnion) { id }
			""",
			schema = """
				|type Query { id: ID }
				|type MyTypeA { name: String }
				|union MyUnion = MyTypeA
			"""
		)
	}
}
