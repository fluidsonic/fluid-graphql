package testing

import io.fluidsonic.graphql.*
import kotlin.test.*


// GraphQL Spec §3.5 — Scalars
class ScalarTypeTests {

	@Test
	fun testBuiltinIntScalar() {
		val schema = GSchema.parse("type Query { field: Int }").valueOrThrow()
		val type = schema.resolveType("Int")
		assertNotNull(type)
		assertIs<GScalarType>(type)
		assertEquals("Int", type!!.name)
	}


	@Test
	fun testBuiltinFloatScalar() {
		val schema = GSchema.parse("type Query { field: Float }").valueOrThrow()
		val type = schema.resolveType("Float")
		assertNotNull(type)
		assertIs<GScalarType>(type)
		assertEquals("Float", type!!.name)
	}


	@Test
	fun testBuiltinStringScalar() {
		val schema = GSchema.parse("type Query { field: String }").valueOrThrow()
		val type = schema.resolveType("String")
		assertNotNull(type)
		assertIs<GScalarType>(type)
		assertEquals("String", type!!.name)
	}


	@Test
	fun testBuiltinBooleanScalar() {
		val schema = GSchema.parse("type Query { field: Boolean }").valueOrThrow()
		val type = schema.resolveType("Boolean")
		assertNotNull(type)
		assertIs<GScalarType>(type)
		assertEquals("Boolean", type!!.name)
	}


	@Test
	fun testBuiltinIdScalar() {
		val schema = GSchema.parse("type Query { field: ID }").valueOrThrow()
		val type = schema.resolveType("ID")
		assertNotNull(type)
		assertIs<GScalarType>(type)
		assertEquals("ID", type!!.name)
	}


	@Test
	fun testCustomScalarDefinition() {
		val schema = GSchema.parse("""
			scalar UUID
			type Query { id: UUID }
		""".trimIndent()).valueOrThrow()
		val type = schema.resolveType("UUID")
		assertNotNull(type)
		assertEquals("UUID", type!!.name)
	}


	@Test
	fun testCustomScalarIsScalarType() {
		val schema = GSchema.parse("""
			scalar UUID
			type Query { id: UUID }
		""".trimIndent()).valueOrThrow()
		val type = schema.resolveType("UUID")
		assertNotNull(type)
		assertIs<GScalarType>(type)
	}


	@Test
	fun testScalarWithSpecifiedByDirective() {
		val result = GSchema.parse("""
			scalar UUID @specifiedBy(url: "https://tools.ietf.org/html/rfc4122")
			type Query { id: UUID }
		""".trimIndent())
		assertTrue(result.errors.isEmpty(), "Expected no parse errors but got: ${result.errors}")
		val schema = result.valueOrThrow()
		assertNotNull(schema.resolveType("UUID"))
	}
}
