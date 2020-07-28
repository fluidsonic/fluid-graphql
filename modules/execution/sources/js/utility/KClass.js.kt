package io.fluidsonic.graphql

import kotlin.reflect.*


internal actual val KClass<*>.qualifiedOrSimpleName: String?
	get() = simpleName
