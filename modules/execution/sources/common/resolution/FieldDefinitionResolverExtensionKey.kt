package io.fluidsonic.graphql


internal object FieldDefinitionResolverExtensionKey : GNodeExtensionKey<GFieldResolver<*>>


val GFieldDefinition.resolver: GFieldResolver<*>?
	get() = extensions[FieldDefinitionResolverExtensionKey]


var GNodeExtensionSet.Builder<GFieldDefinition>.resolver: GFieldResolver<*>?
	get() = get(FieldDefinitionResolverExtensionKey)
	set(value) {
		set(FieldDefinitionResolverExtensionKey, value)
	}
