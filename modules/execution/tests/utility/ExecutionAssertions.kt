package testing

import io.fluidsonic.graphql.*
import kotlin.test.*


internal suspend fun assertExecution(
	schema: GSchema,
	document: String,
	variableValues: Map<String, Any?> = emptyMap(),
	operationName: String? = null,
	expected: Map<String, Any?>,
) {
	val executor = GExecutor.default(schema = schema)
	val result = executor.serializeResult(
		executor.execute(document, operationName = operationName, variableValues = variableValues)
	)
	assertEquals(expected = expected, actual = result)
}


internal suspend fun assertExecutionErrors(
	schema: GSchema,
	document: String,
	variableValues: Map<String, Any?> = emptyMap(),
	operationName: String? = null,
	expectedErrors: List<String>,
) {
	val executor = GExecutor.default(schema = schema)
	val result = executor.execute(document, operationName = operationName, variableValues = variableValues)
	assertErrors(expected = expectedErrors, actual = result.errors)
}
