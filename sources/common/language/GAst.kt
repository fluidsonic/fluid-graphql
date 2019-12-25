package io.fluidsonic.graphql

import kotlin.reflect.*


// TODO add acceptChildren
// FIXME hashCode & equals
sealed class GAst(
	val origin: GOrigin?
) {

	abstract fun <Result, Data> accept(visitor: GAstVisitor<Result, Data>, data: Data): Result


	fun accept(visitor: GAstVoidVisitor) =
		accept(visitor, data = null)


	abstract fun equalsAst(other: GAst, includingOrigin: Boolean = false): Boolean


	override fun toString() =
		print(this) // FIXME check if this is OK and why it didn't work the last time


	@Suppress("UNCHECKED_CAST")
	fun <Node : GAst, Data> transform(visitor: GAstTransformer<Data>, data: Data) =
		accept(visitor, data) as Node


	companion object {

		fun print(ast: GAst, indent: String = "\t") =
			Printer.print(ast = ast, indent = indent)
	}


	interface Depreciable {

		val deprecationReason: String?
		val directives: List<GDirective>
		val isDeprecated: Boolean
	}
}


sealed class GAbstractType(
	description: GValue.String?,
	directives: List<GDirective>,
	kind: GTypeKind,
	name: GName,
	origin: GOrigin?
) : GNamedType(
	description = description,
	directives = directives,
	kind = kind,
	name = name,
	origin = origin
) {

	companion object
}


class GArgument(
	name: GName,
	val value: GValue,
	origin: GOrigin? = null
) : GAst(origin = origin) {

	val name get() = nameNode.value
	val nameNode = name


	constructor(
		name: String,
		value: GValue
	) : this(
		name = GName(name),
		value = value
	)


	override fun <Result, Data> accept(visitor: GAstVisitor<Result, Data>, data: Data) =
		visitor.visitArgument(this, data)


	override fun equalsAst(other: GAst, includingOrigin: Boolean) =
		this === other || (
			other is GArgument &&
				nameNode.equalsAst(other.nameNode, includingOrigin = includingOrigin) &&
				value.equalsAst(other.value, includingOrigin = includingOrigin) &&
				(!includingOrigin || origin == other.origin)
			)


	companion object
}


fun GAst?.equalsAst(other: GAst?, includingOrigin: Boolean = false) =
	this === other || (this !== null && other !== null && equalsAst(other, includingOrigin = includingOrigin))


fun List<GAst?>.equalsAst(other: List<GAst?>, includingOrigin: Boolean): Boolean {
	if (this === other)
		return true

	if (size != other.size)
		return false

	forEachIndexed { index, ast ->
		if (!ast.equalsAst(other[index], includingOrigin = includingOrigin))
			return false
	}

	return true
}


class GArgumentDefinition(
	name: GName,
	val type: GTypeRef,
	val defaultValue: GValue? = null,
	description: GValue.String? = null,
	val directives: List<GDirective> = emptyList(),
	origin: GOrigin? = null
) : GAst(origin = origin) {

	val description get() = descriptionNode?.value?.ifEmpty { null }
	val descriptionNode = description
	val name get() = nameNode.value
	val nameNode = name


	constructor(
		name: String,
		type: GTypeRef,
		defaultValue: GValue? = null,
		description: String? = null,
		directives: List<GDirective> = emptyList()
	) : this(
		name = GName(name),
		type = type,
		defaultValue = defaultValue,
		description = description?.let { GValue.String(it) },
		directives = directives
	)


	override fun <Result, Data> accept(visitor: GAstVisitor<Result, Data>, data: Data) =
		visitor.visitArgumentDefinition(this, data)


	override fun equalsAst(other: GAst, includingOrigin: Boolean) =
		this === other || (
			other is GArgumentDefinition &&
				defaultValue.equalsAst(other.defaultValue, includingOrigin = includingOrigin) &&
				descriptionNode.equalsAst(other.descriptionNode, includingOrigin = includingOrigin) &&
				directives.equalsAst(other.directives, includingOrigin = includingOrigin) &&
				nameNode.equalsAst(other.nameNode, includingOrigin = includingOrigin) &&
				type.equalsAst(other.type, includingOrigin = includingOrigin) &&
				(!includingOrigin || origin == other.origin)
			)


	companion object
}


class GCustomScalarType(
	name: GName,
	description: GValue.String? = null,
	directives: List<GDirective> = emptyList(),
	origin: GOrigin? = null
) : GScalarType(
	description = description,
	directives = directives,
	name = name,
	origin = origin
) {

	constructor(
		name: String,
		description: String? = null,
		directives: List<GDirective> = emptyList()
	) : this(
		name = GName(name),
		description = description?.let { GValue.String(it) },
		directives = directives
	)


	companion object
}


sealed class GDefinition(
	origin: GOrigin?
) : GAst(origin = origin) {

	companion object
}


class GDirective(
	name: GName,
	val arguments: List<GArgument> = emptyList(),
	origin: GOrigin? = null
) : GAst(origin = origin) {

	val argumentsByName = arguments.associateBy { it.name }
	val name get() = nameNode.value
	val nameNode = name


	constructor(
		name: String,
		arguments: List<GArgument> = emptyList()
	) : this(
		name = GName(name),
		arguments = arguments
	)


	override fun <Result, Data> accept(visitor: GAstVisitor<Result, Data>, data: Data) =
		visitor.visitDirective(this, data)


	override fun equalsAst(other: GAst, includingOrigin: Boolean) =
		this === other || (
			other is GDirective &&
				arguments.equalsAst(other.arguments, includingOrigin = includingOrigin) &&
				nameNode.equalsAst(other.nameNode, includingOrigin = includingOrigin) &&
				(!includingOrigin || origin == other.origin)
			)


	companion object
}


class GDirectiveDefinition(
	name: GName,
	locations: List<GName>,
	val isRepeatable: Boolean = false,
	val arguments: List<GArgumentDefinition> = emptyList(),
	description: GValue.String? = null,
	origin: GOrigin? = null
) : GTypeSystemDefinition(origin = origin) {

	val argumentsByName = arguments.associateBy { it.name }
	val description get() = descriptionNode?.value?.ifEmpty { null }
	val descriptionNode = description
	val locations: Set<GDirectiveLocation> = locations.mapNotNullTo(mutableSetOf()) { node ->
		GDirectiveLocation.values().firstOrNull { it.name == node.value }
	}
	val locationNodes = locations
	val name get() = nameNode.value
	val nameNode = name


	constructor(
		name: String,
		locations: Set<GDirectiveLocation>,
		isRepeatable: Boolean = false,
		arguments: List<GArgumentDefinition> = emptyList(),
		description: String? = null
	) : this(
		name = GName(name),
		locations = locations.map { GName(it.name) },
		isRepeatable = isRepeatable,
		arguments = arguments,
		description = description?.let { GValue.String(it) }
	)


	override fun <Result, Data> accept(visitor: GAstVisitor<Result, Data>, data: Data) =
		visitor.visitDirectiveDefinition(this, data)


	override fun equalsAst(other: GAst, includingOrigin: Boolean) =
		this === other || (
			other is GDirectiveDefinition &&
				arguments.equalsAst(other.arguments, includingOrigin = includingOrigin) &&
				descriptionNode.equalsAst(other.descriptionNode, includingOrigin = includingOrigin) &&
				isRepeatable == other.isRepeatable &&
				locationNodes.equalsAst(other.locationNodes, includingOrigin = includingOrigin) &&
				nameNode.equalsAst(other.nameNode, includingOrigin = includingOrigin) &&
				(!includingOrigin || origin == other.origin)
			)


	companion object
}


