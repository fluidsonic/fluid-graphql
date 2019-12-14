package io.fluidsonic.graphql


// Note that equality of `GNamedType` instance is based on identity and `GWrappedType` based on the type of wrapping and the equality of the wrapped type.
// Each schema (or any construction that created types using `.Unresolved` variants) has its own instances and there is exactly one instance per named type.
// The only notable exceptions are built-in scalar types. Those are singletons and shared between all schemas.
//
// https://graphql.github.io/graphql-spec/June2018/#sec-Wrapping-Types
// https://graphql.github.io/graphql-spec/June2018/#sec-Types
// https://graphql.github.io/graphql-spec/June2018/#sec-The-__Type-Type
sealed class GType(
	val directives: List<GDirective>,
	val kind: Kind
) {

	// https://graphql.github.io/graphql-spec/June2018/#IsInputType()
	fun isInputType(): Boolean =
		when (this) {
			is GWrappingType -> ofType.isInputType()
			is GScalarType, is GEnumType, is GInputObjectType -> true
			else -> false
		}


	// https://graphql.github.io/graphql-spec/June2018/#IsOutputType()
	fun isOutputType(): Boolean =
		when (this) {
			is GWrappingType -> ofType.isOutputType()
			is GScalarType, is GObjectType, is GInterfaceType, is GUnionType, is GEnumType -> true
			else -> false
		}


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
class GEnumType private constructor(
	typeRegistry: GTypeRegistry?,
	description: String?,
	directives: List<GDirective>,
	name: String,
	values: List<GEnumValueDefinition>
) : GNamedType(
	typeRegistry = typeRegistry,
	description = description,
	directives = directives,
	kind = Kind.ENUM,
	name = name
) {

	val values: Map<String, GEnumValueDefinition>


	constructor(
		name: String,
		values: List<GEnumValueDefinition>,
		description: String? = null,
		directives: List<GDirective> = emptyList()
	) : this(
		typeRegistry = null,
		name = name,
		values = values,
		description = description,
		directives = directives
	)


	init {
		require(values.isNotEmpty()) { "'values' must not be empty" }
		require(values.size <= 1 || values.mapTo(hashSetOf()) { it.name }.size == values.size) {
			"'values' must not contain multiple elements with the same name: $values"
		}

		this.values = values.associateBy { it.name }
	}


	override fun isSupertypeOf(other: GType) =
		other === this ||
			(other is GNonNullType && other.ofType === this)


	companion object


	class Unresolved(
		override val name: String,
		val values: List<GEnumValueDefinition>,
		val description: String? = null,
		override val directives: List<GDirective> = emptyList()
	) : GNamedType.Unresolved() {

		override fun resolve(typeRegistry: GTypeRegistry) = GEnumType(
			typeRegistry = typeRegistry,
			description = description,
			directives = directives,
			name = name,
			values = values
		)


		companion object
	}
}


// https://graphql.github.io/graphql-spec/June2018/#sec-Input-Objects
// https://graphql.github.io/graphql-spec/June2018/#sec-Input-Object
class GInputObjectType private constructor(
	typeRegistry: GTypeRegistry?,
	arguments: List<GArgumentDefinition>?,
	argumentsToResolve: List<GArgumentDefinition.Unresolved>?,
	description: String?,
	directives: List<GDirective>,
	name: String
) : GNamedType(
	typeRegistry = typeRegistry,
	description = description,
	directives = directives,
	kind = Kind.INPUT_OBJECT,
	name = name
) {

	val arguments: Map<String, GArgumentDefinition>


	constructor(
		name: String,
		arguments: List<GArgumentDefinition>,
		description: String? = null,
		directives: List<GDirective> = emptyList()
	) : this(
		typeRegistry = null,
		arguments = arguments,
		argumentsToResolve = null,
		description = description,
		directives = directives,
		name = name
	)


	init {
		val resolvedArguments = arguments
			?: typeRegistry?.let { argumentsToResolve?.map { it.resolve(typeRegistry) } }
			?: error("impossible")

		require(resolvedArguments.isNotEmpty()) { "'arguments' must not be empty" }
		require(resolvedArguments.size <= 1 || resolvedArguments.mapTo(hashSetOf()) { it.name }.size == resolvedArguments.size) {
			"'arguments' must not contain multiple elements with the same name: ${arguments ?: argumentsToResolve}"
		}
		require(resolvedArguments.all { it.type.isInputType() }) {
			"'arguments' must not contain elements with an output type: ${arguments ?: argumentsToResolve}"
		}

		this.arguments = resolvedArguments.associateBy { it.name }
	}


	override fun isSupertypeOf(other: GType) =
		other === this ||
			(other is GNonNullType && other.ofType === this)


	companion object


	class Unresolved(
		override val name: String,
		val arguments: List<GArgumentDefinition.Unresolved>,
		val description: String? = null,
		override val directives: List<GDirective> = emptyList()
	) : GNamedType.Unresolved() {

		override fun resolve(typeRegistry: GTypeRegistry) = GInputObjectType(
			typeRegistry = typeRegistry,
			arguments = null,
			argumentsToResolve = arguments,
			description = description,
			directives = directives,
			name = name
		)


		companion object
	}
}


// https://graphql.github.io/graphql-spec/June2018/#sec-Interfaces
// https://graphql.github.io/graphql-spec/June2018/#sec-Interface
class GInterfaceType private constructor(
	typeRegistry: GTypeRegistry?,
	description: String?,
	directives: List<GDirective>,
	fields: List<GFieldDefinition>?,
	fieldsToResolve: List<GFieldDefinition.Unresolved>?,
	name: String
) : GNamedType(
	typeRegistry = typeRegistry,
	description = description,
	directives = directives,
	kind = Kind.INTERFACE,
	name = name
) {

	val fields: Map<String, GFieldDefinition>


	constructor(
		name: String,
		fields: List<GFieldDefinition>,
		description: String? = null,
		directives: List<GDirective> = emptyList()
	) : this(
		typeRegistry = null,
		description = description,
		directives = directives,
		fields = fields,
		fieldsToResolve = null,
		name = name
	)


	init {
		val resolvedFields = fields
			?: typeRegistry?.let { fieldsToResolve?.map { it.resolve(typeRegistry) } }
			?: error("impossible")

		require(resolvedFields.isNotEmpty()) { "'fields' in '$name' must not be empty" }
		require(resolvedFields.size <= 1 || resolvedFields.mapTo(hashSetOf()) { it.name }.size == resolvedFields.size) {
			"'fields' in '$name' must not contain multiple elements with the same name: ${fields ?: fieldsToResolve}"
		}

		this.fields = resolvedFields.associateBy { it.name }
	}


	override fun isSupertypeOf(other: GType): Boolean =
		other === this ||
			(other is GObjectType && other.interfaces.contains(this)) ||
			(other is GNonNullType && isSupertypeOf(other.ofType))


	companion object


	class Unresolved(
		override val name: String,
		val fields: List<GFieldDefinition.Unresolved>,
		val description: String? = null,
		override val directives: List<GDirective> = emptyList()
	) : GNamedType.Unresolved() {

		override fun resolve(typeRegistry: GTypeRegistry) = GInterfaceType(
			typeRegistry = typeRegistry,
			description = description,
			directives = directives,
			fields = null,
			fieldsToResolve = fields,
			name = name
		)


		companion object
	}
}


// https://graphql.github.io/graphql-spec/June2018/#sec-Type-System.List
// https://graphql.github.io/graphql-spec/June2018/#sec-Type-Kinds.List
class GListType(
	ofType: GType
) : GWrappingType(
	kind = Kind.LIST,
	ofType = ofType
) {

	override fun equals(other: Any?) =
		this === other || (other is GListType && ofType == other.ofType)


	override fun hashCode() =
		1 xor ofType.hashCode()


	override fun isSupertypeOf(other: GType): Boolean =
		other === this ||
			(other is GListType && ofType.isSupertypeOf(other.ofType)) ||
			(other is GNonNullType && isSupertypeOf(other.ofType))


	companion object
}


sealed class GNamedType(
	typeRegistry: GTypeRegistry?,
	description: String?,
	directives: List<GDirective>,
	kind: Kind,
	val name: String
) : GType(directives = directives, kind = kind) {

	val description = description?.ifEmpty { null }


	init {
		@Suppress("LeakingThis")
		typeRegistry?.register(this)
	}


	override fun equals(other: Any?) =
		this === other


	override fun hashCode() =
		name.hashCode()


	companion object;


	abstract class Unresolved internal constructor() {

		abstract val directives: List<GDirective>
		abstract val name: String


		abstract fun resolve(typeRegistry: GTypeRegistry): GType


		companion object
	}
}


// https://graphql.github.io/graphql-spec/June2018/#sec-Type-System.Non-Null
// https://graphql.github.io/graphql-spec/June2018/#sec-Type-Kinds.Non-Null
class GNonNullType(
	ofType: GType
) : GWrappingType(
	kind = Kind.NON_NULL,
	ofType = ofType
) {

	init {
		require(ofType !is GNonNullType) { "Cannot create a non-null type that wraps another non-null type" }
	}


	override fun equals(other: Any?) =
		this === other || (other is GNonNullType && ofType == other.ofType)


	override fun hashCode() =
		2 xor ofType.hashCode()


	override fun isSupertypeOf(other: GType) =
		other === this ||
			(other is GNonNullType && ofType.isSupertypeOf(other.ofType))
}


// https://graphql.github.io/graphql-spec/June2018/#sec-Objects
// https://graphql.github.io/graphql-spec/June2018/#sec-Object
class GObjectType private constructor(
	typeRegistry: GTypeRegistry?,
	description: String?,
	directives: List<GDirective>,
	fields: List<GFieldDefinition>?,
	fieldsToResolve: List<GFieldDefinition.Unresolved>?,
	interfaces: List<GInterfaceType>?,
	interfacesToResolve: List<GNamedTypeRef>?,
	name: String
) : GNamedType(
	typeRegistry = typeRegistry,
	description = description,
	directives = directives,
	kind = Kind.OBJECT,
	name = name
) {

	val fields: Map<String, GFieldDefinition>
	val interfaces: List<GInterfaceType>


	constructor(
		name: String,
		fields: List<GFieldDefinition>,
		interfaces: List<GInterfaceType> = emptyList(),
		description: String? = null,
		directives: List<GDirective> = emptyList()
	) : this(
		typeRegistry = null,
		description = description,
		directives = directives,
		fields = fields,
		fieldsToResolve = null,
		interfaces = interfaces,
		interfacesToResolve = null,
		name = name
	)


	init {
		val resolvedFields = fields
			?: typeRegistry?.let { fieldsToResolve?.map { it.resolve(typeRegistry) } }
			?: error("impossible")

		require(resolvedFields.isNotEmpty()) { "'fields' must not be empty" }
		require(resolvedFields.size <= 1 || resolvedFields.mapTo(hashSetOf()) { it.name }.size == resolvedFields.size) {
			"'fields' must not contain multiple elements with the same name: ${fields ?: fieldsToResolve}"
		}

		val resolvedInterfaces = interfaces
			?: typeRegistry?.let { interfacesToResolve?.map { typeRegistry.resolveKind<GInterfaceType>(it) } }
			?: error("impossible")

		require(resolvedInterfaces.size <= 1 || resolvedInterfaces.mapTo(hashSetOf()) { it.name }.size == resolvedInterfaces.size) {
			"'interfaces' must not contain multiple elements with the same name: ${interfaces ?: interfacesToResolve}"
		}

		resolvedInterfaces.forEach { requireImplementationOfInterface(name = name, iface = it, fields = resolvedFields) }

		this.fields = resolvedFields.associateBy { it.name }
		this.interfaces = resolvedInterfaces
	}


	override fun isSupertypeOf(other: GType): Boolean =
		other === this ||
			(other is GNonNullType && isSupertypeOf(other.ofType))


	companion object {

		private fun requireImplementationOfInterface(name: String, iface: GInterfaceType, fields: List<GFieldDefinition>) {
			for (expectedField in iface.fields.values) {
				val implementedField = fields.firstOrNull { it.name == expectedField.name }
				requireNotNull(implementedField) {
					"'fields' in object '$name' must contain an element named '${expectedField.name}' as required by interface '${iface.name}': $fields"
				}
				require(implementedField.type.isSubtypeOf(expectedField.type)) {
					"'fields' element '${implementedField.name}' in object '$name' must be a subtype of '${expectedField.type}' " +
						"as required by interface '${iface.name}': $fields"
				}

				for (expectedArg in expectedField.arguments.values) {
					val implementedArg = implementedField.arguments.values.firstOrNull { it.name == expectedArg.name }
					requireNotNull(implementedArg) {
						"'fields' element '${implementedField.name}' in object '$name' must accept an argument named '${expectedArg.name}' " +
							"as required by interface '${iface.name}': $fields"
					}
					require(implementedArg.type == expectedArg.type) {
						"'fields' element '${implementedField.name}' argument '${implementedArg.name}' in object '$name' must be of type '${expectedArg.type}' " +
							"as required by interface '${iface.name}': $fields"
					}
				}

				for (implementedArg in implementedField.arguments.values) {
					if (expectedField.arguments.values.none { it.name == implementedArg.name }) {
						require(implementedArg.type !is GNonNullType) {
							"'fields' element '${implementedField.name}' argument '${implementedArg.name}' in object '$name' must be of a nullable type " +
								"as interface '${iface.name}' doesn't require it: $fields"
						}
					}
				}
			}
		}
	}


	data class Unresolved(
		override val name: String,
		val fields: List<GFieldDefinition.Unresolved>,
		val interfaces: List<GNamedTypeRef> = emptyList(),
		val description: String? = null,
		override val directives: List<GDirective> = emptyList()
	) : GNamedType.Unresolved() {

		override fun resolve(typeRegistry: GTypeRegistry) = GObjectType(
			typeRegistry = typeRegistry,
			description = description,
			directives = directives,
			fields = null,
			fieldsToResolve = fields,
			interfaces = null,
			interfacesToResolve = interfaces,
			name = name
		)


		companion object
	}
}


// https://graphql.github.io/graphql-spec/June2018/#sec-Scalars
// https://graphql.github.io/graphql-spec/June2018/#sec-Scalar
sealed class GScalarType(
	name: String,
	typeRegistry: GTypeRegistry? = null,
	description: String? = null,
	directives: List<GDirective> = emptyList()
) : GNamedType(
	typeRegistry = typeRegistry,
	description = description,
	directives = directives,
	kind = Kind.SCALAR,
	name = name
) {

	override fun isSupertypeOf(other: GType): Boolean =
		this == other ||
			(other is GNonNullType && isSupertypeOf(other.ofType))


	companion object
}


object GBooleanType : GScalarType(name = "Boolean")
object GFloatType : GScalarType(name = "Float")
object GIDType : GScalarType(name = "ID")
object GIntType : GScalarType(name = "Int")
object GStringType : GScalarType(name = "String")


class GCustomScalarType private constructor(
	typeRegistry: GTypeRegistry?,
	description: String?,
	directives: List<GDirective>,
	name: String
) : GScalarType(
	typeRegistry = typeRegistry,
	description = description,
	directives = directives,
	name = name
) {

	constructor(
		name: String,
		description: String? = null,
		directives: List<GDirective> = emptyList()
	) : this(
		typeRegistry = null,
		description = description,
		directives = directives,
		name = name
	)


	companion object


	class Unresolved(
		override val name: String,
		val description: String? = null,
		override val directives: List<GDirective> = emptyList()
	) : GNamedType.Unresolved() {

		override fun resolve(typeRegistry: GTypeRegistry) = GCustomScalarType(
			typeRegistry = typeRegistry,
			description = description,
			directives = directives,
			name = name
		)


		companion object
	}
}


// https://graphql.github.io/graphql-spec/June2018/#sec-Unions
// https://graphql.github.io/graphql-spec/June2018/#sec-Union
class GUnionType private constructor(
	typeRegistry: GTypeRegistry?,
	description: String?,
	directives: List<GDirective>,
	name: String,
	possibleTypes: List<GObjectType>?,
	possibleTypesToResolve: List<GNamedTypeRef>?
) : GNamedType(
	typeRegistry = typeRegistry,
	description = description,
	directives = directives,
	kind = Kind.UNION,
	name = name
) {

	val possibleTypes: List<GObjectType>


	constructor(
		name: String,
		possibleTypes: List<GObjectType>,
		description: String? = null,
		directives: List<GDirective> = emptyList()
	) : this(
		typeRegistry = null,
		description = description,
		directives = directives,
		name = name,
		possibleTypes = possibleTypes,
		possibleTypesToResolve = null
	)


	init {
		val resolvedPossibleTypes = possibleTypes
			?: typeRegistry?.let { possibleTypesToResolve?.map { typeRegistry.resolveKind<GObjectType>(it) } }
			?: error("impossible")

		require(resolvedPossibleTypes.isNotEmpty()) { "'possibleTypes' must not be empty" }
		require(resolvedPossibleTypes.size <= 1 || resolvedPossibleTypes.mapTo(hashSetOf()) { it.name }.size == resolvedPossibleTypes.size) {
			"'interfaces' must not contain multiple elements with the same name: ${possibleTypes ?: possibleTypesToResolve}"
		}

		this.possibleTypes = resolvedPossibleTypes
	}


	override fun isSupertypeOf(other: GType): Boolean =
		other === this ||
			other is GObjectType && possibleTypes.contains(other) ||
			(other is GNonNullType && isSupertypeOf(other.ofType))


	companion object


	class Unresolved(
		override val name: String,
		val possibleTypes: List<GNamedTypeRef>,
		val description: String? = null,
		override val directives: List<GDirective> = emptyList()
	) : GNamedType.Unresolved() {

		override fun resolve(typeRegistry: GTypeRegistry) = GUnionType(
			typeRegistry = typeRegistry,
			description = description,
			directives = directives,
			name = name,
			possibleTypes = null,
			possibleTypesToResolve = possibleTypes
		)


		companion object
	}
}


// https://graphql.github.io/graphql-spec/June2018/#sec-Wrapping-Types
// https://graphql.github.io/graphql-spec/June2018/#sec-Types
sealed class GWrappingType(
	kind: Kind,
	val ofType: GType
) : GType(
	directives = emptyList(),
	kind = kind
) {

	companion object
}
