package testing

import io.fluidsonic.graphql.*
import kotlin.test.*


// GraphQL Spec §2.9 — Fragments
class FragmentTests {

	@Test
	fun testNamedFragmentDefinition() {
		val doc = GDocument.parse("fragment F on Query { field }").valueWithoutErrorsOrThrow()
		assertEquals(1, doc.definitions.size)
		val frag = doc.definitions.single() as GFragmentDefinition
		assertEquals("F", frag.name)
	}


	@Test
	fun testFragmentSpread() {
		val doc = GDocument.parse("{ ...F } fragment F on Query { field }").valueWithoutErrorsOrThrow()
		val op = doc.definitions.first() as GOperationDefinition
		val spread = op.selectionSet.selections.single() as GFragmentSelection
		assertEquals("F", spread.name)
	}


	@Test
	fun testFragmentTypeCondition() {
		val doc = GDocument.parse("fragment F on Query { field }").valueWithoutErrorsOrThrow()
		val frag = doc.definitions.single() as GFragmentDefinition
		assertEquals("Query", frag.typeCondition.name)
	}


	@Test
	fun testInlineFragmentWithTypeCondition() {
		val doc = GDocument.parse("{ ... on Query { field } }").valueWithoutErrorsOrThrow()
		val op = doc.definitions.single() as GOperationDefinition
		val inline = op.selectionSet.selections.single() as GInlineFragmentSelection
		assertNotNull(inline.typeCondition)
		assertEquals("Query", inline.typeCondition.name)
	}


	@Test
	fun testInlineFragmentWithoutTypeCondition() {
		val doc = GDocument.parse("{ ... { field } }").valueWithoutErrorsOrThrow()
		val op = doc.definitions.single() as GOperationDefinition
		val inline = op.selectionSet.selections.single() as GInlineFragmentSelection
		assertNull(inline.typeCondition)
	}


	@Test
	fun testFragmentNotNamedOn() {
		val result = GDocument.parse("fragment on on on { on }")
		assertTrue(result.errors.isNotEmpty(), "Expected parse error: fragment cannot be named 'on'")
	}


	@Test
	fun testFragmentSpreadOfOn() {
		val result = GDocument.parse("{ ...on }")
		assertTrue(result.errors.isNotEmpty(), "Expected parse error for '{ ...on }'")
	}


	@Test
	fun testInlineFragmentWithDirective() {
		val doc = GDocument.parse("{ ... @skip(if: false) { field } }").valueWithoutErrorsOrThrow()
		val op = doc.definitions.single() as GOperationDefinition
		val inline = op.selectionSet.selections.single() as GInlineFragmentSelection
		assertEquals(1, inline.directives.size)
		assertEquals("skip", inline.directives.single().name)
	}
}
