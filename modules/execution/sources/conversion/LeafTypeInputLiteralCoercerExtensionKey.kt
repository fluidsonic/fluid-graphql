package io.fluidsonic.graphql

internal object LeafTypeInputLiteralCoercerExtensionKey : GNodeExtensionKey<GInputLiteralCoercer<GValue>>

/** The inline input coercer attached to this leaf type (scalar or enum), or `null` if none was set. */
public val GLeafType.inputLiteralCoercer: GInputLiteralCoercer<GValue>?
	get() = extensions[LeafTypeInputLiteralCoercerExtensionKey]

/** The inline input coercer to attach to this leaf type when building the schema. */
public var GNodeExtensionSet.Builder<GLeafType>.inputLiteralCoercer: GInputLiteralCoercer<GValue>?
	get() = get(LeafTypeInputLiteralCoercerExtensionKey)
	set(value) {
		set(LeafTypeInputLiteralCoercerExtensionKey, value)
	}
