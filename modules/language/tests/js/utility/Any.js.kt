package testing
	

private var nextIdentity = 1


actual fun uniqueIdOf(value: Any): Int {
	val dynamic = value.asDynamic()

	(dynamic.___identity as? Int)
		?.let { return it }
		?: return (++nextIdentity).also { dynamic.___identity = it }
}
