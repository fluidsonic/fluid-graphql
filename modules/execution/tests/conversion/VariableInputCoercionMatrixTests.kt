/*
 * Characterisation tests for the VARIABLE INPUT coercion table.
 *
 * Mirrors `GScalarType.coerceInputValue` on the five built-in scalars
 * (modules/language/sources/model/nodes/BuiltinScalarTypes.kt), reached through
 * `VariableInputConverter.coerceValueForScalar`, plus `VariableInputConverter.coerceValueForEnum`. Every
 * cell records what the library does TODAY, captured by running it — not what the GraphQL specification
 * requires.
 *
 * A changed cell is a BEHAVIOURAL CHANGE. Investigate it and decide deliberately whether the new
 * behaviour is wanted; never re-paste an actual value to make the test green again.
 *
 * Variable coercion runs before execution begins, so every failure here is a REQUEST error: the "data"
 * key is absent from the serialized result. Contrast the literal-input table, whose failures are field
 * errors. The `echo` resolver returns a *rendering* of the coerced argument so that the value never
 * passes back through output coercion.
 *
 * Notable characterised behaviour:
 *   - The input direction is deliberately stricter than the output direction, mirroring graphql-js: a
 *     `Boolean` and a numeric `String` are rejected for `Int` and `Float`, a number is rejected for
 *     `Boolean` and `String`. `OutputCoercionMatrixTests` accepts all of those.
 *   - `Int` and `ID` accept an integral `Double`; a fractional or non-finite one is rejected.
 *   - Non-finite `Double`s are rejected by every built-in scalar.
 *   - A `Boolean` input value produces the generic "Value is not valid …" wording, because
 *     `makeInvalidValueError` has no `is Boolean` arm; every other Kotlin type is named.
 *   - The `Custom` scalar without a coercer is an unvalidated pass-through.
 *   - The scalar contributes only the bare parenthetical; the surrounding wording comes from
 *     `VariableInputConverter.Context.makeInvalidValueError`.
 */
package testing

import io.fluidsonic.graphql.GExecutor
import io.fluidsonic.graphql.GSchema
import io.fluidsonic.graphql.GTypeRef
import io.fluidsonic.graphql.GraphQL
import io.fluidsonic.graphql.arguments
import io.fluidsonic.graphql.resolve
import io.fluidsonic.graphql.schema
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

private const val ENUM_TYPE = "Color"
private const val CUSTOM_TYPE = "Custom"

/** Every Kotlin value fed to the variable table, labelled for the matrix key. */
private val kotlinValues: List<Pair<String, Any>> = listOf(
	"Boolean true" to true,
	"Boolean false" to false,
	"Byte 1" to 1.toByte(),
	"Short 1" to 1.toShort(),
	"Int 1" to 1,
	"Int -1" to -1,
	"Long 1L" to 1L,
	"Long Long.MAX_VALUE" to Long.MAX_VALUE,
	"Float 1.5f" to 1.5f,
	"Double 1.5" to 1.5,
	"Double 1.0" to 1.0,
	"Double NaN" to Double.NaN,
	"Double POSITIVE_INFINITY" to Double.POSITIVE_INFINITY,
	"Double NEGATIVE_INFINITY" to Double.NEGATIVE_INFINITY,
	"UByte 1u" to 1.toUByte(),
	"UShort 1u" to 1.toUShort(),
	"UInt 1u" to 1u,
	"ULong 1uL" to 1uL,
	"ULong ULong.MAX_VALUE" to ULong.MAX_VALUE,
	"String '1'" to "1",
	"String 'abc'" to "abc",
	"String ''" to "",
	"List listOf(1)" to listOf(1),
	"Map mapOf('a' to 1)" to mapOf("a" to 1),
	"String 'RED'" to "RED",
)

/** Renders a coerced argument value as `<simple class name>:<toString>`, or `null`. */
private fun render(value: Any?): String = when (value) {
	null -> "null"
	is List<*> -> "List:$value"
	is Map<*, *> -> "Map:$value"
	else -> "${value::class.simpleName}:$value"
}

/**
 * The full wording of `VariableInputConverter.Context.makeInvalidValueError`.
 *
 * [valueKind] is the leading phrase it derives from the Kotlin value — `"Byte value"`, `"Map value"`, …,
 * or the bare `"Value"` for any type it does not name. [details] is the bare message the scalar threw.
 */
