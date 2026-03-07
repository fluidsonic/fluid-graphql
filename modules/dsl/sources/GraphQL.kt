package io.fluidsonic.graphql


/**
 * Entry point for the fluid-graphql DSL.
 *
 * Use [GraphQL.schema] to build a [GSchema] and [GraphQL.document] to build a [GDocument].
 *
 * ```kotlin
 * val schema = GraphQL.schema {
 *     Query {
 *         "hello" of String
 *     }
 * }
 *
 * val document = GraphQL.document {
 *     query {
 *         "hello"()
 *     }
 * }
 * ```
 */
@GraphQLMarker
public object GraphQL
