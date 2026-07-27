package testing

import io.fluidsonic.graphql.GBooleanType
import io.fluidsonic.graphql.GBooleanValue
import io.fluidsonic.graphql.GCustomScalarType
import io.fluidsonic.graphql.GErrorException
import io.fluidsonic.graphql.GFloatType
import io.fluidsonic.graphql.GFloatValue
import io.fluidsonic.graphql.GIdType
import io.fluidsonic.graphql.GIntType
import io.fluidsonic.graphql.GIntValue
import io.fluidsonic.graphql.GStringType
import io.fluidsonic.graphql.GStringValue
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/** Returns the bare message of the [GErrorException] thrown by [coerce]. */
private fun messageOfFailure(coerce: () -> Any?): String = assertFailsWith<GErrorException> { coerce() }.errors.single().message

// Built-in scalars are per-schema instances rather than singletons, so the coercion tests need instances of
// their own. Any instance coerces identically — the extension channel is the only per-instance state.
private val booleanType = GBooleanType()
private val floatType = GFloatType()
private val idType = GIdType()
private val intType = GIntType()
private val stringType = GStringType()

class BuiltinScalarCoercionTests {

	@Test
	fun coerceOutputValue_booleanMapsToOneAndZeroForInt() {
		assertEquals(actual = intType.coerceOutputValue(true), expected = 1)
		assertEquals(actual = intType.coerceOutputValue(false), expected = 0)
	}

	@Test
	fun coerceOutputValue_intParsesNumericString() {
		assertEquals(actual = intType.coerceOutputValue("1"), expected = 1)
		assertEquals(actual = intType.coerceOutputValue("1e3"), expected = 1000)
		assertEquals(actual = intType.coerceOutputValue("+5"), expected = 5)
	}

	@Test
	fun coerceOutputValue_intRejectsNonIntegerString() {
		assertEquals(
			actual = messageOfFailure { intType.coerceOutputValue("abc") },
			expected = "Int cannot represent non-integer value: \"abc\"",
		)
		assertEquals(
			actual = messageOfFailure { intType.coerceOutputValue("1.5") },
			expected = "Int cannot represent non-integer value: \"1.5\"",
		)
	}

	@Test
	fun coerceOutputValue_intRejectsOutOfRangeWithDistinctMessage() {
		assertEquals(
			actual = messageOfFailure { intType.coerceOutputValue("9999999999") },
			expected = "Int cannot represent non 32-bit signed integer value: \"9999999999\"",
		)
		assertEquals(
			actual = messageOfFailure { intType.coerceOutputValue(Long.MAX_VALUE) },
			expected = "Int cannot represent non 32-bit signed integer value: 9223372036854775807",
		)
	}

	@Test
	fun coerceInputValue_intRejectsBooleanAndString() {
		assertEquals(
			actual = messageOfFailure { intType.coerceInputValue(true) },
			expected = "Int cannot represent non-integer value: true",
		)
		assertEquals(
			actual = messageOfFailure { intType.coerceInputValue("1") },
			expected = "Int cannot represent non-integer value: \"1\"",
		)
	}

	@Test
	fun coerceInputLiteral_intAcceptsOnlyIntValue() {
		assertEquals(actual = intType.coerceInputLiteral.invoke(GIntValue(1)), expected = 1)
		assertEquals(
			actual = messageOfFailure { intType.coerceInputLiteral.invoke(GStringValue("1")) },
			expected = "Int cannot represent non-integer value: \"1\"",
		)
	}

	// A deliberate graphql-js parity port of JS `Number(string)`: the guard is `value !== ''`, not a trim,
	// so a blank string parses as zero while an empty one is rejected.
	@Test
	fun coerceOutputValue_floatAppliesJsNumberCarveOuts() {
		assertEquals(actual = floatType.coerceOutputValue("   "), expected = 0.0)
		assertEquals(actual = floatType.coerceOutputValue("0x10"), expected = 16.0)
		assertEquals(actual = floatType.coerceOutputValue("0b101"), expected = 5.0)
		assertEquals(actual = floatType.coerceOutputValue("0o17"), expected = 15.0)
		assertEquals(actual = floatType.coerceOutputValue("5."), expected = 5.0)
		assertEquals(
			actual = messageOfFailure { floatType.coerceOutputValue("") },
			expected = "Float cannot represent non numeric value: \"\"",
		)
		assertEquals(
			actual = messageOfFailure { floatType.coerceOutputValue("Infinity") },
			expected = "Float cannot represent non numeric value: \"Infinity\"",
		)
	}

