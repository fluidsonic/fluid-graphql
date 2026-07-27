/*
 * The registry `GSchema` builds from a document: which types it keeps, which built-in scalars it adds, and
 * which names it refuses outright. Verified against graphql@17.0.2 for everything but the two cases where
 * fluid deliberately throws where upstream does not — see `docs/scout/spec/graphql-js-parity.md`.
 */
package testing

import io.fluidsonic.graphql.GBooleanType
import io.fluidsonic.graphql.GDocument
import io.fluidsonic.graphql.GErrorException
import io.fluidsonic.graphql.GIntType
import io.fluidsonic.graphql.GScalarType
import io.fluidsonic.graphql.GSchema
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertNotSame
import kotlin.test.assertNull
import kotlin.test.assertSame

private val builtinScalarNames = listOf("Boolean", "Float", "ID", "Int", "String")

// Every schema carries these as ordinary members of its type map — see `IntrospectionTypeMergeTests`.
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

class SchemaTypeRegistryTests {

	@Test
	fun builtinScalars_appearExactlyOnceEach() {
		val schema = schemaOf("type Query { b: Boolean, f: Float, i: ID, n: Int, s: String }")

		for (name in builtinScalarNames) {
			assertEquals(actual = schema.types.count { it.name == name }, expected = 1, message = "occurrences of '$name' in `types`")
		}
	}

	// graphql-js keeps every *defined* type whether or not anything refers to it.
	@Test
	fun userTypes_surviveWhenUnreferenced() {
		val schema = schemaOf(
			"""
			type Query { a: String }
			type Unused { x: String }
			enum UnusedEnum { A B }
			input UnusedIn { y: String }
			scalar UnusedScalar
			""",
		)

		assertEquals(
			actual = schema.types.map { it.name }.filterNot { it in builtinScalarNames || it in introspectionTypeNames }.sorted(),
			expected = listOf("Query", "Unused", "UnusedEnum", "UnusedIn", "UnusedScalar"),
		)
	}

	// ...but a built-in scalar is added only when something refers to it, matching graphql-js. `Boolean` and
	// `String` are therefore always present: the built-in directives take arguments of those types, as do the
	// introspection types. graphql@17.0.2 reports exactly these eleven names for the same schema.
	@Test
	fun builtinScalars_areAddedOnlyWhenReferenced() {
		val schema = schemaOf("type Query { a: String }")

		assertEquals(actual = schema.types.map { it.name }.sorted(), expected = (listOf("Boolean", "Query", "String") + introspectionTypeNames).sorted())
		assertNull(actual = schema.resolveType("Int"), message = "`Int` must not be registered by a schema that never refers to it")
		assertNull(actual = schema.resolveType("Float"))
		assertNull(actual = schema.resolveType("ID"))
	}

	@Test
	fun builtinScalars_areAddedWhenReferencedByAnArgument() {
		val schema = schemaOf("type Query { a(count: Int): String }")

		assertIs<GIntType>(value = schema.resolveType("Int"))
	}

	@Test
	fun builtinScalars_areAddedWhenReferencedByAnUnreferencedType() {
		val schema = schemaOf("type Query { a: String } input Unused { n: Int }")

		assertIs<GIntType>(value = schema.resolveType("Int"))
	}

	@Test
	fun builtinScalars_areAddedWhenReferencedByAUserDirectiveDefinition() {
		val schema = schemaOf("type Query { a: String } directive @foo(n: Int!) on FIELD")

		assertIs<GIntType>(value = schema.resolveType("Int"))
	}

	@Test
	fun builtinScalars_booleanIsAlwaysAddedByTheBuiltinDirectives() {
		val schema = schemaOf("type Query { a: String }")

		assertNotNull(actual = schema.resolveType("Boolean"))
		assertIs<GBooleanType>(value = schema.resolveType("Boolean"))
	}

	// The tie-break the deduplication has to get right. Keeping the *first* entry — the obvious
	// `distinctBy { it.name }` — would hand every `Int` field to the user scalar's unvalidated pass-through
	// instead of the built-in's validating coercion, with nothing failing to say so.
	@Test
	fun builtinScalars_winOverASameNamedDefinition() {
		val document = GDocument.parse("scalar Int type Query { a: Int }").valueWithoutErrorsOrThrow()
		val definedInt = document.definitions.filterIsInstance<GScalarType>().single { it.name == "Int" }
		val schema = GSchema(document = document, allowsReservedTypeNames = true)

		assertEquals(actual = schema.types.count { it.name == "Int" }, expected = 1)
		assertIs<GIntType>(value = schema.resolveType("Int"))
		assertNotSame(actual = schema.resolveType("Int"), illegal = definedInt)
		assertSame(actual = schema.resolveType("Int"), expected = schema.types.single { it.name == "Int" })
	}

	// A deliberate divergence: graphql-js discards an SDL redefinition of a built-in scalar without a
	// diagnostic, and only throws programmatically for `String` and `Boolean` — an accident of which
	// built-ins the introspection types make reachable, not a rule.
	@Test
	fun types_rejectABuiltinScalarName() {
		for (name in builtinScalarNames) {
			val exception = assertFailsWith<GErrorException> { schemaOf("scalar $name type Query { a: $name }") }

			assertEquals(actual = exception.errors.single().message, expected = "Cannot redefine built-in scalar type \"$name\".")
		}
	}

	@Test
	fun types_rejectAnIntrospectionTypeName() {
		val exception = assertFailsWith<GErrorException> { schemaOf("type __Type { x: String } type Query { a: String }") }

		assertEquals(
			actual = exception.errors.single().message,
			expected = "Name \"__Type\" must not begin with \"__\", which is reserved by GraphQL introspection.",
		)
	}

	// A deliberate divergence: graphql-js reports this from `validateSchema()` rather than refusing to
	// build the schema.
	@Test
	fun types_rejectAnyIntrospectionPrefixedName() {
		val exception = assertFailsWith<GErrorException> { schemaOf("type __Foo { x: String } type Query { a: __Foo }") }

		assertEquals(
			actual = exception.errors.single().message,
			expected = "Name \"__Foo\" must not begin with \"__\", which is reserved by GraphQL introspection.",
		)
	}

	@Test
	fun types_rejectTwoDefinitionsSharingAName() {
		val exception = assertFailsWith<GErrorException> { schemaOf("type Query { a: A } type A { x: String } type A { y: String }") }

		assertEquals(actual = exception.errors.single().message, expected = "There can be only one type named \"A\".")
	}

	// Directive names follow the opposite rule from type names: a user-declared built-in directive wholly
	// replaces the built-in on both graphql-js paths.
	@Test
	fun directiveDefinitions_acceptAUserDeclaredDeprecatedDirective() {
		val schema = schemaOf("directive @deprecated(zzz: String) on FIELD_DEFINITION type Query { a: String }")

		assertEquals(actual = schema.directiveDefinitions.count { it.name == "deprecated" }, expected = 1)
		assertEquals(actual = schema.directiveDefinition("deprecated")?.argumentDefinitions?.map { it.name }, expected = listOf("zzz"))
	}
}
