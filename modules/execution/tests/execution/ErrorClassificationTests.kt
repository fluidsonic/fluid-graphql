package testing

import io.fluidsonic.graphql.GDocument
import io.fluidsonic.graphql.GError
import io.fluidsonic.graphql.GExecutor
import io.fluidsonic.graphql.GFieldResolver
import io.fluidsonic.graphql.GSchema
import io.fluidsonic.graphql.GVariableRef
import io.fluidsonic.graphql.GraphQL
import io.fluidsonic.graphql.Object
import io.fluidsonic.graphql.default
import io.fluidsonic.graphql.resolve
import io.fluidsonic.graphql.schema
import io.fluidsonic.graphql.type
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.test.fail

// Every failure raised during execution falls into exactly one of two groups, and the group decides
// whether it becomes part of the GraphQL response or escapes as a JVM exception.
//
// What settles the group is *where the offending input came from*, not how serious the failure looks:
//
//  - from the **schema** (a declared type that does not resolve, a field typed with an input type) →
//    the schema is broken → **Group A, throw**. graphql-js asserts the schema is valid before
//    `execute` does anything, so every schema defect throws out of `execute` there too.
//  - from a **caller of an internal API** (an empty selection list, `unwrap()` on a variable
//    reference) → **Group A, throw**. No upstream analogue; nothing a client can reach.
//  - from the **document or the variable values** (a variable's declared type, an argument literal, a
//    variable value) → **Group B, a `GError` in the response**, request error or field error per the
//    spec.
//
// Group A throws `IllegalStateException`/`IllegalArgumentException` and travels straight out of
// `execute(...)`; the catch boundaries in the executor catch `GErrorException` only, so nothing
// swallows it on the way. Reporting those as GraphQL errors would answer HTTP 200 with a polite
// message for what is a wiring bug, and would leak internal detail to untrusted clients.
//
// Two pairs look nearly identical in the code and land on opposite sides of the line:
//
//  - an *argument* definition's type is schema-derived (Group A); a *variable* definition's type is
//    document-derived (Group B), even though both fail with "Type 'X' cannot be resolved".
//  - "the schema declares type `Foo` and no such type exists" is Group A; "this runtime object
//    matches none of the possible types" is Group B, because it depends on what a resolver returned.
class ErrorClassificationTests {

	private data class Dog(val name: String = "Rex")

	/** Executes [documentSource] without validating it, so the executor sees the document as-is. */
	private suspend fun execute(schema: GSchema, documentSource: String, variableValues: Map<String, Any?> = emptyMap()) =
		GExecutor.default(schema = schema).let { executor ->
			executor.execute(GDocument.parse(documentSource).valueOrThrow(), variableValues = variableValues)
		}

	/** Fails unless the execution returns its errors in the result rather than throwing them. */
	// Catching Throwable is the point: the assertion is that *nothing at all* escapes for a Group B site.
	@Suppress("TooGenericExceptionCaught")
	private suspend fun assertErrorsAreReturned(schema: GSchema, documentSource: String, variableValues: Map<String, Any?> = emptyMap()) {
		val executor = GExecutor.default(schema = schema)
		val document = GDocument.parse(documentSource).valueOrThrow()

		val result = try {
			executor.execute(document, variableValues = variableValues)
		} catch (throwable: Throwable) {
			fail("execution threw ${throwable::class.simpleName} instead of returning errors: $throwable")
		}

		assertTrue(
			result.errors.isNotEmpty(),
			"expected the result to carry errors but got: $result",
		)
		// Serialization must survive too — raptor always calls it.
		executor.serializeResult(result)
	}

