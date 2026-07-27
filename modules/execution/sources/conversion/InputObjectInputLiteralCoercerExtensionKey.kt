package io.fluidsonic.graphql

internal object InputObjectInputLiteralCoercerExtensionKey : GNodeExtensionKey<GInputLiteralCoercer<Map<String, Any?>>>

/** The inline input coercer attached to this input object type, or `null` if none was set. */
public val GInputObjectType.inputLiteralCoercer: GInputLiteralCoercer<Map<String, Any?>>?
	get() = extensions[InputObjectInputLiteralCoercerExtensionKey]

/** The inline input coercer to attach to this input object type when building the schema. */
public var GNodeExtensionSet.Builder<GInputObjectType>.inputLiteralCoercer: GInputLiteralCoercer<Map<String, Any?>>?
	get() = get(InputObjectInputLiteralCoercerExtensionKey)
	set(value) {
		set(InputObjectInputLiteralCoercerExtensionKey, value)
	}
