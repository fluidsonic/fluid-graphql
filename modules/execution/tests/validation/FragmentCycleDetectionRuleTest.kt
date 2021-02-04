package testing

import io.fluidsonic.graphql.*
import kotlin.test.*


class FragmentCycleDetectionRuleTest {

	@Test
	fun testAcceptsNoncyclicFragmentReferences() {
		assertValidationRule(
			rule = FragmentCycleDetectionRule,
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
				|  ...fragmentTwo
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
	fun testRejectsCyclicFragmentReferences() {
		assertValidationRule(
			rule = FragmentCycleDetectionRule,
			errors = listOf(
				"""
					Fragment 'nameFragment' cannot recursively reference itself through 'barkVolumeFragment'.

					<document>:9:6
					 8 |   name
					 9 |   ...barkVolumeFragment
					   |      ^
					10 |   ...nameFragment

					<document>:15:6
					14 |   barkVolume
					15 |   ...nameFragment
					   |      ^
					16 |   ... {
				""",
				"""
					Fragment 'nameFragment' cannot recursively reference itself through 'barkVolumeFragment' -> 'mooFragment'.

					<document>:9:6
					 8 |   name
					 9 |   ...barkVolumeFragment
					   |      ^
					10 |   ...nameFragment

					<document>:17:8
					16 |   ... {
					17 |     ...mooFragment
					   |        ^
					18 |   }

					<document>:23:6
					22 |   doesMoo
					23 |   ...nameFragment
					   |      ^
					24 | }
				""",
				"""
					Fragment 'nameFragment' cannot recursively reference itself.

					<document>:10:6
					 9 |   ...barkVolumeFragment
					10 |   ...nameFragment
					   |      ^
					11 | }
				"""
			),
			document = """
				|{
				|  dog {
				|    ...nameFragment
				|  }
				|}
				|
				|fragment nameFragment on Dog {
				|  name
				|  ...barkVolumeFragment
				|  ...nameFragment
				|}
				|
				|fragment barkVolumeFragment on Dog {
				|  barkVolume
				|  ...nameFragment
				|  ... {
				|    ...mooFragment
				|  }
				|}
				|
				|fragment mooFragment on Dog {
				|  doesMoo
				|  ...nameFragment
				|}
			""",
			schema = "type Query { id: ID }"
		)
	}
}
