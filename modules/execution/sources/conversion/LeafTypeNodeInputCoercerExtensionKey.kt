package io.fluidsonic.graphql


internal object LeafTypeNodeInputCoercerExtensionKey : GNodeExtensionKey<GNodeInputCoercer<GValue>>


public val GLeafType.nodeInputCoercer: GNodeInputCoercer<GValue>?
	get() = extensions[LeafTypeNodeInputCoercerExtensionKey]


public var GNodeExtensionSet.Builder<GLeafType>.nodeInputCoercer: GNodeInputCoercer<GValue>?
	get() = get(LeafTypeNodeInputCoercerExtensionKey)
	set(value) {
		set(LeafTypeNodeInputCoercerExtensionKey, value)
	}