	/** Fails unless the execution returns request errors and the serialized response omits `"data"` entirely. */
	@Suppress("TooGenericExceptionCaught")
	private suspend fun assertRequestErrorIsReturned(schema: GSchema, documentSource: String, variableValues: Map<String, Any?> = emptyMap()) {
		val executor = GExecutor.default(schema = schema)
		val document = GDocument.parse(documentSource).valueOrThrow()

		val result = try {
			executor.execute(document, variableValues = variableValues)
		} catch (throwable: Throwable) {
			fail("execution threw ${throwable::class.simpleName} instead of returning errors: $throwable")
		}

		assertTrue(result.errors.isNotEmpty(), "expected the result to carry errors but got: $result")
		assertTrue(result.errors.all { it.isRequestError }, "expected only request errors but got: ${result.errors}")

		val serialized = executor.serializeResult(result)
		assertFalse(serialized.containsKey("data"), "expected the 'data' key to be absent but got: $serialized")
	}

	// --- Group A — developer errors escape as exceptions.

	// A field with no resolver is a wiring bug in the application. There is deliberately no
	// property-lookup default resolver: Kotlin is not a dynamic language, so resolving by name is not
	// the right shape here. graphql@17.0.2 answers `{"data":{"noResolver":null}}` only because its
	// `defaultFieldResolver` succeeds at returning `undefined`, not because it treats this as an error.
	@Test
	fun testFieldWithoutResolver_throws() = runTest {
		val schema = GraphQL.schema {
			Query {
				field("foo" of String)
			}
		}

		val exception = assertFailsWith<IllegalStateException> {
			execute(schema, "{ foo }")
		}

		assertEquals(actual = exception.message, expected = "No resolver is set for field 'Query.foo'.")
	}

	@Test
	fun testResolverDelegatingPastEndOfChain_throws() = runTest {
		val schema = GraphQL.schema {
			Query {
				field("foo" of String) { resolve { next() } }
			}
		}

		val exception = assertFailsWith<IllegalStateException> {
			execute(schema, "{ foo }")
		}

		assertEquals(actual = exception.message, expected = "Resolver of field 'Query.foo' cannot delegate resolution any further.")
	}

	// A field declaring a type the schema does not define cannot even be built upstream:
	// `buildSchema('type Query { foo: Undefined }')` throws `Unknown type "Undefined".`
	@Test
	fun testUnresolvableFieldType_throws() = runTest {
		val schema = GSchema.parse("type Query { foo: Undefined }").valueOrThrow()

		val exception = assertFailsWith<IllegalStateException> {
			execute(schema, "{ foo }")
		}

		assertEquals(actual = exception.message, expected = "Cannot resolve type 'Undefined' of field 'foo' in 'Query'.")
	}

	@Test
	fun testInputObjectAsOutputType_throws() = runTest {
		val schema = GSchema.parse(
			"""
			|type Query { foo: Input }
			|input Input { bar: String }
			""".trimMargin(),
		).valueOrThrow()
		// A resolver is needed for the field to produce a value at all — the output type is only
		// inspected once there is something to complete.
		val executor = GExecutor.default(schema = schema, fieldResolver = GFieldResolver<Any> { mapOf("bar" to "baz") })
		val document = GDocument.parse("{ foo { bar } }").valueOrThrow()

		val exception = assertFailsWith<IllegalStateException> {
			executor.execute(document)
		}

		assertEquals(actual = exception.message, expected = "Field 'Query.foo' must have an output type but has input type 'Input'.")
	}

	@Test
	fun testUnresolvableArgumentType_throws() = runTest {
		val schema = GSchema.parse("type Query { foo(arg: Undefined): String }").valueOrThrow()

		val exception = assertFailsWith<IllegalStateException> {
			execute(schema, """{ foo(arg: "x") }""")
		}

		assertTrue(
			exception.message.orEmpty().startsWith(
				"There is an error in the document. It should be validated before use:\nType 'Undefined' cannot be resolved.",
			),
			"unexpected message: ${exception.message}",
		)
	}

	@Test
	fun testIntrospectionOfUnresolvableFieldType_throws() = runTest {
		val schema = GSchema.parse("type Query { foo: Undefined }").valueOrThrow()

		val exception = assertFailsWith<IllegalStateException> {
			execute(schema, "{ __schema { types { fields { type { name } } } } }")
		}

		assertEquals(
			actual = exception.message,
			expected = "Introspection cannot resolve type 'Undefined'. The schema should be validated before execution.",
		)
	}

