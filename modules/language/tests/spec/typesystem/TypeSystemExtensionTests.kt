package testing

import io.fluidsonic.graphql.GCustomScalarType
import io.fluidsonic.graphql.GEnumType
import io.fluidsonic.graphql.GInputObjectType
import io.fluidsonic.graphql.GInterfaceType
import io.fluidsonic.graphql.GSchema
import io.fluidsonic.graphql.GUnionType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertNull

// GraphQL Spec §3.4.3, §3.6.3, §3.7.3, §3.8.3, §3.9.3, §3.10.3 — Type System Extensions
class TypeSystemExtensionTests {

	@Test
	fun testObjectTypeExtensionAddsFields() {
		val schema = GSchema.parse(
			"""
			type Query { a: Int }
			extend type Query { b: String }
			""".trimIndent(),
		).valueOrThrow()
		val queryType = schema.queryType
		assertNotNull(queryType)

		assertEquals(
			actual = queryType.fieldDefinitions.map { it.name },
			expected = listOf("a", "b"),
			message = "An object type extension must append its fields to the extended type.",
		)
	}

	@Test
	fun testObjectTypeExtensionAddsDirectivesAndKeepsInterfaces() {
		val schema = GSchema.parse(
			"""
			interface One { x: Int }
			interface Two { y: Int }
			type Query implements One { x: Int }
			extend type Query implements Two @onType { y: Int }
			""".trimIndent(),
		).valueOrThrow()
		val queryType = schema.queryType
		assertNotNull(queryType)

		assertEquals(
			actual = queryType.interfaces.map { it.name },
			expected = listOf("One", "Two"),
			message = "An object type extension must add to the interfaces of the extended type without dropping the original ones.",
		)
		assertEquals(
			actual = queryType.directives.map { it.name },
			expected = listOf("onType"),
			message = "An object type extension must contribute its directives to the extended type.",
		)
	}

	@Test
	fun testObjectTypeExtensionAffectsPossibleTypes() {
		val schema = GSchema.parse(
			"""
			interface Animal { name: String }
			type Dog { name: String }
			extend type Dog implements Animal
			type Query { dog: Dog }
			""".trimIndent(),
		).valueOrThrow()
		val animalType = schema.resolveType("Animal")
		assertIs<GInterfaceType>(animalType)

		assertEquals(
			actual = schema.getPossibleTypes(animalType).map { it.name },
			expected = listOf("Dog"),
			message = "Interface membership must be computed from the merged types, not from the unmerged ones.",
		)
	}

	@Test
	fun testInterfaceTypeExtensionAddsFieldsAndInterfaces() {
		val schema = GSchema.parse(
			"""
			interface Two { y: Int }
			interface Bar { x: Int }
			extend interface Bar implements Two { y: Int }
			type Query { bar: Bar }
			""".trimIndent(),
		).valueOrThrow()
		val barType = schema.resolveType("Bar")
		assertIs<GInterfaceType>(barType)

		assertEquals(
			actual = barType.fieldDefinitions.map { it.name },
			expected = listOf("x", "y"),
			message = "An interface type extension must append its fields to the extended interface.",
		)
		assertEquals(
			actual = barType.interfaces.map { it.name },
			expected = listOf("Two"),
			message = "An interface type extension must contribute its implemented interfaces to the extended interface.",
		)
	}

	@Test
	fun testUnionTypeExtensionAddsPossibleTypes() {
		val schema = GSchema.parse(
			"""
			type Story { id: ID }
			type Photo { id: ID }
			union Feed = Story
			extend union Feed @onUnion = Photo
			type Query { feed: Feed }
			""".trimIndent(),
		).valueOrThrow()
		val feedType = schema.resolveType("Feed")
		assertIs<GUnionType>(feedType)

		assertEquals(
			actual = feedType.possibleTypes.map { it.name },
			expected = listOf("Story", "Photo"),
			message = "A union type extension must append its member types to the extended union.",
		)
		assertEquals(
			actual = feedType.directives.map { it.name },
			expected = listOf("onUnion"),
			message = "A union type extension must contribute its directives to the extended union.",
		)
	}

	@Test
	fun testEnumTypeExtensionAddsValues() {
		val schema = GSchema.parse(
			"""
			enum Site { DESKTOP }
			extend enum Site @onEnum { VR }
			type Query { site: Site }
			""".trimIndent(),
		).valueOrThrow()
		val siteType = schema.resolveType("Site")
		assertIs<GEnumType>(siteType)

		assertEquals(
			actual = siteType.values.map { it.name },
			expected = listOf("DESKTOP", "VR"),
			message = "An enum type extension must append its values to the extended enum.",
		)
		assertEquals(
			actual = siteType.directives.map { it.name },
			expected = listOf("onEnum"),
			message = "An enum type extension must contribute its directives to the extended enum.",
		)
	}

	@Test
	fun testInputObjectTypeExtensionAddsFields() {
		val schema = GSchema.parse(
			"""
			input InputType { key: String! }
			extend input InputType @onInputObject { other: Float }
			type Query { field(input: InputType): String }
			""".trimIndent(),
		).valueOrThrow()
		val inputType = schema.resolveType("InputType")
		assertIs<GInputObjectType>(inputType)

		assertEquals(
			actual = inputType.argumentDefinitions.map { it.name },
			expected = listOf("key", "other"),
			message = "An input object type extension must append its fields to the extended input object.",
		)
		assertEquals(
			actual = inputType.directives.map { it.name },
			expected = listOf("onInputObject"),
			message = "An input object type extension must contribute its directives to the extended input object.",
		)
	}

