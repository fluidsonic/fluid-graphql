package io.fluidsonic.graphql


internal object LeafTypeNodeInputCoercerExtensionKey : GNodeExtensionKey<GNodeInputCoercer<GValue>>


/** The inline input coercer for this leaf type (scalar or enum), or `null` if none was set. */
public val GLeafType.nodeInputCoercer: GNodeInputCoercer<GValue>?
	get() = extensions[LeafTypeNodeInputCoercerExtensionKey]


/** The inline input coercer to attach to this leaf type when building the schema. */
public var GNodeExtensionSet.Builder<GLeafType>.nodeInputCoercer: GNodeInputCoercer<GValue>?
	get() = get(LeafTypeNodeInputCoercerExtensionKey)
	set(value) {
		set(LeafTypeNodeInputCoercerExtensionKey, value)
	}
