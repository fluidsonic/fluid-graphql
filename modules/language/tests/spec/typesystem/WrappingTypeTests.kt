package testing
import io.fluidsonic.graphql.GListTypeRef
import io.fluidsonic.graphql.GNamedTypeRef
import io.fluidsonic.graphql.GNonNullTypeRef
import io.fluidsonic.graphql.GSchema
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull

// GraphQL Spec §3.11-3.12 — Wrapping Types
class WrappingTypeTests {

	@Test
	fun testListType() {
		val schema = GSchema.parse(
			"""
			type Query { field: [String] }
			""".trimIndent(),
		).valueOrThrow()
		val queryType = schema.queryType
		assertNotNull(queryType)
		val field = queryType.fieldDefinition("field")
		assertNotNull(field)
		assertIs<GListTypeRef>(field.type)
	}

	@Test
	fun testNonNullType() {
		val schema = GSchema.parse(
			"""
			type Query { field: String! }
			""".trimIndent(),
		).valueOrThrow()
		val queryType = schema.queryType
		assertNotNull(queryType)
		val field = queryType.fieldDefinition("field")
		assertNotNull(field)
		assertIs<GNonNullTypeRef>(field.type)
	}

	@Test
	fun testNonNullList() {
		val schema = GSchema.parse(
			"""
			type Query { field: [String]! }
			""".trimIndent(),
		).valueOrThrow()
		val queryType = schema.queryType
		assertNotNull(queryType)
		val field = queryType.fieldDefinition("field")
		assertNotNull(field)
		val outerNonNull = field.type as? GNonNullTypeRef
		assertNotNull(outerNonNull)
		assertIs<GListTypeRef>(outerNonNull.nullableRef)
	}

	@Test
	fun testNonNullListOfNonNull() {
		val schema = GSchema.parse(
			"""
			type Query { field: [String!]! }
			""".trimIndent(),
		).valueOrThrow()
		val queryType = schema.queryType
		assertNotNull(queryType)
		val field = queryType.fieldDefinition("field")
		assertNotNull(field)
		val outerNonNull = field.type as? GNonNullTypeRef
		assertNotNull(outerNonNull)
		val listType = outerNonNull.nullableRef as? GListTypeRef
		assertNotNull(listType)
		assertIs<GNonNullTypeRef>(listType.elementType)
	}

	@Test
	fun testNullableField() {
		val schema = GSchema.parse(
			"""
			type Query { field: String }
			""".trimIndent(),
		).valueOrThrow()
		val queryType = schema.queryType
		assertNotNull(queryType)
		val field = queryType.fieldDefinition("field")
		assertNotNull(field)
		assertIs<GNamedTypeRef>(field.type)
		assertEquals("String", field.type.name)
	}
}
