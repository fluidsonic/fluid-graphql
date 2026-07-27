package io.fluidsonic.graphql

internal object ObjectTypeOutputValueCoercerExtensionKey : GNodeExtensionKey<GOutputValueCoercer<Map<String, Any?>>>

/** The output coercer attached to this object type, or `null` if none was set. */
public val GObjectType.outputValueCoercer: GOutputValueCoercer<Map<String, Any?>>?
	get() = extensions[ObjectTypeOutputValueCoercerExtensionKey]

/** The output coercer to attach to this object type when building the schema. */
public var GNodeExtensionSet.Builder<GObjectType>.outputValueCoercer: GOutputValueCoercer<Map<String, Any?>>?
	get() = get(ObjectTypeOutputValueCoercerExtensionKey)
	set(value) {
		set(ObjectTypeOutputValueCoercerExtensionKey, value)
	}
