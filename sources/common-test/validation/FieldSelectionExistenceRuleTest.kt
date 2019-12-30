package io.fluidsonic.graphql

import tests.*
import kotlin.test.*


class FieldSelectionExistenceRuleTest {

	@Test
	fun `accepts existing field in field selection`() {
		assertValidationRule(
			rule = FieldSelectionExistenceRule,
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
			rule = FieldSelectionExistenceRule,
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
			rule = FieldSelectionExistenceRule,
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
	fun `rejects nonexistent field in field selection`() {
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
			schema = "type Query { id: ID }"
		)
	}


	@Test
	fun `rejects nonexistent field in inline fragment selection`() {
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
			schema = "type Query { id: ID }"
		)
	}


	@Test
	fun `rejects nonexistent field in fragment selection`() {
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
			schema = "type Query { id: ID }"
		)
	}
}
