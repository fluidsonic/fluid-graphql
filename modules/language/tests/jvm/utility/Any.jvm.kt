package testing


actual fun uniqueIdOf(value: Any): Int =
	System.identityHashCode(value)
