package io.fluidsonic.graphql


internal object LeafTypeVariableInputCoercerExtensionKey : GNodeExtensionKey<GVariableInputCoercer<Any>>


public val GLeafType.variableInputCoercer: GVariableInputCoercer<Any>?
	get() = extensions[LeafTypeVariableInputCoercerExtensionKey]


public var GNodeExtensionSet.Builder<GLeafType>.variableInputCoercer: GVariableInputCoercer<Any>?
	get() = get(LeafTypeVariableInputCoercerExtensionKey)
	set(value) {
		set(LeafTypeVariableInputCoercerExtensionKey, value)
	}
