package io.fluidsonic.graphql


sealed class GTypeRef {

	abstract val underlyingName: String


	override fun toString() =
		GWriter { writeTypeRef(this@GTypeRef) }


	companion object
}


class GNamedTypeRef private constructor(
	val name: String
) : GTypeRef() {

	override val underlyingName
		get() = name


	override fun equals(other: Any?) =
		this === other || (other is GNamedTypeRef && name == other.name)


	override fun hashCode() =
		name.hashCode()


	companion object {

		@Suppress("USELESS_ELVIS")
		operator fun invoke(name: String): GNamedTypeRef =
			when (name) {
				"Boolean" -> GBooleanTypeRef ?: GNamedTypeRef(name)
				"Float" -> GFloatTypeRef ?: GNamedTypeRef(name)
				"ID" -> GIDTypeRef ?: GNamedTypeRef(name)
				"Int" -> GIntTypeRef ?: GNamedTypeRef(name)
				"String" -> GStringTypeRef ?: GNamedTypeRef(name)
				else -> GNamedTypeRef(name)
			}
	}
}


class GListTypeRef(
	val elementType: GTypeRef
) : GTypeRef() {

	override val underlyingName
		get() = elementType.underlyingName


	override fun equals(other: Any?) =
		this === other || (other is GListTypeRef && elementType == other.elementType)


	override fun hashCode() =
		1 + elementType.hashCode()


	companion object
}


class GNonNullTypeRef private constructor(
	val nullableType: GTypeRef
) : GTypeRef() {

	override val underlyingName
		get() = nullableType.underlyingName


	override fun equals(other: Any?) =
		this === other || (other is GNonNullTypeRef && nullableType == other.nullableType)


	override fun hashCode() =
		2 + nullableType.hashCode()


	companion object {

		operator fun invoke(nullableType: GTypeRef) =
			nullableType as? GNonNullTypeRef ?: GNonNullTypeRef(nullableType)
	}
}


val GBooleanTypeRef = GNamedTypeRef("Boolean")
val GFloatTypeRef = GNamedTypeRef("Float")
val GIDTypeRef = GNamedTypeRef("ID")
val GIntTypeRef = GNamedTypeRef("Int")
val GStringTypeRef = GNamedTypeRef("String")
