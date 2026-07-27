package testing

import io.fluidsonic.graphql.GSchema
import io.fluidsonic.graphql.printSchema
import io.fluidsonic.graphql.printType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotSame
import kotlin.test.assertTrue

/**
 * Pins [printSchema] and [printType] against graphql@17.0.2, whose `printSchema()` and `printType()`
 * produced every expectation in this file verbatim. Parity is the specification here, so an expectation
 * may only be changed by re-running upstream.
 */
class SchemaPrinterTests {

	@Test
	fun printSchema_matchesReferenceImplementation() {
		assertEquals(
			actual = printSchema(schemaOf(kitchenSinkSource)),
			expected = kitchenSinkExpectation,
		)
	}

	@Test
	fun printSchema_omitsSchemaBlockForDefaultRootTypeNames() {
		assertEquals(
			actual = printSchema(
				schemaOf(
					"""
					type Query { a: String }
					type Mutation { b: String }
					type Subscription { c: String }
					""",
				),
			),
			expected = """
				type Query {
				  a: String
				}

				type Mutation {
				  b: String
				}

				type Subscription {
				  c: String
				}
			""".trimIndent(),
		)
	}

	@Test
	fun printSchema_printsSchemaBlockForCustomRootTypeName() {
		assertEquals(
			actual = printSchema(
				schemaOf(
					"""
					schema { query: Root }
					type Root { a: String }
					""",
				),
			),
			expected = """
				schema {
				  query: Root
				}

				type Root {
				  a: String
				}
			""".trimIndent(),
		)
	}

	@Test
	fun printSchema_printsSchemaBlockForSchemaDescription() {
		assertEquals(
			actual = printSchema(
				schemaOf(
					"""
					"The whole schema."
					schema { query: Query }
					type Query { a: String }
					""",
				),
			),
			expected = """
				${tripleQuote}The whole schema.$tripleQuote
				schema {
				  query: Query
				}

				type Query {
				  a: String
				}
			""".trimIndent(),
		)
	}

	@Test
	fun printSchema_omitsBuiltinScalarsByName() {
		val schema = schemaOf("type Query { text: String, count: Int, id: ID! }")
		val otherSchema = schemaOf("type Query { text: String }")

		// Step 27 gave every schema its own built-in scalar instances, so nothing can be filtered by identity.
		assertNotSame(
			actual = schema.resolveType("String"),
			illegal = otherSchema.resolveType("String"),
		)

		assertEquals(
			actual = printSchema(schema),
			expected = """
				type Query {
				  text: String
				  count: Int
				  id: ID!
				}
			""".trimIndent(),
		)
	}

	@Test
	fun printSchema_omitsIntrospectionTypesAndSpecifiedDirectiveDefinitions() {
		val schema = schemaOf("type Query { a: String }")
		val printed = printSchema(schema)

		assertTrue(schema.types.any { it.name == "__Schema" }, "the schema under test must contain the introspection types")
		assertTrue(schema.directiveDefinitions.any { it.name == "skip" }, "the schema under test must contain the specified directives")
		assertFalse(printed.contains("__"), "must not print an introspection type:\n$printed")
		assertFalse(printed.contains("directive @"), "must not print a specified directive definition:\n$printed")

		assertEquals(
			actual = printed,
			expected = """
				type Query {
				  a: String
				}
			""".trimIndent(),
		)
	}

	@Test
	fun printSchema_printsBareDeprecatedForTheDefaultReason() {
		assertEquals(
			actual = printSchema(schemaOf("""type Query { a: String @deprecated(reason: "No longer supported") }""")),
			expected = """
				type Query {
				  a: String @deprecated
				}
			""".trimIndent(),
		)
	}

	@Test
	fun printSchema_printsMergedTypeExtensions() {
		assertEquals(
			actual = printSchema(
				schemaOf(
					"""
					type Query { a: String }
					extend type Query { b: Int }
					""",
				),
			),
			expected = """
				type Query {
				  a: String
				  b: Int
				}
			""".trimIndent(),
		)
	}

	@Test
	fun printType_printsSingleTypeDefinition() {
		val schema = schemaOf(kitchenSinkSource)

		assertEquals(
			actual = printType(schema.resolveType("Droid")!!),
			expected = """
				type Droid implements Character {
				  id: ID!
				  name: String
				  primaryFunction: String @deprecated(reason: "Droids are people too")
				  model: String @deprecated
				}
			""".trimIndent(),
		)
	}

	@Test
	fun toString_delegatesToPrintSchema() {
		val schema = schemaOf(kitchenSinkSource)

		assertEquals(
			actual = schema.toString(),
			expected = printSchema(schema, indent = "\t"),
		)

		assertTrue(schema.toString().contains("\n\tid: ID!"), "GSchema.toString() must indent with tabs")
	}

