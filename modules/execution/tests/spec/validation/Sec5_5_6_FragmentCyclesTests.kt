package testing

import io.fluidsonic.graphql.*
import kotlin.test.*


// GraphQL Spec §5.5.6 — Fragment Cycles
class Sec5_5_6_FragmentCyclesTests {

	@Test
	fun testAcceptsFragmentWithNoCycles() {
		assertValidationRule(
			rule = FragmentCycleDetectionRule,
			errors = emptyList(),
			document = """
				|{ dog { ...fragA } }
				|fragment fragA on Dog { name ...fragB }
				|fragment fragB on Dog { name }
			""",
			schema = """
				|type Query { dog: Dog }
				|type Dog { name: String }
			"""
		)
	}


	@Test
	fun testRejectsDirectCycle() {
		assertValidationRule(
			rule = FragmentCycleDetectionRule,
			errors = listOf("""
				Fragment 'fragA' cannot recursively reference itself.

				<document>:2:28
				1 | { dog { ...fragA } }
				2 | fragment fragA on Dog { ...fragA }
				  |                            ^
			"""),
			document = """
				|{ dog { ...fragA } }
				|fragment fragA on Dog { ...fragA }
			""",
			schema = """
				|type Query { dog: Dog }
				|type Dog { name: String }
			"""
		)
	}


	@Test
	fun testRejectsTransitiveCycle() {
		assertValidationRule(
			rule = FragmentCycleDetectionRule,
			errors = listOf("""
					Fragment 'fragA' cannot recursively reference itself through 'fragB'.

					<document>:2:28
					1 | { dog { ...fragA } }
					2 | fragment fragA on Dog { ...fragB }
					  |                            ^
					3 | fragment fragB on Dog { ...fragA }

					<document>:3:28
					2 | fragment fragA on Dog { ...fragB }
					3 | fragment fragB on Dog { ...fragA }
					  |                            ^
				"""),
			document = """
				|{ dog { ...fragA } }
				|fragment fragA on Dog { ...fragB }
				|fragment fragB on Dog { ...fragA }
			""",
			schema = """
				|type Query { dog: Dog }
				|type Dog { name: String }
			"""
		)
	}


	@Test
	fun testRejectsLongCycle() {
		assertValidationRule(
			rule = FragmentCycleDetectionRule,
			errors = listOf("""
					Fragment 'fragA' cannot recursively reference itself through 'fragB' -> 'fragC'.

					<document>:2:28
					1 | { dog { ...fragA } }
					2 | fragment fragA on Dog { ...fragB }
					  |                            ^
					3 | fragment fragB on Dog { ...fragC }

					<document>:3:28
					2 | fragment fragA on Dog { ...fragB }
					3 | fragment fragB on Dog { ...fragC }
					  |                            ^
					4 | fragment fragC on Dog { ...fragA }

					<document>:4:28
					3 | fragment fragB on Dog { ...fragC }
					4 | fragment fragC on Dog { ...fragA }
					  |                            ^
				"""),
			document = """
				|{ dog { ...fragA } }
				|fragment fragA on Dog { ...fragB }
				|fragment fragB on Dog { ...fragC }
				|fragment fragC on Dog { ...fragA }
			""",
			schema = """
				|type Query { dog: Dog }
				|type Dog { name: String }
			"""
		)
	}


	@Test
	fun testAcceptsFragmentUsedMultipleTimesNoCycle() {
		assertValidationRule(
			rule = FragmentCycleDetectionRule,
			errors = emptyList(),
			document = """
				|{ dog { ...fragA ...fragA } }
				|fragment fragA on Dog { name }
			""",
			schema = """
				|type Query { dog: Dog }
				|type Dog { name: String }
			"""
		)
	}
}
