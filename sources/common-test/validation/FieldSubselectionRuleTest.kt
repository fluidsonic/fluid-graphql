package io.fluidsonic.graphql

import tests.*
import kotlin.test.*


class FieldSubselectionRuleTest {

	@Test
	fun `accepts leaf fields without subselection`() {
		assertValidationRule(
			rule = FieldSubselectionRule,
			errors = emptyList(),
			document = """
				|{
				|   boolean
				|   enum
				|   id
				|   int
				|   float
				|   string
				|}
			""",
			schema = """
				|type Query {
				|   boolean: Boolean
				|   enum: Enum
				|   id: ID
				|   int: Int
				|   float: Float
				|   string: String
				|}
				|
				|enum Enum { VALUE }
			"""
		)
	}


	@Test
	fun `accepts composite type fields with subselection`() {
		assertValidationRule(
			rule = FieldSubselectionRule,
			errors = emptyList(),
			document = """
				|{
				|   interface { __typename }
				|   object { __typename }
				|   union { __typename }
				|}
			""",
			schema = """
				|type Query {
				|   interface: Interface
				|   object: Object1
				|   union: Union
				|}
				|
				|interface Interface { id: ID }
				|type Object1 implements Interface { id: ID }
				|type Object2 { id: ID }
				|union Union = Object1 | Object2
			"""
		)
	}


	@Test
	fun `rejects leaf fields with subselection`() {
		assertValidationRule(
			rule = FieldSubselectionRule,
			errors = listOf(
				"""
					Cannot select children of 'Boolean' field 'boolean'.

					<document>:2:12
					1 | {
					2 |    boolean { __typename }
					  |            ^
					3 |    enum { __typename }

					<document>:2:4
					1 | type Query {
					2 |    boolean: Boolean
					  |    ^
					3 |    enum: Enum
				""",
				"""
					Cannot select children of 'Enum' field 'enum'.

					<document>:3:9
					2 |    boolean { __typename }
					3 |    enum { __typename }
					  |         ^
					4 |    id { __typename }

					<document>:3:4
					2 |    boolean: Boolean
					3 |    enum: Enum
					  |    ^
					4 |    id: ID
				""",
				"""
					Cannot select children of 'ID' field 'id'.

					<document>:4:7
					3 |    enum { __typename }
					4 |    id { __typename }
					  |       ^
					5 |    int { __typename }

					<document>:4:4
					3 |    enum: Enum
					4 |    id: ID
					  |    ^
					5 |    int: Int
				""",
				"""
					Cannot select children of 'Int' field 'int'.

					<document>:5:8
					4 |    id { __typename }
					5 |    int { __typename }
					  |        ^
					6 |    float { __typename }

					<document>:5:4
					4 |    id: ID
					5 |    int: Int
					  |    ^
					6 |    float: Float
				""",
				"""
					Cannot select children of 'Float' field 'float'.

					<document>:6:10
					5 |    int { __typename }
					6 |    float { __typename }
					  |          ^
					7 |    string { __typename }

					<document>:6:4
					5 |    int: Int
					6 |    float: Float
					  |    ^
					7 |    string: String
				""",
				"""
					Cannot select children of 'String' field 'string'.

					<document>:7:11
					6 |    float { __typename }
					7 |    string { __typename }
					  |           ^
					8 | }

					<document>:7:4
					6 |    float: Float
					7 |    string: String
					  |    ^
					8 | }
				"""
			),
			document = """
				|{
				|   boolean { __typename }
				|   enum { __typename }
				|   id { __typename }
				|   int { __typename }
				|   float { __typename }
				|   string { __typename }
				|}
			""",
			schema = """
				|type Query {
				|   boolean: Boolean
				|   enum: Enum
				|   id: ID
				|   int: Int
				|   float: Float
				|   string: String
				|}
				|
				|enum Enum { VALUE }
			"""
		)
	}


	@Test
	fun `rejects composite type fields without subselection`() {
		assertValidationRule(
			rule = FieldSubselectionRule,
			errors = listOf(
				"""
					Must select children of 'Interface' field 'interface'.

					<document>:2:4
					1 | {
					2 |    interface
					  |    ^
					3 |    object

					<document>:2:4
					1 | type Query {
					2 |    interface: Interface
					  |    ^
					3 |    object: Object1
				""",
				"""
					Must select children of 'Object1' field 'object'.

					<document>:3:4
					2 |    interface
					3 |    object
					  |    ^
					4 |    union

					<document>:3:4
					2 |    interface: Interface
					3 |    object: Object1
					  |    ^
					4 |    union: Union
				""",
				"""
					Must select children of 'Union' field 'union'.

					<document>:4:4
					3 |    object
					4 |    union
					  |    ^
					5 | }

					<document>:4:4
					3 |    object: Object1
					4 |    union: Union
					  |    ^
					5 | }
			"""),
			document = """
				|{
				|   interface
				|   object
				|   union
				|}
			""",
			schema = """
				|type Query {
				|   interface: Interface
				|   object: Object1
				|   union: Union
				|}
				|
				|interface Interface { id: ID }
				|type Object1 implements Interface { id: ID }
				|type Object2 { id: ID }
				|union Union = Object1 | Object2
			"""
		)
	}
}
