/*
 * Characterisation tests for the VALUE VALIDITY table.
 *
 * Mirrors `GSchema.validateValue` (modules/language/sources/model/GSchema.kt). Every cell records what
 * the library does TODAY, captured by running it — not what the GraphQL specification requires.
 *
 * A changed cell is a BEHAVIOURAL CHANGE. Investigate it and decide deliberately whether the new
 * behaviour is wanted; never re-paste an actual value to make the test green again.
 *
 * This is the table the document validator consults, so it decides which literals a client's document
 * survives before execution begins. Compare `LiteralInputCoercionMatrixTests`, which characterises what
 * the literal *coercer* does once validation is skipped — for the built-in scalars and the enum the two
 * accept exactly the same literals today.
 *
 * Notable characterised behaviour:
 *   - A built-in scalar rejects a literal with the wording of its own coercion, because `validateValue`
 *     delegates to it instead of keeping a second table. The enum and the missing-field cases keep the
 *     generic `Type 'X' does not allow …` wording, which only `validateValue` itself produces.
 *   - A custom scalar carrying no coercer accepts EVERY literal, including a list and an input object: it
 *     defines no literal coercion, and the value coercion it falls back to is the identity.
 *   - `null` is valid for every nullable type, including the enum.
 *   - A rejected input object literal is quoted the way the printer writes it, which is across several
 *     lines — graphql-js prints it inline instead.
 */
package testing

import io.fluidsonic.graphql.GSchema
import io.fluidsonic.graphql.GTypeRef
import io.fluidsonic.graphql.GValue
import io.fluidsonic.graphql.GraphQL
import io.fluidsonic.graphql.resolve
import io.fluidsonic.graphql.schema
import kotlin.test.Test
import kotlin.test.assertEquals

private const val ENUM_TYPE = "Color"
private const val CUSTOM_TYPE = "Custom"

private val typeNames = listOf("Boolean", "Float", "ID", "Int", "String", ENUM_TYPE, CUSTOM_TYPE)

/** Every GraphQL literal fed to the validity table, parsed with `GValue.parse`. */
private val literals = listOf(
	"true",
	"1",
	"-1",
	"1.5",
	"\"s\"",
	"\"1\"",
	"RED",
	"NOPE",
	"null",
	"[1]",
	"{a: 1}",
)

/** The full wording of the `reportError` default message in `GSchema.validateValue`. */
private fun invalidValue(typeName: String, valueText: String): String = "INVALID: Type '$typeName' does not allow $valueText."

/** How the printer writes the `{a: 1}` literal, and therefore how a scalar quotes it back. */
private const val PRINTED_OBJECT = "{\n\ta: 1\n}"

private fun booleanRejects(valueText: String): String = "INVALID: Boolean cannot represent a non boolean value: $valueText"
private fun floatRejects(valueText: String): String = "INVALID: Float cannot represent non numeric value: $valueText"
private fun idRejects(valueText: String): String = "INVALID: ID cannot represent a non-string and non-integer value: $valueText"
private fun intRejects(valueText: String): String = "INVALID: Int cannot represent non-integer value: $valueText"
private fun stringRejects(valueText: String): String = "INVALID: String cannot represent a non string value: $valueText"

private val schema: GSchema = GraphQL.schema {
	Enum(GTypeRef(ENUM_TYPE)) {
		value("RED")
		value("GREEN")
		value("BLUE")
	}
	Scalar(GTypeRef(CUSTOM_TYPE))

	Query {
		field("boolean" of Boolean) { resolve { true } }
		field("float" of Float) { resolve { 1.0 } }
		field("id" of ID) { resolve { "1" } }
		field("int" of Int) { resolve { 1 } }
		field("string" of String) { resolve { "1" } }
		field("color" of GTypeRef(ENUM_TYPE)) { resolve { "RED" } }
		field("custom" of GTypeRef(CUSTOM_TYPE)) { resolve { "x" } }
	}
}

private fun validityCell(typeName: String, literal: String): String {
	val errors = schema.validateValue(value = GValue.parse(literal).valueOrThrow(), typeRef = GTypeRef(typeName))

	return when {
		errors.isEmpty() -> "valid"
		else -> "INVALID: ${errors.joinToString(separator = " | ") { it.message }}"
	}
}

private val expectedBooleanCells = mapOf(
	"Boolean <- true" to "valid",
	"Boolean <- 1" to booleanRejects("1"),
	"Boolean <- -1" to booleanRejects("-1"),
	"Boolean <- 1.5" to booleanRejects("1.5"),
	"Boolean <- \"s\"" to booleanRejects("\"s\""),
	"Boolean <- \"1\"" to booleanRejects("\"1\""),
	"Boolean <- RED" to booleanRejects("RED"),
	"Boolean <- NOPE" to booleanRejects("NOPE"),
	"Boolean <- null" to "valid",
	"Boolean <- [1]" to booleanRejects("[1]"),
	"Boolean <- {a: 1}" to booleanRejects(PRINTED_OBJECT),
)

