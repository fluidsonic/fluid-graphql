package testing

import io.fluidsonic.graphql.*
import kotlin.test.*


// GraphQL Spec §2.11 — Variables
class VariableTests {

	@Test
	fun testSimpleVariable() {
		val doc = GDocument.parse("query Q(\$x: Int) { f(a: \$x) }").valueWithoutErrorsOrThrow()
		val op = doc.definitions.single() as GOperationDefinition
		assertEquals(1, op.variableDefinitions.size)
		val varDef = op.variableDefinitions.single()
		assertEquals("x", varDef.name)
		val namedType = varDef.type as GNamedTypeRef
		assertEquals("Int", namedType.name)
	}


	@Test
	fun testVariableWithDefault() {
		val doc = GDocument.parse("query Q(\$x: Int = 5) { f(a: \$x) }").valueWithoutErrorsOrThrow()
		val op = doc.definitions.single() as GOperationDefinition
		val varDef = op.variableDefinitions.single()
		assertNotNull(varDef.defaultValue)
		val defaultVal = varDef.defaultValue as GIntValue
		assertEquals(5, defaultVal.value)
	}


	@Test
	fun testVariableNoDefault() {
		val doc = GDocument.parse("query Q(\$x: String) { f }").valueWithoutErrorsOrThrow()
		val op = doc.definitions.single() as GOperationDefinition
		val varDef = op.variableDefinitions.single()
		assertNull(varDef.defaultValue)
	}


	@Test
	fun testVariableInArgument() {
		val doc = GDocument.parse("query Q(\$x: Int) { f(a: \$x) }").valueWithoutErrorsOrThrow()
		val op = doc.definitions.single() as GOperationDefinition
		val field = op.selectionSet.selections.single() as GFieldSelection
		val arg = field.arguments.single()
		val varRef = arg.value as GVariableRef
		assertEquals("x", varRef.name)
	}


	@Test
	fun testNonNullVariableType() {
		val doc = GDocument.parse("query Q(\$x: Int!) { f }").valueWithoutErrorsOrThrow()
		val op = doc.definitions.single() as GOperationDefinition
		val varDef = op.variableDefinitions.single()
		val nonNullType = varDef.type as GNonNullTypeRef
		val namedType = nonNullType.nullableRef as GNamedTypeRef
		assertEquals("Int", namedType.name)
	}
}
