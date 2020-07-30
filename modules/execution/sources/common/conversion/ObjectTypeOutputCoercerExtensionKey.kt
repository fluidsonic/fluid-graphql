package io.fluidsonic.graphql


internal object ObjectTypeOutputCoercerExtensionKey : GNodeExtensionKey<GOutputCoercer<Map<String, Any?>>>


public val GObjectType.outputCoercer: GOutputCoercer<Map<String, Any?>>?
	get() = extensions[ObjectTypeOutputCoercerExtensionKey]


public var GNodeExtensionSet.Builder<GObjectType>.outputCoercer: GOutputCoercer<Map<String, Any?>>?
	get() = get(ObjectTypeOutputCoercerExtensionKey)
	set(value) {
		set(ObjectTypeOutputCoercerExtensionKey, value)
	}
