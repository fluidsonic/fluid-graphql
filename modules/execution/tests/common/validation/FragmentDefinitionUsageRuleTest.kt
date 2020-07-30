package testing

import io.fluidsonic.graphql.*
import kotlin.test.*


class FragmentDefinitionUsageRuleTest {

	@Test
	fun testAcceptsFragmentsReferencedByAnOperation() {
		assertValidationRule(
			rule = FragmentDefinitionUsageRule,
			errors = emptyList(),
			document = """
				|{
				|  dog {
				|    ...nameFragment
				|  }
				|}
				|
				|fragment nameFragment on Dog {
				|  ...nameFragment2
				|}
				|
				|fragment nameFragment2 on Dog {
				|  name
				|}
			""",
			schema = """
				|type Query { dog: Dog }
				|type Dog { name: String }
			"""
		)
	}


	@Test
	fun testIgnoresRecursiveReferences() {
		assertValidationRule(
			rule = FragmentDefinitionUsageRule,
			errors = emptyList(),
			document = """
				|{
				|  dog {
				|    ...nameFragment
				|  }
				|}
				|
				|fragment nameFragment on Dog {
				|  ...nameFragment
				|}
			""",
			schema = """
				|type Query { dog: Dog }
				|type Dog { name: String }
			"""
		)
	}


	@Test
	fun testRejectsUnreferencedFragments() {
		assertValidationRule(
			rule = FragmentDefinitionUsageRule,
			errors = listOf(
				"""
					Fragment 'nameFragment' is not used by any operation.

					<document>:7:10
					6 | 
					7 | fragment nameFragment on Dog {
					  |          ^
					8 |   name
				""",
				"""
					Fragment 'nameFragment2' is not used by any operation.

					<document>:11:10
					10 | 
					11 | fragment nameFragment2 on Dog {
					   |          ^
					12 |   name
				"""
			),
			document = """
				|{
				|  dog {
				|    name
				|  }
				|}
				|
				|fragment nameFragment on Dog {
				|  name
				|}
				|
				|fragment nameFragment2 on Dog {
				|  name
				|}
			""",
			schema = """
				|type Query { dog: Dog }
				|type Dog { name: String }
			"""
		)
	}
}
