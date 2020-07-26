package io.fluidsonic.graphql


internal object InputObjectNodeInputCoercerExtensionKey : GNodeExtensionKey<GNodeInputCoercer<Map<String, Any?>>>


val GInputObjectType.nodeInputCoercer: GNodeInputCoercer<Map<String, Any?>>?
	get() = extensions[InputObjectNodeInputCoercerExtensionKey]


var GNodeExtensionSet.Builder<GInputObjectType>.nodeInputCoercer: GNodeInputCoercer<Map<String, Any?>>?
	get() = get(InputObjectNodeInputCoercerExtensionKey)
	set(value) {
		set(InputObjectNodeInputCoercerExtensionKey, value)
	}
