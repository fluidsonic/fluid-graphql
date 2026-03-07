package io.fluidsonic.graphql


internal object LeafTypeOutputCoercerExtensionKey : GNodeExtensionKey<GOutputCoercer<Any>>


/** The output coercer for this leaf type (scalar or enum), or `null` if none was set. */
public val GLeafType.outputCoercer: GOutputCoercer<Any>?
	get() = extensions[LeafTypeOutputCoercerExtensionKey]


/** The output coercer to attach to this leaf type when building the schema. */
public var GNodeExtensionSet.Builder<GLeafType>.outputCoercer: GOutputCoercer<Any>?
	get() = get(LeafTypeOutputCoercerExtensionKey)
	set(value) {
		set(LeafTypeOutputCoercerExtensionKey, value)
	}
