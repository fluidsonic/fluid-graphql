package io.fluidsonic.graphql

internal object FieldDefinitionResolverExtensionKey : GNodeExtensionKey<GFieldResolver<*>>

/**
 * The [GFieldResolver] attached to this field definition, or `null` if none was set.
 *
 * A field of an introspection type (`__Schema.types`, `__Type.fields`, …) always answers `null`: the library
 * resolves those in the executor rather than by attaching anything to the node.
 */
public val GFieldDefinition.resolver: GFieldResolver<*>?
	get() = extensions[FieldDefinitionResolverExtensionKey]

/** The [GFieldResolver] to attach to this field definition when building the schema. */
public var GNodeExtensionSet.Builder<GFieldDefinition>.resolver: GFieldResolver<*>?
	get() = get(FieldDefinitionResolverExtensionKey)
	set(value) {
		set(FieldDefinitionResolverExtensionKey, value)
	}
