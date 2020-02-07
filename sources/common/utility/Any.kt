package io.fluidsonic.graphql

import kotlin.contracts.*


@UseExperimental(ExperimentalContracts::class)
inline fun <T : Any> T?.ifNull(onNull: () -> T): T {
	contract {
		callsInPlace(onNull, InvocationKind.AT_MOST_ONCE)
	}

	return if (this !== null) this else onNull()
}


internal inline fun Any.coerceToDoubleOrNull(): Double? =
	when (this) {
		is Byte -> toDouble()
		is Double -> this
		is Float -> toDouble()
		is Int -> toDouble()
		is Long -> toDouble()
		is Short -> toDouble()
		is UByte -> toDouble()
		is UInt -> toDouble()
		is ULong -> toDouble()
		is UShort -> toDouble()
		else -> null
	}


internal inline fun Any.coerceToIntOrNull(): Int? =
	when (this) {
		is Byte -> toInt()
		is Int -> this
		is Long -> takeIf { this >= Int.MIN_VALUE && this < Int.MAX_VALUE }?.toInt()
		is Short -> toInt()
		is UByte -> toInt()
		is UInt -> takeIf { this < Int.MAX_VALUE.toUInt() }?.toInt()
		is ULong -> takeIf { this < Int.MAX_VALUE.toULong() }?.toInt()
		is UShort -> toInt()
		else -> null
	}
