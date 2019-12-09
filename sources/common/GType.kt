package io.fluidsonic.graphql


// Note that Type instances can be compared by identity. Each schema has its own instances and there is exactly one instance per GraphQL type.
// That includes wrapped types independent of the depth of nesting.
// The only notable exception are built-in scalar types. Those are singletons and shared between all schemes.
//
// https://graphql.github.io/graphql-spec/June2018/#sec-Wrapping-Types
// https://graphql.github.io/graphql-spec/June2018/#sec-Types
// https://graphql.github.io/graphql-spec/June2018/#sec-The-__Type-Type
sealed class GType(
	val directives: List<GDirective>,
	val kind: Kind
) {

	open val description: String?
		get() = null

	open val inputFields: List<GArgumentDefinition>?
		get() = null

	open val interfaces: List<GInterfaceType>?
		get() = null

	open val name: String?
		get() = null

	open val ofType: GType?
		get() = null

	open val possibleTypes: List<GObjectType>?
		get() = null


	open fun enumValues(includeDeprecated: Boolean = false): List<GEnumValueDefinition>? =
		null


	open fun fields(includeDeprecated: Boolean = false): List<GFieldDefinition>? =
		null


	fun isSubtypeOf(other: GType) =
		other.isSupertypeOf(this)


	abstract fun isSupertypeOf(other: GType): Boolean


	override fun toString() =
		GWriter { writeTypeName(this@GType) }


	companion object;


	// https://graphql.github.io/graphql-spec/June2018/#sec-Schema-Introspection
	// https://graphql.github.io/graphql-spec/June2018/#sec-Type-Kinds
	enum class Kind {

		ENUM,
		INPUT_OBJECT,
		INTERFACE,
		LIST,
		NON_NULL,
		OBJECT,
		SCALAR,
		UNION;


		override fun toString() =
			when (this) {
				ENUM -> "Enum"
				INPUT_OBJECT -> "Input Object"
				INTERFACE -> "Interface"
				LIST -> "List"
				NON_NULL -> "Non-Null"
				OBJECT -> "Object"
				SCALAR -> "Scalar"
				UNION -> "Union"
			}


		companion object
	}
}


// https://graphql.github.io/graphql-spec/June2018/#sec-Enums
// https://graphql.github.io/graphql-spec/June2018/#sec-Input-Object
class GEnumType internal constructor(
	typeFactory: TypeFactory,
	input: GQLInput.Type.Enum
) : GNamedType(
	typeFactory = typeFactory,
	directives = input.directives.map(::GDirective),
	kind = Kind.ENUM,
	name = input.name
) {

	private val valuesIncludingDeprecated = input.values.map(::GEnumValueDefinition)
	private val valuesExcludingDeprecated = valuesIncludingDeprecated.filterNot(GEnumValueDefinition::isDeprecated)

	override val description = input.description

//
//				init {
//					require(values.isNotEmpty()) { "'values' must not be empty" }
//					require(values.size > 1 && values.mapTo(hashSetOf()) { it.name }.size == values.size) {
//						"'values' must not contain multiple elements with the same name: $values"
//					}
//				}


	override fun enumValues(includeDeprecated: Boolean) =
		if (includeDeprecated) valuesIncludingDeprecated else valuesExcludingDeprecated


	override fun isSupertypeOf(other: GType) =
		other === this


	companion object
}


// https://graphql.github.io/graphql-spec/June2018/#sec-Input-Objects
// https://graphql.github.io/graphql-spec/June2018/#sec-Input-Object
class GInputObjectType internal constructor(
	typeFactory: TypeFactory,
	input: GQLInput.Type.InputObject
) : GNamedType(
	typeFactory = typeFactory,
	directives = input.directives.map(::GDirective),
	kind = Kind.INPUT_OBJECT,
	name = input.name
) {

	override val description = input.description
	override val inputFields = input.fields.map { GArgumentDefinition(typeFactory, it) } // FIXME rn parameters


//				init {
//					require(fields.isNotEmpty()) { "'fields' must not be empty" }
//					require(fields.size > 1 && fields.mapTo(hashSetOf()) { it.name }.size == fields.size) {
//						"'fields' must not contain multiple elements with the same name: $fields"
//					}
//					require(fields.all { Specification.isInputType(it.type) }) {
//						"'fields' must not contain elements of an output type: $fields"
//					}
//				}


	override fun isSupertypeOf(other: GType) =
		other === this


	companion object
}