	@Test
	fun testIntrospectionOfUnresolvableArgumentType_throws() = runTest {
		val schema = GSchema.parse("type Query { foo(arg: Undefined): String }").valueOrThrow()

		val exception = assertFailsWith<IllegalStateException> {
			execute(schema, "{ __schema { types { fields { args { type { name } } } } } }")
		}

		assertEquals(
			actual = exception.message,
			expected = "Introspection cannot resolve type 'Undefined'. The schema should be validated before execution.",
		)
	}

	@Test
	fun testIntrospectionOfUnresolvableInterface_throws() = runTest {
		val schema = GSchema.parse("type Query implements Undefined { foo: String }").valueOrThrow()

		val exception = assertFailsWith<IllegalStateException> {
			execute(schema, "{ __schema { types { interfaces { name } } } }")
		}

		assertEquals(
			actual = exception.message,
			expected = "Introspection cannot resolve type 'Undefined'. The schema should be validated before execution.",
		)
	}

	@Test
	fun testUnwrappingVariableRef_throws() {
		val exception = assertFailsWith<IllegalStateException> {
			GVariableRef("foo").unwrap()
		}

		assertEquals(actual = exception.message, expected = "Cannot unwrap a GraphQL variable: foo")
	}

	// --- Group B — client errors are returned in the result.

	// Which concrete type a runtime value has depends entirely on what the resolver returned, so this
	// is a field error. graphql@17.0.2 answers `{"data":{"pet":null}}` plus
	// `Abstract type "Pet" must resolve to an Object type at runtime …`.
	@Test
	fun testUnresolvableAbstractTypeForRuntimeValue_returnsErrors() = runTest {
		val schema = GraphQL.schema {
			val Animal by type
			val Cat by type

			Interface(Animal) {
				field("name" of String)
			}

			Object<Dog>(Cat implements Animal) {
				field("name" of String) { resolve { it.name } }
			}

			Query {
				field("animal" of Animal) { resolve<Any> { "not an object at all" } }
			}
		}

		assertErrorsAreReturned(schema, "{ animal { name } }")
	}

	// Output coercion of a resolver's return value. graphql@17.0.2 answers `{"data":{"n":null}}` plus
	// `Int cannot represent non-integer value: {}`.
	@Test
	fun testResolverReturningUncoercibleValue_returnsErrors() = runTest {
		val schema = GraphQL.schema {
			Query {
				field("foo" of String) { resolve<Any> { Dog() } }
			}
		}

		assertErrorsAreReturned(schema, "{ foo }")
	}

	@Test
	fun testNullForNonNullField_returnsErrors() = runTest {
		val schema = GraphQL.schema {
			Query {
				field("foo" of !String) { resolve<Any?> { null } }
			}
		}

		assertErrorsAreReturned(schema, "{ foo }")
	}

	@Test
	fun testUncoercibleArgumentValue_returnsErrors() = runTest {
		val schema = GraphQL.schema {
			Query {
				field("foo" of String) {
					argument("arg" of String)

					resolve { "bar" }
				}
			}
		}

		assertErrorsAreReturned(schema, "{ foo(arg: 42) }")
	}

	// A variable definition lives in the *document*, so a client causes this simply by sending a type
	// name the schema does not define. https://spec.graphql.org/draft/#sec-Coercing-Variable-Values
	// requires a request error, and graphql@17.0.2 agrees: `execute query ($x: Undefined) { f }`
	// answers `Variable "$x" expected value of type "Undefined" which cannot be used as an input type.`
	// with no "data" key. Contrast `testUnresolvableArgumentType_throws`: an argument *definition* comes
	// from the schema, so the same-looking failure is a broken schema and throws.
	@Test
	fun testUnresolvableVariableType_returnsRequestError() = runTest {
		val schema = GSchema.parse("type Query { foo(arg: String): String }").valueOrThrow()

		assertRequestErrorIsReturned(schema, "query (\$v: Undefined) { foo(arg: \$v) }", variableValues = mapOf("v" to "x"))
	}

