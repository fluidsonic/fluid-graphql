package io.fluidsonic.graphql


internal object LeafTypeOutputCoercerExtensionKey : GNodeExtensionKey<GOutputCoercer<Any>>


public val GLeafType.outputCoercer: GOutputCoercer<Any>?
	get() = extensions[LeafTypeOutputCoercerExtensionKey]


public var GNodeExtensionSet.Builder<GLeafType>.outputCoercer: GOutputCoercer<Any>?
	get() = get(LeafTypeOutputCoercerExtensionKey)
	set(value) {
		set(LeafTypeOutputCoercerExtensionKey, value)
	}
