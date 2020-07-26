package io.fluidsonic.graphql


internal object LeafTypeNodeInputCoercerExtensionKey : GNodeExtensionKey<GNodeInputCoercer<GValue>>


val GLeafType.nodeInputCoercer: GNodeInputCoercer<GValue>?
	get() = extensions[LeafTypeNodeInputCoercerExtensionKey]


var GNodeExtensionSet.Builder<GLeafType>.nodeInputCoercer: GNodeInputCoercer<GValue>?
	get() = get(LeafTypeNodeInputCoercerExtensionKey)
	set(value) {
		set(LeafTypeNodeInputCoercerExtensionKey, value)
	}
