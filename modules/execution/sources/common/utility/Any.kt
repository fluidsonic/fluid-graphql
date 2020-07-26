@file:JvmName("Any@execution")

package io.fluidsonic.graphql

import kotlin.contracts.*
import kotlin.jvm.*


@Suppress("unused")
internal fun <Receiver, Value> Receiver.identity(value: Value) =
	value


// FIXME use refactored fluid-stdlib
@OptIn(ExperimentalContracts::class)
internal inline fun <T : Any> T?.ifNull(onNull: () -> T): T {
	contract {
		callsInPlace(onNull, InvocationKind.AT_MOST_ONCE)
	}

	return if (this !== null) this else onNull()
}
