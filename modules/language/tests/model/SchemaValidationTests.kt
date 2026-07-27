/*
 * The three-tier contract of schema correctness, mirroring graphql-js:
 *
 *   structural problem  -> the `GSchema(document)` factory throws       (upstream: constructor throws)
 *   spec-rule violation -> `GSchema.validate()` returns errors as data  (upstream: `validateSchema()`)
 *   assert form         -> `GSchema.assertValid()` throws, carrying them  (upstream: `assertValidSchema()`)
 *
 * Every message below was produced by executing graphql@17.0.2.
 */
package testing

import io.fluidsonic.graphql.GDocument
import io.fluidsonic.graphql.GErrorException
import io.fluidsonic.graphql.GSchema
import io.fluidsonic.graphql.assertValid
import io.fluidsonic.graphql.validate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertSame
import kotlin.test.assertTrue

private fun schemaOf(sdl: String): GSchema = GSchema.parse(sdl).valueWithoutErrorsOrThrow()

class SchemaValidationTests {

	// Tier 1 — a structural problem is refused at construction and never becomes data.
	@Test
	fun duplicateTypeName_throwsFromFactory() {
		val document = GDocument.parse("type Query { x: Int } type Query { y: Int }").valueOrThrow()
		val exception = assertFailsWith<GErrorException> { GSchema(document) }

		assertEquals(
			actual = exception.errors.map { it.message },
			expected = listOf("There can be only one type named \"Query\"."),
		)
	}

	// Tier 2 — a spec-rule violation must not prevent the schema from being built.
	@Test
	fun unimplementedInterfaceField_buildsButFailsValidation() {
		val schema = schemaOf("interface I { a: Int b: Int } type Query implements I { a: Int }")

		assertEquals(
			actual = schema.validate().map { it.message },
			expected = listOf("Interface field I.b expected but Query does not provide it."),
		)
	}

	@Test
	fun oneOfInputField_mustBeNullable() {
		val schema = schemaOf("type Query { x: Int } input In @oneOf { a: Int! }")

		assertEquals(
			actual = schema.validate().map { it.message },
			expected = listOf("OneOf input field In.a must be nullable."),
		)
	}

	@Test
	fun oneOfInputField_cannotHaveDefaultValue() {
		val schema = schemaOf("type Query { x: Int } input In @oneOf { a: Int = 1 }")

		assertEquals(
			actual = schema.validate().map { it.message },
			expected = listOf("OneOf input field In.a cannot have a default value."),
		)
	}

	@Test
	fun validSchema_reportsNoErrors() {
		val schema = schemaOf("interface I { a: Int } type Query implements I { a: Int } input In @oneOf { a: Int b: String }")

		assertEquals(actual = schema.validate(), expected = emptyList())
	}

	// The per-request pipeline validates on every request, so the result must be computed exactly once.
	@Test
	fun validate_returnsTheIdenticalListOnEveryCall() {
		val schema = schemaOf("interface I { a: Int b: Int } type Query implements I { a: Int }")

		assertSame(actual = schema.validate(), expected = schema.validate())
	}

	// Tier 3 — the assert form throws, carrying the very errors `validate` reports. graphql-js flattens here
	// only because `GraphQLError[]` is not something its `Error` can carry; `GErrorException` carries a list
	// natively, and flattening would throw away the nodes that give each error its excerpt and caret.
	@Test
	fun assertValid_throwsCarryingEveryError() {
		val schema = schemaOf("type Query { x: Int } input In @oneOf { a: Int! b: Int = 1 }")
		val exception = assertFailsWith<GErrorException> { schema.assertValid() }

		assertEquals(
			actual = exception.errors.map { it.message },
			expected = listOf(
				"OneOf input field In.a must be nullable.",
				"OneOf input field In.b cannot have a default value.",
			),
		)
		assertSame(actual = exception.errors, expected = schema.validate())
	}

	@Test
	fun assertValid_acceptsValidSchema() {
		schemaOf("type Query { x: Int }").assertValid()
	}

	@Test
	fun missingQueryRootType_failsValidation() {
		val schema = schemaOf("type Foo { x: Int }")

		assertEquals(
			actual = schema.validate().map { it.message },
			expected = listOf("Query root type must be provided."),
		)
	}

	// Upstream reports "Type Obj must define one or more fields." here. fluid deliberately does not — the
	// "must define one or more members" rule family is not implemented; see the KDoc of `GSchema.validate`.
	@Test
	fun emptyTypeDefinitions_areAcceptedForNow() {
		val schema = schemaOf("type Query { x: Int } type Obj")

		assertEquals(actual = schema.validate(), expected = emptyList())
	}

	@Test
	fun undeclaredTransitiveInterface_failsValidation() {
		val schema = schemaOf("interface A { a: Int } interface B implements A { a: Int } type Query implements B { a: Int }")

		assertEquals(
			actual = schema.validate().map { it.message },
			expected = listOf("Type Query must implement A because it is implemented by B."),
		)
	}

	@Test
	fun validationErrors_carryTheOffendingNode() {
		val schema = schemaOf("type Query { x: Int } input In @oneOf { a: Int! }")

		assertTrue(schema.validate().single().nodes.isNotEmpty(), "expected the error to point at the offending node")
	}
}
