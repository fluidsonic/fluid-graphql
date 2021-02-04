package testing

import kotlinx.cinterop.*


actual fun uniqueIdOf(value: Any): String =
	value.objcPtr().toString()
