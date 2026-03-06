package testing

import io.fluidsonic.graphql.*
import kotlin.test.*
import kotlinx.coroutines.test.*

class ExceptionHandlerTests {

	@Test
	fun testHandledExceptionInFieldResolver() = runTest {
		val testError1 = GError(message = "test 1", path = GPath.ofName("foo"))
		val testError2 = GError(message = "test 2")
		val testException = TestException(1)

		val exceptions = mutableListOf<Throwable>()
		val result = GExecutor
			.default(
				schema = GraphQL.schema {
					Query {
						field("foo" of String) { resolve { throw testException } }
						field("bar" of String) { resolve { testError2.throwException() } }
						field("baz" of String) { resolve { "success" } }
					}
				},
				exceptionHandler = { exception ->
					exceptions += exception
					testError1
				}
			)
			.execute("{ foo bar baz }")

		assertEquals(expected = listOf<Throwable>(testException), actual = exceptions)
		assertEquals(
			expected = GResult.success(
				value = mapOf("foo" to null, "bar" to null, "baz" to "success"),
				errors = listOf(testError1, testError2)
			),
			actual = result
		)
	}


	@Test
	fun testRethrownExceptionInFieldResolver() = runTest {
		val testError = GError(message = "test")
		val testException = TestException(1)

		val thrownException = kotlin.runCatching {
			GExecutor
				.default(
					schema = GraphQL.schema {
						Query {
							field("foo" of String) { resolve { throw testException } }
							field("bar" of String) { resolve { testError.throwException() } }
							field("baz" of String) { resolve { "success" } }
						}
					},
					exceptionHandler = { throw it }
				)
				.execute("{ foo bar baz }")
		}.exceptionOrNull()

		assertEquals(expected = testException, actual = thrownException)
	}


	@Test
	fun testHandledExceptionInNodeInputCoercer() = runTest {
		val testError1 = GError(message = "test 1", path = GPath.ofName("foo"))
		val testError2 = GError(message = "test 2")
		val testException = TestException(1)

		val exceptions = mutableListOf<Throwable>()
		val result = GExecutor
			.default(
				schema = GraphQL.schema {
					val Foo by type
					val Bar by type
					Scalar(Foo) { coerceNodeInput { throw testException } }
					Scalar(Bar) { coerceNodeInput { testError2.throwException() } }
					Query {
						field("foo" of String) { argument("arg" of Foo) }
						field("bar" of String) { argument("arg" of Bar) }
						field("baz" of String) { resolve { "success" } }
					}
				},
				exceptionHandler = { exception ->
					exceptions += exception
					testError1
				}
			)
			.execute("{ foo(arg:42) bar(arg:42) baz }")

		assertEquals(expected = listOf<Throwable>(testException), actual = exceptions)
		assertEquals(
			expected = GResult.success(
				value = mapOf("foo" to null, "bar" to null, "baz" to "success"),
				errors = listOf(testError1, testError2)
			),
			actual = result
		)
	}


	@Test
	fun testRethrownExceptionInNodeInputCoercer() = runTest {
		val testError = GError(message = "test")
		val testException = TestException(1)

		val thrownException = kotlin.runCatching {
			GExecutor
				.default(
					schema = GraphQL.schema {
						val Foo by type
						val Bar by type
						Scalar(Foo) { coerceNodeInput { throw testException } }
						Scalar(Bar) { coerceNodeInput { testError.throwException() } }
						Query {
							field("foo" of String) { argument("arg" of Foo) }
							field("bar" of String) { argument("arg" of Bar) }
							field("baz" of String) { resolve { "success" } }
						}
					},
					exceptionHandler = { throw it }
				)
				.execute("{ foo(arg:42) bar(arg:42) baz }")
		}.exceptionOrNull()

		assertEquals(expected = testException, actual = thrownException)
	}


	@Test
	fun testHandledExceptionInOutputCoercer() = runTest {
		val testError1 = GError(message = "test 1", path = GPath.ofName("foo"))
		val testError2 = GError(message = "test 2")
		val testException = TestException(1)

		val exceptions = mutableListOf<Throwable>()
		val result = GExecutor
			.default(
				schema = GraphQL.schema {
					val Foo by type
					val Bar by type
					Scalar(Foo) { coerceOutput { throw testException } }
					Scalar(Bar) { coerceOutput { testError2.throwException() } }
					Query {
						field("foo" of Foo) { resolve { "foo" } }
						field("bar" of Bar) { resolve { "bar" } }
						field("baz" of String) { resolve { "success" } }
					}
				},
				exceptionHandler = { exception ->
					exceptions += exception
					testError1
				}
			)
			.execute("{ foo(arg:42) bar(arg:42) baz }")

		assertEquals(expected = listOf<Throwable>(testException), actual = exceptions)
		assertEquals(
			expected = GResult.success(
				value = mapOf("foo" to null, "bar" to null, "baz" to "success"),
				errors = listOf(testError1, testError2)
			),
			actual = result
		)
	}


