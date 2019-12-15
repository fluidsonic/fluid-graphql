package tests

import io.fluidsonic.graphql.*
import io.fluidsonic.graphql.GAst.*
import kotlin.test.*


// https://github.com/graphql/graphql-js/blob/master/src/language/__tests__/blockString-test.js
class AstBlockStringNormalizationTest {

	@Test
	fun `normalizes value`() {
		val input = """
			|
			|    Hello,
			|      World!
			|
			|    Yours,
			|      GraphQL.
		""".toBlockString()

		val parsed = (GAst.parseValue(input) as Value.String).value

		assertEquals(
			expected = listOf("Hello,", "  World!", "", "Yours,", "  GraphQL."),
			actual = parsed.split('\n')
		)
	}


	@Test
	fun `removes empty leading and trailing lines`() {
		val input = """
			|
			|
			|    Hello,
			|      World!
			|
			|    Yours,
			|      GraphQL.
			|
			|
		""".toBlockString()

		val parsed = (GAst.parseValue(input) as Value.String).value

		assertEquals(
			expected = listOf("Hello,", "  World!", "", "Yours,", "  GraphQL."),
			actual = parsed.split('\n')
		)
	}


	@Test
	fun `removes blank leading and trailing lines`() {
		val input = """
			|  
			|        
			|    Hello,
			|      World!
			|
			|    Yours,
			|      GraphQL.
			|        
			|  
		""".toBlockString()

		val parsed = (GAst.parseValue(input) as Value.String).value

		assertEquals(
			expected = listOf("Hello,", "  World!", "", "Yours,", "  GraphQL."),
			actual = parsed.split('\n')
		)
	}


	@Test
	fun `retains indentation from first line`() {
		val input = """
			|    Hello,
			|      World!
			|
			|    Yours,
			|      GraphQL.
		""".toBlockString()

		val parsed = (GAst.parseValue(input) as Value.String).value

		assertEquals(
			expected = listOf("Hello,", "  World!", "", "Yours,", "  GraphQL."),
			actual = parsed.split('\n')
		)
	}


	@Test
	fun `does not alter trailing spaces`() {
		val input = """
			|               
			|    Hello,     
			|      World!   
			|               
			|    Yours,     
			|      GraphQL. 
			|               
		""".toBlockString()

		val parsed = (GAst.parseValue(input) as Value.String).value

		assertEquals(
			expected = listOf(
				"Hello,     ",
				"  World!   ",
				"           ",
				"Yours,     ",
				"  GraphQL. "
			),
			actual = parsed.split('\n')
		)
	}


	@Test
	fun `considers mixed tabs and spaces as indentation`() {
		val input = """
			|	
			|	    Hello,
			|	      World!
			|	
			|	    Yours,
			|	      GraphQL.
			|
		""".toBlockString()

		val parsed = (GAst.parseValue(input) as Value.String).value

		assertEquals(
			expected = listOf("Hello,", "  World!", "", "Yours,", "  GraphQL."),
			actual = parsed.split('\n')
		)
	}


	@Test
	fun `trims leading indentation for single line`() {
		val input = """
			|	    Hello
		""".toBlockString()

		val parsed = (GAst.parseValue(input) as Value.String).value

		assertEquals(
			expected = listOf("Hello"),
			actual = parsed.split('\n')
		)
	}


	private fun String.toBlockString() =
		"\"\"\"${this.trimMargin()}\"\"\""
}
