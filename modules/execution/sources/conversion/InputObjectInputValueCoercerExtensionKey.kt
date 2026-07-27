package io.fluidsonic.graphql

internal object InputObjectInputValueCoercerExtensionKey : GNodeExtensionKey<GInputValueCoercer<Map<String, Any?>>>

/** The variable input coercer attached to this input object type, or `null` if none was set. */
public val GInputObjectType.inputValueCoercer: GInputValueCoercer<Map<String, Any?>>?
	get() = extensions[InputObjectInputValueCoercerExtensionKey]

/** The variable input coercer to attach to this input object type when building the schema. */
public var GNodeExtensionSet.Builder<GInputObjectType>.inputValueCoercer: GInputValueCoercer<Map<String, Any?>>?
	get() = get(InputObjectInputValueCoercerExtensionKey)
	set(value) {
		set(InputObjectInputValueCoercerExtensionKey, value)
	}
