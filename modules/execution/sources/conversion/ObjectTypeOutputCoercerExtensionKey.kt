package io.fluidsonic.graphql


internal object ObjectTypeOutputCoercerExtensionKey : GNodeExtensionKey<GOutputCoercer<Map<String, Any?>>>


/** The output coercer for this object type, or `null` if none was set. */
public val GObjectType.outputCoercer: GOutputCoercer<Map<String, Any?>>?
	get() = extensions[ObjectTypeOutputCoercerExtensionKey]


/** The output coercer to attach to this object type when building the schema. */
public var GNodeExtensionSet.Builder<GObjectType>.outputCoercer: GOutputCoercer<Map<String, Any?>>?
	get() = get(ObjectTypeOutputCoercerExtensionKey)
	set(value) {
		set(ObjectTypeOutputCoercerExtensionKey, value)
	}
