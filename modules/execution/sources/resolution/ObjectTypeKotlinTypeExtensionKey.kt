package io.fluidsonic.graphql

import kotlin.reflect.*


internal object ObjectKotlinTypeNodeExtensionKey : GNodeExtensionKey<KClass<out Any>>


public val GObjectType.kotlinType: KClass<out Any>?
	get() = extensions[ObjectKotlinTypeNodeExtensionKey]


public var GNodeExtensionSet.Builder<GObjectType>.kotlinType: KClass<out Any>?
	get() = get(ObjectKotlinTypeNodeExtensionKey)
	set(value) {
		set(ObjectKotlinTypeNodeExtensionKey, value)
	}
