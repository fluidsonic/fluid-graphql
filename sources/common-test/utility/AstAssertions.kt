package tests

import io.fluidsonic.graphql.*
import kotlin.test.*


// expected to not throw
fun assertAst(actual: String) =
	GAst.parseDocument(makeSource(actual.trimMargin()))


// Note that "start .. end" origin notations use an exclusive end rather than an inclusive for sake of readability
fun assertAst(actual: String, expected: AstBuilder.() -> GAst) {
	assertEquals(
		expected = ast(expected),
		actual = GAst.parseDocument(makeSource(actual.trimMargin()))
	)
}


inline fun <T : GAst> List<T>.assertMany(count: Int, block: List<T>.() -> Unit) {
	assertEquals(expected = count, actual = size)

	block()
}


inline fun <T : GAst> List<T>.assertOne(block: T.() -> Unit) =
	assertMany(1) {
		single().apply(block)
	}


inline fun <reified T : GAst> GAst.assertClass(block: T.() -> Unit) {
	assertTrue(this is T, "Expected '$this' to be of ${T::class}")

	block(this)
}


fun GAst.Definition.TypeSystem.Type.assertName(name: String, range: IntRange) {
	assertEquals(expected = name, actual = this.name.value)
	this.name.assertAt(range)
}


fun assertSyntaxError(
	content: String,
	message: String,
	line: Int,
	column: Int
) {
	val error = assertFailsWith<GError> { GAst.parseDocument(content.trimMargin()) }
	assertEquals(expected = message, actual = error.message)
	assertEquals(expected = line, actual = error.origins.first().line, message = "incorrect line")
	assertEquals(expected = column, actual = error.origins.first().column, message = "incorrect column")
}


private fun makeSource(content: String) =
	object : GSource.Parsable {

		override val content = content
		override val name = "<test>"


		override fun makeOrigin(startPosition: Int, endPosition: Int, column: Int, line: Int) =
			AstBuilder.Origin(
				startPosition = startPosition,
				endPosition = endPosition,
				line = line,
				column = column
			)
	}


////////////

fun GAst.assertAt(range: IntRange) =
	assertEquals(expected = origin.startPosition .. origin.endPosition, actual = range)


fun GAst.Definition.asOperation() =
	this as GAst.Definition.Operation


inline fun <T : GAst> T?.assert(block: T.() -> Unit) {
	assertNotNull(this)

	block()
}


inline fun <reified T : GAst> GAst?.assertOf(block: T.() -> Unit) {
	assertNotNull(this)
	assertTrue { this is T }

	(this as T).block()
}


inline fun <reified T : GAst> List<*>.assertOneOf(block: T.() -> Unit) {
	assertEquals(expected = 1, actual = size)

	val single = single()
	assertTrue { single is T }

	(single as T).block()
}


fun GAst.Selection.asFieldSelection() =
	this as GAst.Selection.Field


fun GAst.Value.asString() =
	this as GAst.Value.String
