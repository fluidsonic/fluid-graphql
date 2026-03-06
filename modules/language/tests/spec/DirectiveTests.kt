package testing

import io.fluidsonic.graphql.*
import kotlin.test.*


// GraphQL Spec §2.13 — Directives
class DirectiveTests {

	@Test
	fun testDirectiveWithoutArgs() {
		val doc = GDocument.parse("{ field @skip(if: false) }").valueWithoutErrorsOrThrow()
		val op = doc.definitions.single() as GOperationDefinition
		val field = op.selectionSet.selections.single() as GFieldSelection
		assertEquals(1, field.directives.size)
		assertEquals("skip", field.directives.single().name)
	}


	@Test
	fun testDirectiveWithArgs() {
		val doc = GDocument.parse("{ field @skip(if: false) }").valueWithoutErrorsOrThrow()
		val op = doc.definitions.single() as GOperationDefinition
		val field = op.selectionSet.selections.single() as GFieldSelection
		val directive = field.directives.single()
		assertEquals(1, directive.arguments.size)
		val arg = directive.arguments.single()
		assertEquals("if", arg.name)
		val boolVal = arg.value as GBooleanValue
		assertFalse(boolVal.value)
	}


	@Test
	fun testMultipleDirectives() {
		val doc = GDocument.parse("{ field @foo @bar }").valueWithoutErrorsOrThrow()
		val op = doc.definitions.single() as GOperationDefinition
		val field = op.selectionSet.selections.single() as GFieldSelection
		assertEquals(2, field.directives.size)
		assertEquals("foo", field.directives[0].name)
		assertEquals("bar", field.directives[1].name)
	}


	@Test
	fun testDirectiveOrderPreserved() {
		val doc = GDocument.parse("{ field @first @second @third }").valueWithoutErrorsOrThrow()
		val op = doc.definitions.single() as GOperationDefinition
		val field = op.selectionSet.selections.single() as GFieldSelection
		assertEquals(3, field.directives.size)
		assertEquals("first", field.directives[0].name)
		assertEquals("second", field.directives[1].name)
		assertEquals("third", field.directives[2].name)
	}
}
