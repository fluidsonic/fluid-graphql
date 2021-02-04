package testing


private var nextIdentity = 1


actual fun uniqueIdOf(value: Any): String {
	val dynamic = value.asDynamic()

	(dynamic.___identity as? String)
		?.let { return it }
		?: return (++nextIdentity).toString().also { dynamic.___identity = it }
}
