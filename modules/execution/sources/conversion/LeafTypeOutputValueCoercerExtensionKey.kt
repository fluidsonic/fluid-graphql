package io.fluidsonic.graphql

internal object LeafTypeOutputValueCoercerExtensionKey : GNodeExtensionKey<GOutputValueCoercer<Any>>

/** The output coercer attached to this leaf type (scalar or enum), or `null` if none was set. */
public val GLeafType.outputValueCoercer: GOutputValueCoercer<Any>?
	get() = extensions[LeafTypeOutputValueCoercerExtensionKey]

/** The output coercer to attach to this leaf type when building the schema. */
public var GNodeExtensionSet.Builder<GLeafType>.outputValueCoercer: GOutputValueCoercer<Any>?
	get() = get(LeafTypeOutputValueCoercerExtensionKey)
	set(value) {
		set(LeafTypeOutputValueCoercerExtensionKey, value)
	}
