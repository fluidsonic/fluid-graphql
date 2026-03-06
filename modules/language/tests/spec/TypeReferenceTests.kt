package testing

import io.fluidsonic.graphql.*
import kotlin.test.*


// GraphQL Spec §2.12 — Type References
class TypeReferenceTests {

	@Test
	fun testNamedTypeRef() {
		val doc = GDocument.parse("query Q(\$x: String) { f }").valueWithoutErrorsOrThrow()
		val op = doc.definitions.single() as GOperationDefinition
		val varDef = op.variableDefinitions.single()
		val namedType = varDef.type as GNamedTypeRef
		assertEquals("String", namedType.name)
	}


	@Test
	fun testListTypeRef() {
		val doc = GDocument.parse("query Q(\$x: [String]) { f }").valueWithoutErrorsOrThrow()
		val op = doc.definitions.single() as GOperationDefinition
		val varDef = op.variableDefinitions.single()
		val listType = varDef.type as GListTypeRef
		val innerType = listType.elementType as GNamedTypeRef
		assertEquals("String", innerType.name)
	}


	@Test
	fun testNonNullTypeRef() {
		val doc = GDocument.parse("query Q(\$x: String!) { f }").valueWithoutErrorsOrThrow()
		val op = doc.definitions.single() as GOperationDefinition
		val varDef = op.variableDefinitions.single()
		val nonNullType = varDef.type as GNonNullTypeRef
		val innerType = nonNullType.nullableRef as GNamedTypeRef
		assertEquals("String", innerType.name)
	}


	@Test
	fun testNonNullListTypeRef() {
		val doc = GDocument.parse("query Q(\$x: [String!]!) { f }").valueWithoutErrorsOrThrow()
		val op = doc.definitions.single() as GOperationDefinition
		val varDef = op.variableDefinitions.single()
		// Outermost: non-null
		val outerNonNull = varDef.type as GNonNullTypeRef
		// Inside: list
		val listType = outerNonNull.nullableRef as GListTypeRef
		// Element: non-null String
		val elementNonNull = listType.elementType as GNonNullTypeRef
		val namedType = elementNonNull.nullableRef as GNamedTypeRef
		assertEquals("String", namedType.name)
	}


	@Test
	fun testDoubleNonNullFails() {
		val result = GDocument.parse("query Q(\$x: String!!) { f }")
		assertTrue(result.errors.isNotEmpty(), "Expected parse error for double non-null '!!'")
	}
}
