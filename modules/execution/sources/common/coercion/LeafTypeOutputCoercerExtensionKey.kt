package io.fluidsonic.graphql


internal object LeafTypeOutputCoercerExtensionKey : GNodeExtensionKey<GOutputCoercer<Any>>


val GLeafType.outputCoercer: GOutputCoercer<Any>?
	get() = extensions[LeafTypeOutputCoercerExtensionKey]


var GNodeExtensionSet.Builder<GLeafType>.outputCoercer: GOutputCoercer<Any>?
	get() = get(LeafTypeOutputCoercerExtensionKey)
	set(value) {
		set(LeafTypeOutputCoercerExtensionKey, value)
	}
