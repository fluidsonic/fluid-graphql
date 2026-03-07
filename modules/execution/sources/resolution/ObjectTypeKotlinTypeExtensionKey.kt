package io.fluidsonic.graphql

import kotlin.reflect.*


internal object ObjectKotlinTypeNodeExtensionKey : GNodeExtensionKey<KClass<out Any>>


/** The Kotlin class associated with this object type for type-safe field resolution, or `null` if not set. */
public val GObjectType.kotlinType: KClass<out Any>?
	get() = extensions[ObjectKotlinTypeNodeExtensionKey]


/** The Kotlin class to associate with this object type when building the schema. */
public var GNodeExtensionSet.Builder<GObjectType>.kotlinType: KClass<out Any>?
	get() = get(ObjectKotlinTypeNodeExtensionKey)
	set(value) {
		set(ObjectKotlinTypeNodeExtensionKey, value)
	}
