package io.fluidsonic.graphql


/**
 * Typed key for storing and retrieving a value of type [Value] in a [GExecutorContextExtensionSet].
 *
 * Implement this interface as an object to create a unique key:
 * ```kotlin
 * object MyKey : GExecutorContextExtensionKey<MyValue>
 * ```
 */
public interface GExecutorContextExtensionKey<Value : Any> {

	public companion object
}
