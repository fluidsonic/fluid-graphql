package testing

import io.fluidsonic.graphql.*
import kotlin.test.*


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
			"""
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
			"""
		)
	}


	@Test
	fun testRejectLeavesWithSelection() {
		assertValidationRule(
			rule = ScalarLeavesRule,
			errors = listOf(
				"""
					Field 'enum' must not have a selection since enum type 'Enum' has no subfields.

					<document>:2:9
					1 | {
					2 |    enum { x }
					  |         ^
					3 |    scalar { x }
				""",
				"""
					Field 'scalar' must not have a selection since scalar type 'String' has no subfields.

					<document>:3:11
					2 |    enum { x }
					3 |    scalar { x }
					  |           ^
					4 | }
				"""
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
			"""
		)
	}


	@Test
	fun testRejectCompositeWithoutSelection() {
		assertValidationRule(
			rule = ScalarLeavesRule,
			errors = listOf(
				"""
					Field 'interface' of interface type 'Interface' must have a selection of subfields. Did you mean 'interface { … }'?

					<document>:2:4
					1 | {
					2 |    interface
					  |    ^
					3 |    object
				""",
				"""
					Field 'object' of object type 'Object' must have a selection of subfields. Did you mean 'object { … }'?

					<document>:3:4
					2 |    interface
					3 |    object
					  |    ^
					4 |    union
				""",
				"""
					Field 'union' of union type 'Union' must have a selection of subfields. Did you mean 'union { … }'?

					<document>:4:4
					3 |    object
					4 |    union
					  |    ^
					5 | }
				"""
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
			"""
		)
	}
}
