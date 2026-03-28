package testing

import io.fluidsonic.graphql.*
import kotlin.test.*


// GraphQL Spec §3.8 — Unions
class UnionTypeTests {

	@Test
	fun testUnionDefinition() {
		val result = GSchema.parse("""
			type Photo { url: String }
			type Person { name: String }
			union SearchResult = Photo | Person
			type Query { search: SearchResult }
		""".trimIndent())
		assertTrue(result.errors.isEmpty(), "Expected no parse errors but got: ${result.errors}")
	}


	@Test
	fun testUnionHasMembers() {
		val schema = GSchema.parse("""
			type Photo { url: String }
			type Person { name: String }
			union SearchResult = Photo | Person
			type Query { search: SearchResult }
		""".trimIndent()).valueOrThrow()
		val unionType = schema.resolveType("SearchResult") as? GUnionType
		assertNotNull(unionType)
		assertEquals(2, unionType.possibleTypes.size)
		assertTrue(unionType.possibleTypes.any { it.name == "Photo" })
		assertTrue(unionType.possibleTypes.any { it.name == "Person" })
	}


	@Test
	fun testUnionIsUnionType() {
		val schema = GSchema.parse("""
			type Photo { url: String }
			type Person { name: String }
			union SearchResult = Photo | Person
			type Query { search: SearchResult }
		""".trimIndent()).valueOrThrow()
		val unionType = schema.resolveType("SearchResult")
		assertNotNull(unionType)
		assertIs<GUnionType>(unionType)
	}


	@Test
	fun testUnionMembersAreObjectTypes() {
		val schema = GSchema.parse("""
			type Photo { url: String }
			type Person { name: String }
			union SearchResult = Photo | Person
			type Query { search: SearchResult }
		""".trimIndent()).valueOrThrow()
		val unionType = schema.resolveType("SearchResult") as? GUnionType
		assertNotNull(unionType)
		for (memberRef in unionType.possibleTypes) {
			val memberType = schema.resolveType(memberRef.name)
			assertNotNull(memberType, "Expected type '${memberRef.name}' to exist in schema")
			assertIs<GObjectType>(memberType)
		}
	}
}
