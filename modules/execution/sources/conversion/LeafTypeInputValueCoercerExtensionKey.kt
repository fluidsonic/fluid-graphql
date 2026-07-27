package io.fluidsonic.graphql

internal object LeafTypeInputValueCoercerExtensionKey : GNodeExtensionKey<GInputValueCoercer<Any>>

/** The variable input coercer attached to this leaf type (scalar or enum), or `null` if none was set. */
public val GLeafType.inputValueCoercer: GInputValueCoercer<Any>?
	get() = extensions[LeafTypeInputValueCoercerExtensionKey]

/** The variable input coercer to attach to this leaf type when building the schema. */
public var GNodeExtensionSet.Builder<GLeafType>.inputValueCoercer: GInputValueCoercer<Any>?
	get() = get(LeafTypeInputValueCoercerExtensionKey)
	set(value) {
		set(LeafTypeInputValueCoercerExtensionKey, value)
	}
