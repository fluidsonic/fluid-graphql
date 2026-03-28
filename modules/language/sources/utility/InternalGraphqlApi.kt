package io.fluidsonic.graphql


/** Marks declarations that are internal to the `io.fluidsonic.graphql` library and should not be used externally. */
@RequiresOptIn(
	level = RequiresOptIn.Level.ERROR,
	message = "This is an internal io.fluidsonic.graphql API that should not be used from outside of io.fluidsonic.graphql. " +
		"No compatibility guarantees are provided." +
		"It is recommended to report your use-case of internal API to io.fluidsonic.graphql issue tracker, so stable API could be provided instead."
)
@Retention(value = AnnotationRetention.BINARY)
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY, AnnotationTarget.TYPEALIAS)
public annotation class InternalGraphqlApi