private fun variableError(valueKind: String, typeName: String, details: String): String =
	"REQUEST-ERROR: $valueKind is not valid for variable 'v' with type '$typeName' ($details)."

/** Same wording, with the valid-values detail `coerceValueForEnum` supplies instead of a scalar message. */
private fun enumVariableError(valueKind: String): String = "REQUEST-ERROR: $valueKind is not valid for variable 'v' with type '$ENUM_TYPE' " +
	"(valid values: BLUE, GREEN, RED)."

private fun echoSchema(typeName: String): GSchema = GraphQL.schema {
	Enum(GTypeRef(ENUM_TYPE)) {
		value("RED")
		value("GREEN")
		value("BLUE")
	}
	Scalar(GTypeRef(CUSTOM_TYPE))

	Query {
		field("echo" of String) {
			argument("value" of GTypeRef(typeName))
			resolve { render(arguments["value"]) }
		}
	}
}

@Suppress("UNCHECKED_CAST")
private suspend fun variableCell(typeName: String, value: Any): String {
	val executor = GExecutor.default(schema = echoSchema(typeName))
	val result = executor.serializeResult(
		executor.execute(
			"query(${'$'}v: $typeName) { echo(value: ${'$'}v) }",
			variableValues = mapOf("v" to value),
		),
	)
	val message = (result["errors"] as List<Map<String, Any?>>?)?.firstOrNull()?.get("message") as String?

	return when {
		!result.containsKey("data") -> "REQUEST-ERROR: $message"
		message !== null -> "ERROR: $message"
		else -> (result["data"] as Map<String, Any?>?)?.get("echo") as String? ?: "null"
	}
}

private suspend fun variableMatrix(typeNames: List<String>): Map<String, String> = buildMap {
	for (typeName in typeNames) {
		for ((label, value) in kotlinValues) {
			put("$typeName <- $label", variableCell(typeName = typeName, value = value))
		}
	}
}

private fun notBoolean(value: String) = "Boolean cannot represent a non boolean value: $value"

private fun notNumeric(value: String) = "Float cannot represent non numeric value: $value"

private fun notId(value: String) = "ID cannot represent value: $value"

private fun notInteger(value: String) = "Int cannot represent non-integer value: $value"

private fun notInRange(value: String) = "Int cannot represent non 32-bit signed integer value: $value"

private fun notString(value: String) = "String cannot represent a non string value: $value"

private val expectedBooleanCells = mapOf(
	"Boolean <- Boolean true" to "Boolean:true",
	"Boolean <- Boolean false" to "Boolean:false",
	"Boolean <- Byte 1" to variableError("Byte value", "Boolean", notBoolean("1")),
	"Boolean <- Short 1" to variableError("Short value", "Boolean", notBoolean("1")),
	"Boolean <- Int 1" to variableError("Int value", "Boolean", notBoolean("1")),
	"Boolean <- Int -1" to variableError("Int value", "Boolean", notBoolean("-1")),
	"Boolean <- Long 1L" to variableError("Long value", "Boolean", notBoolean("1")),
	"Boolean <- Long Long.MAX_VALUE" to variableError("Long value", "Boolean", notBoolean("9223372036854775807")),
	"Boolean <- Float 1.5f" to variableError("Float value", "Boolean", notBoolean("1.5")),
	"Boolean <- Double 1.5" to variableError("Double value", "Boolean", notBoolean("1.5")),
	"Boolean <- Double 1.0" to variableError("Double value", "Boolean", notBoolean("1.0")),
	"Boolean <- Double NaN" to variableError("Double value", "Boolean", notBoolean("NaN")),
	"Boolean <- Double POSITIVE_INFINITY" to variableError("Double value", "Boolean", notBoolean("Infinity")),
	"Boolean <- Double NEGATIVE_INFINITY" to variableError("Double value", "Boolean", notBoolean("-Infinity")),
	"Boolean <- UByte 1u" to variableError("UByte value", "Boolean", notBoolean("1")),
	"Boolean <- UShort 1u" to variableError("UShort value", "Boolean", notBoolean("1")),
	"Boolean <- UInt 1u" to variableError("UInt value", "Boolean", notBoolean("1")),
	"Boolean <- ULong 1uL" to variableError("ULong value", "Boolean", notBoolean("1")),
	"Boolean <- ULong ULong.MAX_VALUE" to variableError("ULong value", "Boolean", notBoolean("18446744073709551615")),
	"Boolean <- String '1'" to variableError("String value", "Boolean", notBoolean("\"1\"")),
	"Boolean <- String 'abc'" to variableError("String value", "Boolean", notBoolean("\"abc\"")),
	"Boolean <- String ''" to variableError("String value", "Boolean", notBoolean("\"\"")),
	"Boolean <- List listOf(1)" to variableError("List value", "Boolean", notBoolean("[1]")),
	"Boolean <- Map mapOf('a' to 1)" to variableError("Map value", "Boolean", notBoolean("{a=1}")),
	"Boolean <- String 'RED'" to variableError("String value", "Boolean", notBoolean("\"RED\"")),
)

