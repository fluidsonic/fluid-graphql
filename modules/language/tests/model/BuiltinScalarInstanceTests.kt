/*
 * The deliberate divergence from graphql-js: built-in scalars are per-schema instances rather than global
 * singletons, because fluid attaches coercers per node via `GNodeExtensionSet`. Identity is therefore no
 * longer stable and `GScalarType` carries name-and-class equality instead.
 */
package testing

import io.fluidsonic.graphql.GCustomScalarType
import io.fluidsonic.graphql.GIntType
import io.fluidsonic.graphql.GNodeExtensionKey
import io.fluidsonic.graphql.GNodeExtensionSet
import io.fluidsonic.graphql.GScalarType
import io.fluidsonic.graphql.GSchema
import io.fluidsonic.graphql.GType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNotSame
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.assertTrue

/** Stands in for the execution module's coercer keys, which are `internal` and not visible here. */
private object CoercerKey : GNodeExtensionKey<(Any) -> Any>

/** Parses a fresh schema and returns the `Int` type it resolved for itself. */
private fun intTypeOfFreshSchema(): GScalarType = GSchema.parse("type Query { value: Int }").valueWithoutErrorsOrThrow().resolveType("Int") as GScalarType

class BuiltinScalarInstanceTests {

	@Test
	fun builtinScalars_differentSchemasGetDistinctButEqualInstances() {
		val first = intTypeOfFreshSchema()
		val second = intTypeOfFreshSchema()

		assertNotSame(illegal = first, actual = second)
		assertEquals(actual = second, expected = first)
		assertEquals(actual = second.hashCode(), expected = first.hashCode())
	}

	@Test
	fun builtinScalars_defaultTypesHandsOutFreshInstancesPerCall() {
		val first = GType.defaultTypes()
		val second = GType.defaultTypes()

		assertEquals(actual = first.size, expected = 5)
		assertEquals(actual = second, expected = first)

		for (type in first) {
			assertTrue(second.none { it === type }, "`defaultTypes()` handed out the same instance twice: $type")
		}
	}

	@Test
	fun isSupertypeOf_holdsAcrossSchemas() {
		val first = intTypeOfFreshSchema()
		val second = intTypeOfFreshSchema()

		assertTrue(first.isSupertypeOf(second), "`Int` of one schema must be a supertype of `Int` of another.")
		assertTrue(second.isSubtypeOf(first), "`Int` of one schema must be a subtype of `Int` of another.")
		assertTrue(first.isSupertypeOf(second.nonNullable), "`Int` must be a supertype of another schema's `Int!`.")
	}

	@Test
	fun builtinScalars_oneSchemasInstanceResolvesAMapKeyedByAnothers() {
		val first = intTypeOfFreshSchema()
		val second = intTypeOfFreshSchema()

		assertEquals(actual = mapOf(first to "value")[second], expected = "value")
	}

	@Test
	fun builtinScalars_areNotEqualToACustomScalarOfTheSameName() {
		val builtin = GIntType()
		val custom = GCustomScalarType(name = "Int")

		assertNotEquals<GScalarType>(illegal = builtin, actual = custom)
		assertNotEquals<GScalarType>(illegal = custom, actual = builtin)
	}

	@Test
	fun builtinScalars_attachedExtensionIsInvisibleToAnotherInstance() {
		val coercer: (Any) -> Any = { value -> "coerced $value" }
		val withCoercer = GIntType(extensions = GNodeExtensionSet { this[CoercerKey] = coercer })
		val withoutCoercer = GIntType()

		assertSame(actual = withCoercer[CoercerKey], expected = coercer)
		assertNull(withoutCoercer[CoercerKey], "A coercer attached to one schema's `Int` must not be visible on another's.")
		assertNull(intTypeOfFreshSchema()[CoercerKey], "A freshly built schema's `Int` must carry no coercer from elsewhere.")
	}
}