	private fun schemaOf(source: String) = GSchema.parse(source).valueOrThrow()
}

/** Lets the fixtures below contain GraphQL block strings, which a Kotlin raw string cannot spell directly. */
private const val tripleQuote = "\"\"\""

private val kitchenSinkSource = """
	directive @key(fields: String!) repeatable on OBJECT | INTERFACE | FIELD_DEFINITION | SCALAR | ENUM_VALUE | INPUT_OBJECT

	"A custom directive with a description."
	directive @auth(role: Role! = ADMIN) on OBJECT | FIELD_DEFINITION

	"The scalar for dates."
	scalar DateTime @specifiedBy(url: "https://example.com/datetime")

	scalar Plain @key(fields: "nope")

	$tripleQuote
	An episode.

	It spans multiple lines.
	$tripleQuote
	enum Episode {
		"The first one."
		NEWHOPE
		EMPIRE @deprecated
		JEDI @deprecated(reason: "Not canon")
	}

	enum Role {
		ADMIN
		USER @key(fields: "x")
	}

	"A character."
	interface Character @key(fields: "id") {
		id: ID!
		name: String
	}

	type Human implements Character @key(fields: "id") @auth {
		id: ID!
		name: String
		height(unit: Unit = METER): Float
		obsolete: String @deprecated @key(fields: "id")
	}

	type Droid implements Character {
		id: ID!
		name: String
		primaryFunction: String @deprecated(reason: "Droids are people too")
	}

	enum Unit {
		METER
		FOOT
	}

	extend type Droid @key(fields: "id") {
		model: String @deprecated
	}

	union SearchResult = Human | Droid

	"A filter."
	input SearchFilter @oneOf {
		"By name."
		byName: String
		byId: ID @deprecated
	}

	input Paging @key(fields: "first") {
		first: Int = 10
		after: String @deprecated(reason: "Use cursor")
	}

	"The root query."
	type Query @auth {
		hero(
			"Which episode."
			episode: Episode = NEWHOPE
			legacy: Boolean @deprecated
			old: Int @deprecated(reason: "use new")
		): Character
		search(filter: SearchFilter, paging: Paging): [SearchResult!]!
		when: DateTime
		plain: Plain
		legacyField: String @deprecated
		tagged: String @key(fields: "id")
	}
""".trimIndent()

private val kitchenSinkExpectation = """
	directive @key(fields: String!) repeatable on OBJECT | INTERFACE | FIELD_DEFINITION | SCALAR | ENUM_VALUE | INPUT_OBJECT

	${tripleQuote}A custom directive with a description.$tripleQuote
	directive @auth(role: Role! = ADMIN) on OBJECT | FIELD_DEFINITION

	${tripleQuote}The scalar for dates.$tripleQuote
	scalar DateTime @specifiedBy(url: "https://example.com/datetime")

	scalar Plain

	$tripleQuote
	An episode.

	It spans multiple lines.
	$tripleQuote
	enum Episode {
	  ${tripleQuote}The first one.$tripleQuote
	  NEWHOPE
	  EMPIRE @deprecated
	  JEDI @deprecated(reason: "Not canon")
	}

	enum Role {
	  ADMIN
	  USER
	}

	${tripleQuote}A character.$tripleQuote
	interface Character {
	  id: ID!
	  name: String
	}

	type Human implements Character {
	  id: ID!
	  name: String
	  height(unit: Unit = METER): Float
	  obsolete: String @deprecated
	}

	type Droid implements Character {
	  id: ID!
	  name: String
	  primaryFunction: String @deprecated(reason: "Droids are people too")
	  model: String @deprecated
	}

	enum Unit {
	  METER
	  FOOT
	}

	union SearchResult = Human | Droid

	${tripleQuote}A filter.$tripleQuote
	input SearchFilter @oneOf {
	  ${tripleQuote}By name.$tripleQuote
	  byName: String
	  byId: ID @deprecated
	}

	input Paging {
	  first: Int = 10
	  after: String @deprecated(reason: "Use cursor")
	}

	${tripleQuote}The root query.$tripleQuote
	type Query {
	  hero(
	    ${tripleQuote}Which episode.$tripleQuote
	    episode: Episode = NEWHOPE
	    legacy: Boolean @deprecated
	    old: Int @deprecated(reason: "use new")
	  ): Character
	  search(filter: SearchFilter, paging: Paging): [SearchResult!]!
	  when: DateTime
	  plain: Plain
	  legacyField: String @deprecated
	  tagged: String
	}
""".trimIndent()