class GDocument(
	val definitions: List<GDefinition>,
	origin: GOrigin? = null
) : GAst(origin = origin) {

	val fragmentsByName = definitions
		.filterIsInstance<GFragmentDefinition>()
		.associateBy { it.name }

	val operationsByName = definitions
		.filterIsInstance<GOperationDefinition>()
		.associateBy { it.name }

	val schema = definitions
		.filterIsInstance<GTypeSystemDefinition>()
		.ifEmpty { null }
		?.let { GSchema(it) } // FIXME


	override fun <Result, Data> accept(visitor: GAstVisitor<Result, Data>, data: Data) =
		visitor.visitDocument(this, data)


	override fun equalsAst(other: GAst, includingOrigin: Boolean) =
		this === other || (
			other is GDocument &&
				definitions.equalsAst(other.definitions, includingOrigin = includingOrigin) &&
				(!includingOrigin || origin == other.origin)
			)


	companion object { // FIXME more companions

		fun parse(source: GSource.Parsable) =
			Parser.parseDocument(source)


		fun parse(content: String, name: String = "<document>") =
			parse(GSource.of(content = content, name = name))
	}
}


// https://graphql.github.io/graphql-spec/June2018/#sec-Enums
class GEnumType(
	name: GName,
	val values: List<GEnumValueDefinition>,
	description: GValue.String? = null,
	directives: List<GDirective> = emptyList(),
	origin: GOrigin? = null
) : GNamedType(
	description = description,
	directives = directives,
	kind = GTypeKind.ENUM,
	name = name,
	origin = origin
) {

	val valuesByName = values.associateBy { it.name }


	constructor(
		name: String,
		values: List<GEnumValueDefinition>,
		description: String? = null,
		directives: List<GDirective> = emptyList()
	) : this(
		name = GName(name),
		values = values,
		description = description?.let { GValue.String(it) },
		directives = directives
	)


	override fun <Result, Data> accept(visitor: GAstVisitor<Result, Data>, data: Data) =
		visitor.visitEnumType(this, data)


	override fun isSupertypeOf(other: GType) =
		other === this ||
			(other is GNonNullType && other.nullableType === this)


	override fun equalsAst(other: GAst, includingOrigin: Boolean) =
		this === other || (
			other is GEnumType &&
				descriptionNode.equalsAst(other.descriptionNode, includingOrigin = includingOrigin) &&
				directives.equalsAst(other.directives, includingOrigin = includingOrigin) &&
				nameNode.equalsAst(other.nameNode, includingOrigin = includingOrigin) &&
				values.equalsAst(other.values, includingOrigin = includingOrigin) &&
				(!includingOrigin || origin == other.origin)
			)


	companion object
}


class GEnumTypeExtension(
	name: GName,
	val values: List<GEnumValueDefinition>,
	directives: List<GDirective> = emptyList(),
	origin: GOrigin? = null
) : GTypeExtension(
	directives = directives,
	name = name,
	origin = origin
) {

	val valuesByName = values.associateBy { it.name }


	constructor(
		name: String,
		values: List<GEnumValueDefinition>,
		directives: List<GDirective> = emptyList()
	) : this(
		name = GName(name),
		values = values,
		directives = directives
	)


	override fun <Result, Data> accept(visitor: GAstVisitor<Result, Data>, data: Data) =
		visitor.visitEnumTypeExtension(this, data)


	override fun equalsAst(other: GAst, includingOrigin: Boolean) =
		this === other || (
			other is GEnumTypeExtension &&
				directives.equalsAst(other.directives, includingOrigin = includingOrigin) &&
				nameNode.equalsAst(other.nameNode, includingOrigin = includingOrigin) &&
				values.equalsAst(other.values, includingOrigin = includingOrigin) &&
				(!includingOrigin || origin == other.origin)
			)


	companion object
}


class GEnumValueDefinition(
	name: GName,
	description: GValue.String? = null,
	override val directives: List<GDirective> = emptyList(),
	origin: GOrigin? = null
) :
	GAst(origin = origin),
	GAst.Depreciable {

	val description get() = descriptionNode?.value?.ifEmpty { null }
	val descriptionNode = description
	val name get() = nameNode.value
	val nameNode = name

	override val deprecationReason: String?
	override val isDeprecated: Boolean


	init {
		val deprecation = directives.firstOrNull { it.name == GSpecification.defaultDeprecatedDirective.name }

		deprecationReason = (deprecation?.argumentsByName?.get("reason")?.value as? GValue.String)?.value?.ifEmpty { null } // FIXME improve
		isDeprecated = deprecation != null
	}


	constructor(
		name: String,
		description: String? = null,
		directives: List<GDirective> = emptyList()
	) : this(
		name = GName(name),
		description = description?.let { GValue.String(it) },
		directives = directives
	)


	override fun <Result, Data> accept(visitor: GAstVisitor<Result, Data>, data: Data) =
		visitor.visitEnumValueDefinition(this, data)


	override fun equalsAst(other: GAst, includingOrigin: Boolean) =
		this === other || (
			other is GEnumValueDefinition &&
				descriptionNode.equalsAst(other.descriptionNode, includingOrigin = includingOrigin) &&
				directives.equalsAst(other.directives, includingOrigin = includingOrigin) &&
				nameNode.equalsAst(other.nameNode, includingOrigin = includingOrigin) &&
				(!includingOrigin || origin == other.origin)
			)


	companion object
}


class GFieldDefinition(
	name: GName,
	val type: GTypeRef,
	val arguments: List<GArgumentDefinition> = emptyList(),
	description: GValue.String? = null,
	override val directives: List<GDirective> = emptyList(),
	val resolver: GFieldResolver<*>? = null,
	origin: GOrigin? = null
) :
	GAst(origin = origin),
	GAst.Depreciable {

	val argumentsByName = arguments.associateBy { it.name }
	val description get() = descriptionNode?.value?.ifEmpty { null }
	val descriptionNode = description
	val name get() = nameNode.value
	val nameNode = name

	override val deprecationReason: String?
	override val isDeprecated: Boolean


	init {
		val deprecation = directives.firstOrNull { it.name == GSpecification.defaultDeprecatedDirective.name }

		deprecationReason = (deprecation?.argumentsByName?.get("reason")?.value as? GValue.String)?.value?.ifEmpty { null } // FIXME improve
		isDeprecated = deprecation != null
	}


	constructor(
		name: String,
		type: GTypeRef,
		arguments: List<GArgumentDefinition> = emptyList(),
		description: String? = null,
		directives: List<GDirective> = emptyList(),
		resolver: GFieldResolver<*>? = null
	) : this(
		name = GName(name),
		type = type,
		arguments = arguments,
		description = description?.let { GValue.String(it) },
		directives = directives,
		resolver = resolver
	)


	override fun <Result, Data> accept(visitor: GAstVisitor<Result, Data>, data: Data) =
		visitor.visitFieldDefinition(this, data)


	override fun equalsAst(other: GAst, includingOrigin: Boolean) =
		this === other || (
			other is GFieldDefinition &&
				arguments.equalsAst(other.arguments, includingOrigin = includingOrigin) &&
				descriptionNode.equalsAst(other.descriptionNode, includingOrigin = includingOrigin) &&
				directives.equalsAst(other.directives, includingOrigin = includingOrigin) &&
				nameNode.equalsAst(other.nameNode, includingOrigin = includingOrigin) &&
				type.equalsAst(other.type, includingOrigin = includingOrigin) &&
				(!includingOrigin || origin == other.origin)
			)


	companion object
}