	// Same origin, same outcome: the type resolves but is not an input type. graphql@17.0.2 answers
	// `Variable "$x" expected value of type "Query" which cannot be used as an input type.`
	@Test
	fun testOutputTypeAsVariableType_returnsRequestError() = runTest {
		val schema = GSchema.parse("type Query { foo(arg: String): String }").valueOrThrow()

		assertRequestErrorIsReturned(schema, "query (\$v: Query) { foo(arg: \$v) }", variableValues = mapOf("v" to "x"))
	}

	@Test
	fun testUncoercibleVariableValue_returnsErrors() = runTest {
		val schema = GraphQL.schema {
			Query {
				field("foo" of String) {
					argument("arg" of String)

					resolve { "bar" }
				}
			}
		}

		assertErrorsAreReturned(schema, "query (\$v: String) { foo(arg: \$v) }", variableValues = mapOf("v" to 42))
	}

	@Test
	fun testUnknownOperationName_returnsErrors() = runTest {
		val schema = GraphQL.schema {
			Query {
				field("foo" of String) { resolve { "bar" } }
			}
		}
		val executor = GExecutor.default(schema = schema)
		val document = GDocument.parse("query Known { foo }").valueOrThrow()

		val result = executor.execute(document, operationName = "Unknown")

		assertTrue(result.errors.isNotEmpty(), "expected the result to carry errors but got: $result")
	}

	@Test
	fun testMutationAgainstQueryOnlySchema_returnsErrors() = runTest {
		val schema = GraphQL.schema {
			Query {
				field("foo" of String) { resolve { "bar" } }
			}
		}

		assertErrorsAreReturned(schema, "mutation { foo }")
	}

	@Test
	fun testSubscription_returnsErrors() = runTest {
		val schema = GSchema.parse(
			"""
			|type Query { foo: String }
			|type Subscription { foo: String }
			""".trimMargin(),
		).valueOrThrow()

		assertErrorsAreReturned(schema, "subscription { foo }")
	}

	@Test
	fun testSubscriptionErrorIsARequestError() = runTest {
		val schema = GSchema.parse(
			"""
			|type Query { foo: String }
			|type Subscription { foo: String }
			""".trimMargin(),
		).valueOrThrow()
		val executor = GExecutor.default(schema = schema)
		val document = GDocument.parse("subscription { foo }").valueOrThrow()

		val result = executor.execute(document)
		assertTrue(result.errors.all { it.isRequestError }, "expected request errors but got: ${result.errors}")

		val serialized = executor.serializeResult(result)
		assertTrue(!serialized.containsKey("data"), "expected the 'data' key to be absent but got: $serialized")
	}

	// A resolver raising a GError directly stays a field error — the executor never reclassifies it.
	@Test
	fun testFieldResolverError_staysAFieldError() = runTest {
		val schema = GraphQL.schema {
			Query {
				field("foo" of String) { resolve { GError(message = "boom").throwException() } }
			}
		}
		val executor = GExecutor.default(schema = schema)
		val result = executor.serializeResult(executor.execute("{ foo }"))

		assertTrue(result.containsKey("data"), "expected the 'data' key to be present but got: $result")
	}

	// --- Neither group: a selection the schema cannot make sense of is skipped, not reported.

	@Test
	fun testUnresolvableFragmentTypeCondition_isSkipped() = runTest {
		val schema = GraphQL.schema {
			Query {
				field("foo" of String) { resolve { "bar" } }
			}
		}

		// graphql-js routes named and inline fragments alike through `doesFragmentConditionMatch`,
		// which simply does not match when the type condition names no type. `SkippedSelectionTests`
		// pins the resulting response shape.
		val result = execute(schema, "{ foo ...F } fragment F on Undefined { foo }")

		assertTrue(result.errors.isEmpty(), "expected the selection to be skipped without errors but got: $result")
	}
}
