package testing

import io.fluidsonic.graphql.*
import kotlin.test.*
import kotlinx.coroutines.test.*

class IntrospectionValidationTests {

	@Test
	fun testFragments() = runTest {
		val document = """
			|{
			|  __schema {
			|    ...schemaFragment
			|  }
			|}
			|
			|fragment directiveFragment on __Directive {
			|  name
			|}
			|fragment enumValueFragment on __EnumValue {
			|  name
			|}
			|fragment fieldFragment on __Field {
			|  name
			|}
			|fragment inputValueFragment on __InputValue {
			|  name
			|}
			|fragment schemaFragment on __Schema {
			|  directives {
			|    ...directiveFragment
			|  }
			|  types {
			|    ...typeFragment
			|  }
			|}
			|fragment typeFragment on __Type {
			|  enumValues {
			|    ...enumValueFragment
			|  }
			|  fields {
			|    ...fieldFragment
			|  }
			|  inputFields {
			|    ...inputValueFragment
			|  }
			|  name
			|}
		""".trimMargin()

		val errors = GDocument.parse(document).valueOrThrow().validate(schema)
		assertEquals(expected = emptyList(), actual = errors)
	}


	companion object {

		private val schema = GraphQL.schema {
			Query {}
		}
	}
}
