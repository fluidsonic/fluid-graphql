package testing

import io.fluidsonic.graphql.GErrorException
import io.fluidsonic.graphql.GLanguage
import io.fluidsonic.graphql.GSchema
import io.fluidsonic.graphql.GType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * `GLanguage.isReservedTypeName` must answer exactly what the [GSchema] factory refuses, so a caller
 * building a schema from names it does not control can check first instead of catching.
 */
class ReservedTypeNameTests {

	@Test
	fun isReservedTypeName_rejectsBuiltinScalarNames() {
		for (name in listOf("Boolean", "Float", "ID", "Int", "String")) {
			assertTrue(GLanguage.isReservedTypeName(name), "'$name' must be reserved")
		}
	}

	@Test
	fun isReservedTypeName_rejectsIntrospectionPrefixedNames() {
		assertTrue(GLanguage.isReservedTypeName("__Schema"))
		assertTrue(GLanguage.isReservedTypeName("__Type"))
		assertTrue(GLanguage.isReservedTypeName("__Foo"))
	}

	@Test
	fun isReservedTypeName_acceptsOrdinaryNames() {
		for (name in listOf("Query", "Url", "Id", "id", "INT", "MyInt", "Int2", "_Int")) {
			assertFalse(GLanguage.isReservedTypeName(name), "'$name' must not be reserved")
		}
	}

	// The predicate covers exactly the built-in scalar names the schema actually registers, so the two
	// cannot drift apart as the set changes.
	@Test
	fun isReservedTypeName_coversEveryBuiltinScalar() {
		for (type in GType.defaultTypes()) {
			assertTrue(GLanguage.isReservedTypeName(type.name), "'${type.name}' is a built-in but is not reserved")
		}
	}

	// The point of the predicate: what it rejects is what the factory throws for, and what it accepts builds.
	@Test
	fun isReservedTypeName_agreesWithTheSchemaFactory() {
		for (name in listOf("ID", "Int", "String", "Boolean", "Float", "__Foo")) {
			assertTrue(GLanguage.isReservedTypeName(name))
			assertFailsWith<GErrorException>("'$name' must be refused by the factory") {
				GSchema.parse("scalar $name type Query { a: String }").valueOrThrow()
			}
		}

		assertFalse(GLanguage.isReservedTypeName("Url"))

		val schema = GSchema.parse("scalar Url type Query { a: Url }").valueOrThrow()

		assertEquals(actual = schema.resolveType("Url")?.name, expected = "Url")
	}
}
