package io.fluidsonic.graphql


internal object FieldDefinitionResolverExtensionKey : GNodeExtensionKey<GFieldResolver<*>>


public val GFieldDefinition.resolver: GFieldResolver<*>?
	get() = extensions[FieldDefinitionResolverExtensionKey]


public var GNodeExtensionSet.Builder<GFieldDefinition>.resolver: GFieldResolver<*>?
	get() = get(FieldDefinitionResolverExtensionKey)
	set(value) {
		set(FieldDefinitionResolverExtensionKey, value)
	}
