package testing

import io.fluidsonic.graphql.GDocument
import io.fluidsonic.graphql.GSchema
import io.fluidsonic.graphql.validate
import kotlin.test.Test
import kotlin.test.assertEquals

// GraphQL Spec §5.6.4 — Input Object Required Fields
//
// These run the whole `Validator.default` rather than one rule, because the defect they guard against is two rules
// reporting the same missing field. As in graphql-js 17.0.2, a missing required field is reported exactly once per
// field, whatever position the input object literal is in.
class Sec5_6_4_InputObjectRequiredFieldsTests {

	@Test
	fun testReportsMissingRequiredFieldOncePerPosition() {
		val documents = listOf(
			"{ nullable(input: {}) }",
			"{ nonNull(input: {}) }",
			"{ list(input: [{}]) }",
			"{ list(input: {}) }",
			"{ nonNullItemList(input: [{}]) }",
			"{ nonNullList(input: [{}]) }",
			"{ nested(input: [{ items: [{}] }]) }",
			"{ nullable(input: { required: true }) @d(input: [{}]) }",
			"query Q(${'$'}v: [Input!]! = [{}]) { nonNullList(input: ${'$'}v) }",
		)

		for (document in documents) {
			assertEquals(
				actual = messagesFor(document),
				expected = listOf("Required field 'required' of type 'Input' is missing."),
				message = "Document: $document",
			)
		}
	}

	@Test
	fun testReportsMissingRequiredFieldOnceInDefaultValues() {
		assertEquals(
			actual = messagesFor(
				"""
					|{ nullable(input: { required: true }) }
					|type Defaults { field(input: [Input!]! = [{}]): String }
					|input DefaultsInput { input: [Input!]! = [{}] }
				""".trimMargin(),
			),
			expected = listOf(
				"In order to be executable, the document must contain only executable definitions.",
				"Required field 'required' of type 'Input' is missing.",
				"Required field 'required' of type 'Input' is missing.",
			),
		)
	}

	@Test
	fun testReportsMissingRequiredFieldOfListItemAtTheItem() {
		assertErrors(
			actual = GDocument.parse("{ nonNullList(input: [{}]) }").valueOrThrow().validate(schema),
			expected = listOf(
				"""
					Required field 'required' of type 'Input' is missing.

					<document>:1:23
					1 | { nonNullList(input: [{}]) }
					  |                       ^

					<document>:13:4
					12 |    optional: Boolean! = true
					13 |    required: Boolean!
					   |    ^
					14 | }
				""",
			),
		)
	}

	// As in graphql-js, a list given where no list is expected is rejected as a whole, without checking its items.
	@Test
	fun testRejectsListInNonListPositionWithoutCheckingItsItems() {
		assertEquals(
			actual = messagesFor("{ nonNull(input: [{}]) }"),
			expected = listOf("Type 'Input' does not allow a list value."),
		)
	}

	@Test
	fun testReportsEachMissingRequiredFieldOnce() {
		assertEquals(
			actual = messagesFor("{ twoRequired(input: [{}]) }"),
			expected = listOf(
				"Required field 'required1' of type 'TwoRequired' is missing.",
				"Required field 'required2' of type 'TwoRequired' is missing.",
			),
		)
	}

	@Test
	fun testAcceptsAbsenceOfOptionalFields() {
		assertEquals(
			actual = messagesFor("{ nonNullList(input: [{ required: true }]) }"),
			expected = emptyList(),
		)
	}

	private fun messagesFor(document: String) = GDocument.parse(document).valueOrThrow()
		.validate(schema)
		.map { it.message }

	private val schema = GSchema.parse(
		"""
			|type Query {
			|   nullable(input: Input): String
			|   nonNull(input: Input!): String
			|   list(input: [Input]): String
			|   nonNullItemList(input: [Input!]): String
			|   nonNullList(input: [Input!]!): String
			|   nested(input: [Outer!]!): String
			|   twoRequired(input: [TwoRequired!]!): String
			|}
			|
			|input Input {
			|   optional: Boolean! = true
			|   required: Boolean!
			|}
			|
			|input Outer {
			|   items: [Input!]!
			|}
			|
			|input TwoRequired {
			|   required1: Boolean!
			|   required2: Boolean!
			|}
			|
			|directive @d(input: [Input!]) on FIELD
		""".trimMargin(),
	).valueOrThrow()
}
