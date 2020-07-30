package io.fluidsonic.graphql


internal object InputObjectNodeInputCoercerExtensionKey : GNodeExtensionKey<GNodeInputCoercer<Map<String, Any?>>>


public val GInputObjectType.nodeInputCoercer: GNodeInputCoercer<Map<String, Any?>>?
	get() = extensions[InputObjectNodeInputCoercerExtensionKey]


public var GNodeExtensionSet.Builder<GInputObjectType>.nodeInputCoercer: GNodeInputCoercer<Map<String, Any?>>?
	get() = get(InputObjectNodeInputCoercerExtensionKey)
	set(value) {
		set(InputObjectNodeInputCoercerExtensionKey, value)
	}
