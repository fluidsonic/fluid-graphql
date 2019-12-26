package io.fluidsonic.graphql

import tests.*
import kotlin.test.*


class FieldSelectionReferencesExistingFieldRuleTest : ValidationRule {

	@Test
	fun `accepts existing field in field selection`() {
		assertValidationRule(
			rule = FieldSelectionReferencesExistingFieldRule,
			errors = emptyList(),
			document = """
				|{ id }
			""",
			schema = "type Query { id: ID }"
		)
	}


	@Test
	fun `accepts existing field in inline fragment selection`() {
		assertValidationRule(
			rule = FieldSelectionReferencesExistingFieldRule,
			errors = emptyList(),
			document = """
				|{
				|   ... {
				|       id
				|   }
				|}
			""",
			schema = "type Query { id: ID }"
		)
	}


	@Test
	fun `accepts existing field in fragment selection`() {
		assertValidationRule(
			rule = FieldSelectionReferencesExistingFieldRule,
			errors = emptyList(),
			document = """
				|{
				|   ...f
				|}
				|
				|fragment f on Query { id }
			""",
			schema = "type Query { id: ID }"
		)
	}


	@Test
	fun `rejects non-existent field in field selection`() {
		assertValidationRule(
			rule = FieldSelectionReferencesExistingFieldRule,
			errors = listOf("""
				Cannot select non-existent field 'foo' on type 'Query'.

				<document>:1:3
				1 | { foo }
				  |   ^
			"""),
			document = """
				|{ foo }
			""",
			schema = "type Query { id: ID }"
		)
	}


	@Test
	fun `rejects non-existent field in inline fragment selection`() {
		assertValidationRule(
			rule = FieldSelectionReferencesExistingFieldRule,
			errors = listOf("""
				Cannot select non-existent field 'foo' on type 'Query'.

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
			schema = "type Query { id: ID }"
		)
	}


	@Test
	fun `rejects non-existent field in fragment selection`() {
		assertValidationRule(
			rule = FieldSelectionReferencesExistingFieldRule,
			errors = listOf("""
				Cannot select non-existent field 'foo' on type 'Query'.

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
			schema = "type Query { id: ID }"
		)
	}
}