private val expectedFloatCells = mapOf(
	"Float <- true" to floatRejects("true"),
	"Float <- 1" to "valid",
	"Float <- -1" to "valid",
	"Float <- 1.5" to "valid",
	"Float <- \"s\"" to floatRejects("\"s\""),
	"Float <- \"1\"" to floatRejects("\"1\""),
	"Float <- RED" to floatRejects("RED"),
	"Float <- NOPE" to floatRejects("NOPE"),
	"Float <- null" to "valid",
	"Float <- [1]" to floatRejects("[1]"),
	"Float <- {a: 1}" to floatRejects(PRINTED_OBJECT),
)

private val expectedIdCells = mapOf(
	"ID <- true" to idRejects("true"),
	"ID <- 1" to "valid",
	"ID <- -1" to "valid",
	"ID <- 1.5" to idRejects("1.5"),
	"ID <- \"s\"" to "valid",
	"ID <- \"1\"" to "valid",
	"ID <- RED" to idRejects("RED"),
	"ID <- NOPE" to idRejects("NOPE"),
	"ID <- null" to "valid",
	"ID <- [1]" to idRejects("[1]"),
	"ID <- {a: 1}" to idRejects(PRINTED_OBJECT),
)

private val expectedIntCells = mapOf(
	"Int <- true" to intRejects("true"),
	"Int <- 1" to "valid",
	"Int <- -1" to "valid",
	"Int <- 1.5" to intRejects("1.5"),
	// The output table accepts a String for `Int`; the validator does not.
	"Int <- \"s\"" to intRejects("\"s\""),
	"Int <- \"1\"" to intRejects("\"1\""),
	"Int <- RED" to intRejects("RED"),
	"Int <- NOPE" to intRejects("NOPE"),
	"Int <- null" to "valid",
	"Int <- [1]" to intRejects("[1]"),
	"Int <- {a: 1}" to intRejects(PRINTED_OBJECT),
)

private val expectedStringCells = mapOf(
	"String <- true" to stringRejects("true"),
	"String <- 1" to stringRejects("1"),
	"String <- -1" to stringRejects("-1"),
	"String <- 1.5" to stringRejects("1.5"),
	"String <- \"s\"" to "valid",
	"String <- \"1\"" to "valid",
	"String <- RED" to stringRejects("RED"),
	"String <- NOPE" to stringRejects("NOPE"),
	"String <- null" to "valid",
	"String <- [1]" to stringRejects("[1]"),
	"String <- {a: 1}" to stringRejects(PRINTED_OBJECT),
)

private val expectedEnumCells = mapOf(
	"Color <- true" to invalidValue("Color", "value 'true'"),
	"Color <- 1" to invalidValue("Color", "value '1'"),
	"Color <- -1" to invalidValue("Color", "value '-1'"),
	"Color <- 1.5" to invalidValue("Color", "value '1.5'"),
	"Color <- \"s\"" to invalidValue("Color", "value '\"s\"'"),
	"Color <- \"1\"" to invalidValue("Color", "value '\"1\"'"),
	"Color <- RED" to "valid",
	"Color <- NOPE" to invalidValue("Color", "value 'NOPE'"),
	"Color <- null" to "valid",
	"Color <- [1]" to invalidValue("Color", "a list value"),
	"Color <- {a: 1}" to invalidValue("Color", "an input object value"),
)

/** A custom scalar without an attached coercer admits every literal: it defines no literal coercion. */
private val expectedCustomScalarCells = mapOf(
	"Custom <- true" to "valid",
	"Custom <- 1" to "valid",
	"Custom <- -1" to "valid",
	"Custom <- 1.5" to "valid",
	"Custom <- \"s\"" to "valid",
	"Custom <- \"1\"" to "valid",
	"Custom <- RED" to "valid",
	"Custom <- NOPE" to "valid",
	"Custom <- null" to "valid",
	"Custom <- [1]" to "valid",
	"Custom <- {a: 1}" to "valid",
)

class ValueValidityMatrixTests {

	@Test
	fun testValueValidityMatrix() {
		val actual = buildMap {
			for (typeName in typeNames) {
				for (literal in literals) {
					put("$typeName <- $literal", validityCell(typeName = typeName, literal = literal))
				}
			}
		}

		assertEquals(
			actual = actual,
			expected = expectedBooleanCells + expectedFloatCells + expectedIdCells + expectedIntCells +
				expectedStringCells + expectedEnumCells + expectedCustomScalarCells,
		)
	}
}
