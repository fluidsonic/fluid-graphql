package io.fluidsonic.graphql


internal object InputObjectVariableInputCoercerExtensionKey : GNodeExtensionKey<GVariableInputCoercer<Map<String, Any?>>>


val GInputObjectType.variableInputCoercer: GVariableInputCoercer<Map<String, Any?>>?
	get() = extensions[InputObjectVariableInputCoercerExtensionKey]


var GNodeExtensionSet.Builder<GInputObjectType>.variableInputCoercer: GVariableInputCoercer<Map<String, Any?>>?
	get() = get(InputObjectVariableInputCoercerExtensionKey)
	set(value) {
		set(InputObjectVariableInputCoercerExtensionKey, value)
	}