	@Test
	fun coerceOutputValue_floatRejectsNonFiniteValues() {
		for (value in listOf(Double.NaN, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY)) {
			assertEquals(
				actual = messageOfFailure { floatType.coerceOutputValue(value) },
				expected = "Float cannot represent non numeric value: $value",
			)
		}
	}

	@Test
	fun coerceInputValue_floatRejectsNonFiniteValues() {
		assertEquals(
			actual = messageOfFailure { floatType.coerceInputValue(Double.NaN) },
			expected = "Float cannot represent non numeric value: NaN",
		)
	}

	@Test
	fun coerceInputLiteral_floatAcceptsIntAndFloatValues() {
		assertEquals(actual = floatType.coerceInputLiteral.invoke(GIntValue(1)), expected = 1.0)
		assertEquals(actual = floatType.coerceInputLiteral.invoke(GFloatValue(1.5)), expected = 1.5)
		assertEquals(
			actual = messageOfFailure { floatType.coerceInputLiteral.invoke(GStringValue("1")) },
			expected = "Float cannot represent non numeric value: \"1\"",
		)
	}

	@Test
	fun coerceOutputValue_stringRendersFiniteNumbersAndBooleans() {
		assertEquals(actual = stringType.coerceOutputValue(1), expected = "1")
		assertEquals(actual = stringType.coerceOutputValue(1.5), expected = "1.5")
		assertEquals(actual = stringType.coerceOutputValue(true), expected = "true")
		assertEquals(
			actual = messageOfFailure { stringType.coerceOutputValue(Double.NaN) },
			expected = "String cannot represent value: NaN",
		)
		assertEquals(
			actual = messageOfFailure { stringType.coerceOutputValue(listOf(1)) },
			expected = "String cannot represent value: [1]",
		)
	}

	// The two directions deliberately use different wording, mirroring graphql-js.
	@Test
	fun coerceInputValue_stringUsesTheNonStringWording() {
		assertEquals(
			actual = messageOfFailure { stringType.coerceInputValue(1) },
			expected = "String cannot represent a non string value: 1",
		)
	}

	@Test
	fun coerceOutputValue_booleanAcceptsFiniteNumbers() {
		assertEquals(actual = booleanType.coerceOutputValue(0), expected = false)
		assertEquals(actual = booleanType.coerceOutputValue(0.0), expected = false)
		assertEquals(actual = booleanType.coerceOutputValue(-0.0), expected = false)
		assertEquals(actual = booleanType.coerceOutputValue(1), expected = true)
		assertEquals(actual = booleanType.coerceOutputValue(1.5), expected = true)
		assertEquals(actual = booleanType.coerceOutputValue(-1), expected = true)
		assertEquals(
			actual = messageOfFailure { booleanType.coerceOutputValue(Double.NaN) },
			expected = "Boolean cannot represent a non boolean value: NaN",
		)
		assertEquals(
			actual = messageOfFailure { booleanType.coerceOutputValue(Double.POSITIVE_INFINITY) },
			expected = "Boolean cannot represent a non boolean value: Infinity",
		)
	}

	@Test
	fun coerceInputValue_booleanRejectsNumbers() {
		assertEquals(
			actual = messageOfFailure { booleanType.coerceInputValue(1) },
			expected = "Boolean cannot represent a non boolean value: 1",
		)
	}

	@Test
	fun coerceInputLiteral_booleanAcceptsOnlyBooleanValue() {
		assertEquals(actual = booleanType.coerceInputLiteral.invoke(GBooleanValue(true)), expected = true)
		assertEquals(
			actual = messageOfFailure { booleanType.coerceInputLiteral.invoke(GIntValue(1)) },
			expected = "Boolean cannot represent a non boolean value: 1",
		)
	}

