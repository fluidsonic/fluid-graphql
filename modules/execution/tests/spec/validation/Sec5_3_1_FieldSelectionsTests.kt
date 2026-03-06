package testing

import io.fluidsonic.graphql.*
import kotlin.test.*


// GraphQL Spec §5.3.1 — Field Selections
class Sec5_3_1_FieldSelectionsTests {

	@Test
	fun testAcceptsFieldOnObject() {
		assertValidationRule(
			rule = FieldSelectionExistenceRule,
			errors = emptyList(),
			document = """
				|{ name }
			""",
			schema = """
				|type Query { name: String }
			"""
		)
	}


	@Test
	fun testAcceptsFieldOnInterface() {
		assertValidationRule(
			rule = FieldSelectionExistenceRule,
			errors = emptyList(),
			document = """
				|{ pet { name } }
			""",
			schema = """
				|type Query { pet: Pet }
				|interface Pet { name: String }
				|type Dog implements Pet { name: String }
			"""
		)
	}


	@Test
	fun testAcceptsTypenameOnObject() {
		assertValidationRule(
			rule = FieldSelectionExistenceRule,
			errors = emptyList(),
			document = """
				|{ __typename }
			""",
			schema = """
				|type Query { name: String }
			"""
		)
	}


	@Test
	fun testAcceptsTypenameOnInterface() {
		assertValidationRule(
			rule = FieldSelectionExistenceRule,
			errors = emptyList(),
			document = """
				|{ pet { __typename } }
			""",
			schema = """
				|type Query { pet: Pet }
				|interface Pet { name: String }
				|type Dog implements Pet { name: String }
			"""
		)
	}


	@Test
	fun testAcceptsTypenameOnUnion() {
		assertValidationRule(
			rule = FieldSelectionExistenceRule,
			errors = emptyList(),
			document = """
				|{ catOrDog { __typename } }
			""",
			schema = """
				|type Query { catOrDog: CatOrDog }
				|type Cat { name: String }
				|type Dog { name: String }
				|union CatOrDog = Cat | Dog
			"""
		)
	}


	@Test
	fun testRejectsFieldOnObject() {
		assertValidationRule(
			rule = FieldSelectionExistenceRule,
			errors = listOf("""
				Cannot select nonexistent field 'unknownField' on type 'Query'.

				<document>:1:3
				1 | { unknownField }
				  |   ^
			"""),
			document = """
				|{ unknownField }
			""",
			schema = """
				|type Query { name: String }
			"""
		)
	}


	@Test
	fun testRejectsFieldOnInterface() {
		assertValidationRule(
			rule = FieldSelectionExistenceRule,
			errors = listOf("""
				Cannot select nonexistent field 'unknownField' on type 'Pet'.

				<document>:1:9
				1 | { pet { unknownField } }
				  |         ^
			"""),
			document = """
				|{ pet { unknownField } }
			""",
			schema = """
				|type Query { pet: Pet }
				|interface Pet { name: String }
				|type Dog implements Pet { name: String }
			"""
		)
	}


	@Test
	fun testAcceptsInlineFragmentField() {
		assertValidationRule(
			rule = FieldSelectionExistenceRule,
			errors = emptyList(),
			document = """
				|{ catOrDog { ... on Dog { name } } }
			""",
			schema = """
				|type Query { catOrDog: CatOrDog }
				|type Cat { name: String }
				|type Dog { name: String }
				|union CatOrDog = Cat | Dog
			"""
		)
	}


	@Test
	fun testRejectsInlineFragmentUnknownField() {
		assertValidationRule(
			rule = FieldSelectionExistenceRule,
			errors = listOf("""
				Cannot select nonexistent field 'unknown' on type 'Dog'.

				<document>:1:27
				1 | { catOrDog { ... on Dog { unknown } } }
				  |                           ^
			"""),
			document = """
				|{ catOrDog { ... on Dog { unknown } } }
			""",
			schema = """
				|type Query { catOrDog: CatOrDog }
				|type Cat { name: String }
				|type Dog { name: String }
				|union CatOrDog = Cat | Dog
			"""
		)
	}
}
