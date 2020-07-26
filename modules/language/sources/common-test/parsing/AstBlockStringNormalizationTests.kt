package testing

import io.fluidsonic.graphql.*
import kotlin.test.*


// https://github.com/graphql/graphql-js/blob/master/src/language/__tests__/blockString-test.js
class AstBlockStringNormalizationTests {

	@Test
	fun testNormalizesValue() {
		val input = """
			|
			|    Hello,
			|      World!
			|
			|    Yours,
			|      GraphQL.
		""".toBlockString()

		val parsed = (GValue.parse(input) as GStringValue).value

		assertEquals(
			expected = listOf("Hello,", "  World!", "", "Yours,", "  GraphQL."),
			actual = parsed.split('\n')
		)
	}


	@Test
	fun testRemovesEmptyLeadingAndTrailingLines() {
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

		val parsed = (GValue.parse(input) as GStringValue).value

		assertEquals(
			expected = listOf("Hello,", "  World!", "", "Yours,", "  GraphQL."),
			actual = parsed.split('\n')
		)
	}


	@Test
	fun testRemovesBlankLeadingAndTrailingLines() {
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

		val parsed = (GValue.parse(input) as GStringValue).value

		assertEquals(
			expected = listOf("Hello,", "  World!", "", "Yours,", "  GraphQL."),
			actual = parsed.split('\n')
		)
	}


	@Test
	fun testRetainsIndentationFromFirstLine() {
		val input = """
			|    Hello,
			|      World!
			|
			|    Yours,
			|      GraphQL.
		""".toBlockString()

		val parsed = (GValue.parse(input) as GStringValue).value

		assertEquals(
			expected = listOf("Hello,", "  World!", "", "Yours,", "  GraphQL."),
			actual = parsed.split('\n')
		)
	}


	@Test
	fun testDoesNotAlterTrailingSpaces() {
		val input = """
			|               
			|    Hello,     
			|      World!   
			|               
			|    Yours,     
			|      GraphQL. 
			|               
		""".toBlockString()

		val parsed = (GValue.parse(input) as GStringValue).value

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
	fun testConsidersMixedTabsAndSpacesAsIndentation() {
		val input = """
			|	
			|	    Hello,
			|	      World!
			|	
			|	    Yours,
			|	      GraphQL.
			|
		""".toBlockString()

		val parsed = (GValue.parse(input) as GStringValue).value

		assertEquals(
			expected = listOf("Hello,", "  World!", "", "Yours,", "  GraphQL."),
			actual = parsed.split('\n')
		)
	}


	@Test
	fun testTrimsLeadingIndentationForSingleLine() {
		val input = """
			|	    Hello
		""".toBlockString()

		val parsed = (GValue.parse(input) as GStringValue).value

		assertEquals(
			expected = listOf("Hello"),
			actual = parsed.split('\n')
		)
	}


	private fun String.toBlockString() =
		"\"\"\"${this.trimMargin()}\"\"\""
}
