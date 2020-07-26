package testing

import io.fluidsonic.graphql.*
import kotlin.test.*


class FragmentDefinitionNameExclusivityRuleTest {

	@Test
	fun `accepts unique fragment names`() {
		assertValidationRule(
			rule = FragmentDefinitionNameExclusivityRule,
			errors = emptyList(),
			document = """
				|{
				|  dog {
				|    ...fragmentOne
				|    ...fragmentTwo
				|  }
				|}
				|
				|fragment fragmentOne on Dog {
				|  name
				|}
				|
				|fragment fragmentTwo on Dog {
				|  owner {
				|    name
				|  }
				|}
			""",
			schema = "type Query { id: ID }"
		)
	}


	@Test
	fun `rejects duplicate fragment names`() {
		assertValidationRule(
			rule = FragmentDefinitionNameExclusivityRule,
			errors = listOf("""
				The document must not contain multiple fragments with the same name 'fragmentOne'.

				<document>:7:10
				6 | 
				7 | fragment fragmentOne on Dog {
				  |          ^
				8 |   name

				<document>:11:10
				10 | 
				11 | fragment fragmentOne on Dog {
				   |          ^
				12 |   owner {
			"""),
			document = """
				|{
				|  dog {
				|    ...fragmentOne
				|  }
				|}
				|
				|fragment fragmentOne on Dog {
				|  name
				|}
				|
				|fragment fragmentOne on Dog {
				|  owner {
				|    name
				|  }
				|}
			""",
			schema = "type Query { id: ID }"
		)
	}


	@Test
	fun `reports all problematic fragments`() {
		assertValidationRule(
			rule = FragmentDefinitionNameExclusivityRule,
			errors = listOf(
				"""
					The document must not contain multiple fragments with the same name 'fragmentOne'.

					<document>:7:10
					6 | 
					7 | fragment fragmentOne on Dog {
					  |          ^
					8 |   name

					<document>:11:10
					10 | 
					11 | fragment fragmentOne on Dog {
					   |          ^
					12 |   owner {
				""",
				"""
					The document must not contain multiple fragments with the same name 'fragmentTwo'.

					<document>:17:10
					16 | 
					17 | fragment fragmentTwo on Dog {
					   |          ^
					18 |   name

					<document>:21:10
					20 | 
					21 | fragment fragmentTwo on Dog {
					   |          ^
					22 |   owner {
				"""
			),
			document = """
				|{
				|  dog {
				|    ...fragmentOne
				|  }
				|}
				|
				|fragment fragmentOne on Dog {
				|  name
				|}
				|
				|fragment fragmentOne on Dog {
				|  owner {
				|    name
				|  }
				|}
				|
				|fragment fragmentTwo on Dog {
				|  name
				|}
				|
				|fragment fragmentTwo on Dog {
				|  owner {
				|    name
				|  }
				|}
			""",
			schema = "type Query { id: ID }"
		)
	}
}
