/*
 * Characterisation tests for the OUTPUT coercion table.
 *
 * Mirrors `GScalarType.coerceOutputValue` on the five built-in scalars
 * (modules/language/sources/model/nodes/BuiltinScalarTypes.kt), reached through
 * `OutputConverter.coerceLeafValue`. Every cell records what the library does TODAY, captured by
 * running it — not what the GraphQL specification requires.
 *
 * A changed cell is a BEHAVIOURAL CHANGE. Investigate it and decide deliberately whether the new
 * behaviour is wanted; never re-paste an actual value to make the test green again.
 *
 * Notable characterised behaviour:
 *   - Output coercion is deliberately lenient, mirroring graphql-js: `Int` and `Float` accept a
 *     `Boolean` and a numeric `String`, `Boolean` accepts any finite number, and `String` renders any
 *     finite number and any `Boolean`. The variable-input table rejects all of those, so the two tables
 *     differ in exactly those cells.
 *   - `Int <- String …` parses with JS `Number(string)` semantics, so `"1"` is accepted and `"abc"` is not.
 *   - Non-finite `Double`s are rejected by every built-in scalar.
 *   - Enum output has no coercion of its own: `coerceLeafValue` passes the resolved value through
 *     unvalidated. The `Color` rows are therefore identical to the `Custom` scalar rows.
 *   - The scalar contributes only the bare parenthetical; the surrounding wording and the JVM qualified
 *     class name come from `OutputConverter.Context.invalid`.
 */
package testing

import io.fluidsonic.graphql.GExecutor
import io.fluidsonic.graphql.GSchema
import io.fluidsonic.graphql.GTypeRef
import io.fluidsonic.graphql.GraphQL
import io.fluidsonic.graphql.resolve
import io.fluidsonic.graphql.schema
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

private const val ENUM_TYPE = "Color"
private const val CUSTOM_TYPE = "Custom"

/** Every Kotlin value fed to the output table, labelled for the matrix key. */
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

/** Renders a coerced output value as `<simple class name>:<toString>`, or `null`. */
private fun render(value: Any?): String = when (value) {
	null -> "null"
	is List<*> -> "List:$value"
	is Map<*, *> -> "Map:$value"
	else -> "${value::class.simpleName}:$value"
}

/**
 * The full wording of `OutputConverter.Context.invalid`.
 *
 * [details] is the bare message the scalar itself threw; [resolvedValue] is the qualified class name and
 * `toString()` of the value the resolver returned, which only the converter knows.
 */
private fun outputError(typeName: String, details: String, resolvedValue: String): String =
	"ERROR: Output coercion encountered an invalid resolved value for field 'value' of type '$typeName' " +
		"in type 'Query' ($details):\n$resolvedValue"

private fun outputSchema(typeName: String, resolvedValue: Any): GSchema = GraphQL.schema {
	Enum(GTypeRef(ENUM_TYPE)) {
		value("RED")
		value("GREEN")
		value("BLUE")
	}
	Scalar(GTypeRef(CUSTOM_TYPE))

	Query {
		field("value" of GTypeRef(typeName)) {
			resolve { resolvedValue }
		}
	}
}

@Suppress("UNCHECKED_CAST")
private suspend fun outputCell(typeName: String, resolvedValue: Any): String {
	val executor = GExecutor.default(schema = outputSchema(typeName = typeName, resolvedValue = resolvedValue))
	val result = executor.serializeResult(executor.execute("{ value }"))
	val message = (result["errors"] as List<Map<String, Any?>>?)?.firstOrNull()?.get("message") as String?

	return when {
		// Output coercion runs during field execution, so its failures are field errors and the "data"
		// key stays present. A missing "data" key would mean a request error instead.
		!result.containsKey("data") -> "REQUEST-ERROR: $message"
		message !== null -> "ERROR: $message"
		else -> render((result["data"] as Map<String, Any?>?)?.get("value"))
	}
}

private suspend fun outputMatrix(typeNames: List<String>): Map<String, String> = buildMap {
	for (typeName in typeNames) {
		for ((label, value) in kotlinValues) {
			put("$typeName <- $label", outputCell(typeName = typeName, resolvedValue = value))
		}
	}
}

