package testing

import io.fluidsonic.graphql.*
import kotlin.test.*


class ExceptionHandlerTests {

	@Test
	fun testHandledExceptionInFieldResolver() = runBlockingTest {
		val testError = GError(message = "test error")
		val testException = TestException(1)

		val schema = graphql.schema {
			val Answer by type
			Scalar(Answer)
			Query { field("echo" of String) { resolve { throw testException } } }
		}

		val exceptions = mutableListOf<Throwable>()
		val result = GExecutor
			.default(
				schema = schema,
				exceptionHandler = { exception ->
					exceptions += exception
					testError
				}
			)
			.execute("{ echo }")

		assertEquals(expected = listOf(testException), actual = exceptions)
		assertEquals(expected = GResult.success(mapOf("echo" to null), listOf(testError)), actual = result)
	}


	@Test
	fun testRethrownExceptionInFieldResolver() = runBlockingTest {
		val testException1 = TestException(1)
		val testException2 = TestException(2)

		val schema = graphql.schema {
			val Answer by type
			Scalar(Answer)
			Query { field("echo" of String) { resolve { throw testException1 } } }
		}

		val thrownException = runCatching {
			GExecutor
				.default(
					schema = schema,
					exceptionHandler = { throw testException2 }
				)
				.execute("{ echo }")
		}.exceptionOrNull()

		assertEquals(expected = testException2, actual = thrownException)
	}


	@Test
	fun testHandledExceptionInNodeInputCoercer() = runBlockingTest {
		val testError = GError(message = "test error")
		val testException = TestException(1)

		val schema = graphql.schema {
			val Answer by type
			Scalar(Answer) { coerceNodeInput { throw testException } }
			Query { field("echo" of String) { argument("input" of !Answer) } }
		}

		val exceptions = mutableListOf<Throwable>()
		val result = GExecutor
			.default(
				schema = schema,
				exceptionHandler = { exception ->
					exceptions += exception
					testError
				}
			)
			.execute("{ echo(input: 42) }")

		assertEquals(expected = listOf(testException), actual = exceptions)
		assertEquals(expected = GResult.success(mapOf("echo" to null), listOf(testError)), actual = result)
	}


	@Test
	fun testRethrownExceptionInNodeInputCoercer() = runBlockingTest {
		val testException1 = TestException(1)
		val testException2 = TestException(2)

		val schema = graphql.schema {
			val Answer by type
			Scalar(Answer) { coerceNodeInput { throw testException1 } }
			Query { field("echo" of String) { argument("input" of !Answer) } }
		}

		val thrownException = runCatching {
			GExecutor
				.default(
					schema = schema,
					exceptionHandler = { throw testException2 }
				)
				.execute("{ echo(input: 42) }")
		}.exceptionOrNull()

		assertEquals(expected = testException2, actual = thrownException)
	}


	@Test
	fun testHandledExceptionInOutputCoercer() = runBlockingTest {
		val testError = GError(message = "test error")
		val testException = TestException(1)

		val schema = graphql.schema {
			val Answer by type
			Scalar(Answer) { coerceOutput { throw testException } }
			Query { field("echo" of Answer) { resolve { 42 } } }
		}

		val exceptions = mutableListOf<Throwable>()
		val result = GExecutor
			.default(
				schema = schema,
				exceptionHandler = { exception ->
					exceptions += exception
					testError
				}
			)
			.execute("{ echo(input: 42) }")

		assertEquals(expected = listOf(testException), actual = exceptions)
		assertEquals(expected = GResult.success(mapOf("echo" to null), listOf(testError)), actual = result)
	}


	@Test
	fun testRethrownExceptionInOutputCoercer() = runBlockingTest {
		val testException1 = TestException(1)
		val testException2 = TestException(2)

		val schema = graphql.schema {
			val Answer by type
			Scalar(Answer) { coerceOutput { throw testException1 } }
			Query { field("echo" of Answer) { resolve { 42 } } }
		}

		val thrownException = runCatching {
			GExecutor
				.default(
					schema = schema,
					exceptionHandler = { throw testException2 }
				)
				.execute("{ echo(input: 42) }")
		}.exceptionOrNull()

		assertEquals(expected = testException2, actual = thrownException)
	}


	@Test
	fun testHandledExceptionInRootResolver() = runBlockingTest {
		val testError = GError(message = "test error")
		val testException = TestException(1)

		val schema = graphql.schema {
			Query { field("echo" of String) }
		}

		val exceptions = mutableListOf<Throwable>()
		val result = GExecutor
			.default(
				schema = schema,
				exceptionHandler = { exception ->
					exceptions += exception
					testError
				},
				rootResolver = object : GRootResolver { // FIXME

					override suspend fun GRootResolverContext.resolveRoot(): Any {
						throw testException
					}
				}
			)
			.execute("{ echo }")

		assertEquals(expected = listOf(testException), actual = exceptions)
		assertEquals(expected = GResult.failure(testError), actual = result)
	}


	@Test
	fun testRethrownExceptionInRootResolver() = runBlockingTest {
		val testException1 = TestException(1)
		val testException2 = TestException(2)

		val schema = graphql.schema {
			Query { field("echo" of String) }
		}

		val thrownException = runCatching {
			GExecutor
				.default(
					schema = schema,
					exceptionHandler = { throw testException2 },
					rootResolver = object : GRootResolver { // FIXME

						override suspend fun GRootResolverContext.resolveRoot(): Any {
							throw testException1
						}
					}
				)
				.execute("{ echo }")
		}.exceptionOrNull()

		assertEquals(expected = testException2, actual = thrownException)
	}


	@Test
	fun testHandledExceptionInVariableInputCoercer() = runBlockingTest {
		val testError = GError(message = "test error")
		val testException = TestException(1)

		val schema = graphql.schema {
			val Answer by type
			Scalar(Answer) { coerceVariableInput { throw testException } }
			Query { field("echo" of String) { argument("input" of !Answer) } }
		}

		val exceptions = mutableListOf<Throwable>()
		val result = GExecutor
			.default(
				schema = schema,
				exceptionHandler = { exception ->
					exceptions += exception
					testError
				}
			)
			.execute("query(\$var: Answer!) { echo(input: \$var) }", variableValues = mapOf("var" to 42))

		assertEquals(expected = listOf(testException), actual = exceptions)
		assertEquals(expected = GResult.failure(testError), actual = result)
	}


	@Test
	fun testRethrownExceptionInVariableInputCoercer() = runBlockingTest {
		val testException1 = TestException(1)
		val testException2 = TestException(2)

		val schema = graphql.schema {
			val Answer by type
			Scalar(Answer) { coerceVariableInput { throw testException1 } }
			Query { field("echo" of String) { argument("input" of !Answer) } }
		}

		val thrownException = runCatching {
			GExecutor
				.default(
					schema = schema,
					exceptionHandler = { throw testException2 }
				)
				.execute("query(\$var: Answer!) { echo(input: \$var) }", variableValues = mapOf("var" to 42))
		}.exceptionOrNull()

		assertEquals(expected = testException2, actual = thrownException)
	}


	private class TestException(val number: Int) : RuntimeException() {

		override fun toString() = "TestException($number)"
	}
}
