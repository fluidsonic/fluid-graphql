package io.fluidsonic.graphql


/**
 * A typed key for attaching arbitrary metadata to a [GNode] via [GNodeExtensionSet].
 *
 * Create a singleton key by implementing this interface in a companion object or object declaration:
 * ```kotlin
 * object MyKey : GNodeExtensionKey<MyData>
 * ```
 * Then read and write values with `node[MyKey]` or via [GNodeExtensionSet.Builder].
 */
public interface GNodeExtensionKey<Value : Any> {

	public companion object
}
