package testing

import io.fluidsonic.graphql.*
import kotlin.test.*


// GraphQL Spec §3.7 — Interfaces
class InterfaceTypeTests {

	@Test
	fun testInterfaceDefinition() {
		val result = GSchema.parse("""
			interface Animal { name: String }
			type Query { animal: Animal }
		""".trimIndent())
		assertTrue(result.errors.isEmpty(), "Expected no parse errors but got: ${result.errors}")
	}


	@Test
	fun testInterfaceHasFields() {
		val schema = GSchema.parse("""
			interface Animal { name: String sound: String }
			type Query { animal: Animal }
		""".trimIndent()).valueOrThrow()
		val animalType = schema.resolveType("Animal") as? GInterfaceType
		assertNotNull(animalType)
		assertEquals(2, animalType!!.fieldDefinitions.size)
		assertNotNull(animalType.fieldDefinition("name"))
		assertNotNull(animalType.fieldDefinition("sound"))
	}


	@Test
	fun testInterfaceIsInterfaceType() {
		val schema = GSchema.parse("""
			interface Animal { name: String }
			type Query { animal: Animal }
		""".trimIndent()).valueOrThrow()
		val animalType = schema.resolveType("Animal")
		assertNotNull(animalType)
		assertIs<GInterfaceType>(animalType)
	}


	@Test
	fun testObjectImplementsInterface() {
		val schema = GSchema.parse("""
			interface Animal { name: String }
			type Dog implements Animal { name: String }
			type Query { dog: Dog }
		""".trimIndent()).valueOrThrow()
		val dogType = schema.resolveType("Dog") as? GObjectType
		assertNotNull(dogType)
		assertTrue(dogType!!.interfaces.any { it.name == "Animal" })
	}


	@Test
	fun testInterfaceImplementsInterface() {
		val result = GSchema.parse("""
			interface Named { name: String }
			interface Animal implements Named { name: String sound: String }
			type Query { animal: Animal }
		""".trimIndent())
		assertTrue(result.errors.isEmpty(), "Expected no parse errors but got: ${result.errors}")
		val schema = result.valueOrThrow()
		val animalType = schema.resolveType("Animal") as? GInterfaceType
		assertNotNull(animalType)
		assertTrue(animalType!!.interfaces.any { it.name == "Named" })
	}
}
