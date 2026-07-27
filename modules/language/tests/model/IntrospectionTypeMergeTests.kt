/*
 * The introspection types are ordinary members of every schema's type map, not a separate schema.
 * Verified against graphql@17.0.2, whose `schema.getTypeMap()` and `{ __schema { types } }` report the same
 * eleven names for a minimal schema: `Boolean`, `Query`, `String` and the eight `__`-prefixed types.
 */
package testing

import io.fluidsonic.graphql.GDocument
import io.fluidsonic.graphql.GEnumType
import io.fluidsonic.graphql.GErrorException
import io.fluidsonic.graphql.GObjectType
import io.fluidsonic.graphql.GSchema
import io.fluidsonic.graphql.typeType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs
import kotlin.test.assertNotSame
import kotlin.test.assertNull
import kotlin.test.assertSame

private val introspectionTypeNames = listOf(
	"__Directive",
	"__DirectiveLocation",
	"__EnumValue",
	"__Field",
	"__InputValue",
	"__Schema",
	"__Type",
	"__TypeKind",
)

private fun schemaOf(sdl: String): GSchema = GSchema.parse(sdl).valueWithoutErrorsOrThrow()

class IntrospectionTypeMergeTests {

	@Test
	fun introspectionTypes_areMembersOfEverySchema() {
		val schema = schemaOf("type Query { a: String }")

		assertEquals(actual = schema.types.map { it.name }.sorted(), expected = (listOf("Boolean", "Query", "String") + introspectionTypeNames).sorted())
	}

	@Test
	fun introspectionTypes_areResolvableByName() {
		val schema = schemaOf("type Query { a: String }")

		assertIs<GObjectType>(value = schema.resolveType("__Schema"))
		assertIs<GObjectType>(value = schema.resolveType("__Type"))
		assertIs<GObjectType>(value = schema.resolveType("__Field"))
		assertIs<GObjectType>(value = schema.resolveType("__InputValue"))
		assertIs<GObjectType>(value = schema.resolveType("__EnumValue"))
		assertIs<GObjectType>(value = schema.resolveType("__Directive"))
		assertIs<GEnumType>(value = schema.resolveType("__TypeKind"))
		assertIs<GEnumType>(value = schema.resolveType("__DirectiveLocation"))
	}

	// One type-identity domain: the instance `resolveType` hands out is the very instance listed in `types`,
	// which is what `{ __schema { types } }` enumerates.
	@Test
	fun introspectionTypes_resolveToTheInstanceListedInTypes() {
		val schema = schemaOf("type Query { a: String }")

		for (name in introspectionTypeNames) {
			assertSame(actual = schema.resolveType(name), expected = schema.types.single { it.name == name }, message = name)
		}
	}

	// The step-27 property, extended to the introspection types: nothing is shared between two schemas, so an
	// extension attached to one schema's `__Type` can never be seen by another's.
	@Test
	fun introspectionTypes_areDistinctInstancesPerSchema() {
		val first = schemaOf("type Query { a: String }")
		val second = schemaOf("type Query { a: String }")

		for (name in introspectionTypeNames) {
			assertNotSame(actual = first.resolveType(name), illegal = second.resolveType(name), message = name)
		}
	}

	// Merging the introspection types must not drag in a built-in scalar the user schema never mentions: the
	// introspection types refer to `String` and `Boolean` only. graphql@17.0.2 reports exactly the same for a
	// minimal schema.
	@Test
	fun introspectionTypes_doNotReferenceUnreferencedBuiltinScalars() {
		val schema = schemaOf("type Query { a: String }")

		assertNull(actual = schema.resolveType("Float"))
		assertNull(actual = schema.resolveType("ID"))
		assertNull(actual = schema.resolveType("Int"))
	}

	// Reachable only through the internal factory, since the public one refuses the document outright. The
	// merged type has to win, exactly as a built-in scalar wins over a same-named definition.
	@Test
	fun introspectionTypes_winOverASameNamedDefinition() {
		val document = GDocument.parse("type __Type { x: String } type Query { a: String }").valueWithoutErrorsOrThrow()
		val schema = GSchema(document = document, allowsReservedTypeNames = true)

		assertEquals(actual = schema.types.count { it.name == "__Type" }, expected = 1)
		assertEquals(
			actual = (schema.resolveType("__Type") as GObjectType).fieldDefinitions.map { it.name },
			expected = typeType().fieldDefinitions.map { it.name },
		)
	}

	// The exemption covers only the types the library merges in, never a definition the document carries.
	@Test
	fun introspectionTypes_doNotExemptAUserDefinitionOfTheSameName() {
		val exception = assertFailsWith<GErrorException> { schemaOf("type __Type { x: String } type Query { a: String }") }

		assertEquals(
			actual = exception.errors.single().message,
			expected = "Name \"__Type\" must not begin with \"__\", which is reserved by GraphQL introspection.",
		)
	}
}