/** Every finite number is truthy unless it is zero; only the non-finite ones are rejected. */
private val expectedBooleanCells = mapOf(
	"Boolean <- Boolean true" to "Boolean:true",
	"Boolean <- Boolean false" to "Boolean:false",
	"Boolean <- Byte 1" to "Boolean:true",
	"Boolean <- Short 1" to "Boolean:true",
	"Boolean <- Int 1" to "Boolean:true",
	"Boolean <- Int -1" to "Boolean:true",
	"Boolean <- Long 1L" to "Boolean:true",
	"Boolean <- Long Long.MAX_VALUE" to "Boolean:true",
	"Boolean <- Float 1.5f" to "Boolean:true",
	"Boolean <- Double 1.5" to "Boolean:true",
	"Boolean <- Double 1.0" to "Boolean:true",
	"Boolean <- Double NaN" to outputError("Boolean", "Boolean cannot represent a non boolean value: NaN", "kotlin.Double: NaN"),
	"Boolean <- Double POSITIVE_INFINITY" to
		outputError("Boolean", "Boolean cannot represent a non boolean value: Infinity", "kotlin.Double: Infinity"),
	"Boolean <- Double NEGATIVE_INFINITY" to
		outputError("Boolean", "Boolean cannot represent a non boolean value: -Infinity", "kotlin.Double: -Infinity"),
	"Boolean <- UByte 1u" to "Boolean:true",
	"Boolean <- UShort 1u" to "Boolean:true",
	"Boolean <- UInt 1u" to "Boolean:true",
	"Boolean <- ULong 1uL" to "Boolean:true",
	"Boolean <- ULong ULong.MAX_VALUE" to "Boolean:true",
	"Boolean <- String '1'" to outputError("Boolean", "Boolean cannot represent a non boolean value: \"1\"", "kotlin.String: 1"),
	"Boolean <- String 'abc'" to outputError("Boolean", "Boolean cannot represent a non boolean value: \"abc\"", "kotlin.String: abc"),
	"Boolean <- String ''" to outputError("Boolean", "Boolean cannot represent a non boolean value: \"\"", "kotlin.String: "),
	"Boolean <- List listOf(1)" to
		outputError("Boolean", "Boolean cannot represent a non boolean value: [1]", "java.util.Collections.SingletonList: [1]"),
	"Boolean <- Map mapOf('a' to 1)" to
		outputError("Boolean", "Boolean cannot represent a non boolean value: {a=1}", "java.util.Collections.SingletonMap: {a=1}"),
	"Boolean <- String 'RED'" to outputError("Boolean", "Boolean cannot represent a non boolean value: \"RED\"", "kotlin.String: RED"),
)

private val expectedFloatCells = mapOf(
	"Float <- Boolean true" to "Double:1.0",
	"Float <- Boolean false" to "Double:0.0",
	"Float <- Byte 1" to "Double:1.0",
	"Float <- Short 1" to "Double:1.0",
	"Float <- Int 1" to "Double:1.0",
	"Float <- Int -1" to "Double:-1.0",
	"Float <- Long 1L" to "Double:1.0",
	"Float <- Long Long.MAX_VALUE" to "Double:9.223372036854776E18",
	"Float <- Float 1.5f" to "Double:1.5",
	"Float <- Double 1.5" to "Double:1.5",
	"Float <- Double 1.0" to "Double:1.0",
	"Float <- Double NaN" to outputError("Float", "Float cannot represent non numeric value: NaN", "kotlin.Double: NaN"),
	"Float <- Double POSITIVE_INFINITY" to
		outputError("Float", "Float cannot represent non numeric value: Infinity", "kotlin.Double: Infinity"),
	"Float <- Double NEGATIVE_INFINITY" to
		outputError("Float", "Float cannot represent non numeric value: -Infinity", "kotlin.Double: -Infinity"),
	"Float <- UByte 1u" to "Double:1.0",
	"Float <- UShort 1u" to "Double:1.0",
	"Float <- UInt 1u" to "Double:1.0",
	"Float <- ULong 1uL" to "Double:1.0",
	"Float <- ULong ULong.MAX_VALUE" to "Double:1.8446744073709552E19",
	// A numeric String is parsed with JS `Number(string)` semantics.
	"Float <- String '1'" to "Double:1.0",
	"Float <- String 'abc'" to outputError("Float", "Float cannot represent non numeric value: \"abc\"", "kotlin.String: abc"),
	"Float <- String ''" to outputError("Float", "Float cannot represent non numeric value: \"\"", "kotlin.String: "),
	"Float <- List listOf(1)" to
		outputError("Float", "Float cannot represent non numeric value: [1]", "java.util.Collections.SingletonList: [1]"),
	"Float <- Map mapOf('a' to 1)" to
		outputError("Float", "Float cannot represent non numeric value: {a=1}", "java.util.Collections.SingletonMap: {a=1}"),
	"Float <- String 'RED'" to outputError("Float", "Float cannot represent non numeric value: \"RED\"", "kotlin.String: RED"),
)

