package testing

import io.fluidsonic.graphql.*
import kotlin.test.*


// GraphQL Spec §3.10 — Input Objects
class InputObjectTypeTests {

	@Test
	fun testInputObjectDefinition() {
		val result = GSchema.parse("""
			input CreatePersonInput { name: String! age: Int }
			type Query { field: String }
		""".trimIndent())
		assertTrue(result.errors.isEmpty(), "Expected no parse errors but got: ${result.errors}")
	}


	@Test
	fun testInputObjectHasFields() {
		val schema = GSchema.parse("""
			input CreatePersonInput { name: String! age: Int }
			type Query { field: String }
		""".trimIndent()).valueOrThrow()
		val inputType = schema.resolveType("CreatePersonInput") as? GInputObjectType
		assertNotNull(inputType)
		assertEquals(2, inputType!!.argumentDefinitions.size)
	}


	@Test
	fun testInputObjectFieldTypes() {
		val schema = GSchema.parse("""
			input CreatePersonInput { name: String! age: Int }
			type Query { field: String }
		""".trimIndent()).valueOrThrow()
		val inputType = schema.resolveType("CreatePersonInput") as? GInputObjectType
		assertNotNull(inputType)
		val nameField = inputType!!.argumentDefinition("name")
		assertNotNull(nameField)
		assertIs<GNonNullTypeRef>(nameField!!.type)
		val ageField = inputType.argumentDefinition("age")
		assertNotNull(ageField)
		assertIs<GNamedTypeRef>(ageField!!.type)
	}


	@Test
	fun testInputObjectRequiredField() {
		val schema = GSchema.parse("""
			input CreatePersonInput { name: String! age: Int }
			type Query { field: String }
		""".trimIndent()).valueOrThrow()
		val inputType = schema.resolveType("CreatePersonInput") as? GInputObjectType
		assertNotNull(inputType)
		val nameField = inputType!!.argumentDefinition("name")
		assertNotNull(nameField)
		assertTrue(nameField!!.isRequired())
	}


	@Test
	fun testInputObjectOptionalField() {
		val schema = GSchema.parse("""
			input CreatePersonInput { name: String! age: Int }
			type Query { field: String }
		""".trimIndent()).valueOrThrow()
		val inputType = schema.resolveType("CreatePersonInput") as? GInputObjectType
		assertNotNull(inputType)
		val ageField = inputType!!.argumentDefinition("age")
		assertNotNull(ageField)
		assertTrue(ageField!!.isOptional())
	}


	@Test
	fun testInputObjectIsInputObjectType() {
		val schema = GSchema.parse("""
			input CreatePersonInput { name: String! }
			type Query { field: String }
		""".trimIndent()).valueOrThrow()
		val type = schema.resolveType("CreatePersonInput")
		assertNotNull(type)
		assertIs<GInputObjectType>(type)
	}
}