class GFieldSelection(
	name: GName,
	val selectionSet: GSelectionSet? = null,
	val arguments: List<GArgument> = emptyList(),
	alias: GName? = null,
	directives: List<GDirective> = emptyList(),
	origin: GOrigin? = null
) : GSelection(
	directives = directives,
	origin = origin
) {

	val alias get() = aliasNode?.value
	val aliasNode = alias
	val argumentsByName = arguments.associateBy { it.name }
	val name get() = nameNode.value
	val nameNode = name


	constructor(
		name: String,
		selectionSet: GSelectionSet? = null,
		arguments: List<GArgument> = emptyList(),
		alias: String? = null,
		directives: List<GDirective> = emptyList()
	) : this(
		name = GName(name),
		selectionSet = selectionSet,
		arguments = arguments,
		alias = alias?.let { GName(alias) },
		directives = directives
	)


	override fun <Result, Data> accept(visitor: GAstVisitor<Result, Data>, data: Data) =
		visitor.visitFieldSelection(this, data)


	override fun equalsAst(other: GAst, includingOrigin: Boolean) =
		this === other || (
			other is GFieldSelection &&
				aliasNode.equalsAst(other.aliasNode, includingOrigin = includingOrigin) &&
				arguments.equalsAst(other.arguments, includingOrigin = includingOrigin) &&
				directives.equalsAst(other.directives, includingOrigin = includingOrigin) &&
				nameNode.equalsAst(other.nameNode, includingOrigin = includingOrigin) &&
				selectionSet.equalsAst(other.selectionSet, includingOrigin = includingOrigin) &&
				(!includingOrigin || origin == other.origin)
			)


	companion object
}


class GFragmentDefinition(
	name: GName,
	val typeCondition: GNamedTypeRef,
	val selectionSet: GSelectionSet,
	val variableDefinitions: List<GVariableDefinition> = emptyList(),
	val directives: List<GDirective> = emptyList(),
	origin: GOrigin? = null
) : GDefinition(origin = origin) {

	val name get() = nameNode.value
	val nameNode = name
	val variableDefinitionsByName = variableDefinitions.associateBy { it.name }


	constructor(
		name: String,
		typeCondition: GNamedTypeRef,
		selectionSet: GSelectionSet,
		variableDefinitions: List<GVariableDefinition> = emptyList(),
		directives: List<GDirective> = emptyList()
	) : this(
		name = GName(name),
		typeCondition = typeCondition,
		selectionSet = selectionSet,
		variableDefinitions = variableDefinitions,
		directives = directives
	)


	override fun <Result, Data> accept(visitor: GAstVisitor<Result, Data>, data: Data) =
		visitor.visitFragmentDefinition(this, data)


	override fun equalsAst(other: GAst, includingOrigin: Boolean) =
		this === other || (
			other is GFragmentDefinition &&
				directives.equalsAst(other.directives, includingOrigin = includingOrigin) &&
				nameNode.equalsAst(other.nameNode, includingOrigin = includingOrigin) &&
				selectionSet.equalsAst(other.selectionSet, includingOrigin = includingOrigin) &&
				typeCondition.equalsAst(other.typeCondition, includingOrigin = includingOrigin) &&
				variableDefinitions.equalsAst(other.variableDefinitions, includingOrigin = includingOrigin) &&
				(!includingOrigin || origin == other.origin)
			)


	companion object
}


class GFragmentSelection(
	name: GName,
	directives: List<GDirective> = emptyList(),
	origin: GOrigin? = null
) : GSelection(
	directives = directives,
	origin = origin
) {

	val name get() = nameNode.value
	val nameNode = name


	constructor(
		name: String,
		directives: List<GDirective> = emptyList()
	) : this(
		name = GName(name),
		directives = directives
	)


	override fun <Result, Data> accept(visitor: GAstVisitor<Result, Data>, data: Data) =
		visitor.visitFragmentSelection(this, data)


	override fun equalsAst(other: GAst, includingOrigin: Boolean) =
		this === other || (
			other is GFragmentSelection &&
				directives.equalsAst(other.directives, includingOrigin = includingOrigin) &&
				nameNode.equalsAst(other.nameNode, includingOrigin = includingOrigin) &&
				(!includingOrigin || origin == other.origin)
			)


	companion object
}


class GInlineFragmentSelection(
	val selectionSet: GSelectionSet,
	val typeCondition: GNamedTypeRef?,
	directives: List<GDirective> = emptyList(),
	origin: GOrigin? = null
) : GSelection(
	directives = directives,
	origin = origin
) {

	override fun <Result, Data> accept(visitor: GAstVisitor<Result, Data>, data: Data) =
		visitor.visitInlineFragmentSelection(this, data)


	override fun equalsAst(other: GAst, includingOrigin: Boolean) =
		this === other || (
			other is GInlineFragmentSelection &&
				directives.equalsAst(other.directives, includingOrigin = includingOrigin) &&
				selectionSet.equalsAst(other.selectionSet, includingOrigin = includingOrigin) &&
				typeCondition.equalsAst(other.typeCondition, includingOrigin = includingOrigin) &&
				(!includingOrigin || origin == other.origin)
			)


	companion object
}


// https://graphql.github.io/graphql-spec/June2018/#sec-Input-Objects
// https://graphql.github.io/graphql-spec/June2018/#sec-Input-Object
class GInputObjectType(
	name: GName,
	override val arguments: List<GArgumentDefinition>,
	description: GValue.String? = null,
	directives: List<GDirective> = emptyList(),
	origin: GOrigin? = null
) :
	GNamedType(
		description = description,
		directives = directives,
		kind = GTypeKind.INPUT_OBJECT,
		name = name,
		origin = origin
	),
	GType.WithArguments {

	override val argumentsByName = arguments.associateBy { it.name }


	constructor(
		name: String,
		arguments: List<GArgumentDefinition>,
		description: String? = null,
		directives: List<GDirective> = emptyList()
	) : this(
		name = GName(name),
		arguments = arguments,
		description = description?.let { GValue.String(it) },
		directives = directives
	)


	override fun <Result, Data> accept(visitor: GAstVisitor<Result, Data>, data: Data) =
		visitor.visitInputObjectType(this, data)


	override fun equalsAst(other: GAst, includingOrigin: Boolean) =
		this === other || (
			other is GInputObjectType &&
				arguments.equalsAst(other.arguments, includingOrigin = includingOrigin) &&
				descriptionNode.equalsAst(other.descriptionNode, includingOrigin = includingOrigin) &&
				directives.equalsAst(other.directives, includingOrigin = includingOrigin) &&
				nameNode.equalsAst(other.nameNode, includingOrigin = includingOrigin) &&
				(!includingOrigin || origin == other.origin)
			)


	override fun isSupertypeOf(other: GType) =
		other === this ||
			(other is GNonNullType && other.nullableType === this)


	companion object
}