// https://graphql.github.io/graphql-spec/June2018/#sec-Interfaces
// https://graphql.github.io/graphql-spec/June2018/#sec-Interface
class GInterfaceType internal constructor(
	typeFactory: TypeFactory,
	input: GQLInput.Type.Interface
) : GNamedType(
	typeFactory = typeFactory,
	directives = input.directives.map(::GDirective),
	kind = Kind.INTERFACE,
	name = input.name
) {

	private val fieldsIncludingDeprecated = input.fields.map { GFieldDefinition(typeFactory, it) }
	private val fieldsExcludingDeprecated = fieldsIncludingDeprecated.filterNot(GFieldDefinition::isDeprecated)

	override val description = input.description
	override val possibleTypes = typeFactory.getObjectsImplementingInterface(name)

//				init {
//					require(fields.isNotEmpty()) { "'fields' must not be empty" }
//					require(fields.size > 1 && fields.mapTo(hashSetOf()) { it.name }.size == fields.size) {
//						"'fields' must not contain multiple elements with the same name: $fields"
//					}
//				}


	override fun fields(includeDeprecated: Boolean) =
		if (includeDeprecated) fieldsIncludingDeprecated else fieldsExcludingDeprecated


	override fun isSupertypeOf(other: GType) =
		other === this ||
			(other is GObjectType && other.interfaces.contains(this))


	companion object
}


// https://graphql.github.io/graphql-spec/June2018/#sec-Type-System.List
// https://graphql.github.io/graphql-spec/June2018/#sec-Type-Kinds.List
class GListType internal constructor(
	typeFactory: TypeFactory,
	input: GListTypeRef
) : GWrappingType(
	typeFactory = typeFactory,
	kind = Kind.LIST,
	ofType = input.elementType,
	underlyingTypeName = input.underlyingName
) {


	override fun isSupertypeOf(other: GType) =
		other === this ||
			(other is GListType && ofType.isSupertypeOf(other.ofType))


	companion object
}


sealed class GNamedType(
	typeFactory: TypeFactory?,
	directives: List<GDirective>,
	kind: Kind,
	final override val name: String
) : GType(directives = directives, kind = kind) {

	init {
		@Suppress("LeakingThis")
		typeFactory?.register(this)
	}

//			init {
//				require(Specification.isValidTypeName(name)) { "'name' is not a valid name: $name" }
//			}


	companion object
}


// https://graphql.github.io/graphql-spec/June2018/#sec-Type-System.Non-Null
// https://graphql.github.io/graphql-spec/June2018/#sec-Type-Kinds.Non-Null
class GNonNullType internal constructor(
	typeFactory: TypeFactory,
	input: GNonNullTypeRef
) : GWrappingType(
	typeFactory = typeFactory,
	kind = Kind.NON_NULL,
	ofType = input.nullableType,
	underlyingTypeName = input.underlyingName
) {

	override fun isSupertypeOf(other: GType) =
		other === this ||
			(other is GNonNullType && ofType.isSupertypeOf(other.ofType))

//				init {
//					require(ofType !is NonNull) { "Cannot create a non-null type reference to another reference that is already non-null" }
//				}
}