private val expectedFloatCells = mapOf(
	// "Value" rather than "Boolean value": `makeInvalidValueError` never names Boolean.
	"Float <- Boolean true" to variableError("Value", "Float", notNumeric("true")),
	"Float <- Boolean false" to variableError("Value", "Float", notNumeric("false")),
	"Float <- Byte 1" to "Double:1.0",
	"Float <- Short 1" to "Double:1.0",
	"Float <- Int 1" to "Double:1.0",
	"Float <- Int -1" to "Double:-1.0",
	"Float <- Long 1L" to "Double:1.0",
	"Float <- Long Long.MAX_VALUE" to "Double:9.223372036854776E18",
	"Float <- Float 1.5f" to "Double:1.5",
	"Float <- Double 1.5" to "Double:1.5",
	"Float <- Double 1.0" to "Double:1.0",
	"Float <- Double NaN" to variableError("Double value", "Float", notNumeric("NaN")),
	"Float <- Double POSITIVE_INFINITY" to variableError("Double value", "Float", notNumeric("Infinity")),
	"Float <- Double NEGATIVE_INFINITY" to variableError("Double value", "Float", notNumeric("-Infinity")),
	"Float <- UByte 1u" to "Double:1.0",
	"Float <- UShort 1u" to "Double:1.0",
	"Float <- UInt 1u" to "Double:1.0",
	"Float <- ULong 1uL" to "Double:1.0",
	"Float <- ULong ULong.MAX_VALUE" to "Double:1.8446744073709552E19",
	// Unlike the output table, a numeric String is NOT parsed here.
	"Float <- String '1'" to variableError("String value", "Float", notNumeric("\"1\"")),
	"Float <- String 'abc'" to variableError("String value", "Float", notNumeric("\"abc\"")),
	"Float <- String ''" to variableError("String value", "Float", notNumeric("\"\"")),
	"Float <- List listOf(1)" to variableError("List value", "Float", notNumeric("[1]")),
	"Float <- Map mapOf('a' to 1)" to variableError("Map value", "Float", notNumeric("{a=1}")),
	"Float <- String 'RED'" to variableError("String value", "Float", notNumeric("\"RED\"")),
)

private val expectedIdCells = mapOf(
	"ID <- Boolean true" to variableError("Value", "ID", notId("true")),
	"ID <- Boolean false" to variableError("Value", "ID", notId("false")),
	"ID <- Byte 1" to "String:1",
	"ID <- Short 1" to "String:1",
	"ID <- Int 1" to "String:1",
	"ID <- Int -1" to "String:-1",
	"ID <- Long 1L" to "String:1",
	"ID <- Long Long.MAX_VALUE" to "String:9223372036854775807",
	"ID <- Float 1.5f" to variableError("Float value", "ID", notId("1.5")),
	"ID <- Double 1.5" to variableError("Double value", "ID", notId("1.5")),
	"ID <- Double 1.0" to "String:1",
	"ID <- Double NaN" to variableError("Double value", "ID", notId("NaN")),
	"ID <- Double POSITIVE_INFINITY" to variableError("Double value", "ID", notId("Infinity")),
	"ID <- Double NEGATIVE_INFINITY" to variableError("Double value", "ID", notId("-Infinity")),
	"ID <- UByte 1u" to "String:1",
	"ID <- UShort 1u" to "String:1",
	"ID <- UInt 1u" to "String:1",
	"ID <- ULong 1uL" to "String:1",
	"ID <- ULong ULong.MAX_VALUE" to "String:18446744073709551615",
	"ID <- String '1'" to "String:1",
	"ID <- String 'abc'" to "String:abc",
	"ID <- String ''" to "String:",
	"ID <- List listOf(1)" to variableError("List value", "ID", notId("[1]")),
	"ID <- Map mapOf('a' to 1)" to variableError("Map value", "ID", notId("{a=1}")),
	"ID <- String 'RED'" to "String:RED",
)

