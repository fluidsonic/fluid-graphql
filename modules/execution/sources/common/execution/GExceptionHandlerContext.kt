package io.fluidsonic.graphql


public interface GExceptionHandlerContext {

	@SchemaBuilderKeywordB // FIXME
	public val origin: GExceptionOrigin
}


@SchemaBuilderKeywordB // FIXME
public val GExceptionHandlerContext.execution: GExecutorContext
	get() = when (val origin = origin) {
		is GExceptionOrigin.NodeInputCoercer -> origin.context.execution
		is GExceptionOrigin.VariableInputCoercer -> origin.context.execution
		is GExceptionOrigin.OutputCoercer -> origin.context.execution
		is GExceptionOrigin.FieldResolver -> origin.context.execution
		is GExceptionOrigin.RootResolver -> origin.context.execution
	}
