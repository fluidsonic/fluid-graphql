package io.fluidsonic.graphql


/**
 * Context provided to a [GRootResolver] when producing the root value for an operation.
 *
 * Provides access to the broader [GExecutorContext] via [GExecutorContext.Child.execution].
 */
public interface GRootResolverContext : GExecutorContext.Child
