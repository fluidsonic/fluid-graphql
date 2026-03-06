package testing

import io.fluidsonic.graphql.*
import kotlin.test.*


// GraphQL Spec §5.2.4 — Subscription Single Root Field
class Sec5_2_4_SubscriptionTests {

	@Test
	fun testValidSubscriptionWithSingleField() {
		assertValidationRule(
			rule = SubscriptionRootFieldExclusivityRule,
			errors = emptyList(),
			document = """
				|subscription sub { newMessage }
			""",
			schema = """
				|type Query { id: ID }
				|type Subscription { newMessage: String }
			"""
		)
	}


	@Test
	fun testValidSubscriptionWithFragment() {
		assertValidationRule(
			rule = SubscriptionRootFieldExclusivityRule,
			errors = emptyList(),
			document = """
				|subscription sub { ...subFields }
				|fragment subFields on Subscription { newMessage }
			""",
			schema = """
				|type Query { id: ID }
				|type Subscription { newMessage: String }
			"""
		)
	}


	@Test
	fun testInvalidSubscriptionWithMultipleFields() {
		assertValidationRule(
			rule = SubscriptionRootFieldExclusivityRule,
			errors = listOf("""
				Subscription operations must have exactly one root field.

				<document>:1:20
				1 | subscription sub { newMessage newComment }
				  |                    ^

				<document>:1:31
				1 | subscription sub { newMessage newComment }
				  |                               ^
			"""),
			document = """
				|subscription sub { newMessage newComment }
			""",
			schema = """
				|type Query { id: ID }
				|type Subscription { newMessage: String newComment: String }
			"""
		)
	}


	@Test
	fun testInvalidSubscriptionWithIntrospection() {
		assertValidationRule(
			rule = SubscriptionRootFieldExclusivityRule,
			errors = listOf("""
				Subscription operations must have exactly one root field.

				<document>:1:20
				1 | subscription sub { __typename }
				  |                    ^
			"""),
			document = """
				|subscription sub { __typename }
			""",
			schema = """
				|type Query { id: ID }
				|type Subscription { newMessage: String }
			"""
		)
	}
}
