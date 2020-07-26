package io.fluidsonic.graphql


internal object ObjectTypeOutputCoercerExtensionKey : GNodeExtensionKey<GOutputCoercer<Map<String, Any?>>>


val GObjectType.outputCoercer: GOutputCoercer<Map<String, Any?>>?
	get() = extensions[ObjectTypeOutputCoercerExtensionKey]


var GNodeExtensionSet.Builder<GObjectType>.outputCoercer: GOutputCoercer<Map<String, Any?>>?
	get() = get(ObjectTypeOutputCoercerExtensionKey)
	set(value) {
		set(ObjectTypeOutputCoercerExtensionKey, value)
	}
