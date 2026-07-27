/*
 * Characterisation tests for the LITERAL INPUT coercion table.
 *
 * Mirrors `GScalarType.coerceInputLiteral` on the five built-in scalars
 * (modules/language/sources/model/nodes/BuiltinScalarTypes.kt), reached through
 * `NodeInputConverter.coerceValueForScalar`, plus `NodeInputConverter.coerceValueForEnum`. Every cell
 * records what the library does TODAY, captured by running it — not what the GraphQL specification
 * requires.
 *
 * A changed cell is a BEHAVIOURAL CHANGE. Investigate it and decide deliberately whether the new
 * behaviour is wanted; never re-paste an actual value to make the test green again.
 *
 * The document is parsed and then executed WITHOUT validation, so that coercion — not the validator —
 * decides every cell. The shipped `execute(documentSource)` pipeline would reject most of the rejected
 * literals earlier, as request errors; see `ValueValidityMatrixTests` for that table. Because coercion
 * runs during field execution, its failures here are FIELD errors: the "data" key stays present and the
 * field is null. The `echo` resolver returns a *rendering* of the coerced argument so that the value
 * never passes back through output coercion.
 *
 * Notable characterised behaviour:
 *   - The `Custom` scalar defines no literal coercion at all, so the converter converts the literal
 *     generically and hands the result to the scalar's identity `coerceInputValue` — an enum literal
 *     becomes its name as a String and an input object becomes a Map.
 *   - Unlike output coercion, the enum arm here DOES validate: only a known value name is accepted.
 *   - `ID` uses a second message here, distinct from the one its value paths use.
 *   - The scalar contributes only the bare parenthetical; the surrounding wording and the printed literal
 *     come from `NodeInputConverter.Context.makeValueError`.
 */
package testing

import io.fluidsonic.graphql.GDocument
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

/** Every GraphQL literal fed to the literal table, spliced verbatim into the document text. */
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

/** Renders a coerced argument value as `<simple class name>:<toString>`, or `null`. */
private fun render(value: Any?): String = when (value) {
	null -> "null"
	is List<*> -> "List:$value"
	is Map<*, *> -> "Map:$value"
	else -> "${value::class.simpleName}:$value"
}

/**
 * The full wording of `NodeInputConverter.Context.makeValueError`.
 *
 * [valueKind] is the literal's `GValue.kind`, first letter upper-cased — `"Int"`, `"Input object"`, … —
 * and [details] is the bare message the scalar itself threw, which quotes the printed literal.
 */
private fun literalError(valueKind: String, typeName: String, invalidValue: String, details: String): String =
	"ERROR: $valueKind value is not valid for argument 'value' with type '$typeName' ($details)." +
		"\nThe invalid value is: $invalidValue"

private fun notBoolean(literal: String) = "Boolean cannot represent a non boolean value: $literal"

private fun notNumeric(literal: String) = "Float cannot represent non numeric value: $literal"

private fun notIdLiteral(literal: String) = "ID cannot represent a non-string and non-integer value: $literal"

private fun notInteger(literal: String) = "Int cannot represent non-integer value: $literal"

private fun notString(literal: String) = "String cannot represent a non string value: $literal"

/** Same wording, plus the valid-values detail `coerceValueForEnum` appends. */
private fun enumLiteralError(valueKind: String, invalidValue: String): String =
	"ERROR: $valueKind value is not valid for argument 'value' with type '$ENUM_TYPE' " +
		"(valid values: BLUE, GREEN, RED).\nThe invalid value is: $invalidValue"

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
private suspend fun literalCell(typeName: String, literal: String): String {
	val executor = GExecutor.default(schema = echoSchema(typeName))
	val document = GDocument.parse("{ echo(value: $literal) }").valueOrThrow()
	val result = executor.serializeResult(executor.execute(document))
	val message = (result["errors"] as List<Map<String, Any?>>?)?.firstOrNull()?.get("message") as String?

	return when {
		!result.containsKey("data") -> "REQUEST-ERROR: $message"
		message !== null -> "ERROR: $message"
		else -> (result["data"] as Map<String, Any?>?)?.get("echo") as String? ?: "null"
	}
}

