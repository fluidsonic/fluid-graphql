package io.fluidsonic.graphql


// https://youtrack.jetbrains.com/issue/KT-21235
internal actual fun Double.toConsistentString(): String =
	when {
		isFinite() -> toString().let { string ->
			when {
				string.contains('e') -> when {
					string.contains('.') -> string.replace("e+", "E").replace("e-", "E-")
					else -> string.replace("e+", ".0E").replace("e-", ".0E-")
				}
				string.contains('.') -> string
				else -> "$string.0"
			}
		}
		else -> toString()
	}
