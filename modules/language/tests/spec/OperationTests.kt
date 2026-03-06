package testing

import io.fluidsonic.graphql.*
import kotlin.test.*


// GraphQL Spec §2.4 — Operations
class OperationTests {

	@Test
	fun testQueryShorthandHasNoName() {
		val doc = GDocument.parse("{ field }").valueWithoutErrorsOrThrow()
		val op = doc.definitions.single() as GOperationDefinition
		assertNull(op.name)
	}


	@Test
	fun testQueryShorthandIsQueryType() {
		val doc = GDocument.parse("{ field }").valueWithoutErrorsOrThrow()
		val op = doc.definitions.single() as GOperationDefinition
		assertEquals(GOperationType.query, op.type)
	}


	@Test
	fun testNamedOperation() {
		val doc = GDocument.parse("query MyQuery { field }").valueWithoutErrorsOrThrow()
		val op = doc.definitions.single() as GOperationDefinition
		assertEquals("MyQuery", op.name)
	}


	@Test
	fun testMutationOperation() {
		val doc = GDocument.parse("mutation M { field }").valueWithoutErrorsOrThrow()
		val op = doc.definitions.single() as GOperationDefinition
		assertEquals(GOperationType.mutation, op.type)
	}


	@Test
	fun testSubscriptionOperation() {
		val doc = GDocument.parse("subscription S { field }").valueWithoutErrorsOrThrow()
		val op = doc.definitions.single() as GOperationDefinition
		assertEquals(GOperationType.subscription, op.type)
	}


	@Test
	fun testOperationWithVariables() {
		val doc = GDocument.parse("query Q(\$x: Int) { field }").valueWithoutErrorsOrThrow()
		val op = doc.definitions.single() as GOperationDefinition
		assertEquals(1, op.variableDefinitions.size)
		assertEquals("x", op.variableDefinitions.single().name)
	}


	@Test
	fun testOperationWithDirectives() {
		val doc = GDocument.parse("query Q @skip(if: false) { field }").valueWithoutErrorsOrThrow()
		val op = doc.definitions.single() as GOperationDefinition
		assertEquals(1, op.directives.size)
		assertEquals("skip", op.directives.single().name)
	}


	@Test
	fun testAnonymousMutation() {
		val doc = GDocument.parse("mutation { field }").valueWithoutErrorsOrThrow()
		val op = doc.definitions.single() as GOperationDefinition
		assertNull(op.name)
		assertEquals(GOperationType.mutation, op.type)
	}
}
