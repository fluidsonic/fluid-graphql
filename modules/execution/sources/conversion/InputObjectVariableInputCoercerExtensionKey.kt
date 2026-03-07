package io.fluidsonic.graphql


internal object InputObjectVariableInputCoercerExtensionKey : GNodeExtensionKey<GVariableInputCoercer<Map<String, Any?>>>


/** The variable input coercer for this input object type, or `null` if none was set. */
public val GInputObjectType.variableInputCoercer: GVariableInputCoercer<Map<String, Any?>>?
	get() = extensions[InputObjectVariableInputCoercerExtensionKey]


/** The variable input coercer to attach to this input object type when building the schema. */
public var GNodeExtensionSet.Builder<GInputObjectType>.variableInputCoercer: GVariableInputCoercer<Map<String, Any?>>?
	get() = get(InputObjectVariableInputCoercerExtensionKey)
	set(value) {
		set(InputObjectVariableInputCoercerExtensionKey, value)
	}