private val expectedIdCells = mapOf(
	"ID <- Boolean true" to outputError("ID", "ID cannot represent value: true", "kotlin.Boolean: true"),
	"ID <- Boolean false" to outputError("ID", "ID cannot represent value: false", "kotlin.Boolean: false"),
	"ID <- Byte 1" to "String:1",
	"ID <- Short 1" to "String:1",
	"ID <- Int 1" to "String:1",
	"ID <- Int -1" to "String:-1",
	"ID <- Long 1L" to "String:1",
	"ID <- Long Long.MAX_VALUE" to "String:9223372036854775807",
	"ID <- Float 1.5f" to outputError("ID", "ID cannot represent value: 1.5", "kotlin.Float: 1.5"),
	"ID <- Double 1.5" to outputError("ID", "ID cannot represent value: 1.5", "kotlin.Double: 1.5"),
	// An integral Double is accepted and rendered without its fractional part.
	"ID <- Double 1.0" to "String:1",
	"ID <- Double NaN" to outputError("ID", "ID cannot represent value: NaN", "kotlin.Double: NaN"),
	"ID <- Double POSITIVE_INFINITY" to outputError("ID", "ID cannot represent value: Infinity", "kotlin.Double: Infinity"),
	"ID <- Double NEGATIVE_INFINITY" to outputError("ID", "ID cannot represent value: -Infinity", "kotlin.Double: -Infinity"),
	"ID <- UByte 1u" to "String:1",
	"ID <- UShort 1u" to "String:1",
	"ID <- UInt 1u" to "String:1",
	"ID <- ULong 1uL" to "String:1",
	"ID <- ULong ULong.MAX_VALUE" to "String:18446744073709551615",
	"ID <- String '1'" to "String:1",
	"ID <- String 'abc'" to "String:abc",
	"ID <- String ''" to "String:",
	"ID <- List listOf(1)" to outputError("ID", "ID cannot represent value: [1]", "java.util.Collections.SingletonList: [1]"),
	"ID <- Map mapOf('a' to 1)" to outputError("ID", "ID cannot represent value: {a=1}", "java.util.Collections.SingletonMap: {a=1}"),
	"ID <- String 'RED'" to "String:RED",
)

/** `Int` has two distinct messages: a value that is not a whole number, and one that will not fit. */
private val expectedIntCells = mapOf(
	"Int <- Boolean true" to "Int:1",
	"Int <- Boolean false" to "Int:0",
	"Int <- Byte 1" to "Int:1",
	"Int <- Short 1" to "Int:1",
	"Int <- Int 1" to "Int:1",
	"Int <- Int -1" to "Int:-1",
	"Int <- Long 1L" to "Int:1",
	"Int <- Long Long.MAX_VALUE" to
		outputError("Int", "Int cannot represent non 32-bit signed integer value: 9223372036854775807", "kotlin.Long: 9223372036854775807"),
	"Int <- Float 1.5f" to outputError("Int", "Int cannot represent non-integer value: 1.5", "kotlin.Float: 1.5"),
	"Int <- Double 1.5" to outputError("Int", "Int cannot represent non-integer value: 1.5", "kotlin.Double: 1.5"),
	"Int <- Double 1.0" to "Int:1",
	"Int <- Double NaN" to outputError("Int", "Int cannot represent non-integer value: NaN", "kotlin.Double: NaN"),
	"Int <- Double POSITIVE_INFINITY" to outputError("Int", "Int cannot represent non-integer value: Infinity", "kotlin.Double: Infinity"),
	"Int <- Double NEGATIVE_INFINITY" to outputError("Int", "Int cannot represent non-integer value: -Infinity", "kotlin.Double: -Infinity"),
	"Int <- UByte 1u" to "Int:1",
	"Int <- UShort 1u" to "Int:1",
	"Int <- UInt 1u" to "Int:1",
	"Int <- ULong 1uL" to "Int:1",
	"Int <- ULong ULong.MAX_VALUE" to
		outputError("Int", "Int cannot represent non 32-bit signed integer value: 18446744073709551615", "kotlin.ULong: 18446744073709551615"),
	// A numeric String is parsed with JS `Number(string)` semantics; the message quotes the original.
	"Int <- String '1'" to "Int:1",
	"Int <- String 'abc'" to outputError("Int", "Int cannot represent non-integer value: \"abc\"", "kotlin.String: abc"),
	"Int <- String ''" to outputError("Int", "Int cannot represent non-integer value: \"\"", "kotlin.String: "),
	"Int <- List listOf(1)" to outputError("Int", "Int cannot represent non-integer value: [1]", "java.util.Collections.SingletonList: [1]"),
	"Int <- Map mapOf('a' to 1)" to
		outputError("Int", "Int cannot represent non-integer value: {a=1}", "java.util.Collections.SingletonMap: {a=1}"),
	"Int <- String 'RED'" to outputError("Int", "Int cannot represent non-integer value: \"RED\"", "kotlin.String: RED"),
)