class GInputObjectTypeExtension(
	name: GName,
	val arguments: List<GArgumentDefinition> = emptyList(),
	directives: List<GDirective> = emptyList(),
	origin: GOrigin? = null
) : GTypeExtension(
	directives = directives,
	name = name,
	origin = origin
) {

	val argumentsByName = arguments.associateBy { it.name }


	constructor(
		name: String,
		arguments: List<GArgumentDefinition> = emptyList(),
		directives: List<GDirective> = emptyList()
	) : this(
		name = GName(name),
		arguments = arguments,
		directives = directives
	)


	override fun <Result, Data> accept(visitor: GAstVisitor<Result, Data>, data: Data) =
		visitor.visitInputObjectTypeExtension(this, data)


	override fun equalsAst(other: GAst, includingOrigin: Boolean) =
		this === other || (
			other is GInputObjectTypeExtension &&
				arguments.equalsAst(other.arguments, includingOrigin = includingOrigin) &&
				directives.equalsAst(other.directives, includingOrigin = includingOrigin) &&
				nameNode.equalsAst(other.nameNode, includingOrigin = includingOrigin) &&
				(!includingOrigin || origin == other.origin)
			)


	companion object
}


// https://graphql.github.io/graphql-spec/June2018/#sec-Interfaces
// https://graphql.github.io/graphql-spec/June2018/#sec-Interface
class GInterfaceType(
	name: GName,
	override val fields: List<GFieldDefinition>,
	override val interfaces: List<GNamedTypeRef> = emptyList(),
	description: GValue.String? = null,
	directives: List<GDirective> = emptyList(),
	origin: GOrigin? = null
) :
	GAbstractType(
		description = description,
		directives = directives,
		kind = GTypeKind.INTERFACE,
		name = name,
		origin = origin
	),
	GType.WithFields,
	GType.WithInterfaces {

	override val fieldsByName = fields.associateBy { it.name }


	constructor(
		name: String,
		fields: List<GFieldDefinition>,
		interfaces: List<GNamedTypeRef> = emptyList(),
		description: String? = null,
		directives: List<GDirective> = emptyList()
	) : this(
		name = GName(name),
		fields = fields,
		interfaces = interfaces,
		description = description?.let { GValue.String(it) },
		directives = directives
	)


	override fun <Result, Data> accept(visitor: GAstVisitor<Result, Data>, data: Data) =
		visitor.visitInterfaceType(this, data)


	override fun equalsAst(other: GAst, includingOrigin: Boolean) =
		this === other || (
			other is GInterfaceType &&
				descriptionNode.equalsAst(other.descriptionNode, includingOrigin = includingOrigin) &&
				directives.equalsAst(other.directives, includingOrigin = includingOrigin) &&
				fields.equalsAst(other.fields, includingOrigin = includingOrigin) &&
				interfaces.equalsAst(other.interfaces, includingOrigin = includingOrigin) &&
				nameNode.equalsAst(other.nameNode, includingOrigin = includingOrigin) &&
				(!includingOrigin || origin == other.origin)
			)


	override fun isSupertypeOf(other: GType): Boolean =
		other === this ||
			(other is WithInterfaces && other.interfaces.any { it.name == name }) ||
			(other is GNonNullType && isSupertypeOf(other.nullableType))


	companion object
}


class GInterfaceTypeExtension(
	name: GName,
	val fields: List<GFieldDefinition> = emptyList(),
	val interfaces: List<GNamedTypeRef> = emptyList(),
	directives: List<GDirective> = emptyList(),
	origin: GOrigin? = null
) : GTypeExtension(
	directives = directives,
	name = name,
	origin = origin
) {

	val fieldsByName = fields.associateBy { it.name }


	constructor(
		name: String,
		fields: List<GFieldDefinition> = emptyList(),
		interfaces: List<GNamedTypeRef> = emptyList(),
		directives: List<GDirective> = emptyList()
	) : this(
		name = GName(name),
		fields = fields,
		interfaces = interfaces,
		directives = directives
	)


	override fun <Result, Data> accept(visitor: GAstVisitor<Result, Data>, data: Data) =
		visitor.visitInterfaceTypeExtension(this, data)


	override fun equalsAst(other: GAst, includingOrigin: Boolean) =
		this === other || (
			other is GInterfaceTypeExtension &&
				directives.equalsAst(other.directives, includingOrigin = includingOrigin) &&
				fields.equalsAst(other.fields, includingOrigin = includingOrigin) &&
				interfaces.equalsAst(other.interfaces, includingOrigin = includingOrigin) &&
				nameNode.equalsAst(other.nameNode, includingOrigin = includingOrigin) &&
				(!includingOrigin || origin == other.origin)
			)


	companion object
}


// https://graphql.github.io/graphql-spec/June2018/#sec-Type-System.List
// https://graphql.github.io/graphql-spec/June2018/#sec-Type-Kinds.List
class GListType(
	elementType: GType
) : GWrappingType(
	kind = GTypeKind.LIST,
	wrappedType = elementType
) {

	val elementType get() = wrappedType


	override fun equalsAst(other: GAst, includingOrigin: Boolean) =
		this === other || (
			other is GListType &&
				elementType.equalsAst(other.elementType, includingOrigin = includingOrigin)
			)


	override fun isSupertypeOf(other: GType): Boolean =
		other === this ||
			(other is GListType && elementType.isSupertypeOf(other.elementType)) ||
			(other is GNonNullType && isSupertypeOf(other.nullableType))


	companion object
}


class GListTypeRef(
	val elementType: GTypeRef,
	origin: GOrigin? = null
) : GTypeRef(origin = origin) {

	override val underlyingName get() = elementType.underlyingName


	override fun <Result, Data> accept(visitor: GAstVisitor<Result, Data>, data: Data) =
		visitor.visitListTypeRef(this, data)


	override fun equalsAst(other: GAst, includingOrigin: Boolean) =
		this === other || (
			other is GListTypeRef &&
				elementType.equalsAst(other.elementType, includingOrigin = includingOrigin) &&
				(!includingOrigin || origin == other.origin)
			)


	companion object
}


class GName(
	val value: String,
	origin: GOrigin? = null
) : GAst(origin = origin) {

	override fun <Result, Data> accept(visitor: GAstVisitor<Result, Data>, data: Data) =
		visitor.visitName(this, data)


	override fun equalsAst(other: GAst, includingOrigin: Boolean) =
		this === other || (
			other is GName &&
				value == other.value &&
				(!includingOrigin || origin == other.origin)
			)


	companion object
}


