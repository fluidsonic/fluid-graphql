package testing

import io.fluidsonic.graphql.*
import kotlin.test.*


// GraphQL Spec §3.11-3.12 — Wrapping Types
class WrappingTypeTests {

	@Test
	fun testListType() {
		val schema = GSchema.parse("""
			type Query { field: [String] }
		""".trimIndent()).valueOrThrow()
		val field = schema.queryType!!.fieldDefinition("field")
		assertNotNull(field)
		assertIs<GListTypeRef>(field!!.type)
	}


	@Test
	fun testNonNullType() {
		val schema = GSchema.parse("""
			type Query { field: String! }
		""".trimIndent()).valueOrThrow()
		val field = schema.queryType!!.fieldDefinition("field")
		assertNotNull(field)
		assertIs<GNonNullTypeRef>(field!!.type)
	}


	@Test
	fun testNonNullList() {
		val schema = GSchema.parse("""
			type Query { field: [String]! }
		""".trimIndent()).valueOrThrow()
		val field = schema.queryType!!.fieldDefinition("field")
		assertNotNull(field)
		val outerNonNull = field!!.type as? GNonNullTypeRef
		assertNotNull(outerNonNull)
		assertIs<GListTypeRef>(outerNonNull!!.nullableRef)
	}


	@Test
	fun testNonNullListOfNonNull() {
		val schema = GSchema.parse("""
			type Query { field: [String!]! }
		""".trimIndent()).valueOrThrow()
		val field = schema.queryType!!.fieldDefinition("field")
		assertNotNull(field)
		val outerNonNull = field!!.type as? GNonNullTypeRef
		assertNotNull(outerNonNull)
		val listType = outerNonNull!!.nullableRef as? GListTypeRef
		assertNotNull(listType)
		assertIs<GNonNullTypeRef>(listType!!.elementType)
	}


	@Test
	fun testNullableField() {
		val schema = GSchema.parse("""
			type Query { field: String }
		""".trimIndent()).valueOrThrow()
		val field = schema.queryType!!.fieldDefinition("field")
		assertNotNull(field)
		assertIs<GNamedTypeRef>(field!!.type)
		assertEquals("String", (field.type as GNamedTypeRef).name)
	}
}
