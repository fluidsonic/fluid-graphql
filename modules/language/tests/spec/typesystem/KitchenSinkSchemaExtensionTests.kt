package testing

import io.fluidsonic.graphql.GCustomScalarType
import io.fluidsonic.graphql.GEnumType
import io.fluidsonic.graphql.GInputObjectType
import io.fluidsonic.graphql.GInterfaceType
import io.fluidsonic.graphql.GObjectType
import io.fluidsonic.graphql.GObjectTypeExtension
import io.fluidsonic.graphql.GSchema
import io.fluidsonic.graphql.GSchemaExtension
import io.fluidsonic.graphql.GUnionType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue

// GraphQL Spec §3.4.3, §3.6.3, §3.7.3, §3.8.3, §3.9.3, §3.10.3 — Type System Extensions,
// asserted against the kitchen sink schema fixture, which carries all seven extension kinds.
class KitchenSinkSchemaExtensionTests {

	@Test
	fun testToStringOfKitchenSinkSchemaHasNoExtensions() {
		val schema = GSchema.parse(kitchenSinkSchema.trimMargin()).valueOrThrow()
		val printed = schema.toString()

		assertFalse(
			printed.contains("extend "),
			"GSchema.toString() must not print any extension block:\n$printed",
		)
		assertFalse(
			printed.contains("scalar Int"),
			"GSchema.toString() must not print the built-in scalars:\n$printed",
		)
		assertEquals(
			actual = printed.split("\n").count { it.startsWith("schema") },
			expected = 1,
			message = "GSchema.toString() must print exactly one schema block:\n$printed",
		)
		assertTrue(
			printed.contains("subscription: SubscriptionType"),
			"GSchema.toString() must print the operation type contributed by a schema extension:\n$printed",
		)
		assertTrue(
			printed.contains("seven(argument: [String]): Type"),
			"GSchema.toString() must print the field definition contributed by an extension that collides with the base:\n$printed",
		)
		assertFalse(
			printed.contains("seven(argument: Int = null): Type"),
			"GSchema.toString() must not print the field definition that an extension replaced:\n$printed",
		)
	}

	@Test
	fun testKitchenSinkSchemaMergesObjectTypeExtensions() {
		val schema = GSchema.parse(kitchenSinkSchema.trimMargin()).valueOrThrow()

		val fooType = schema.resolveType("Foo")
		assertIs<GObjectType>(fooType)
		assertEquals(
			actual = fooType.fieldDefinitions.map { it.name },
			expected = listOf("one", "two", "three", "four", "five", "six", "seven"),
			message = "The colliding `seven` field of `Foo` must be replaced in place, keeping its original position.",
		)
		assertEquals(
			actual = fooType.fieldDefinition("seven")?.argumentDefinitions?.single()?.type?.toString(),
			expected = "[String]",
			message = "The extension's `seven` field definition must win over the base one.",
		)
		assertEquals(
			actual = fooType.interfaces.map { it.name },
			expected = listOf("Bar", "Baz", "Two"),
			message = "An extension without an `implements` clause must keep the extended type's interfaces.",
		)
		assertEquals(
			actual = fooType.directives.map { it.name },
			expected = listOf("onType"),
			message = "The second `extend type Foo` must contribute its directive.",
		)
	}

	@Test
	fun testKitchenSinkSchemaMergesInterfaceTypeExtensions() {
		val schema = GSchema.parse(kitchenSinkSchema.trimMargin()).valueOrThrow()

		val barType = schema.resolveType("Bar")
		assertIs<GInterfaceType>(barType)
		assertEquals(
			actual = barType.fieldDefinitions.map { it.name },
			expected = listOf("one", "four", "two"),
			message = "Both `extend interface Bar` blocks must apply.",
		)
		assertEquals(
			actual = barType.interfaces.map { it.name },
			expected = listOf("Two"),
			message = "`extend interface Bar implements Two` must add to the interface list.",
		)
		assertEquals(
			actual = barType.directives.map { it.name },
			expected = listOf("onInterface"),
			message = "The second `extend interface Bar` must contribute its directive.",
		)
	}

	@Test
	fun testKitchenSinkSchemaMergesUnionAndScalarTypeExtensions() {
		val schema = GSchema.parse(kitchenSinkSchema.trimMargin()).valueOrThrow()

		val feedType = schema.resolveType("Feed")
		assertIs<GUnionType>(feedType)
		assertEquals(
			actual = feedType.possibleTypes.map { it.name },
			expected = listOf("Story", "Article", "Advert", "Photo", "Video"),
			message = "`extend union Feed` must append its member types.",
		)
		assertEquals(
			actual = feedType.directives.map { it.name },
			expected = listOf("onUnion"),
			message = "The second `extend union Feed` must contribute its directive.",
		)

		val customScalarType = schema.resolveType("CustomScalar")
		assertIs<GCustomScalarType>(customScalarType)
		assertEquals(
			actual = customScalarType.directives.map { it.name },
			expected = listOf("onScalar"),
			message = "`extend scalar CustomScalar` must contribute its directive.",
		)
	}

	@Test
	fun testKitchenSinkSchemaMergesEnumAndInputObjectTypeExtensions() {
		val schema = GSchema.parse(kitchenSinkSchema.trimMargin()).valueOrThrow()

		val siteType = schema.resolveType("Site")
		assertIs<GEnumType>(siteType)
		assertEquals(
			actual = siteType.values.map { it.name },
			expected = listOf("DESKTOP", "MOBILE", "WEB", "VR"),
			message = "`extend enum Site` must append its value.",
		)
		assertEquals(
			actual = siteType.directives.map { it.name },
			expected = listOf("onEnum"),
			message = "The second `extend enum Site` must contribute its directive.",
		)

		val inputType = schema.resolveType("InputType")
		assertIs<GInputObjectType>(inputType)
		assertEquals(
			actual = inputType.argumentDefinitions.map { it.name },
			expected = listOf("key", "answer", "other"),
			message = "`extend input InputType` must append its field.",
		)
		assertEquals(
			actual = inputType.directives.map { it.name },
			expected = listOf("onInputObject"),
			message = "The second `extend input InputType` must contribute its directive.",
		)
	}

	@Test
	fun testDocumentKeepsExtensionsVerbatim() {
		val schema = GSchema.parse(kitchenSinkSchema.trimMargin()).valueOrThrow()

		assertEquals(
			actual = schema.document.definitions.filterIsInstance<GObjectTypeExtension>().size,
			expected = 2,
			message = "GSchema.document must keep the object type extension nodes of the source document.",
		)
		assertEquals(
			actual = schema.document.definitions.filterIsInstance<GSchemaExtension>().size,
			expected = 2,
			message = "GSchema.document must keep the schema extension nodes of the source document.",
		)
	}
}