sealed class GNamedType(
	description: GValue.String?,
	val directives: List<GDirective>,
	kind: GTypeKind,
	name: GName,
	origin: GOrigin?
) : GType(
	kind = kind,
	origin = origin
) {

	val description get() = descriptionNode?.value?.ifEmpty { null }
	val descriptionNode = description
	val name get() = nameNode.value
	val nameNode = name


	companion object
}


class GNamedTypeRef(
	name: GName,
	origin: GOrigin? = null
) : GTypeRef(origin = origin) {

	val name get() = nameNode.value
	val nameNode = name

	override val underlyingName get() = name


	constructor(
		name: String
	) : this(
		name = GName(name)
	)


	override fun <Result, Data> accept(visitor: GAstVisitor<Result, Data>, data: Data) =
		visitor.visitNamedTypeRef(this, data)


	override fun equalsAst(other: GAst, includingOrigin: Boolean) =
		this === other || (
			other is GNamedTypeRef &&
				nameNode.equalsAst(other.nameNode, includingOrigin = includingOrigin) &&
				(!includingOrigin || origin == other.origin)
			)


	companion object
}


// https://graphql.github.io/graphql-spec/June2018/#sec-Type-System.Non-Null
// https://graphql.github.io/graphql-spec/June2018/#sec-Type-Kinds.Non-Null
class GNonNullType(
	nullableType: GType
) : GWrappingType(
	kind = GTypeKind.NON_NULL,
	wrappedType = nullableType
) {

	val nullableType get() = wrappedType


	override fun equalsAst(other: GAst, includingOrigin: Boolean) =
		this === other || (
			other is GNonNullType &&
				nullableType.equalsAst(other.nullableType, includingOrigin = includingOrigin) &&
				(!includingOrigin || origin == other.origin)
			)


	override fun isSupertypeOf(other: GType) =
		other === this ||
			(other is GNonNullType && nullableType.isSupertypeOf(other.nullableType))
}


class GNonNullTypeRef(
	val nullableType: GTypeRef,
	origin: GOrigin? = null
) : GTypeRef(origin = origin) {

	override val underlyingName get() = nullableType.underlyingName


	override fun <Result, Data> accept(visitor: GAstVisitor<Result, Data>, data: Data) =
		visitor.visitNonNullTypeRef(this, data)


	override fun equalsAst(other: GAst, includingOrigin: Boolean) =
		this === other || (
			other is GNonNullTypeRef &&
				nullableType.equalsAst(other.nullableType, includingOrigin = includingOrigin) &&
				(!includingOrigin || origin == other.origin)
			)


	companion object
}


// https://graphql.github.io/graphql-spec/June2018/#sec-Objects
// https://graphql.github.io/graphql-spec/June2018/#sec-Object
class GObjectType(
	name: GName,
	override val fields: List<GFieldDefinition>,
	override val interfaces: List<GNamedTypeRef> = emptyList(),
	description: GValue.String? = null,
	directives: List<GDirective> = emptyList(),
	val kotlinType: KClass<*>? = null,
	origin: GOrigin? = null
) :
	GNamedType(
		description = description,
		directives = directives,
		kind = GTypeKind.OBJECT,
		name = name,
		origin = origin
	),
	GType.WithFields,
	GType.WithInterfaces {

	override val fieldsByName = fields.associateBy { it.name }


	constructor(
		name: String,
		fields: List<GFieldDefinition>,
		interfaces: List<GNamedTypeRef> = emptyList(),
		description: String? = null,
		kotlinType: KClass<*>? = null,
		directives: List<GDirective> = emptyList()
	) : this(
		name = GName(name),
		fields = fields,
		interfaces = interfaces,
		description = description?.let { GValue.String(it) },
		directives = directives,
		kotlinType = kotlinType
	)


	override fun <Result, Data> accept(visitor: GAstVisitor<Result, Data>, data: Data) =
		visitor.visitObjectType(this, data)


	override fun equalsAst(other: GAst, includingOrigin: Boolean) =
		this === other || (
			other is GObjectType &&
				descriptionNode.equalsAst(other.descriptionNode, includingOrigin = includingOrigin) &&
				directives.equalsAst(other.directives, includingOrigin = includingOrigin) &&
				fields.equalsAst(other.fields, includingOrigin = includingOrigin) &&
				interfaces.equalsAst(other.interfaces, includingOrigin = includingOrigin) &&
				nameNode.equalsAst(other.nameNode, includingOrigin = includingOrigin) &&
				(!includingOrigin || origin == other.origin)
			)


	override fun isSupertypeOf(other: GType): Boolean =
		other === this ||
			(other is GNonNullType && isSupertypeOf(other.nullableType))


	companion object
}


class GObjectTypeExtension(
	name: GName,
	val fields: List<GFieldDefinition> = emptyList(),
	val interfaces: List<GNamedTypeRef> = emptyList(),
	directives: List<GDirective> = emptyList(),
	origin: GOrigin? = null
) : GTypeExtension(
	directives = directives,
	name = name,
	origin = origin
) {

	val fieldsByName = fields.associateBy { it.name }


	constructor(
		name: String,
		fields: List<GFieldDefinition> = emptyList(),
		interfaces: List<GNamedTypeRef> = emptyList(),
		directives: List<GDirective> = emptyList()
	) : this(
		name = GName(name),
		fields = fields,
		interfaces = interfaces,
		directives = directives
	)


	override fun <Result, Data> accept(visitor: GAstVisitor<Result, Data>, data: Data) =
		visitor.visitObjectTypeExtension(this, data)


	override fun equalsAst(other: GAst, includingOrigin: Boolean) =
		this === other || (
			other is GObjectTypeExtension &&
				directives.equalsAst(other.directives, includingOrigin = includingOrigin) &&
				fields.equalsAst(other.fields, includingOrigin = includingOrigin) &&
				interfaces.equalsAst(other.interfaces, includingOrigin = includingOrigin) &&
				nameNode.equalsAst(other.nameNode, includingOrigin = includingOrigin) &&
				(!includingOrigin || origin == other.origin)
			)


	companion object
}


class GObjectValueField(
	name: GName,
	val value: GValue,
	origin: GOrigin? = null
) : GAst(origin = origin) {

	val name get() = nameNode.value
	val nameNode = name


	constructor(
		name: String,
		value: GValue
	) : this(
		name = GName(name),
		value = value
	)


	override fun <Result, Data> accept(visitor: GAstVisitor<Result, Data>, data: Data) =
		visitor.visitObjectValueField(this, data)


	override fun equalsAst(other: GAst, includingOrigin: Boolean) =
		this === other || (
			other is GObjectValueField &&
				nameNode.equalsAst(other.nameNode, includingOrigin = includingOrigin) &&
				value.equalsAst(other.value, includingOrigin = includingOrigin) &&
				(!includingOrigin || origin == other.origin)
			)
}