private val expectedIntCells = mapOf(
	"Int <- Boolean true" to variableError("Value", "Int", notInteger("true")),
	"Int <- Boolean false" to variableError("Value", "Int", notInteger("false")),
	"Int <- Byte 1" to "Int:1",
	"Int <- Short 1" to "Int:1",
	"Int <- Int 1" to "Int:1",
	"Int <- Int -1" to "Int:-1",
	"Int <- Long 1L" to "Int:1",
	"Int <- Long Long.MAX_VALUE" to variableError("Long value", "Int", notInRange("9223372036854775807")),
	"Int <- Float 1.5f" to variableError("Float value", "Int", notInteger("1.5")),
	"Int <- Double 1.5" to variableError("Double value", "Int", notInteger("1.5")),
	"Int <- Double 1.0" to "Int:1",
	"Int <- Double NaN" to variableError("Double value", "Int", notInteger("NaN")),
	"Int <- Double POSITIVE_INFINITY" to variableError("Double value", "Int", notInteger("Infinity")),
	"Int <- Double NEGATIVE_INFINITY" to variableError("Double value", "Int", notInteger("-Infinity")),
	"Int <- UByte 1u" to "Int:1",
	"Int <- UShort 1u" to "Int:1",
	"Int <- UInt 1u" to "Int:1",
	"Int <- ULong 1uL" to "Int:1",
	"Int <- ULong ULong.MAX_VALUE" to variableError("ULong value", "Int", notInRange("18446744073709551615")),
	// Unlike the output table, a numeric String is NOT parsed here.
	"Int <- String '1'" to variableError("String value", "Int", notInteger("\"1\"")),
	"Int <- String 'abc'" to variableError("String value", "Int", notInteger("\"abc\"")),
	"Int <- String ''" to variableError("String value", "Int", notInteger("\"\"")),
	"Int <- List listOf(1)" to variableError("List value", "Int", notInteger("[1]")),
	"Int <- Map mapOf('a' to 1)" to variableError("Map value", "Int", notInteger("{a=1}")),
	"Int <- String 'RED'" to variableError("String value", "Int", notInteger("\"RED\"")),
)

private val expectedStringCells = mapOf(
	"String <- Boolean true" to variableError("Value", "String", notString("true")),
	"String <- Boolean false" to variableError("Value", "String", notString("false")),
	"String <- Byte 1" to variableError("Byte value", "String", notString("1")),
	"String <- Short 1" to variableError("Short value", "String", notString("1")),
	"String <- Int 1" to variableError("Int value", "String", notString("1")),
	"String <- Int -1" to variableError("Int value", "String", notString("-1")),
	"String <- Long 1L" to variableError("Long value", "String", notString("1")),
	"String <- Long Long.MAX_VALUE" to variableError("Long value", "String", notString("9223372036854775807")),
	"String <- Float 1.5f" to variableError("Float value", "String", notString("1.5")),
	"String <- Double 1.5" to variableError("Double value", "String", notString("1.5")),
	"String <- Double 1.0" to variableError("Double value", "String", notString("1.0")),
	"String <- Double NaN" to variableError("Double value", "String", notString("NaN")),
	"String <- Double POSITIVE_INFINITY" to variableError("Double value", "String", notString("Infinity")),
	"String <- Double NEGATIVE_INFINITY" to variableError("Double value", "String", notString("-Infinity")),
	"String <- UByte 1u" to variableError("UByte value", "String", notString("1")),
	"String <- UShort 1u" to variableError("UShort value", "String", notString("1")),
	"String <- UInt 1u" to variableError("UInt value", "String", notString("1")),
	"String <- ULong 1uL" to variableError("ULong value", "String", notString("1")),
	"String <- ULong ULong.MAX_VALUE" to variableError("ULong value", "String", notString("18446744073709551615")),
	"String <- String '1'" to "String:1",
	"String <- String 'abc'" to "String:abc",
	"String <- String ''" to "String:",
	"String <- List listOf(1)" to variableError("List value", "String", notString("[1]")),
	"String <- Map mapOf('a' to 1)" to variableError("Map value", "String", notString("{a=1}")),
	"String <- String 'RED'" to "String:RED",
)