// https://graphql.github.io/graphql-spec/June2018/#sec-Objects
// https://graphql.github.io/graphql-spec/June2018/#sec-Object
class GObjectType internal constructor(
	typeFactory: TypeFactory,
	input: GQLInput.Type.Object
) : GNamedType(
	typeFactory = typeFactory,
	directives = input.directives.map(::GDirective),
	kind = Kind.OBJECT,
	name = input.name
) {

	private val fieldsIncludingDeprecated = input.fields.map { GFieldDefinition(typeFactory, it) }
	private val fieldsExcludingDeprecated = fieldsIncludingDeprecated.filterNot(GFieldDefinition::isDeprecated)

	override val description = input.description
	override val interfaces = input.interfaces.map { typeFactory.get(it, Kind.INTERFACE) as GInterfaceType }


//				init {
//					require(fields.isNotEmpty()) { "'fields' must not be empty" }
//					require(fields.size > 1 && fields.mapTo(hashSetOf()) { it.name }.size == fields.size) {
//						"'fields' must not contain multiple elements with the same name: $fields"
//					}
//					require(interfaces.size > 1 && interfaces.mapTo(hashSetOf()) { it.name }.size == interfaces.size) {
//						"'interfaces' must not contain multiple elements with the same name: $fields"
//					}
//
//					for (iface in interfaces) {
//						requireImplementationOfInterface(iface, fields = fields)
//					}
//				}


	fun field(name: String) =
		fieldsIncludingDeprecated.firstOrNull { it.name == name }


	override fun fields(includeDeprecated: Boolean) =
		if (includeDeprecated) fieldsIncludingDeprecated else fieldsExcludingDeprecated


	override fun isSupertypeOf(other: GType) =
		other === this


	companion object {

		private fun requireImplementationOfInterface(iface: GInterfaceType, fields: List<GFieldDefinition>) {
			for (expectedField in iface.fields(includeDeprecated = true)) {
				val implementedField = fields.firstOrNull { it.name == expectedField.name }
				requireNotNull(implementedField) {
					"'fields' must contain an element named '${expectedField.name}' as required by interface '${iface.name}': $fields"
				}
				require(implementedField.type.isSubtypeOf(expectedField.type)) {
					"'fields' element '${implementedField.name}' must be a subtype of '${expectedField.type}' " +
						"as required by interface '${iface.name}': $fields"
				}

				for (expectedArg in expectedField.args) {
					val implementedArg = implementedField.args.firstOrNull { it.name == expectedArg.name }
					requireNotNull(implementedArg) {
						"'fields' element '${implementedField.name}' must accept an argument named '${expectedArg.name}' " +
							"as required by interface '${iface.name}': $fields"
					}
					require(implementedArg.type == expectedArg.type) {
						"'fields' element '${implementedField.name}' argument '${implementedArg.name}' must be of type '${expectedArg.type}' " +
							"as required by interface '${iface.name}': $fields"
					}
				}

				for (implementedArg in implementedField.args) {
					if (expectedField.args.none { it.name == implementedArg.name }) {
						require(implementedArg.type !is GNonNullType) {
							"'fields' element '${implementedField.name}' argument '${implementedArg.name}' must be of a nullable type " +
								"as interface '${iface.name}' doesn't require it: $fields"
						}
					}
				}
			}
		}
	}
}


// https://graphql.github.io/graphql-spec/June2018/#sec-Scalars
// https://graphql.github.io/graphql-spec/June2018/#sec-Scalar
sealed class GScalarType(
	typeFactory: TypeFactory?,
	directives: List<GDirective>,
	name: String
) : GNamedType(
	typeFactory = typeFactory,
	directives = directives,
	kind = Kind.SCALAR,
	name = name
) {

	override fun isSupertypeOf(other: GType) =
		this == other


	companion object
}


object GBooleanType : GScalarType(typeFactory = null, directives = emptyList(), name = "Boolean")
object GFloatType : GScalarType(typeFactory = null, directives = emptyList(), name = "Float")
object GIDType : GScalarType(typeFactory = null, directives = emptyList(), name = "ID")
object GIntType : GScalarType(typeFactory = null, directives = emptyList(), name = "Int")
object GStringType : GScalarType(typeFactory = null, directives = emptyList(), name = "String")


class GCustomScalarType internal constructor(
	typeFactory: TypeFactory,
	input: GQLInput.Type.Scalar
) : GScalarType(
	typeFactory = typeFactory,
	directives = input.directives.map(::GDirective),
	name = input.name
) {

	override val description = input.description


	companion object
}


// https://graphql.github.io/graphql-spec/June2018/#sec-Unions
// https://graphql.github.io/graphql-spec/June2018/#sec-Union
class GUnionType internal constructor(
	typeFactory: TypeFactory,
	input: GQLInput.Type.Union
) : GNamedType(
	typeFactory = typeFactory,
	directives = input.directives.map(::GDirective),
	kind = Kind.UNION,
	name = input.name
) {

	override val description = input.description
	override val possibleTypes = input.possibleTypes.map { typeFactory.get(it, Kind.OBJECT) as GObjectType }


//				init {
//					require(possibleTypes.isNotEmpty()) { "'possibleTypes' must not be empty" }
//					require(possibleTypes.size > 1 && possibleTypes.mapTo(hashSetOf()) { it.name }.size == possibleTypes.size) {
//						"'possibleTypes' must not contain multiple elements with the same name: $possibleTypes"
//					}
//				}


	override fun isSupertypeOf(other: GType) =
		other === this ||
			other is GObjectType && possibleTypes.contains(other)
}


// https://graphql.github.io/graphql-spec/June2018/#sec-Wrapping-Types
// https://graphql.github.io/graphql-spec/June2018/#sec-Types
sealed class GWrappingType(
	typeFactory: TypeFactory,
	kind: Kind,
	ofType: GTypeRef,
	underlyingTypeName: String
) : GType(
	directives = emptyList(),
	kind = kind
) {

	final override val ofType = typeFactory.get(ofType)
	val underlyingNamedType = typeFactory.get(underlyingTypeName)

	companion object
}
