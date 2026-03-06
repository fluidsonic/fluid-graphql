package testing

import io.fluidsonic.graphql.*
import kotlin.test.*


// GraphQL Spec §2.3 — Document
class DocumentTests {

	@Test
	fun testSingleQueryOperation() {
		val doc = GDocument.parse("query Q { field }").valueWithoutErrorsOrThrow()
		assertEquals(1, doc.definitions.size)
		val op = doc.definitions.single() as GOperationDefinition
		assertEquals(GOperationType.query, op.type)
		assertEquals("Q", op.name)
	}


	@Test
	fun testSingleAnonymousQuery() {
		val doc = GDocument.parse("{ field }").valueWithoutErrorsOrThrow()
		assertEquals(1, doc.definitions.size)
		val op = doc.definitions.single() as GOperationDefinition
		assertEquals(GOperationType.query, op.type)
		assertNull(op.name)
	}


	@Test
	fun testMultipleOperations() {
		val doc = GDocument.parse("query Q1 { field } query Q2 { other }").valueWithoutErrorsOrThrow()
		assertEquals(2, doc.definitions.size)
		assertTrue(doc.definitions[0] is GOperationDefinition)
		assertTrue(doc.definitions[1] is GOperationDefinition)
	}


	@Test
	fun testMixedOperationsAndFragments() {
		val doc = GDocument.parse("query Q { ...F } fragment F on Query { field }").valueWithoutErrorsOrThrow()
		assertEquals(2, doc.definitions.size)
		assertTrue(doc.definitions[0] is GOperationDefinition)
		assertTrue(doc.definitions[1] is GFragmentDefinition)
	}


	@Test
	fun testEmptyDocumentFails() {
		val result = GDocument.parse("")
		assertTrue(result.errors.isNotEmpty(), "Expected parse error for empty document")
	}


	@Test
	fun testDocumentWithOnlyFragment() {
		val doc = GDocument.parse("fragment F on T { id }").valueWithoutErrorsOrThrow()
		assertEquals(1, doc.definitions.size)
		assertTrue(doc.definitions.single() is GFragmentDefinition)
	}


	@Test
	fun testOperationTypeQuery() {
		val doc = GDocument.parse("query { field }").valueWithoutErrorsOrThrow()
		val op = doc.definitions.single() as GOperationDefinition
		assertEquals(GOperationType.query, op.type)
	}


	@Test
	fun testOperationTypeMutation() {
		val doc = GDocument.parse("mutation { field }").valueWithoutErrorsOrThrow()
		val op = doc.definitions.single() as GOperationDefinition
		assertEquals(GOperationType.mutation, op.type)
	}


	@Test
	fun testOperationTypeSubscription() {
		val doc = GDocument.parse("subscription { field }").valueWithoutErrorsOrThrow()
		val op = doc.definitions.single() as GOperationDefinition
		assertEquals(GOperationType.subscription, op.type)
	}
}
