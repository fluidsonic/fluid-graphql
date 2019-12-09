@file:Suppress("CanBeParameter", "MemberVisibilityCanBePrivate", "unused")

// FIXME reeeefactor

package io.fluidsonic.graphql

// FIXME remove once testing is set up
// FIXME add type system extension
// FIXME structured errors
// FIXME toString(), equals(), hashCode()

import io.fluidsonic.graphql.GType.*
import kotlin.collections.List as KList


internal interface TypeFactory {

	fun get(name: String, kind: Kind? = null): GType
	fun get(reference: GTypeRef): GType
	fun get(reference: GTypeRef, kind: Kind): GType
	fun getObjectsImplementingInterface(name: String): KList<GObjectType>
	fun register(type: GType)
}
