package testing


actual fun uniqueIdOf(value: Any): String =
	System.identityHashCode(value).toString()