class GOperationDefinition(
	val type: GOperationType,
	name: GName? = null,
	val selectionSet: GSelectionSet,
	val variableDefinitions: List<GVariableDefinition> = emptyList(),
	val directives: List<GDirective> = emptyList(),
	origin: GOrigin? = null
) : GDefinition(origin = origin) {

	val name get() = nameNode?.value
	val nameNode = name
	val variableDefinitionsByName = variableDefinitions.associateBy { it.name }


	constructor(
		type: GOperationType,
		name: String? = null,
		selectionSet: GSelectionSet,
		variableDefinitions: List<GVariableDefinition> = emptyList(),
		directives: List<GDirective> = emptyList()
	) : this(
		type = type,
		name = name?.let { GName(it) },
		selectionSet = selectionSet,
		variableDefinitions = variableDefinitions,
		directives = directives
	)


	override fun <Result, Data> accept(visitor: GAstVisitor<Result, Data>, data: Data) =
		visitor.visitOperationDefinition(this, data)


	override fun equalsAst(other: GAst, includingOrigin: Boolean) =
		this === other || (
			other is GOperationDefinition &&
				directives.equalsAst(other.directives, includingOrigin = includingOrigin) &&
				nameNode.equalsAst(other.nameNode, includingOrigin = includingOrigin) &&
				selectionSet.equalsAst(other.selectionSet, includingOrigin = includingOrigin) &&
				type == other.type &&
				variableDefinitions.equalsAst(other.variableDefinitions, includingOrigin = includingOrigin) &&
				(!includingOrigin || origin == other.origin)
			)


	companion object
}


class GOperationTypeDefinition(
	val operation: GOperationType,
	val type: GNamedTypeRef,
	origin: GOrigin? = null
) : GAst(origin = origin) {

	override fun <Result, Data> accept(visitor: GAstVisitor<Result, Data>, data: Data) =
		visitor.visitOperationTypeDefinition(this, data)


	override fun equalsAst(other: GAst, includingOrigin: Boolean) =
		this === other || (
			other is GOperationTypeDefinition &&
				operation == other.operation &&
				type.equalsAst(other.type, includingOrigin = includingOrigin) &&
				(!includingOrigin || origin == other.origin)
			)


	companion object
}


// https://graphql.github.io/graphql-spec/June2018/#sec-Scalars
// https://graphql.github.io/graphql-spec/June2018/#sec-Scalar
sealed class GScalarType(
	name: GName,
	description: GValue.String? = null,
	directives: List<GDirective> = emptyList(),
	origin: GOrigin? = null
) : GNamedType(
	description = description,
	directives = directives,
	kind = GTypeKind.SCALAR,
	name = name,
	origin = origin
) {

	constructor(
		name: String,
		description: String? = null,
		directives: List<GDirective> = emptyList()
	) : this(
		name = GName(name),
		description = description?.let { GValue.String(it) },
		directives = directives
	)


	final override fun <Result, Data> accept(visitor: GAstVisitor<Result, Data>, data: Data) =
		visitor.visitScalarType(this, data)


	final override fun equalsAst(other: GAst, includingOrigin: Boolean) =
		this === other || (
			other is GScalarType &&
				descriptionNode.equalsAst(other.descriptionNode, includingOrigin = includingOrigin) &&
				directives.equalsAst(other.directives, includingOrigin = includingOrigin) &&
				nameNode.equalsAst(other.nameNode, includingOrigin = includingOrigin) &&
				(!includingOrigin || origin == other.origin)
			)


	final override fun isSupertypeOf(other: GType): Boolean =
		this == other ||
			(other is GNonNullType && isSupertypeOf(other.nullableType))


	companion object
}


object GBooleanType : GScalarType(name = "Boolean")
object GFloatType : GScalarType(name = "Float")
object GIDType : GScalarType(name = "ID")
object GIntType : GScalarType(name = "Int")
object GStringType : GScalarType(name = "String")


class GScalarTypeExtension(
	name: GName,
	directives: List<GDirective> = emptyList(),
	origin: GOrigin? = null
) : GTypeExtension(
	directives = directives,
	name = name,
	origin = origin
) {

	constructor(
		name: String,
		directives: List<GDirective> = emptyList()
	) : this(
		name = GName(name),
		directives = directives
	)


	override fun <Result, Data> accept(visitor: GAstVisitor<Result, Data>, data: Data) =
		visitor.visitScalarTypeExtension(this, data)


	override fun equalsAst(other: GAst, includingOrigin: Boolean) =
		this === other || (
			other is GScalarTypeExtension &&
				directives.equalsAst(other.directives, includingOrigin = includingOrigin) &&
				nameNode.equalsAst(other.nameNode, includingOrigin = includingOrigin) &&
				(!includingOrigin || origin == other.origin)
			)


	companion object
}


class GSchemaDefinition(
	val operationTypes: List<GOperationTypeDefinition> = emptyList(),
	val directives: List<GDirective> = emptyList(),
	origin: GOrigin? = null
) : GTypeSystemDefinition(origin = origin) {

	val operationTypesByName = operationTypes.associateBy { it.operation }


	override fun <Result, Data> accept(visitor: GAstVisitor<Result, Data>, data: Data) =
		visitor.visitSchemaDefinition(this, data)


	override fun equalsAst(other: GAst, includingOrigin: Boolean) =
		this === other || (
			other is GSchemaDefinition &&
				directives.equalsAst(other.directives, includingOrigin = includingOrigin) &&
				operationTypes.equalsAst(other.operationTypes, includingOrigin = includingOrigin) &&
				(!includingOrigin || origin == other.origin)
			)


	companion object
}


class GSchemaExtensionDefinition(
	val operationTypes: List<GOperationTypeDefinition> = emptyList(),
	val directives: List<GDirective> = emptyList(),
	origin: GOrigin? = null
) : GTypeSystemExtensionDefinition(origin = origin) {

	val operationTypesByName = operationTypes.associateBy { it.operation }


	override fun <Result, Data> accept(visitor: GAstVisitor<Result, Data>, data: Data) =
		visitor.visitSchemaExtension(this, data)


	override fun equalsAst(other: GAst, includingOrigin: Boolean) =
		this === other || (
			other is GSchemaExtensionDefinition &&
				directives.equalsAst(other.directives, includingOrigin = includingOrigin) &&
				operationTypes.equalsAst(other.operationTypes, includingOrigin = includingOrigin) &&
				(!includingOrigin || origin == other.origin)
			)


	companion object
}


sealed class GSelection(
	val directives: List<GDirective>,
	origin: GOrigin?
) : GAst(origin = origin)


class GSelectionSet(
	val selections: List<GSelection>,
	origin: GOrigin? = null
) : GAst(origin = origin) {

	override fun <Result, Data> accept(visitor: GAstVisitor<Result, Data>, data: Data) =
		visitor.visitSelectionSet(this, data)


	override fun equalsAst(other: GAst, includingOrigin: Boolean) =
		this === other || (
			other is GSelectionSet &&
				selections.equalsAst(other.selections, includingOrigin = includingOrigin) &&
				(!includingOrigin || origin == other.origin)
			)


	companion object
}