private val expectedStringCells = mapOf(
	"String <- Boolean true" to "String:true",
	"String <- Boolean false" to "String:false",
	"String <- Byte 1" to "String:1",
	"String <- Short 1" to "String:1",
	"String <- Int 1" to "String:1",
	"String <- Int -1" to "String:-1",
	"String <- Long 1L" to "String:1",
	"String <- Long Long.MAX_VALUE" to "String:9223372036854775807",
	// Kotlin's `toString()` keeps the fractional part that JS drops for a whole Double.
	"String <- Float 1.5f" to "String:1.5",
	"String <- Double 1.5" to "String:1.5",
	"String <- Double 1.0" to "String:1.0",
	"String <- Double NaN" to outputError("String", "String cannot represent value: NaN", "kotlin.Double: NaN"),
	"String <- Double POSITIVE_INFINITY" to outputError("String", "String cannot represent value: Infinity", "kotlin.Double: Infinity"),
	"String <- Double NEGATIVE_INFINITY" to outputError("String", "String cannot represent value: -Infinity", "kotlin.Double: -Infinity"),
	"String <- UByte 1u" to "String:1",
	"String <- UShort 1u" to "String:1",
	"String <- UInt 1u" to "String:1",
	"String <- ULong 1uL" to "String:1",
	"String <- ULong ULong.MAX_VALUE" to "String:18446744073709551615",
	"String <- String '1'" to "String:1",
	"String <- String 'abc'" to "String:abc",
	"String <- String ''" to "String:",
	"String <- List listOf(1)" to
		outputError("String", "String cannot represent value: [1]", "java.util.Collections.SingletonList: [1]"),
	"String <- Map mapOf('a' to 1)" to
		outputError("String", "String cannot represent value: {a=1}", "java.util.Collections.SingletonMap: {a=1}"),
	"String <- String 'RED'" to "String:RED",
)

/**
 * UNVALIDATED PASS-THROUGH: `coerceLeafValue` has no coercion for `GEnumType`, so every resolved value
 * reaches the response untouched — including values that are not enum value names at all. Deliberately
 * identical to [expectedCustomScalarCells].
 */
private val expectedEnumCells = mapOf(
	"Color <- Boolean true" to "Boolean:true",
	"Color <- Boolean false" to "Boolean:false",
	"Color <- Byte 1" to "Byte:1",
	"Color <- Short 1" to "Short:1",
	"Color <- Int 1" to "Int:1",
	"Color <- Int -1" to "Int:-1",
	"Color <- Long 1L" to "Long:1",
	"Color <- Long Long.MAX_VALUE" to "Long:9223372036854775807",
	"Color <- Float 1.5f" to "Float:1.5",
	"Color <- Double 1.5" to "Double:1.5",
	"Color <- Double 1.0" to "Double:1.0",
	"Color <- Double NaN" to "Double:NaN",
	"Color <- Double POSITIVE_INFINITY" to "Double:Infinity",
	"Color <- Double NEGATIVE_INFINITY" to "Double:-Infinity",
	"Color <- UByte 1u" to "UByte:1",
	"Color <- UShort 1u" to "UShort:1",
	"Color <- UInt 1u" to "UInt:1",
	"Color <- ULong 1uL" to "ULong:1",
	"Color <- ULong ULong.MAX_VALUE" to "ULong:18446744073709551615",
	"Color <- String '1'" to "String:1",
	"Color <- String 'abc'" to "String:abc",
	"Color <- String ''" to "String:",
	"Color <- List listOf(1)" to "List:[1]",
	"Color <- Map mapOf('a' to 1)" to "Map:{a=1}",
	"Color <- String 'RED'" to "String:RED",
)

/** A custom scalar's default output coercion is the identity, so it too passes everything through. */
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

class OutputCoercionMatrixTests {

	@Test
	fun testOutputMatrix_booleanAndFloat() = runTest {
		assertEquals(
			actual = outputMatrix(listOf("Boolean", "Float")),
			expected = expectedBooleanCells + expectedFloatCells,
		)
	}

	@Test
	fun testOutputMatrix_idAndInt() = runTest {
		assertEquals(
			actual = outputMatrix(listOf("ID", "Int")),
			expected = expectedIdCells + expectedIntCells,
		)
	}

	@Test
	fun testOutputMatrix_stringEnumAndCustomScalar() = runTest {
		assertEquals(
			actual = outputMatrix(listOf("String", ENUM_TYPE, CUSTOM_TYPE)),
			expected = expectedStringCells + expectedEnumCells + expectedCustomScalarCells,
		)
	}
}
