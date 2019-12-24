package io.fluidsonic.graphql


// FIXME fluid-stdlib?
internal fun <T> Iterable<T>.joinToString(
	separator: CharSequence,
	lastSeparator: CharSequence,
	transform: (element: T) -> String = { it.toString() }
) = buildString {
	var previousElement: T? = null
	var size = 0

	forEachIndexed { index: Int, element: T ->
		size = index + 1

		if (index > 0) {
			if (index > 1)
				append(separator)

			append(transform(previousElement!!))
		}

		previousElement = element
	}

	if (size > 0) {
		if (size > 1)
			append(lastSeparator)

		append(transform(previousElement!!))
	}
}
