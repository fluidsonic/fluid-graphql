package io.fluidsonic.graphql


// FIXME fluid-stdlib?
@Suppress("UNCHECKED_CAST")
internal inline fun <K, V> Map<out K, V>.getOrElseNullable(key: K, defaultValue: () -> V): V {
	val value = get(key)

	return if (value == null && !containsKey(key))
		defaultValue()
	else
		value as V
}