	@Test
	fun coerceOutputValue_idAcceptsStringsAndIntegralNumbers() {
		assertEquals(actual = idType.coerceOutputValue("abc"), expected = "abc")
		assertEquals(actual = idType.coerceOutputValue(1), expected = "1")
		assertEquals(actual = idType.coerceOutputValue(Long.MAX_VALUE), expected = "9223372036854775807")
		assertEquals(
			actual = messageOfFailure { idType.coerceOutputValue(1.5) },
			expected = "ID cannot represent value: 1.5",
		)
	}

	// An integral Double beyond Long's range must not be silently clamped to Long.MAX_VALUE — an ID that
	// quietly becomes a different ID is worse than a rejected one. graphql-js renders it as "1e+30" instead,
	// which Kotlin cannot reproduce without a JS number formatter, and a differently-spelled identifier is no
	// better than none; so fluid rejects. Deliberate, recorded divergence.
	@Test
	fun coerceOutputValue_idRejectsIntegralNumbersBeyondLongRange() {
		assertEquals(
			actual = messageOfFailure { idType.coerceOutputValue(1e30) },
			expected = "ID cannot represent value: 1.0E30",
		)
		assertEquals(
			actual = messageOfFailure { idType.coerceInputValue(-1e30) },
			expected = "ID cannot represent value: -1.0E30",
		)
	}

	// ID's value paths and its literal path use two different messages.
	@Test
	fun coerceInputLiteral_idUsesTheNonStringNonIntegerWording() {
		assertEquals(actual = idType.coerceInputLiteral.invoke(GStringValue("abc")), expected = "abc")
		assertEquals(actual = idType.coerceInputLiteral.invoke(GIntValue(1)), expected = "1")
		assertEquals(
			actual = messageOfFailure { idType.coerceInputLiteral.invoke(GFloatValue(1.5)) },
			expected = "ID cannot represent a non-string and non-integer value: 1.5",
		)
	}

	// Round-trip hazard: an integer-looking String becomes an IntValue, not a StringValue.
	@Test
	fun valueToLiteral_idTurnsIntegerLookingStringIntoIntValue() {
		val literal = idType.valueToLiteral("1")
		assertTrue(literal is GIntValue, "expected a GIntValue but got $literal")
		assertEquals(actual = literal.value, expected = 1)
	}

	@Test
	fun valueToLiteral_idKeepsNonIntegerLookingStringsAsStringValue() {
		for (value in listOf("abc", "", "01", "1.0", "+1")) {
			val literal = idType.valueToLiteral(value)
			assertTrue(literal is GStringValue, "expected a GStringValue for '$value' but got $literal")
			assertEquals(actual = literal.value, expected = value)
		}
	}

	@Test
	fun valueToLiteral_idRejectsNonIntegralNumbers() {
		assertEquals(
			actual = messageOfFailure { idType.valueToLiteral(1.5) },
			expected = "ID cannot represent value: 1.5",
		)
	}

	@Test
	fun valueToLiteral_idReturnsNullForUnrepresentableValues() {
		assertNull(idType.valueToLiteral(true))
		assertNull(idType.valueToLiteral(null))
	}

	// The nullable function reference models two distinct states: no literal coercion at all (a custom
	// scalar) versus a literal coercion that is present and may itself yield null (every built-in).
	@Test
	fun coerceInputLiteral_isAbsentOnCustomScalarsAndPresentOnBuiltins() {
		assertNull(GCustomScalarType(name = "Custom").coerceInputLiteral)

		for (type in listOf(booleanType, floatType, idType, intType, stringType)) {
			assertNotNull(type.coerceInputLiteral, "${type.name} must define a literal coercion")
		}
	}

	@Test
	fun coerceInputValue_customScalarIsIdentityByDefault() {
		val type = GCustomScalarType(name = "Custom")
		val value = listOf(1, 2)
		assertEquals(actual = type.coerceInputValue(value), expected = value)
		assertEquals(actual = type.coerceOutputValue(value), expected = value)
	}

	@Test
	fun valueToLiteral_isAbsentForScalarsThatDefineNone() {
		assertNull(stringType.valueToLiteral("abc"))
		assertNull(GCustomScalarType(name = "Custom").valueToLiteral("abc"))
	}
}
