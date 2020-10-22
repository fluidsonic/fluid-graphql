package testing

import io.fluidsonic.graphql.*
import kotlin.test.*


class FieldSelectionExistenceRuleTest {

	@Test
	fun testAcceptsExistingFieldInFieldSelection() {
		assertValidationRule(
			rule = FieldSelectionExistenceRule,
			errors = emptyList(),
			document = """
				|{ id }
			""",
			schema = "type Query { id: ID! }"
		)
	}


	@Test
	fun testAcceptsExistingFieldInNestedFieldSelection() {
		assertValidationRule(
			rule = FieldSelectionExistenceRule,
			errors = emptyList(),
			document = """
				|{ object { id } }
			""",
			schema = """
				|type Object { id: ID! }
				|type Query { object: Object! }
			"""
		)
	}


	@Test
	fun testAcceptsExistingFieldInInlineFragmentSelection() {
		assertValidationRule(
			rule = FieldSelectionExistenceRule,
			errors = emptyList(),
			document = """
				|{
				|   ... {
				|       id
				|   }
				|}
			""",
			schema = "type Query { id: ID! }"
		)
	}


	@Test
	fun testAcceptsExistingFieldInFragmentSelection() {
		assertValidationRule(
			rule = FieldSelectionExistenceRule,
			errors = emptyList(),
			document = """
				|{
				|   ...f
				|}
				|
				|fragment f on Query { id }
			""",
			schema = "type Query { id: ID! }"
		)
	}


	@Test
	fun testAcceptsSchemaIntrospectionField() {
		assertValidationRule(
			rule = FieldSelectionExistenceRule,
			errors = emptyList(),
			document = """
				|{
				|   __schema
				|}
			""",
			schema = "type Query { id: ID! }"
		)
	}


	@Test
	fun testAcceptsTypeIntrospectionField() {
		assertValidationRule(
			rule = FieldSelectionExistenceRule,
			errors = emptyList(),
			document = """
				|{
				|   __type
				|}
			""",
			schema = "type Query { id: ID! }"
		)
	}


	@Test
	fun testAcceptsTypenameIntrospectionField() {
		assertValidationRule(
			rule = FieldSelectionExistenceRule,
			errors = emptyList(),
			document = """
				|{
				|   __typename
				|   object { __typename }
				|}
			""",
			schema = """
				type Object { id: ID! }
				type Query { object: Object! }
			"""
		)
	}


	@Test
	fun testRejectsNonexistentFieldInFieldSelection() {
		assertValidationRule(
			rule = FieldSelectionExistenceRule,
			errors = listOf("""
				Cannot select nonexistent field 'foo' on type 'Query'.

				<document>:1:3
				1 | { foo }
				  |   ^
			"""),
			document = """
				|{ foo }
			""",
			schema = "type Query { id: ID! }"
		)
	}


	@Test
	fun testRejectsNonexistentFieldInInlineFragmentSelection() {
		assertValidationRule(
			rule = FieldSelectionExistenceRule,
			errors = listOf("""
				Cannot select nonexistent field 'foo' on type 'Query'.

				<document>:3:8
				2 |    ... {
				3 |        foo
				  |        ^
				4 |    }
			"""),
			document = """
				|{
				|   ... {
				|       foo
				|   }
				|}
			""",
			schema = "type Query { id: ID! }"
		)
	}


	@Test
	fun testRejectsNonexistentFieldInFragmentSelection() {
		assertValidationRule(
			rule = FieldSelectionExistenceRule,
			errors = listOf("""
				Cannot select nonexistent field 'foo' on type 'Query'.

				<document>:5:23
				4 | 
				5 | fragment f on Query { foo }
				  |                       ^
			"""),
			document = """
				|{
				|   ...f
				|}
				|
				|fragment f on Query { foo }
			""",
			schema = "type Query { id: ID! }"
		)
	}


	@Test
	fun testRejectsSchemaIntrospectionInWrongLocation() {
		assertValidationRule(
			rule = FieldSelectionExistenceRule,
			errors = listOf("""
				Cannot select nonexistent field '__schema' on type 'Object'.

				<document>:1:12
				1 | { object { __schema } }
				  |            ^
			"""),
			document = """
				|{ object { __schema } }
			""",
			schema = """
				|type Object { id: ID! }
				|type Query { object: Object! }
			"""
		)
	}


	@Test
	fun testRejectsTypeIntrospectionInWrongLocation() {
		assertValidationRule(
			rule = FieldSelectionExistenceRule,
			errors = listOf("""
				Cannot select nonexistent field '__type' on type 'Object'.

				<document>:1:12
				1 | { object { __type } }
				  |            ^
			"""),
			document = """
				|{ object { __type } }
			""",
			schema = """
				|type Object { id: ID! }
				|type Query { object: Object! }
			"""
		)
	}
}
