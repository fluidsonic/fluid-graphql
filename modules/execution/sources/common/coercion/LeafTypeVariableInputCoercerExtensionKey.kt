package io.fluidsonic.graphql


internal object LeafTypeVariableInputCoercerExtensionKey : GNodeExtensionKey<GVariableInputCoercer<Any>>


val GLeafType.variableInputCoercer: GVariableInputCoercer<Any>?
	get() = extensions[LeafTypeVariableInputCoercerExtensionKey]


var GNodeExtensionSet.Builder<GLeafType>.variableInputCoercer: GVariableInputCoercer<Any>?
	get() = get(LeafTypeVariableInputCoercerExtensionKey)
	set(value) {
		set(LeafTypeVariableInputCoercerExtensionKey, value)
	}