	@Test
	fun testRethrownExceptionInOutputCoercer() = runTest {
		val testError = GError(message = "test")
		val testException = TestException(1)

		val thrownException = kotlin.runCatching {
			GExecutor
				.default(
					schema = GraphQL.schema {
						val Foo by type
						val Bar by type
						Scalar(Foo) { coerceOutput { throw testException } }
						Scalar(Bar) { coerceOutput { testError.throwException() } }
						Query {
							field("foo" of Foo) { resolve { "foo" } }
							field("bar" of Bar) { resolve { "bar" } }
							field("baz" of String) { resolve { "success" } }
						}
					},
					exceptionHandler = { throw it }
				)
				.execute("{ foo bar baz }")
		}.exceptionOrNull()

		assertEquals(expected = testException, actual = thrownException)
	}


	@Test
	fun testHandledExceptionInRootResolver() = runTest {
		val testError = GError(message = "test", path = GPath.ofName("foo"))
		val testException = TestException(1)

		val exceptions = mutableListOf<Throwable>()
		val result = GExecutor
			.default(
				schema = GraphQL.schema {
					Query { field("foo" of String) }
				},
				exceptionHandler = { exception ->
					exceptions += exception
					testError
				},
				rootResolver = object : GRootResolver { // TODO https://youtrack.jetbrains.com/issue/KT-40165

					override suspend fun GRootResolverContext.resolveRoot(): Any {
						throw testException
					}
				}
			)
			.execute("{ foo }")

		assertEquals(expected = listOf<Throwable>(testException), actual = exceptions)
		assertEquals(
			expected = GResult.failure(
				errors = listOf(testError)
			),
			actual = result
		)
	}


	@Test
	fun testIgnoresErrorExceptionInRootResolver() = runTest {
		val testError = GError(message = "test")

		val result = GExecutor
			.default(
				schema = GraphQL.schema {
					Query { field("foo" of String) }
				},
				exceptionHandler = { error("Shouldn't be called.") },
				rootResolver = object : GRootResolver { // FIXME

					override suspend fun GRootResolverContext.resolveRoot(): Any {
						testError.throwException()
					}
				}
			)
			.execute("{ foo }")

		assertEquals(
			expected = GResult.failure(
				errors = listOf(testError)
			),
			actual = result
		)
	}


	@Test
	fun testRethrownExceptionInRootResolver() = runTest {
		val testException = TestException(1)

		val thrownException = kotlin.runCatching {
			GExecutor
				.default(
					schema = GraphQL.schema {
						Query { field("foo" of String) }
					},
					exceptionHandler = { throw it },
					rootResolver = object : GRootResolver { // TODO https://youtrack.jetbrains.com/issue/KT-40165

						override suspend fun GRootResolverContext.resolveRoot(): Any {
							throw testException
						}
					}
				)
				.execute("{ foo }")
		}.exceptionOrNull()

		assertEquals(expected = testException, actual = thrownException)
	}


	@Test
	fun testHandledExceptionInVariableInputCoercer() = runTest {
		val testError = GError(message = "test", path = GPath.ofName("foo"))
		val testException = TestException(1)

		val exceptions = mutableListOf<Throwable>()
		val result = GExecutor
			.default(
				schema = GraphQL.schema {
					val Foo by type
					Scalar(Foo) { coerceVariableInput { throw testException } }
					Query {
						field("foo" of String) { argument("arg" of Foo) }
						field("bar" of String) { resolve { "success" } }
					}
				},
				exceptionHandler = { exception ->
					exceptions += exception
					testError
				}
			)
			.execute(
				documentSource = "query(\$foo: Foo!) { foo(arg:\$foo) bar }",
				variableValues = mapOf("foo" to "foo")
			)

		assertEquals(expected = listOf<Throwable>(testException), actual = exceptions)
		assertEquals(
			expected = GResult.failure(
				errors = listOf(testError)
			),
			actual = result
		)
	}


	@Test
	fun testIgnoresErrorExceptionsInVariableInputCoercer() = runTest {
		val testError = GError(message = "test")

		val result = GExecutor
			.default(
				schema = GraphQL.schema {
					val Foo by type
					Scalar(Foo) { coerceVariableInput { testError.throwException() } }
					Query {
						field("foo" of String) { argument("arg" of Foo) }
						field("bar" of String) { resolve { "success" } }
					}
				},
				exceptionHandler = { error("Shouldn't be called.") }
			)
			.execute(
				documentSource = "query(\$foo: Foo!) { foo(arg:\$foo) bar }",
				variableValues = mapOf("foo" to "foo")
			)

		assertEquals(
			expected = GResult.failure(
				errors = listOf(testError)
			),
			actual = result
		)
	}


	@Test
	fun testRethrownExceptionInVariableInputCoercer() = runTest {
		val testException = TestException(1)

		val thrownException = kotlin.runCatching {
			GExecutor
				.default(
					schema = GraphQL.schema {
						val Foo by type
						Scalar(Foo) { coerceVariableInput { throw testException } }
						Query {
							field("foo" of String) { argument("arg" of Foo) }
							field("bar" of String) { resolve { "success" } }
						}
					},
					exceptionHandler = { throw it }
				)
				.execute(
					documentSource = "query(\$foo: Foo!) { foo(arg:\$foo) bar }",
					variableValues = mapOf("foo" to "foo")
				)
		}.exceptionOrNull()

		assertEquals(expected = testException, actual = thrownException)
	}


	private class TestException(val number: Int) : RuntimeException() {

		override fun toString() = "TestException($number)"
	}
}
