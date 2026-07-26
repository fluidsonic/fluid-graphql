package testing

import io.fluidsonic.graphql.GDirectiveLocation
import io.fluidsonic.graphql.GLanguage
import io.fluidsonic.graphql.GSchema
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

// https://spec.graphql.org/draft/#sec-Type-System.Directives.Built-in-Directives
class BuiltinDirectivesTests {

	// https://spec.graphql.org/draft/#sec--deprecated
	@Test
	fun testDeprecatedDirective_isSpecifiedOnAllFiveLocations() {
		val definition = assertNotNull(minimalSchema().directiveDefinition("deprecated"))

		assertEquals(
			actual = definition.locations,
			expected = setOf(
				GDirectiveLocation.FIELD_DEFINITION,
				GDirectiveLocation.ARGUMENT_DEFINITION,
				GDirectiveLocation.INPUT_FIELD_DEFINITION,
				GDirectiveLocation.ENUM_VALUE,
				GDirectiveLocation.DIRECTIVE_DEFINITION,
			),
		)
	}

	// https://spec.graphql.org/draft/#sec--oneOf
	@Test
	fun testOneOfDirective_isSpecifiedOnInputObjectOnly() {
		val definition = assertNotNull(minimalSchema().directiveDefinition("oneOf"))

		assertEquals(actual = definition.locations, expected = setOf(GDirectiveLocation.INPUT_OBJECT))
		assertEquals(actual = definition.argumentDefinitions, expected = emptyList())
		assertEquals(actual = definition.isRepeatable, expected = false)
	}

	@Test
	fun testOneOfDirective_isExposedByGLanguage() {
		assertEquals(actual = GLanguage.defaultOneOfDirective.name, expected = "oneOf")
	}

	@Test
	fun testSpecifiedDirectives_areAllDefinedByDefault() {
		val schema = minimalSchema()

		assertEquals(
			actual = listOf("deprecated", "include", "oneOf", "skip", "specifiedBy")
				.filter { name -> schema.directiveDefinition(name) !== null },
			expected = listOf("deprecated", "include", "oneOf", "skip", "specifiedBy"),
		)
	}

	@Test
	fun testDeprecatedDirective_isNotDuplicatedWhenDeclaredByDocument() {
		assertSingleDefinition(name = "deprecated", declaration = "directive @deprecated(reason: String) on FIELD_DEFINITION")
	}

	// This second directive is load-bearing: a lookup keyed on the wrong property would still
	// deduplicate a single directive by accident, but never two.
	@Test
	fun testSpecifiedByDirective_isNotDuplicatedWhenDeclaredByDocument() {
		assertSingleDefinition(name = "specifiedBy", declaration = "directive @specifiedBy(url: String!) on SCALAR")
	}

	@Test
	fun testIncludeDirective_isNotDuplicatedWhenDeclaredByDocument() {
		assertSingleDefinition(name = "include", declaration = "directive @include(if: Boolean!) on FIELD")
	}

	@Test
	fun testSkipDirective_isNotDuplicatedWhenDeclaredByDocument() {
		assertSingleDefinition(name = "skip", declaration = "directive @skip(if: Boolean!) on FIELD")
	}

	@Test
	fun testOneOfDirective_isNotDuplicatedWhenDeclaredByDocument() {
		assertSingleDefinition(name = "oneOf", declaration = "directive @oneOf on INPUT_OBJECT")
	}

	@Test
	fun testUserDeclaredDeprecatedDirective_isPreferredOverTheBuiltIn() {
		val schema = GSchema.parse(
			"""
			|directive @deprecated(reason: String) on ENUM_VALUE
			|
			|type Query { field: String }
			""".trimMargin(),
		).valueOrThrow()
		val definition = assertNotNull(schema.directiveDefinition("deprecated"))

		assertEquals(actual = definition.locations, expected = setOf(GDirectiveLocation.ENUM_VALUE))
	}

	private fun assertSingleDefinition(name: String, declaration: String) {
		val schema = GSchema.parse(
			"""
			|$declaration
			|
			|type Query { field: String }
			""".trimMargin(),
		).valueOrThrow()

		assertEquals(
			actual = schema.directiveDefinitions.count { it.name == name },
			expected = 1,
			message = "Expected exactly one '@$name' directive definition.",
		)
	}

	private fun minimalSchema(): GSchema = GSchema.parse("type Query { field: String }").valueOrThrow()
}