private suspend fun literalMatrix(typeNames: List<String>): Map<String, String> = buildMap {
	for (typeName in typeNames) {
		for (literal in literals) {
			put("$typeName <- $literal", literalCell(typeName = typeName, literal = literal))
		}
	}
}

private val expectedBooleanCells = mapOf(
	"Boolean <- true" to "Boolean:true",
	"Boolean <- 1" to literalError("Int", "Boolean", "1", notBoolean("1")),
	"Boolean <- -1" to literalError("Int", "Boolean", "-1", notBoolean("-1")),
	"Boolean <- 1.5" to literalError("Float", "Boolean", "1.5", notBoolean("1.5")),
	"Boolean <- \"s\"" to literalError("String", "Boolean", "\"s\"", notBoolean("\"s\"")),
	"Boolean <- \"1\"" to literalError("String", "Boolean", "\"1\"", notBoolean("\"1\"")),
	"Boolean <- RED" to literalError("Enum", "Boolean", "RED", notBoolean("RED")),
	"Boolean <- NOPE" to literalError("Enum", "Boolean", "NOPE", notBoolean("NOPE")),
	"Boolean <- null" to "null",
	"Boolean <- [1]" to literalError("List", "Boolean", "[1]", notBoolean("[1]")),
	"Boolean <- {a: 1}" to literalError("Input object", "Boolean", "{\n\ta: 1\n}", notBoolean("{\n\ta: 1\n}")),
)

private val expectedFloatCells = mapOf(
	"Float <- true" to literalError("Boolean", "Float", "true", notNumeric("true")),
	"Float <- 1" to "Double:1.0",
	"Float <- -1" to "Double:-1.0",
	"Float <- 1.5" to "Double:1.5",
	"Float <- \"s\"" to literalError("String", "Float", "\"s\"", notNumeric("\"s\"")),
	"Float <- \"1\"" to literalError("String", "Float", "\"1\"", notNumeric("\"1\"")),
	"Float <- RED" to literalError("Enum", "Float", "RED", notNumeric("RED")),
	"Float <- NOPE" to literalError("Enum", "Float", "NOPE", notNumeric("NOPE")),
	"Float <- null" to "null",
	"Float <- [1]" to literalError("List", "Float", "[1]", notNumeric("[1]")),
	"Float <- {a: 1}" to literalError("Input object", "Float", "{\n\ta: 1\n}", notNumeric("{\n\ta: 1\n}")),
)

/** The literal path uses ID's second message, distinct from the one its value paths use. */
private val expectedIdCells = mapOf(
	"ID <- true" to literalError("Boolean", "ID", "true", notIdLiteral("true")),
	"ID <- 1" to "String:1",
	"ID <- -1" to "String:-1",
	"ID <- 1.5" to literalError("Float", "ID", "1.5", notIdLiteral("1.5")),
	"ID <- \"s\"" to "String:s",
	"ID <- \"1\"" to "String:1",
	"ID <- RED" to literalError("Enum", "ID", "RED", notIdLiteral("RED")),
	"ID <- NOPE" to literalError("Enum", "ID", "NOPE", notIdLiteral("NOPE")),
	"ID <- null" to "null",
	"ID <- [1]" to literalError("List", "ID", "[1]", notIdLiteral("[1]")),
	"ID <- {a: 1}" to literalError("Input object", "ID", "{\n\ta: 1\n}", notIdLiteral("{\n\ta: 1\n}")),
)

