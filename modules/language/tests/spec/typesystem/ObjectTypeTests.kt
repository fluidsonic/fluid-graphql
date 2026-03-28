package testing

import io.fluidsonic.graphql.*
import kotlin.test.*


// GraphQL Spec §3.6 — Objects
class ObjectTypeTests {

	@Test
	fun testObjectTypeHasFields() {
		val schema = GSchema.parse("""
			type Query { name: String age: Int }
		""".trimIndent()).valueOrThrow()
		val queryType = schema.queryType
		assertNotNull(queryType)
		assertEquals(2, queryType.fieldDefinitions.size)
		assertNotNull(queryType.fieldDefinition("name"))
		assertNotNull(queryType.fieldDefinition("age"))
	}


	@Test
	fun testObjectTypeImplementsInterface() {
		val schema = GSchema.parse("""
			interface Animal { name: String }
			type Dog implements Animal { name: String }
			type Query { dog: Dog }
		""".trimIndent()).valueOrThrow()
		val dogType = schema.resolveType("Dog") as? GObjectType
		assertNotNull(dogType)
		assertTrue(dogType.interfaces.any { it.name == "Animal" })
	}


	@Test
	fun testObjectFieldWithArgument() {
		val schema = GSchema.parse("""
			type Query { greet(name: String): String }
		""".trimIndent()).valueOrThrow()
		val queryType = schema.queryType
		assertNotNull(queryType)
		val field = queryType.fieldDefinition("greet")
		assertNotNull(field)
		assertEquals(1, field.argumentDefinitions.size)
		assertEquals("name", field.argumentDefinitions.first().name)
	}


	@Test
	fun testObjectFieldIsNullableByDefault() {
		val schema = GSchema.parse("""
			type Query { field: String }
		""".trimIndent()).valueOrThrow()
		val queryType = schema.queryType
		assertNotNull(queryType)
		val field = queryType.fieldDefinition("field")
		assertNotNull(field)
		assertIs<GNamedTypeRef>(field.type)
	}


	@Test
	fun testObjectFieldNonNull() {
		val schema = GSchema.parse("""
			type Query { field: String! }
		""".trimIndent()).valueOrThrow()
		val queryType = schema.queryType
		assertNotNull(queryType)
		val field = queryType.fieldDefinition("field")
		assertNotNull(field)
		assertIs<GNonNullTypeRef>(field.type)
	}


	@Test
	fun testObjectFieldListType() {
		val schema = GSchema.parse("""
			type Query { field: [String] }
		""".trimIndent()).valueOrThrow()
		val queryType = schema.queryType
		assertNotNull(queryType)
		val field = queryType.fieldDefinition("field")
		assertNotNull(field)
		assertIs<GListTypeRef>(field.type)
	}
}
