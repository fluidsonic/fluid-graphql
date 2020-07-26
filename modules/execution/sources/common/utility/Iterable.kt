package io.fluidsonic.graphql


// FIXME use refactored fluid-stdlib
internal fun <Element> Iterable<Element>.joinToString(
	separator: CharSequence,
	lastSeparator: CharSequence,
	transform: (element: Element) -> String = { it.toString() }
) = buildString {
	var previousElement: Element? = null
	var size = 0

	forEachIndexed { index: Int, element: Element ->
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