private val expectedIntCells = mapOf(
	"Int <- true" to literalError("Boolean", "Int", "true", notInteger("true")),
	"Int <- 1" to "Int:1",
	"Int <- -1" to "Int:-1",
	"Int <- 1.5" to literalError("Float", "Int", "1.5", notInteger("1.5")),
	// Unlike the output table, a String literal is NOT accepted for `Int` here.
	"Int <- \"s\"" to literalError("String", "Int", "\"s\"", notInteger("\"s\"")),
	"Int <- \"1\"" to literalError("String", "Int", "\"1\"", notInteger("\"1\"")),
	"Int <- RED" to literalError("Enum", "Int", "RED", notInteger("RED")),
	"Int <- NOPE" to literalError("Enum", "Int", "NOPE", notInteger("NOPE")),
	"Int <- null" to "null",
	"Int <- [1]" to literalError("List", "Int", "[1]", notInteger("[1]")),
	"Int <- {a: 1}" to literalError("Input object", "Int", "{\n\ta: 1\n}", notInteger("{\n\ta: 1\n}")),
)

private val expectedStringCells = mapOf(
	"String <- true" to literalError("Boolean", "String", "true", notString("true")),
	"String <- 1" to literalError("Int", "String", "1", notString("1")),
	"String <- -1" to literalError("Int", "String", "-1", notString("-1")),
	"String <- 1.5" to literalError("Float", "String", "1.5", notString("1.5")),
	"String <- \"s\"" to "String:s",
	"String <- \"1\"" to "String:1",
	"String <- RED" to literalError("Enum", "String", "RED", notString("RED")),
	"String <- NOPE" to literalError("Enum", "String", "NOPE", notString("NOPE")),
	"String <- null" to "null",
	"String <- [1]" to literalError("List", "String", "[1]", notString("[1]")),
	"String <- {a: 1}" to literalError("Input object", "String", "{\n\ta: 1\n}", notString("{\n\ta: 1\n}")),
)

private val expectedEnumCells = mapOf(
	"Color <- true" to enumLiteralError("Boolean", "true"),
	"Color <- 1" to enumLiteralError("Int", "1"),
	"Color <- -1" to enumLiteralError("Int", "-1"),
	"Color <- 1.5" to enumLiteralError("Float", "1.5"),
	"Color <- \"s\"" to enumLiteralError("String", "\"s\""),
	"Color <- \"1\"" to enumLiteralError("String", "\"1\""),
	"Color <- RED" to "String:RED",
	"Color <- NOPE" to enumLiteralError("Enum", "NOPE"),
	"Color <- null" to "null",
	"Color <- [1]" to enumLiteralError("List", "[1]"),
	"Color <- {a: 1}" to enumLiteralError("Input object", "{\n\ta: 1\n}"),
)

/** A custom scalar with no coercer accepts everything and yields `GValue.unwrap()`. */
private val expectedCustomScalarCells = mapOf(
	"Custom <- true" to "Boolean:true",
	"Custom <- 1" to "Int:1",
	"Custom <- -1" to "Int:-1",
	"Custom <- 1.5" to "Double:1.5",
	"Custom <- \"s\"" to "String:s",
	"Custom <- \"1\"" to "String:1",
	"Custom <- RED" to "String:RED",
	"Custom <- NOPE" to "String:NOPE",
	"Custom <- null" to "null",
	"Custom <- [1]" to "List:[1]",
	"Custom <- {a: 1}" to "Map:{a=1}",
)

class LiteralInputCoercionMatrixTests {

	@Test
	fun testLiteralMatrix_booleanFloatIdAndInt() = runTest {
		assertEquals(
			actual = literalMatrix(listOf("Boolean", "Float", "ID", "Int")),
			expected = expectedBooleanCells + expectedFloatCells + expectedIdCells + expectedIntCells,
		)
	}

	@Test
	fun testLiteralMatrix_stringEnumAndCustomScalar() = runTest {
		assertEquals(
			actual = literalMatrix(listOf("String", ENUM_TYPE, CUSTOM_TYPE)),
			expected = expectedStringCells + expectedEnumCells + expectedCustomScalarCells,
		)
	}
}
