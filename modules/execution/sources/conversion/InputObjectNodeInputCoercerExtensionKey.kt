package io.fluidsonic.graphql


internal object InputObjectNodeInputCoercerExtensionKey : GNodeExtensionKey<GNodeInputCoercer<Map<String, Any?>>>


/** The inline input coercer for this input object type, or `null` if none was set. */
public val GInputObjectType.nodeInputCoercer: GNodeInputCoercer<Map<String, Any?>>?
	get() = extensions[InputObjectNodeInputCoercerExtensionKey]


/** The inline input coercer to attach to this input object type when building the schema. */
public var GNodeExtensionSet.Builder<GInputObjectType>.nodeInputCoercer: GNodeInputCoercer<Map<String, Any?>>?
	get() = get(InputObjectNodeInputCoercerExtensionKey)
	set(value) {
		set(InputObjectNodeInputCoercerExtensionKey, value)
	}