// https://graphql.github.io/graphql-spec/June2018/#sec-Wrapping-Types
// https://graphql.github.io/graphql-spec/June2018/#sec-Types
// https://graphql.github.io/graphql-spec/June2018/#sec-The-__Type-Type
sealed class GType(
	val kind: GTypeKind,
	origin: GOrigin?
) : GTypeSystemDefinition(origin = origin) {

	// https://graphql.github.io/graphql-spec/June2018/#IsInputType()
	fun isInputType(): Boolean =
		when (this) {
			is GWrappingType -> wrappedType.isInputType()
			is GScalarType, is GEnumType, is GInputObjectType -> true
			else -> false
		}


	// https://graphql.github.io/graphql-spec/June2018/#IsOutputType()
	fun isOutputType(): Boolean =
		when (this) {
			is GWrappingType -> wrappedType.isOutputType()
			is GScalarType, is GObjectType, is GInterfaceType, is GUnionType, is GEnumType -> true
			else -> false
		}


	fun isSubtypeOf(other: GType) =
		other.isSupertypeOf(this)


	abstract fun isSupertypeOf(other: GType): Boolean


	companion object {

		val defaultTypes = setOf<GNamedType>(
			GBooleanType,
			GFloatType,
			GIDType,
			GIntType,
			GStringType
		)
	}


	interface WithArguments {

		val arguments: List<GArgumentDefinition>
		val argumentsByName: Map<String, GArgumentDefinition>
	}


	interface WithFields {

		val fields: List<GFieldDefinition>
		val fieldsByName: Map<String, GFieldDefinition>
	}


	interface WithInterfaces {

		val interfaces: List<GNamedTypeRef>
	}
}


sealed class GTypeExtension(
	val directives: List<GDirective>,
	name: GName,
	origin: GOrigin?
) : GTypeSystemExtensionDefinition(origin = origin) {

	val name get() = nameNode.value
	val nameNode = name
}


