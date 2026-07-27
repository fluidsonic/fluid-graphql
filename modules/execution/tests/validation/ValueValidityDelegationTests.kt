/*
 * Hand-written expectations for value validation delegating leaf coercion to the scalar types.
 *
 * Unlike the `ValueValidityRule*Errors` fixtures next to this file, NOTHING here is regenerated from a
 * test run: every expected message was written by reading the scalar's own `fail` wording in
 * `modules/language/sources/model/nodes/BuiltinScalarTypes.kt`, and the expected error counts were taken
 * from graphql-js 17.0.2 (`ValuesOfCorrectTypeRule` reports per value node, so sibling values keep being
 * checked after one of them is rejected). If an assertion here fails, read the scalar and decide whether
 * the change of behaviour is wanted — do not paste the actual value back in.
 */
package testing

import io.fluidsonic.graphql.GDocument
import io.fluidsonic.graphql.GError
import io.fluidsonic.graphql.GListTypeRef
import io.fluidsonic.graphql.GSchema
import io.fluidsonic.graphql.GTypeRef
import io.fluidsonic.graphql.GValue
import io.fluidsonic.graphql.GraphQL
import io.fluidsonic.graphql.coerceInputLiteral
import io.fluidsonic.graphql.schema
import io.fluidsonic.graphql.type
import io.fluidsonic.graphql.validate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

private val schema: GSchema = GSchema.parse(
	"""
	input Inp {
		a: Int
		b: Int
	}

	scalar Custom

	# Every built-in scalar has to be referenced: a schema only registers the ones it refers to, and an
	# unresolvable type reference makes `validateValue` pass everything.
	type Query {
		placeholder(boolean: Boolean, float: Float, id: ID): String
	}
	""",
).valueOrThrow()

/** The messages reported for [literal] validated against the type named by [typeName]. */
private fun messagesFor(typeName: String, literal: String): List<String> =
	schema.validateValue(value = GValue.parse(literal).valueOrThrow(), typeRef = GTypeRef(typeName)).map { it.message }

class ValueValidityDelegationTests {

	@Test
	fun validateValue_booleanRejectsAnEnumLiteralWithTheScalarsOwnWording() {
		assertEquals(
			actual = messagesFor(typeName = "Boolean", literal = "VALUE"),
			expected = listOf("Boolean cannot represent a non boolean value: VALUE"),
		)
	}

	@Test
	fun validateValue_floatRejectsAStringLiteralWithTheScalarsOwnWording() {
		assertEquals(
			actual = messagesFor(typeName = "Float", literal = "\"s\""),
			expected = listOf("Float cannot represent non numeric value: \"s\""),
		)
	}

	@Test
	fun validateValue_idRejectsAFloatLiteralWithTheScalarsOwnWording() {
		assertEquals(
			actual = messagesFor(typeName = "ID", literal = "1.5"),
			expected = listOf("ID cannot represent a non-string and non-integer value: 1.5"),
		)
	}

	@Test
	fun validateValue_intRejectsAFloatLiteralWithTheScalarsOwnWording() {
		assertEquals(
			actual = messagesFor(typeName = "Int", literal = "1.5"),
			expected = listOf("Int cannot represent non-integer value: 1.5"),
		)
	}

	@Test
	fun validateValue_stringRejectsAnIntLiteralWithTheScalarsOwnWording() {
		assertEquals(
			actual = messagesFor(typeName = "String", literal = "1"),
			expected = listOf("String cannot represent a non string value: 1"),
		)
	}

	// The accept-everything arm belongs to `GCustomScalarType` alone. A built-in handed a literal kind no
	// arm mentions must still be rejected rather than falling through to it.
	@Test
	fun validateValue_builtinRejectsAnInputObjectLiteralInsteadOfFallingThroughToTheCustomScalarArm() {
		assertEquals(
			actual = messagesFor(typeName = "Boolean", literal = "{}"),
			expected = listOf("Boolean cannot represent a non boolean value: {}"),
		)
	}

	@Test
	fun validateValue_aCustomScalarWithoutACoercerStillAcceptsEveryLiteral() {
		assertEquals(actual = messagesFor(typeName = "Custom", literal = "{}"), expected = emptyList())
		assertEquals(actual = messagesFor(typeName = "Custom", literal = "[1]"), expected = emptyList())
		assertEquals(actual = messagesFor(typeName = "Custom", literal = "VALUE"), expected = emptyList())
	}

