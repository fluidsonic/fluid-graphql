package testing

import io.fluidsonic.graphql.ScalarLeavesRule
import kotlin.test.Test

class ScalarLeavesRuleTest {

	@Test
	fun testAcceptLeavesWithoutSelection() {
		assertValidationRule(
			rule = ScalarLeavesRule,
			errors = emptyList(),
			document = """
				|{
				|   enum
				|   scalar
				|}
			""",
			schema = """
				|enum Enum { foo }
				|type Query {
				|   enum: Enum!
				|   scalar: String!
				|}
			""",
		)
	}

	@Test
	fun testAcceptsCompositeWithSelection() {
		assertValidationRule(
			rule = ScalarLeavesRule,
			errors = emptyList(),
			document = """
				|{
				|   interface { foo }
				|   object { foo }
				|   union { foo }
				|}
			""",
			schema = """
				|interface Interface { foo: String }
				|type Object implements Interface { foo: String }
				|union Union = Object
				|type Query {
				|   interface: Interface!
				|   object: Object!
				|   union: Union!
				|}
			""",
		)
	}

	@Test
	fun testRejectLeavesWithSelection() {
		assertValidationRule(
			rule = ScalarLeavesRule,
			errors = listOf(
				"""
					Field "enum" must not have a selection since type "Enum" has no subfields.

					<document>:2:9
					1 | {
					2 |    enum { x }
					  |         ^
					3 |    scalar { x }
				""",
				"""
					Field "scalar" must not have a selection since type "String" has no subfields.

					<document>:3:11
					2 |    enum { x }
					3 |    scalar { x }
					  |           ^
					4 | }
				""",
			),
			document = """
				|{
				|   enum { x }
				|   scalar { x }
				|}
			""",
			schema = """
				|enum Enum { foo }
				|type Query {
				|   enum: Enum
				|   scalar: String
				|}
			""",
		)
	}

	@Test
	fun testRejectCompositeWithoutSelection() {
		assertValidationRule(
			rule = ScalarLeavesRule,
			errors = listOf(
				"""
					Field "interface" of type "Interface" must have a selection of subfields. Did you mean "interface { ... }"?

					<document>:2:4
					1 | {
					2 |    interface
					  |    ^
					3 |    object
				""",
				"""
					Field "object" of type "Object" must have a selection of subfields. Did you mean "object { ... }"?

					<document>:3:4
					2 |    interface
					3 |    object
					  |    ^
					4 |    union
				""",
				"""
					Field "union" of type "Union" must have a selection of subfields. Did you mean "union { ... }"?

					<document>:4:4
					3 |    object
					4 |    union
					  |    ^
					5 | }
				""",
			),
			document = """
				|{
				|   interface
				|   object
				|   union
				|}
			""",
			schema = """
				|interface Interface { foo: String }
				|type Object implements Interface { foo: String }
				|union Union = Object
				|type Query {
				|   interface: Interface
				|   object: Object
				|   union: Union
				|}
			""",
		)
	}

	// The message names the full wrapped type, not the underlying named type — leaf-ness itself is still decided on the underlying named type.
	@Test
	fun testRejectWrappedLeavesWithSelection() {
		assertValidationRule(
			rule = ScalarLeavesRule,
			errors = listOf(
				"""
					Field "nonNull" must not have a selection since type "Int!" has no subfields.

					<document>:2:12
					1 | {
					2 |    nonNull { x }
					  |            ^
					3 |    list { x }
				""",
				"""
					Field "list" must not have a selection since type "[Int]" has no subfields.

					<document>:3:9
					2 |    nonNull { x }
					3 |    list { x }
					  |         ^
					4 |    nonNullEnum { x }
				""",
				"""
					Field "nonNullEnum" must not have a selection since type "Enum!" has no subfields.

					<document>:4:16
					3 |    list { x }
					4 |    nonNullEnum { x }
					  |                ^
					5 | }
				""",
			),
			document = """
				|{
				|   nonNull { x }
				|   list { x }
				|   nonNullEnum { x }
				|}
			""",
			schema = """
				|enum Enum { foo }
				|type Query {
				|   nonNull: Int!
				|   list: [Int]
				|   nonNullEnum: Enum!
				|}
			""",
		)
	}

	@Test
	fun testRejectWrappedCompositesWithoutSelection() {
		assertValidationRule(
			rule = ScalarLeavesRule,
			errors = listOf(
				"""
					Field "nonNull" of type "Object!" must have a selection of subfields. Did you mean "nonNull { ... }"?

					<document>:2:4
					1 | {
					2 |    nonNull
					  |    ^
					3 |    list
				""",
				"""
					Field "list" of type "[Object]" must have a selection of subfields. Did you mean "list { ... }"?

					<document>:3:4
					2 |    nonNull
					3 |    list
					  |    ^
					4 | }
				""",
			),
			document = """
				|{
				|   nonNull
				|   list
				|}
			""",
			schema = """
				|type Object { foo: String }
				|type Query {
				|   nonNull: Object!
				|   list: [Object]
				|}
			""",
		)
	}

	@Test
	fun testReportsFieldNameRatherThanAlias() {
		assertValidationRule(
			rule = ScalarLeavesRule,
			errors = listOf(
				"""
					Field "scalar" must not have a selection since type "String" has no subfields.

					<document>:2:22
					1 | {
					2 |    leafAlias: scalar { x }
					  |                      ^
					3 |    compositeAlias: object
				""",
				"""
					Field "object" of type "Object" must have a selection of subfields. Did you mean "object { ... }"?

					<document>:3:4
					2 |    leafAlias: scalar { x }
					3 |    compositeAlias: object
					  |    ^
					4 | }
				""",
			),
			document = """
				|{
				|   leafAlias: scalar { x }
				|   compositeAlias: object
				|}
			""",
			schema = """
				|type Object { foo: String }
				|type Query {
				|   scalar: String
				|   object: Object
				|}
			""",
		)
	}
}