	@Test
	fun testScalarTypeExtensionAddsDirectives() {
		val schema = GSchema.parse(
			"""
			scalar CustomScalar
			extend scalar CustomScalar @onScalar
			type Query { value: CustomScalar }
			""".trimIndent(),
		).valueOrThrow()
		val scalarType = schema.resolveType("CustomScalar")
		assertIs<GCustomScalarType>(scalarType)

		assertEquals(
			actual = scalarType.directives.map { it.name },
			expected = listOf("onScalar"),
			message = "A scalar type extension must contribute its directives to the extended scalar.",
		)
	}

	@Test
	fun testSchemaExtensionAddsRootType() {
		val schema = GSchema.parse(
			"""
			schema { query: QueryType }
			type QueryType { a: Int }
			type SubscriptionType { b: Int }
			extend schema { subscription: SubscriptionType }
			""".trimIndent(),
		).valueOrThrow()

		assertEquals(
			actual = schema.subscriptionType?.name,
			expected = "SubscriptionType",
			message = "A schema extension must contribute its operation types to the schema definition.",
		)
		assertEquals(
			actual = schema.queryType?.name,
			expected = "QueryType",
			message = "A schema extension must not drop the operation types of the schema definition.",
		)
	}

	@Test
	fun testSchemaExtensionWithoutSchemaDefinition() {
		val schema = GSchema.parse(
			"""
			type Q { a: Int }
			extend schema { query: Q }
			""".trimIndent(),
		).valueOrThrow()

		assertEquals(
			actual = schema.queryType?.name,
			expected = "Q",
			message = "A schema extension must be honoured even when the document has no schema definition.",
		)
	}

	@Test
	fun testTwoExtensionsOfSameTypeBothApply() {
		val schema = GSchema.parse(
			"""
			type Query { a: Int }
			extend type Query { b: String }
			extend type Query @onType { c: Boolean }
			""".trimIndent(),
		).valueOrThrow()
		val queryType = schema.queryType
		assertNotNull(queryType)

		assertEquals(
			actual = queryType.fieldDefinitions.map { it.name },
			expected = listOf("a", "b", "c"),
			message = "Two extensions of the same type must both apply, in document order.",
		)
	}

	@Test
	fun testExtensionReplacesCollidingFieldInPlace() {
		val schema = GSchema.parse(
			"""
			type Query { a: Int b: Boolean }
			extend type Query { a: String }
			""".trimIndent(),
		).valueOrThrow()
		val queryType = schema.queryType
		assertNotNull(queryType)

		assertEquals(
			actual = queryType.fieldDefinitions.map { it.name },
			expected = listOf("a", "b"),
			message = "A colliding field must be replaced in place rather than appended, keeping its original position.",
		)
		assertEquals(
			actual = queryType.fieldDefinition("a")?.type?.toString(),
			expected = "String",
			message = "On a name collision the extension's field definition must win.",
		)
	}

	@Test
	fun testExtensionOfUndefinedTypeIsIgnored() {
		val schema = GSchema.parse(
			"""
			type Query { a: Int }
			extend type Undefined { b: String }
			extend enum UndefinedEnum { VALUE }
			extend union UndefinedUnion = Query
			extend input UndefinedInput { b: String }
			extend interface UndefinedInterface { b: String }
			extend scalar UndefinedScalar @onScalar
			""".trimIndent(),
		).valueOrThrow()

		assertNull(
			actual = schema.resolveType("Undefined"),
			message = "Extending an undefined type must be silently ignored.",
		)
		assertNotNull(
			actual = schema.queryType,
			message = "A document that extends an undefined type must still build a schema.",
		)
	}

	@Test
	fun testExtensionOfBuiltinScalarIsIgnored() {
		val schema = GSchema.parse(
			"""
			type Query { a: Int }
			extend scalar Int @onScalar
			""".trimIndent(),
		).valueOrThrow()
		val intType = schema.resolveType("Int")
		assertNotNull(intType)

		assertEquals(
			actual = intType.directives.map { it.name },
			expected = emptyList(),
			message = "Extending a built-in scalar must be silently ignored.",
		)
	}

	@Test
	fun testToStringShowsMergedResult() {
		val schema = GSchema.parse(
			"""
			type Query { a: Int }
			extend type Query { b: String }
			""".trimIndent(),
		).valueOrThrow()

		assertEquals(
			actual = schema.toString(),
			expected = "type Query {\n\ta: Int\n\tb: String\n}",
			message = "GSchema.toString() must print the merged type, not the base type plus an extension block.",
		)
	}

	@Test
	fun testToStringShowsMergedCollisionResult() {
		val schema = GSchema.parse(
			"""
			type Query { a: Int }
			extend type Query { a: String }
			""".trimIndent(),
		).valueOrThrow()

		assertEquals(
			actual = schema.toString(),
			expected = "type Query {\n\ta: String\n}",
			message = "GSchema.toString() must print the replaced field once, in its original position.",
		)
	}
}
