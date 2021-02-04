package testing

import io.fluidsonic.graphql.*
import kotlin.test.*


class ObjectFieldExistenceRuleTest {

	@Test
	fun testAcceptsFieldNamesThatExist() {
		assertValidationRule(
			rule = ObjectFieldExistenceRule,
			errors = emptyList(),
			document = """
				|{
				|   id(input: { exists: true })
				|}
			""",
			schema = """
				|type Query {
				|   id(input: Input): ID
				|}
				|
				|input Input {
				|   exists: Boolean
				|}
			"""
		)
	}


	@Test
	fun testIgnoresFieldOnNonInputTypeNestedInInputType() {
		assertValidationRule(
			rule = ObjectFieldExistenceRule,
			errors = emptyList(),
			document = """
				|{
				|   id(input: { scalar: { irrelevant: true } })
				|}
			""",
			schema = """
				|type Query {
				|   id(input: Input): ID
				|}
				|
				|input Input {
				|   scalar: Scalar
				|}
				|
				|scalar Scalar
			"""
		)
	}


	@Test
	fun testRejectsFieldNamesThatDontExist() {
		assertValidationRule(
			rule = ObjectFieldExistenceRule,
			errors = listOf(
				"""
					Unknown field 'doesNotExist' for input object type 'Input'.

					<document>:4:10
					3 |       input: {
					4 |          doesNotExist: true
					  |          ^
					5 |          nested: { doesNotExist: true }

					<document>:5:7
					4 | 
					5 | input Input {
					  |       ^
					6 |    exists: Boolean
				""",
				"""
					Unknown field 'doesNotExist' for input object type 'OtherInput'.

					<document>:5:20
					4 |          doesNotExist: true
					5 |          nested: { doesNotExist: true }
					  |                    ^
					6 |       }

					<document>:10:7
					 9 | 
					10 | input OtherInput {
					   |       ^
					11 |    exists: Boolean
				"""
			),
			document = """
				|{
				|   id(
				|      input: {
				|         doesNotExist: true
				|         nested: { doesNotExist: true }
                |      }
				|   )
				|}
			""",
			schema = """
				|type Query {
				|   id(input: Input): ID
				|}
				|
				|input Input {
				|   exists: Boolean
				|   nested: OtherInput
				|}
				|
				|input OtherInput {
				|   exists: Boolean
				|}
			"""
		)
	}
}
