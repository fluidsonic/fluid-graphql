package io.fluidsonic.graphql


/**
 * DSL marker for the GraphQL document and operation builder DSL.
 *
 * Prevents implicit receiver leakage across nested DSL scopes, ensuring that DSL
 * functions are only callable on the correct receiver.
 */
@DslMarker
@Retention(AnnotationRetention.BINARY)
@Target(AnnotationTarget.CLASS)
public annotation class GraphQLMarker