	@Test
	fun validateValue_reportsEveryRejectedFieldOfAnInputObject() {
		assertEquals(
			actual = schema
				.validateValue(value = GValue.parse("""{a: "x", b: "y"}""").valueOrThrow(), typeRef = GTypeRef("Inp"))
				.map { it.message },
			expected = listOf(
				"Int cannot represent non-integer value: \"x\"",
				"Int cannot represent non-integer value: \"y\"",
			),
		)
	}

	@Test
	fun validateValue_reportsEveryRejectedItemOfAList() {
		assertEquals(
			actual = schema
				.validateValue(value = GValue.parse("""["x", "y", "z"]""").valueOrThrow(), typeRef = GListTypeRef(GTypeRef("Int")))
				.map { it.message },
			expected = listOf(
				"Int cannot represent non-integer value: \"x\"",
				"Int cannot represent non-integer value: \"y\"",
				"Int cannot represent non-integer value: \"z\"",
			),
		)
	}

	// A null element inside a list whose element type is non-null. The rejection comes from the non-null
	// guard rather than from `Int`'s coercion, so it carries the generic wording and not a scalar message —
	// which is exactly why it needs its own case: every other `[Int!]` case in the big fixture supplies a
	// non-list value and therefore never reaches the element at all.
	@Test
	fun validateValue_rejectsANullElementOfANonNullElementType() {
		assertEquals(
			actual = schema
				.validateValue(value = GValue.parse("[1, null, 3]").valueOrThrow(), typeRef = GListTypeRef(GTypeRef("Int").nonNullableRef))
				.map { it.message },
			expected = listOf("Type 'Int' does not allow value 'null'."),
		)
	}

	// A coercer attached to a scalar is what the executor applies, so the document validator must apply it
	// too — otherwise a literal passes validation and then fails during execution.
	@Test
	fun validate_appliesTheCoercerAttachedToAScalarAndReportsEveryRejectedValue() {
		val schema = GraphQL.schema {
			val Custom by type

			Scalar(Custom) { coerceInputLiteral { GError(message = "no such thing").throwException() } }

			Query {
				field("custom" of String) {
					argument("a" of Custom)
					argument("b" of Custom)
					argument("c" of Custom)
				}
			}
		}

		assertEquals(
			actual = GDocument.parse("{ custom(a: 1, b: 2, c: 3) }").valueOrThrow().validate(schema).map { it.message },
			expected = listOf("no such thing", "no such thing", "no such thing"),
		)
	}

	// A coercer that raises a structured error keeps that structure. Validation adds where the literal was
	// written; it must not rebuild the error from the message alone, or a client loses the machine-readable
	// part while the human-readable part survives — the wrong half to keep.
	//
	// graphql@17.0.2 also preserves the extensions here. It does *not* attach a location, though its own
	// built-in rejections through the same rule do — verified both ways. fluid attaches one in both cases,
	// because an error whose location depends on who raised it is worse than one that always has it.
	@Test
	fun validate_keepsTheExtensionsOfAnErrorRaisedByAnAttachedCoercer() {
		val schema = GraphQL.schema {
			val Custom by type

			Scalar(Custom) {
				coerceInputLiteral {
					GError(message = "no such thing", extensions = mapOf("code" to "BAD_CUSTOM", "retryable" to false)).throwException()
				}
			}

			Query {
				field("custom" of String) { argument("a" of Custom) }
			}
		}

		val error = GDocument.parse("{ custom(a: 1) }").valueOrThrow().validate(schema).single()

		assertEquals(actual = error.message, expected = "no such thing")
		assertEquals(actual = error.extensions, expected = mapOf("code" to "BAD_CUSTOM", "retryable" to false))
		assertTrue(error.nodes.isNotEmpty(), "validation must still say where the literal was written")
	}

	// An arbitrary exception must not escape validation, whose contract is to *return* the errors it found.
	// graphql-js wraps it in the same way; note that the executor deliberately does NOT, because there a
	// non-`GErrorException` throwable belongs to the consumer's `GExceptionHandler`.
	@Test
	fun validate_wrapsANonGraphqlExceptionThrownByAnAttachedCoercer() {
		val schema = GraphQL.schema {
			val Custom by type

			Scalar(Custom) { coerceInputLiteral { throw IllegalStateException("boom") } }

			Query {
				field("custom" of String) {
					argument("a" of Custom)
					argument("b" of Custom)
				}
			}
		}

		assertEquals(
			actual = GDocument.parse("{ custom(a: 1, b: \"two\") }").valueOrThrow().validate(schema).map { it.message },
			expected = listOf(
				"""Expected value of type "Custom", but encountered error "boom"; found: 1.""",
				"""Expected value of type "Custom", but encountered error "boom"; found: "two".""",
			),
		)
	}
}
