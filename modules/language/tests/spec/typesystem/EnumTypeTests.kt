package testing

import io.fluidsonic.graphql.*
import kotlin.test.*


// GraphQL Spec §3.9 — Enums
class EnumTypeTests {

	@Test
	fun testEnumDefinition() {
		val result = GSchema.parse("""
			enum Direction { NORTH SOUTH EAST WEST }
			type Query { direction: Direction }
		""".trimIndent())
		assertTrue(result.errors.isEmpty(), "Expected no parse errors but got: ${result.errors}")
	}


	@Test
	fun testEnumHasValues() {
		val schema = GSchema.parse("""
			enum Direction { NORTH SOUTH EAST WEST }
			type Query { direction: Direction }
		""".trimIndent()).valueOrThrow()
		val enumType = schema.resolveType("Direction") as? GEnumType
		assertNotNull(enumType)
		assertEquals(4, enumType!!.values.size)
	}


	@Test
	fun testEnumValueNames() {
		val schema = GSchema.parse("""
			enum Direction { NORTH SOUTH EAST WEST }
			type Query { direction: Direction }
		""".trimIndent()).valueOrThrow()
		val enumType = schema.resolveType("Direction") as? GEnumType
		assertNotNull(enumType)
		val valueNames = enumType!!.values.map { it.name }
		assertTrue("NORTH" in valueNames)
		assertTrue("SOUTH" in valueNames)
		assertTrue("EAST" in valueNames)
		assertTrue("WEST" in valueNames)
	}


	@Test
	fun testEnumIsEnumType() {
		val schema = GSchema.parse("""
			enum Direction { NORTH SOUTH }
			type Query { direction: Direction }
		""".trimIndent()).valueOrThrow()
		val type = schema.resolveType("Direction")
		assertNotNull(type)
		assertIs<GEnumType>(type)
	}


	@Test
	fun testEnumValueWithDeprecated() {
		val result = GSchema.parse("""
			enum Status { ACTIVE INACTIVE @deprecated(reason: "Use ACTIVE instead") }
			type Query { status: Status }
		""".trimIndent())
		assertTrue(result.errors.isEmpty(), "Expected no parse errors but got: ${result.errors}")
		val schema = result.valueOrThrow()
		val enumType = schema.resolveType("Status") as? GEnumType
		assertNotNull(enumType)
		val inactiveValue = enumType!!.value("INACTIVE")
		assertNotNull(inactiveValue)
		assertNotNull(inactiveValue!!.deprecation)
	}
}
