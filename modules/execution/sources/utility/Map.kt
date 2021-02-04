package io.fluidsonic.graphql


// FIXME use refactored fluid-stdlib
// https://youtrack.jetbrains.com/issue/KT-21392
@Suppress("UNCHECKED_CAST")
internal inline fun <K, V> Map<out K, V>.getOrElseNullable(key: K, defaultValue: () -> V): V {
	val value = get(key)

	return if (value == null && !containsKey(key))
		defaultValue()
	else
		value as V
}