// https://graphql.github.io/graphql-spec/June2018/#sec-Schema-Introspection
// https://graphql.github.io/graphql-spec/June2018/#sec-Type-Kinds
enum class GTypeKind {

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


sealed class GTypeRef(
	origin: GOrigin?
) : GAst(origin = origin) {

	abstract val underlyingName: String


	val nonNullable
		get() = when (this) {
			is GListTypeRef -> GNonNullTypeRef(this)
			is GNamedTypeRef -> GNonNullTypeRef(this)
			is GNonNullTypeRef -> this
		}


	val nullable
		get() = when (this) {
			is GListTypeRef -> this
			is GNamedTypeRef -> this
			is GNonNullTypeRef -> nullableType
		}


	companion object {

		fun parse(source: GSource.Parsable) =
			Parser.parseTypeReference(source)


		fun parse(content: String, name: String = "<type reference>") =
			parse(GSource.of(content = content, name = name))
	}
}


fun GTypeRef(name: String) =
	GNamedTypeRef(name)


val GBooleanTypeRef = GTypeRef("Boolean")
val GFloatTypeRef = GTypeRef("Float")
val GIDTypeRef = GTypeRef("ID")
val GIntTypeRef = GTypeRef("Int")
val GStringTypeRef = GTypeRef("String")


sealed class GTypeSystemDefinition(
	origin: GOrigin?
) : GDefinition(origin = origin)


sealed class GTypeSystemExtensionDefinition(
	origin: GOrigin?
) : GDefinition(origin = origin)


// https://graphql.github.io/graphql-spec/June2018/#sec-Unions
// https://graphql.github.io/graphql-spec/June2018/#sec-Union
class GUnionType(
	name: GName,
	val possibleTypes: List<GNamedTypeRef>,
	description: GValue.String? = null,
	directives: List<GDirective> = emptyList(),
	origin: GOrigin? = null
) : GAbstractType(
	description = description,
	directives = directives,
	kind = GTypeKind.UNION,
	name = name,
	origin = origin
) {

	constructor(
		name: String,
		possibleTypes: List<GNamedTypeRef>,
		description: String? = null,
		directives: List<GDirective> = emptyList()
	) : this(
		name = GName(name),
		possibleTypes = possibleTypes,
		description = description?.let { GValue.String(it) },
		directives = directives
	)


	override fun <Result, Data> accept(visitor: GAstVisitor<Result, Data>, data: Data) =
		visitor.visitUnionType(this, data)


	override fun equalsAst(other: GAst, includingOrigin: Boolean) =
		this === other || (
			other is GUnionType &&
				descriptionNode.equalsAst(other.descriptionNode, includingOrigin = includingOrigin) &&
				directives.equalsAst(other.directives, includingOrigin = includingOrigin) &&
				nameNode.equalsAst(other.nameNode, includingOrigin = includingOrigin) &&
				possibleTypes.equalsAst(other.possibleTypes, includingOrigin = includingOrigin) &&
				(!includingOrigin || origin == other.origin)
			)


	override fun isSupertypeOf(other: GType): Boolean =
		other === this ||
			other is GObjectType && possibleTypes.any { it.name == other.name } ||
			(other is GNonNullType && isSupertypeOf(other.nullableType))


	companion object
}


class GUnionTypeExtension(
	name: GName,
	val possibleTypes: List<GNamedTypeRef> = emptyList(),
	directives: List<GDirective> = emptyList(),
	origin: GOrigin? = null
) : GTypeExtension(
	directives = directives,
	name = name,
	origin = origin
) {

	constructor(
		name: String,
		possibleTypes: List<GNamedTypeRef> = emptyList(),
		directives: List<GDirective> = emptyList()
	) : this(
		name = GName(name),
		possibleTypes = possibleTypes,
		directives = directives
	)


	override fun <Result, Data> accept(visitor: GAstVisitor<Result, Data>, data: Data) =
		visitor.visitUnionTypeExtension(this, data)


	override fun equalsAst(other: GAst, includingOrigin: Boolean) =
		this === other || (
			other is GUnionTypeExtension &&
				directives.equalsAst(other.directives, includingOrigin = includingOrigin) &&
				nameNode.equalsAst(other.nameNode, includingOrigin = includingOrigin) &&
				possibleTypes.equalsAst(other.possibleTypes, includingOrigin = includingOrigin) &&
				(!includingOrigin || origin == other.origin)
			)


	companion object
}


sealed class GValue(
	origin: GOrigin?
) : GAst(origin = origin) {

	abstract val type: Type


	class Boolean(
		val value: kotlin.Boolean,
		origin: GOrigin? = null
	) : GValue(origin = origin) {

		override val type get() = Type.BOOLEAN


		override fun <Result, Data> accept(visitor: GAstVisitor<Result, Data>, data: Data) =
			visitor.visitBooleanValue(this, data)


		override fun equalsAst(other: GAst, includingOrigin: kotlin.Boolean) =
			this === other || (
				other is Boolean &&
					value == other.value &&
					(!includingOrigin || origin == other.origin)
				)


		companion object
	}


	class Enum(
		val name: kotlin.String,
		origin: GOrigin? = null
	) : GValue(origin = origin) {

		override val type get() = Type.ENUM


		override fun <Result, Data> accept(visitor: GAstVisitor<Result, Data>, data: Data) =
			visitor.visitEnumValue(this, data)


		override fun equalsAst(other: GAst, includingOrigin: kotlin.Boolean) =
			this === other || (
				other is Enum &&
					name == other.name &&
					(!includingOrigin || origin == other.origin)
				)


		companion object
	}


	class Float(
		val value: kotlin.String,
		origin: GOrigin? = null
	) : GValue(origin = origin) {

		override val type get() = Type.FLOAT


		override fun <Result, Data> accept(visitor: GAstVisitor<Result, Data>, data: Data) =
			visitor.visitFloatValue(this, data)


		override fun equalsAst(other: GAst, includingOrigin: kotlin.Boolean) =
			this === other || (
				other is Float &&
					value == other.value &&
					(!includingOrigin || origin == other.origin)
				)


		companion object
	}


	class Int(
		val value: kotlin.String,
		origin: GOrigin? = null
	) : GValue(origin = origin) {

		override val type get() = Type.INT


		override fun <Result, Data> accept(visitor: GAstVisitor<Result, Data>, data: Data) =
			visitor.visitIntValue(this, data)


		override fun equalsAst(other: GAst, includingOrigin: kotlin.Boolean) =
			this === other || (
				other is Int &&
					value == other.value &&
					(!includingOrigin || origin == other.origin)
				)


		companion object
	}


	class List(
		val elements: kotlin.collections.List<GValue>,
		origin: GOrigin? = null
	) : GValue(origin = origin) {

		override val type get() = Type.FLOAT


		override fun <Result, Data> accept(visitor: GAstVisitor<Result, Data>, data: Data) =
			visitor.visitListValue(this, data)


		override fun equalsAst(other: GAst, includingOrigin: kotlin.Boolean) =
			this === other || (
				other is List &&
					elements.equalsAst(other.elements, includingOrigin = includingOrigin) &&
					(!includingOrigin || origin == other.origin)
				)


		companion object
	}


	class Null(
		origin: GOrigin? = null
	) : GValue(origin = origin) {

		override val type get() = Type.NULL


		override fun <Result, Data> accept(visitor: GAstVisitor<Result, Data>, data: Data) =
			visitor.visitNullValue(this, data)


		override fun equalsAst(other: GAst, includingOrigin: kotlin.Boolean) =
			this === other || (
				other is Null &&
					(!includingOrigin || origin == other.origin)
				)


		companion object
	}


	class Object(
		val fields: kotlin.collections.List<GObjectValueField>,
		origin: GOrigin? = null
	) : GValue(origin = origin) {

		val fieldsByName = fields.associate { it.name to it.value }

		override val type get() = Type.OBJECT


		override fun <Result, Data> accept(visitor: GAstVisitor<Result, Data>, data: Data) =
			visitor.visitObjectValue(this, data)


		override fun equalsAst(other: GAst, includingOrigin: kotlin.Boolean) =
			this === other || (
				other is Object &&
					fields.equalsAst(other.fields, includingOrigin = includingOrigin) &&
					(!includingOrigin || origin == other.origin)
				)


		companion object
	}


	class String(
		val value: kotlin.String,
		val isBlock: kotlin.Boolean = false,
		origin: GOrigin? = null
	) : GValue(origin = origin) {

		override val type get() = Type.STRING


		override fun <Result, Data> accept(visitor: GAstVisitor<Result, Data>, data: Data) =
			visitor.visitStringValue(this, data)


		override fun equalsAst(other: GAst, includingOrigin: kotlin.Boolean) =
			this === other || (
				other is String &&
					value == other.value &&
					isBlock == other.isBlock &&
					(!includingOrigin || origin == other.origin)
				)


		companion object
	}


	class Variable(
		name: GName,
		origin: GOrigin? = null
	) : GValue(origin = origin) {

		val name get() = nameNode.value
		val nameNode = name

		override val type get() = Type.VARIABLE


		constructor(
			name: kotlin.String
		) : this(
			name = GName(name)
		)


		override fun <Result, Data> accept(visitor: GAstVisitor<Result, Data>, data: Data) =
			visitor.visitVariableValue(this, data)


		override fun equalsAst(other: GAst, includingOrigin: kotlin.Boolean) =
			this === other || (
				other is Variable &&
					nameNode.equalsAst(other.nameNode, includingOrigin = includingOrigin) &&
					(!includingOrigin || origin == other.origin)
				)


		companion object
	}


	companion object {

		// FIXME temporary
		fun of(value: Any?): GValue? {
			return when (value) {
				null -> GNullValue
				is kotlin.Boolean -> Boolean(value)
				is kotlin.Float -> Float(value.toString())
				is kotlin.Int -> Int(value.toString())
				is Map<*, *> -> Object(value.map { (fieldName, fieldValue) ->
					GObjectValueField(
						name = fieldName as? kotlin.String ?: return null,
						value = of(fieldValue) ?: return null
					)
				})
				is Collection<*> -> List(value.map { of(it) ?: return null })
				is kotlin.String -> String(value)
				else -> return null
			}
		}


		fun parse(source: GSource.Parsable) =
			Parser.parseValue(source)


		fun parse(content: kotlin.String, name: kotlin.String = "<value>") =
			parse(GSource.of(content = content, name = name))
	}


	enum class Type {

		BOOLEAN,
		ENUM,
		FLOAT,
		INT,
		NULL,
		OBJECT,
		STRING,
		VARIABLE;


		override fun toString() = when (this) {
			BOOLEAN -> "boolean"
			ENUM -> "enum value"
			FLOAT -> "float"
			INT -> "int"
			NULL -> "null"
			OBJECT -> "input object"
			STRING -> "string"
			VARIABLE -> "variable"
		}


		companion object
	}
}


val GNullValue = GValue.Null()


class GVariableDefinition(
	val variable: GValue.Variable,
	val type: GTypeRef,
	val defaultValue: GValue? = null,
	val directives: List<GDirective> = emptyList(),
	origin: GOrigin? = null
) : GAst(origin = origin) {

	val name get() = variable.name


	constructor(
		name: String,
		type: GTypeRef,
		defaultValue: GValue? = null,
		directives: List<GDirective> = emptyList()
	) : this(
		variable = GValue.Variable(name = name),
		type = type,
		defaultValue = defaultValue,
		directives = directives
	)


	override fun <Result, Data> accept(visitor: GAstVisitor<Result, Data>, data: Data) =
		visitor.visitVariableDefinition(this, data)


	override fun equalsAst(other: GAst, includingOrigin: Boolean) =
		this === other || (
			other is GVariableDefinition &&
				defaultValue.equalsAst(other.defaultValue, includingOrigin = includingOrigin) &&
				directives.equalsAst(other.directives, includingOrigin = includingOrigin) &&
				type.equalsAst(other.type, includingOrigin = includingOrigin) &&
				variable.equalsAst(other.variable, includingOrigin = includingOrigin) &&
				(!includingOrigin || origin == other.origin)
			)


	companion object
}


// https://graphql.github.io/graphql-spec/June2018/#sec-Wrapping-Types
// https://graphql.github.io/graphql-spec/June2018/#sec-Types
sealed class GWrappingType(
	kind: GTypeKind,
	val wrappedType: GType
) : GType(
	kind = kind,
	origin = null
) {

	final override fun <Result, Data> accept(visitor: GAstVisitor<Result, Data>, data: Data) =
		wrappedType.accept(visitor = visitor, data = data)


	companion object
}