/**
 * Unlike output coercion, the variable path DOES validate enum values: only a String naming one of the
 * enum's values is accepted.
 */
private val expectedEnumCells = mapOf(
	"Color <- Boolean true" to enumVariableError("Value"),
	"Color <- Boolean false" to enumVariableError("Value"),
	"Color <- Byte 1" to enumVariableError("Byte value"),
	"Color <- Short 1" to enumVariableError("Short value"),
	"Color <- Int 1" to enumVariableError("Int value"),
	"Color <- Int -1" to enumVariableError("Int value"),
	"Color <- Long 1L" to enumVariableError("Long value"),
	"Color <- Long Long.MAX_VALUE" to enumVariableError("Long value"),
	"Color <- Float 1.5f" to enumVariableError("Float value"),
	"Color <- Double 1.5" to enumVariableError("Double value"),
	"Color <- Double 1.0" to enumVariableError("Double value"),
	"Color <- Double NaN" to enumVariableError("Double value"),
	"Color <- Double POSITIVE_INFINITY" to enumVariableError("Double value"),
	"Color <- Double NEGATIVE_INFINITY" to enumVariableError("Double value"),
	"Color <- UByte 1u" to enumVariableError("UByte value"),
	"Color <- UShort 1u" to enumVariableError("UShort value"),
	"Color <- UInt 1u" to enumVariableError("UInt value"),
	"Color <- ULong 1uL" to enumVariableError("ULong value"),
	"Color <- ULong ULong.MAX_VALUE" to enumVariableError("ULong value"),
	"Color <- String '1'" to enumVariableError("String value"),
	"Color <- String 'abc'" to enumVariableError("String value"),
	"Color <- String ''" to enumVariableError("String value"),
	"Color <- List listOf(1)" to enumVariableError("List value"),
	"Color <- Map mapOf('a' to 1)" to enumVariableError("Map value"),
	"Color <- String 'RED'" to "String:RED",
)

private val expectedCustomScalarCells = mapOf(
	"Custom <- Boolean true" to "Boolean:true",
	"Custom <- Boolean false" to "Boolean:false",
	"Custom <- Byte 1" to "Byte:1",
	"Custom <- Short 1" to "Short:1",
	"Custom <- Int 1" to "Int:1",
	"Custom <- Int -1" to "Int:-1",
	"Custom <- Long 1L" to "Long:1",
	"Custom <- Long Long.MAX_VALUE" to "Long:9223372036854775807",
	"Custom <- Float 1.5f" to "Float:1.5",
	"Custom <- Double 1.5" to "Double:1.5",
	"Custom <- Double 1.0" to "Double:1.0",
	"Custom <- Double NaN" to "Double:NaN",
	"Custom <- Double POSITIVE_INFINITY" to "Double:Infinity",
	"Custom <- Double NEGATIVE_INFINITY" to "Double:-Infinity",
	"Custom <- UByte 1u" to "UByte:1",
	"Custom <- UShort 1u" to "UShort:1",
	"Custom <- UInt 1u" to "UInt:1",
	"Custom <- ULong 1uL" to "ULong:1",
	"Custom <- ULong ULong.MAX_VALUE" to "ULong:18446744073709551615",
	"Custom <- String '1'" to "String:1",
	"Custom <- String 'abc'" to "String:abc",
	"Custom <- String ''" to "String:",
	"Custom <- List listOf(1)" to "List:[1]",
	"Custom <- Map mapOf('a' to 1)" to "Map:{a=1}",
	"Custom <- String 'RED'" to "String:RED",
)

class VariableInputCoercionMatrixTests {

	@Test
	fun testVariableMatrix_booleanAndFloat() = runTest {
		assertEquals(
			actual = variableMatrix(listOf("Boolean", "Float")),
			expected = expectedBooleanCells + expectedFloatCells,
		)
	}

	@Test
	fun testVariableMatrix_idAndInt() = runTest {
		assertEquals(
			actual = variableMatrix(listOf("ID", "Int")),
			expected = expectedIdCells + expectedIntCells,
		)
	}

	@Test
	fun testVariableMatrix_stringEnumAndCustomScalar() = runTest {
		assertEquals(
			actual = variableMatrix(listOf("String", ENUM_TYPE, CUSTOM_TYPE)),
			expected = expectedStringCells + expectedEnumCells + expectedCustomScalarCells,
		)
	}
}
