package testing

import io.fluidsonic.graphql.*
import kotlin.test.*


// GraphQL Spec §3.13 — Built-in Directives
class DirectiveDefinitionTests {

	@Test
	fun testSkipDirectiveDefined() {
		val schema = GSchema.parse("type Query { field: String }").valueOrThrow()
		val skipDirective = schema.directiveDefinition("skip")
		assertNotNull(skipDirective)
	}


	@Test
	fun testIncludeDirectiveDefined() {
		val schema = GSchema.parse("type Query { field: String }").valueOrThrow()
		val includeDirective = schema.directiveDefinition("include")
		assertNotNull(includeDirective)
	}


	@Test
	fun testDeprecatedDirectiveDefined() {
		val schema = GSchema.parse("type Query { field: String }").valueOrThrow()
		val deprecatedDirective = schema.directiveDefinition("deprecated")
		assertNotNull(deprecatedDirective)
	}


	@Test
	fun testSkipDirectiveHasIfArg() {
		val schema = GSchema.parse("type Query { field: String }").valueOrThrow()
		val skipDirective = schema.directiveDefinition("skip")
		assertNotNull(skipDirective)
		val ifArg = skipDirective.argumentDefinition("if")
		assertNotNull(ifArg)
		// @skip(if: Boolean!) — the type must be non-null Boolean
		assertIs<GNonNullTypeRef>(ifArg.type)
		val innerType = ifArg.type.nullableRef as? GNamedTypeRef
		assertNotNull(innerType)
		assertEquals("Boolean", innerType.name)
	}


	@Test
	fun testIncludeDirectiveHasIfArg() {
		val schema = GSchema.parse("type Query { field: String }").valueOrThrow()
		val includeDirective = schema.directiveDefinition("include")
		assertNotNull(includeDirective)
		val ifArg = includeDirective.argumentDefinition("if")
		assertNotNull(ifArg)
		assertEquals("if", ifArg.name)
	}


	@Test
	fun testDeprecatedDirectiveHasReasonArg() {
		val schema = GSchema.parse("type Query { field: String }").valueOrThrow()
		val deprecatedDirective = schema.directiveDefinition("deprecated")
		assertNotNull(deprecatedDirective)
		val reasonArg = deprecatedDirective.argumentDefinition("reason")
		assertNotNull(reasonArg)
		// @deprecated has a default value for reason
		assertNotNull(reasonArg.defaultValue)
	}
}
