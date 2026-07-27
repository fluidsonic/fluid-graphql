package testing

import io.fluidsonic.graphql.GSchema
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

// GraphQL Spec §3.3 — Schema
class SchemaDefinitionTests {

	@Test
	fun testSchemaWithQueryTypeOnly() {
		val schema = GSchema.parse("type Query { field: String }").valueOrThrow()
		assertNotNull(schema.queryType)
		assertEquals("Query", schema.queryType.name)
	}

	@Test
	fun testSchemaWithMutationType() {
		val schema = GSchema.parse(
			"""
			type Query { field: String }
			type Mutation { doSomething: String }
			""".trimIndent(),
		).valueOrThrow()
		assertNotNull(schema.mutationType)
		assertEquals("Mutation", schema.mutationType.name)
	}

	@Test
	fun testSchemaWithSubscriptionType() {
		val schema = GSchema.parse(
			"""
			type Query { field: String }
			type Subscription { onEvent: String }
			""".trimIndent(),
		).valueOrThrow()
		assertNotNull(schema.subscriptionType)
		assertEquals("Subscription", schema.subscriptionType.name)
	}

	@Test
	fun testSchemaWithoutMutationType() {
		val schema = GSchema.parse("type Query { field: String }").valueOrThrow()
		assertNull(schema.mutationType)
	}

	@Test
	fun testExplicitSchemaDefinition() {
		val schema = GSchema.parse(
			"""
			schema { query: MyQuery }
			type MyQuery { field: String }
			""".trimIndent(),
		).valueOrThrow()
		assertNotNull(schema.queryType)
		assertEquals("MyQuery", schema.queryType.name)
	}

	@Test
	fun testSchemaTypesContainUserType() {
		val schema = GSchema.parse(
			"""
			type Query { field: String }
			type Person { name: String }
			""".trimIndent(),
		).valueOrThrow()
		val personType = schema.resolveType("Person")
		assertNotNull(personType)
		assertEquals("Person", personType.name)
	}

	@Test
	fun testSchemaTypesContainBuiltinScalars() {
		val schema = GSchema.parse("type Query { b: Boolean, f: Float, i: ID, n: Int, s: String }").valueOrThrow()
		assertNotNull(schema.resolveType("Boolean"))
		assertNotNull(schema.resolveType("Int"))
		assertNotNull(schema.resolveType("String"))
		assertNotNull(schema.resolveType("Float"))
		assertNotNull(schema.resolveType("ID"))
	}

	// A built-in scalar is added only when something refers to it, matching graphql-js. `String` is referred
	// to here by the field, `Boolean` by the built-in `@skip`/`@include` directives.
	@Test
	fun testSchemaTypesOmitUnreferencedBuiltinScalars() {
		val schema = GSchema.parse("type Query { field: String }").valueOrThrow()
		assertNotNull(schema.resolveType("Boolean"))
		assertNotNull(schema.resolveType("String"))
		assertNull(schema.resolveType("Float"))
		assertNull(schema.resolveType("ID"))
		assertNull(schema.resolveType("Int"))
	}
}
