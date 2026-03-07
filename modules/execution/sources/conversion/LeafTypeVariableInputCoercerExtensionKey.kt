package io.fluidsonic.graphql


internal object LeafTypeVariableInputCoercerExtensionKey : GNodeExtensionKey<GVariableInputCoercer<Any>>


/** The variable input coercer for this leaf type (scalar or enum), or `null` if none was set. */
public val GLeafType.variableInputCoercer: GVariableInputCoercer<Any>?
	get() = extensions[LeafTypeVariableInputCoercerExtensionKey]


/** The variable input coercer to attach to this leaf type when building the schema. */
public var GNodeExtensionSet.Builder<GLeafType>.variableInputCoercer: GVariableInputCoercer<Any>?
	get() = get(LeafTypeVariableInputCoercerExtensionKey)
	set(value) {
		set(LeafTypeVariableInputCoercerExtensionKey, value)
	}
