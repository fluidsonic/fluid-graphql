package io.fluidsonic.graphql


// FIXME hashCode
sealed class GNode(
	val extensions: GNodeExtensionSet,
	val origin: GDocumentPosition?
) {

	fun childAt(index: Int): GNode? {
		var childIndex = 0

		forEachChild { child ->
			if (childIndex == index)
				return child

			childIndex += 1
		}

		return null
	}


	fun children(): List<GNode> {
		var list: MutableList<GNode>? = null

		forEachChild { child ->
			(list ?: mutableListOf<GNode>().also { list = it })
				.add(child)
		}

		return list.orEmpty()
	}


	fun countChildren(): Int {
		var count = 0
		forEachChild { count += 1 }

		return count
	}


	abstract fun equalsNode(other: GNode, includingOrigin: Boolean = false): Boolean


	private inline fun forNode(node: GNode?, block: (node: GNode) -> Unit) {
		if (node !== null)
			block(node)
	}


	private inline fun forNodes(nodes: List<GNode>, block: (node: GNode) -> Unit) {
		for (node in nodes)
			block(node)
	}


	private inline fun forEachChild(block: (child: GNode) -> Unit) {
		@Suppress("UNUSED_VARIABLE") // Exhaustiveness check.
		val exhaustive = when (this) {
			is GArgument -> {
				forNode(nameNode, block)
				forNode(value, block)
			}

			is GArgumentDefinition -> {
				forNode(descriptionNode, block)
				forNode(nameNode, block)
				forNode(type, block)
				forNode(defaultValue, block)
				forNodes(directives, block)
			}

			is GBooleanValue ->
				Unit

			is GDirective -> {
				forNode(nameNode, block)
				forNodes(arguments, block)
			}


			is GDirectiveDefinition -> {
				forNode(descriptionNode, block)
				forNode(nameNode, block)
				forNodes(argumentDefinitions, block)
				forNodes(locationNodes, block)
			}

			is GDocument ->
				forNodes(definitions, block)

			is GEnumType -> {
				forNode(descriptionNode, block)
				forNode(nameNode, block)
				forNodes(directives, block)
				forNodes(values, block)
			}

			is GEnumTypeExtension -> {
				forNode(nameNode, block)
				forNodes(directives, block)
				forNodes(values, block)
			}

			is GEnumValue ->
				Unit

			is GEnumValueDefinition -> {
				forNode(descriptionNode, block)
				forNode(nameNode, block)
				forNodes(directives, block)
			}

			is GFieldDefinition -> {
				forNode(descriptionNode, block)
				forNode(nameNode, block)
				forNodes(argumentDefinitions, block)
				forNode(type, block)
				forNodes(directives, block)
			}

			is GFieldSelection -> {
				forNode(aliasNode, block)
				forNode(nameNode, block)
				forNodes(arguments, block)
				forNodes(directives, block)
				forNode(selectionSet, block)
			}

			is GFloatValue ->
				Unit

			is GFragmentDefinition -> {
				forNode(nameNode, block)
				forNodes(variableDefinitions, block)
				forNode(typeCondition, block)
				forNodes(directives, block)
				forNode(selectionSet, block)
			}

			is GFragmentSelection -> {
				forNode(nameNode, block)
				forNodes(directives, block)
			}

			is GInlineFragmentSelection -> {
				forNode(typeCondition, block)
				forNodes(directives, block)
				forNode(selectionSet, block)
			}

			is GInputObjectType -> {
				forNode(descriptionNode, block)
				forNode(nameNode, block)
				forNodes(directives, block)
				forNodes(argumentDefinitions, block)
			}

			is GInputObjectTypeExtension -> {
				forNode(nameNode, block)
				forNodes(directives, block)
				forNodes(argumentDefinitions, block)
			}

			is GIntValue ->
				Unit

			is GInterfaceType -> {
				forNode(descriptionNode, block)
				forNode(nameNode, block)
				forNodes(interfaces, block)
				forNodes(directives, block)
				forNodes(fieldDefinitions, block)
			}

			is GInterfaceTypeExtension -> {
				forNode(nameNode, block)
				forNodes(interfaces, block)
				forNodes(directives, block)
				forNodes(fieldDefinitions, block)
			}

			is GListType ->
				forNode(elementType, block)

			is GListTypeRef ->
				forNode(elementType, block)

			is GListValue ->
				forNodes(elements, block)

			is GName ->
				Unit

			is GNamedTypeRef ->
				forNode(nameNode, block)

			is GNonNullType ->
				forNode(nullableType, block)

			is GNonNullTypeRef ->
				forNode(nullableRef, block)

			is GNullValue ->
				Unit

			is GObjectType -> {
				forNode(descriptionNode, block)
				forNode(nameNode, block)
				forNodes(interfaces, block)
				forNodes(directives, block)
				forNodes(fieldDefinitions, block)
			}

			is GObjectTypeExtension -> {
				forNode(nameNode, block)
				forNodes(interfaces, block)
				forNodes(directives, block)
				forNodes(fieldDefinitions, block)
			}

			is GObjectValue ->
				forNodes(arguments, block)

			is GOperationDefinition -> {
				forNode(nameNode, block)
				forNodes(variableDefinitions, block)
				forNodes(directives, block)
				forNode(selectionSet, block)
			}

			is GOperationTypeDefinition ->
				forNode(type, block)

			is GScalarType -> {
				forNode(descriptionNode, block)
				forNode(nameNode, block)
				forNodes(directives, block)
			}

			is GScalarTypeExtension -> {
				forNode(nameNode, block)
				forNodes(directives, block)
			}

			is GSchemaDefinition -> {
				forNodes(directives, block)
				forNodes(operationTypeDefinitions, block)
			}

			is GSchemaExtension -> {
				forNodes(directives, block)
				forNodes(operationTypeDefinitions, block)
			}

			is GSelectionSet ->
				forNodes(selections, block)

			is GStringValue ->
				Unit

			is GUnionType -> {
				forNode(descriptionNode, block)
				forNode(nameNode, block)
				forNodes(directives, block)
				forNodes(possibleTypes, block)
			}

			is GUnionTypeExtension -> {
				forNode(nameNode, block)
				forNodes(directives, block)
				forNodes(possibleTypes, block)
			}

			is GVariableDefinition -> {
				forNode(nameNode, block)
				forNode(type, block)
				forNode(defaultValue, block)
				forNodes(directives, block)
			}

			is GVariableRef ->
				forNode(nameNode, block)
		}
	}


	operator fun <Value : Any> get(extensionKey: GNodeExtensionKey<Value>): Value? =
		extensions[extensionKey]


	fun hasChildren(): Boolean {
		forEachChild { return true }

		return false
	}


	override fun toString(): String =
		print(this)


	companion object {

		fun print(node: GNode, indent: String = "\t"): String =
			Printer.print(node = node, indent = indent)
	}


	interface WithArguments {

		val arguments: List<GArgument>

		fun argument(name: String) =
			arguments.firstOrNull { it.name == name }
	}


	interface WithArgumentDefinitions {

		val argumentDefinitions: List<GArgumentDefinition>


		fun argumentDefinition(name: String) =
			argumentDefinitions.firstOrNull { it.name == name }
	}


	interface WithDirectives {

		val directives: List<GDirective>


		fun directive(name: String) =
			directives.firstOrNull { it.name == name }


		fun directives(name: String) =
			directives.filter { it.name == name }
	}


	interface WithFieldDefinitions {

		val fieldDefinitions: List<GFieldDefinition>


		fun field(name: String) =
			fieldDefinitions.firstOrNull { it.name == name }
	}


	interface WithInterfaces {

		val interfaces: List<GNamedTypeRef>
	}


	interface WithName : WithOptionalName {

		override val name
			get() = nameNode.value


		override val nameNode: GName
	}


	interface WithOperationTypeDefinitions {

		val operationTypeDefinitions: List<GOperationTypeDefinition>


		fun operationTypeDefinition(operationType: GOperationType) =
			operationTypeDefinitions.firstOrNull { it.operationType == operationType }
	}


	interface WithOptionalDeprecation : WithDirectives, WithName {

		val deprecation
			get() = directive(GLanguage.defaultDeprecatedDirective.name)


		val deprecationReason
			get() = (deprecation?.argument("reason")?.value as? GStringValue)?.value
	}


	interface WithOptionalDescription {

		val description
			get() = descriptionNode?.value


		val descriptionNode: GStringValue?
	}


	interface WithOptionalName {

		val name
			get() = nameNode?.value


		val nameNode: GName?
	}


	interface WithVariableDefinitions {

		val variableDefinitions: List<GVariableDefinition>


		fun variableDefinition(name: String) =
			variableDefinitions.firstOrNull { it.name == name }
	}
}


fun GNode?.equalsNode(other: GNode?, includingOrigin: Boolean = false) =
	this === other || (this !== null && other !== null && equalsNode(other, includingOrigin = includingOrigin))


fun List<GNode?>.equalsNode(other: List<GNode?>, includingOrigin: Boolean): Boolean {
	if (this === other)
		return true

	if (size != other.size)
		return false

	forEachIndexed { index, ast ->
		if (!ast.equalsNode(other[index], includingOrigin = includingOrigin))
			return false
	}

	return true
}


sealed class GAbstractType(
	description: GStringValue?,
	directives: List<GDirective>,
	extensions: GNodeExtensionSet,
	kind: Kind,
	name: GName,
	origin: GDocumentPosition?
) : GCompositeType(
	description = description,
	directives = directives,
	extensions = extensions,
	kind = kind,
	name = name,
	origin = origin
) {

	companion object
}


class GArgument(
	name: GName,
	val value: GValue,
	origin: GDocumentPosition? = null,
	extensions: (GNodeExtensionSet.Builder<GArgument>.() -> Unit)? = null
) :
	GNode(
		extensions = extensions.build(),
		origin = origin
	),
	GNode.WithName {

	override val nameNode = name


	constructor(
		name: String,
		value: GValue,
		extensions: (GNodeExtensionSet.Builder<GArgument>.() -> Unit)? = null
	) : this(
		name = GName(name),
		value = value,
		extensions = extensions
	)


	override fun equalsNode(other: GNode, includingOrigin: Boolean) =
		this === other || (
			other is GArgument &&
				nameNode.equalsNode(other.nameNode, includingOrigin = includingOrigin) &&
				value.equalsNode(other.value, includingOrigin = includingOrigin) &&
				(!includingOrigin || origin == other.origin)
			)


	companion object
}


sealed class GArgumentDefinition(
	val defaultValue: GValue?,
	description: GStringValue?,
	override val directives: List<GDirective>,
	extensions: GNodeExtensionSet,
	name: GName,
	origin: GDocumentPosition?,
	val type: GTypeRef
) :
	GNode(
		extensions = extensions,
		origin = origin
	),
	GNode.WithDirectives,
	GNode.WithName,
	GNode.WithOptionalDescription {

	override val descriptionNode = description
	override val nameNode = name


	override fun equalsNode(other: GNode, includingOrigin: Boolean) =
		this === other || (
			other is GArgumentDefinition &&
				defaultValue.equalsNode(other.defaultValue, includingOrigin = includingOrigin) &&
				descriptionNode.equalsNode(other.descriptionNode, includingOrigin = includingOrigin) &&
				directives.equalsNode(other.directives, includingOrigin = includingOrigin) &&
				nameNode.equalsNode(other.nameNode, includingOrigin = includingOrigin) &&
				type.equalsNode(other.type, includingOrigin = includingOrigin) &&
				(!includingOrigin || origin == other.origin)
			)


	fun isOptional() =
		!isRequired()


	fun isRequired() =
		type is GNonNullTypeRef && defaultValue === null


	companion object
}


// https://graphql.github.io/graphql-spec/draft/#sec-Boolean.Input-Coercion
object GBooleanType : GScalarType(name = "Boolean")


class GBooleanValue(
	val value: Boolean,
	origin: GDocumentPosition? = null,
	extensions: (GNodeExtensionSet.Builder<GFieldDefinition>.() -> Unit)? = null
) : GValue(
	extensions = extensions.build(),
	origin = origin
) {

	override val kind get() = Kind.BOOLEAN


	override fun equals(other: Any?) =
		this === other || (other is GBooleanValue && value == other.value)


	override fun equalsNode(other: GNode, includingOrigin: Boolean) =
		this === other || (
			other is GBooleanValue &&
				value == other.value &&
				(!includingOrigin || origin == other.origin)
			)


	override fun hashCode() =
		value.hashCode()


	override fun unwrap() =
		value


	companion object
}


sealed class GCompositeType(
	description: GStringValue?,
	directives: List<GDirective>,
	extensions: GNodeExtensionSet,
	kind: Kind,
	name: GName,
	origin: GDocumentPosition?
) : GNamedType(
	description = description,
	directives = directives,
	extensions = extensions,
	kind = kind,
	name = name,
	origin = origin
) {

	companion object
}


// https://graphql.github.io/graphql-spec/draft/#sec-Scalars.Input-Coercion
class GCustomScalarType(
	name: GName,
	description: GStringValue? = null,
	directives: List<GDirective> = emptyList(),
	origin: GDocumentPosition? = null,
	extensions: (GNodeExtensionSet.Builder<GFieldDefinition>.() -> Unit)? = null
) : GScalarType(
	description = description,
	directives = directives,
	extensions = extensions.build(),
	name = name,
	origin = origin
) {

	constructor(
		name: String,
		description: String? = null,
		directives: List<GDirective> = emptyList(),
		extensions: (GNodeExtensionSet.Builder<GFieldDefinition>.() -> Unit)? = null
	) : this(
		name = GName(name),
		description = description?.let(::GStringValue),
		directives = directives,
		extensions = extensions
	)


	companion object
}


sealed class GDefinition(
	extensions: GNodeExtensionSet,
	origin: GDocumentPosition?
) : GNode(
	extensions = extensions,
	origin = origin
) {

	companion object
}


class GDirective(
	name: GName,
	override val arguments: List<GArgument> = emptyList(),
	origin: GDocumentPosition? = null,
	extensions: (GNodeExtensionSet.Builder<GFieldDefinition>.() -> Unit)? = null
) :
	GNode(
		extensions = extensions.build(),
		origin = origin
	),
	GNode.WithArguments,
	GNode.WithName {

	override val nameNode = name


	constructor(
		name: String,
		arguments: List<GArgument> = emptyList(),
		extensions: (GNodeExtensionSet.Builder<GFieldDefinition>.() -> Unit)? = null
	) : this(
		name = GName(name),
		arguments = arguments,
		extensions = extensions.build()
	)


	override fun equalsNode(other: GNode, includingOrigin: Boolean) =
		this === other || (
			other is GDirective &&
				arguments.equalsNode(other.arguments, includingOrigin = includingOrigin) &&
				nameNode.equalsNode(other.nameNode, includingOrigin = includingOrigin) &&
				(!includingOrigin || origin == other.origin)
			)


	companion object
}


class GDirectiveArgumentDefinition(
	name: GName,
	type: GTypeRef,
	defaultValue: GValue? = null,
	description: GStringValue? = null,
	directives: List<GDirective> = emptyList(),
	origin: GDocumentPosition? = null,
	extensions: (GNodeExtensionSet.Builder<GFieldDefinition>.() -> Unit)? = null
) : GArgumentDefinition(
	defaultValue = defaultValue,
	description = description,
	directives = directives,
	extensions = extensions.build(),
	name = name,
	type = type,
	origin = origin
) {

	constructor(
		name: String,
		type: GTypeRef,
		defaultValue: GValue? = null,
		description: String? = null,
		directives: List<GDirective> = emptyList(),
		extensions: (GNodeExtensionSet.Builder<GFieldDefinition>.() -> Unit)? = null
	) : this(
		name = GName(name),
		type = type,
		defaultValue = defaultValue,
		description = description?.let(::GStringValue),
		directives = directives,
		extensions = extensions.build()
	)

	companion object
}


class GDirectiveDefinition(
	name: GName,
	locations: List<GName>,
	val isRepeatable: Boolean = false,
	override val argumentDefinitions: List<GDirectiveArgumentDefinition> = emptyList(),
	description: GStringValue? = null,
	origin: GDocumentPosition? = null,
	extensions: (GNodeExtensionSet.Builder<GFieldDefinition>.() -> Unit)? = null
) :
	GTypeSystemDefinition(
		extensions = extensions.build(),
		origin = origin
	),
	GNode.WithArgumentDefinitions,
	GNode.WithName,
	GNode.WithOptionalDescription {

	val locations: Set<GDirectiveLocation> = locations.mapNotNullTo(mutableSetOf()) { node ->
		GDirectiveLocation.values().firstOrNull { it.name == node.value }
	}
	val locationNodes = locations

	override val descriptionNode = description
	override val nameNode = name


	constructor(
		name: String,
		locations: Set<GDirectiveLocation>,
		isRepeatable: Boolean = false,
		argumentDefinitions: List<GDirectiveArgumentDefinition> = emptyList(),
		description: String? = null,
		extensions: (GNodeExtensionSet.Builder<GFieldDefinition>.() -> Unit)? = null
	) : this(
		name = GName(name),
		locations = locations.map { GName(it.name) },
		isRepeatable = isRepeatable,
		argumentDefinitions = argumentDefinitions,
		description = description?.let(::GStringValue),
		extensions = extensions
	)


	override fun argumentDefinition(name: String) =
		argumentDefinitions.firstOrNull { it.name == name }


	override fun equalsNode(other: GNode, includingOrigin: Boolean) =
		this === other || (
			other is GDirectiveDefinition &&
				argumentDefinitions.equalsNode(other.argumentDefinitions, includingOrigin = includingOrigin) &&
				descriptionNode.equalsNode(other.descriptionNode, includingOrigin = includingOrigin) &&
				isRepeatable == other.isRepeatable &&
				locationNodes.equalsNode(other.locationNodes, includingOrigin = includingOrigin) &&
				nameNode.equalsNode(other.nameNode, includingOrigin = includingOrigin) &&
				(!includingOrigin || origin == other.origin)
			)


	companion object
}


class GDocument(
	val definitions: List<GDefinition>,
	origin: GDocumentPosition? = null,
	extensions: (GNodeExtensionSet.Builder<GFieldDefinition>.() -> Unit)? = null
) : GNode(
	extensions = extensions.build(),
	origin = origin
) {

	override fun equalsNode(other: GNode, includingOrigin: Boolean) =
		this === other || (
			other is GDocument &&
				definitions.equalsNode(other.definitions, includingOrigin = includingOrigin) &&
				(!includingOrigin || origin == other.origin)
			)


	fun fragment(name: String): GFragmentDefinition? {
		for (definition in definitions)
			if (definition is GFragmentDefinition && definition.name == name)
				return definition

		return null
	}


	fun operation(name: String?): GOperationDefinition? {
		for (definition in definitions)
			if (definition is GOperationDefinition && definition.name == name)
				return definition

		return null
	}


	companion object {

		fun parse(source: GDocumentSource.Parsable) =
			Parser.parseDocument(source)


		fun parse(content: String, name: String = "<document>") =
			parse(GDocumentSource.of(content = content, name = name))
	}
}


// https://graphql.github.io/graphql-spec/June2018/#sec-Enums
// https://graphql.github.io/graphql-spec/draft/#sec-Enums.Input-Coercion
class GEnumType(
	name: GName,
	val values: List<GEnumValueDefinition>,
	description: GStringValue? = null,
	directives: List<GDirective> = emptyList(),
	origin: GDocumentPosition? = null,
	extensions: (GNodeExtensionSet.Builder<GFieldDefinition>.() -> Unit)? = null
) : GLeafType(
	description = description,
	directives = directives,
	extensions = extensions.build(),
	kind = Kind.ENUM,
	name = name,
	origin = origin
) {

	constructor(
		name: String,
		values: List<GEnumValueDefinition>,
		description: String? = null,
		directives: List<GDirective> = emptyList(),
		extensions: (GNodeExtensionSet.Builder<GFieldDefinition>.() -> Unit)? = null
	) : this(
		name = GName(name),
		values = values,
		description = description?.let(::GStringValue),
		directives = directives,
		extensions = extensions.build()
	)


	override fun equalsNode(other: GNode, includingOrigin: Boolean) =
		this === other || (
			other is GEnumType &&
				descriptionNode.equalsNode(other.descriptionNode, includingOrigin = includingOrigin) &&
				directives.equalsNode(other.directives, includingOrigin = includingOrigin) &&
				nameNode.equalsNode(other.nameNode, includingOrigin = includingOrigin) &&
				values.equalsNode(other.values, includingOrigin = includingOrigin) &&
				(!includingOrigin || origin == other.origin)
			)


	override fun isSupertypeOf(other: GType) =
		other === this ||
			(other is GNonNullType && other.nullableType === this)


	fun value(name: String) =
		values.firstOrNull { it.name == name }


	companion object
}


class GEnumTypeExtension(
	name: GName,
	val values: List<GEnumValueDefinition>,
	directives: List<GDirective> = emptyList(),
	origin: GDocumentPosition? = null,
	extensions: (GNodeExtensionSet.Builder<GFieldDefinition>.() -> Unit)? = null
) : GTypeExtension(
	directives = directives,
	extensions = extensions.build(),
	name = name,
	origin = origin
) {

	constructor(
		name: String,
		values: List<GEnumValueDefinition>,
		directives: List<GDirective> = emptyList(),
		extensions: (GNodeExtensionSet.Builder<GFieldDefinition>.() -> Unit)? = null
	) : this(
		name = GName(name),
		values = values,
		directives = directives,
		extensions = extensions
	)


	override fun equalsNode(other: GNode, includingOrigin: Boolean) =
		this === other || (
			other is GEnumTypeExtension &&
				directives.equalsNode(other.directives, includingOrigin = includingOrigin) &&
				nameNode.equalsNode(other.nameNode, includingOrigin = includingOrigin) &&
				values.equalsNode(other.values, includingOrigin = includingOrigin) &&
				(!includingOrigin || origin == other.origin)
			)


	fun value(name: String) =
		values.firstOrNull { it.name == name }


	companion object
}


class GEnumValue(
	val name: String,
	origin: GDocumentPosition? = null,
	extensions: (GNodeExtensionSet.Builder<GFieldDefinition>.() -> Unit)? = null
) : GValue(
	extensions = extensions.build(),
	origin = origin
) {

	override val kind get() = Kind.ENUM


	override fun equals(other: Any?) =
		this === other || (other is GEnumValue && name == other.name)


	override fun equalsNode(other: GNode, includingOrigin: Boolean) =
		this === other || (
			other is GEnumValue &&
				name == other.name &&
				(!includingOrigin || origin == other.origin)
			)


	override fun hashCode() =
		name.hashCode()


	override fun unwrap() =
		name


	companion object
}


class GEnumValueDefinition(
	name: GName,
	description: GStringValue? = null,
	override val directives: List<GDirective> = emptyList(),
	origin: GDocumentPosition? = null,
	extensions: (GNodeExtensionSet.Builder<GFieldDefinition>.() -> Unit)? = null
) :
	GNode(
		extensions = extensions.build(),
		origin = origin
	),
	GNode.WithOptionalDeprecation,
	GNode.WithOptionalDescription {

	override val descriptionNode = description
	override val nameNode = name


	constructor(
		name: String,
		description: String? = null,
		directives: List<GDirective> = emptyList(),
		extensions: (GNodeExtensionSet.Builder<GFieldDefinition>.() -> Unit)? = null
	) : this(
		name = GName(name),
		description = description?.let(::GStringValue),
		directives = directives,
		extensions = extensions
	)


	override fun equalsNode(other: GNode, includingOrigin: Boolean) =
		this === other || (
			other is GEnumValueDefinition &&
				descriptionNode.equalsNode(other.descriptionNode, includingOrigin = includingOrigin) &&
				directives.equalsNode(other.directives, includingOrigin = includingOrigin) &&
				nameNode.equalsNode(other.nameNode, includingOrigin = includingOrigin) &&
				(!includingOrigin || origin == other.origin)
			)


	companion object
}


sealed class GExecutableDefinition(
	extensions: GNodeExtensionSet,
	origin: GDocumentPosition?
) : GDefinition(
	extensions = extensions,
	origin = origin
) {

	companion object
}


class GFieldArgumentDefinition(
	name: GName,
	type: GTypeRef,
	defaultValue: GValue? = null,
	description: GStringValue? = null,
	directives: List<GDirective> = emptyList(),
	origin: GDocumentPosition? = null,
	extensions: (GNodeExtensionSet.Builder<GFieldDefinition>.() -> Unit)? = null
) : GArgumentDefinition(
	name = name,
	type = type,
	defaultValue = defaultValue,
	description = description,
	directives = directives,
	origin = origin,
	extensions = extensions.build()
) {

	constructor(
		name: String,
		type: GTypeRef,
		defaultValue: GValue? = null,
		description: String? = null,
		directives: List<GDirective> = emptyList(),
		extensions: (GNodeExtensionSet.Builder<GFieldDefinition>.() -> Unit)? = null
	) : this(
		name = GName(name),
		type = type,
		defaultValue = defaultValue,
		description = description?.let(::GStringValue),
		directives = directives,
		extensions = extensions
	)

	companion object
}


class GFieldDefinition(
	name: GName,
	val type: GTypeRef,
	override val argumentDefinitions: List<GFieldArgumentDefinition> = emptyList(),
	description: GStringValue? = null,
	override val directives: List<GDirective> = emptyList(),
	origin: GDocumentPosition? = null,
	extensions: (GNodeExtensionSet.Builder<GFieldDefinition>.() -> Unit)? = null
) :
	GNode(
		extensions = extensions.build(),
		origin = origin
	),
	GNode.WithArgumentDefinitions,
	GNode.WithOptionalDescription,
	GNode.WithOptionalDeprecation {

	override val descriptionNode = description
	override val nameNode = name


	constructor(
		name: String,
		type: GTypeRef,
		argumentDefinitions: List<GFieldArgumentDefinition> = emptyList(),
		description: String? = null,
		directives: List<GDirective> = emptyList(),
		extensions: (GNodeExtensionSet.Builder<GFieldDefinition>.() -> Unit)? = null
	) : this(
		name = GName(name),
		type = type,
		argumentDefinitions = argumentDefinitions,
		description = description?.let(::GStringValue),
		directives = directives,
		extensions = extensions
	)


	override fun argumentDefinition(name: String) =
		argumentDefinitions.firstOrNull { it.name == name }


	override fun equalsNode(other: GNode, includingOrigin: Boolean) =
		this === other || (
			other is GFieldDefinition &&
				argumentDefinitions.equalsNode(other.argumentDefinitions, includingOrigin = includingOrigin) &&
				descriptionNode.equalsNode(other.descriptionNode, includingOrigin = includingOrigin) &&
				directives.equalsNode(other.directives, includingOrigin = includingOrigin) &&
				nameNode.equalsNode(other.nameNode, includingOrigin = includingOrigin) &&
				type.equalsNode(other.type, includingOrigin = includingOrigin) &&
				(!includingOrigin || origin == other.origin)
			)


	companion object
}


class GFieldSelection(
	name: GName,
	val selectionSet: GSelectionSet? = null,
	override val arguments: List<GArgument> = emptyList(),
	alias: GName? = null,
	directives: List<GDirective> = emptyList(),
	origin: GDocumentPosition? = null,
	extensions: (GNodeExtensionSet.Builder<GFieldDefinition>.() -> Unit)? = null
) :
	GSelection(
		directives = directives,
		extensions = extensions.build(),
		origin = origin
	),
	GNode.WithArguments {

	val alias get() = aliasNode?.value
	val aliasNode = alias
	val name get() = nameNode.value
	val nameNode = name


	constructor(
		name: String,
		selectionSet: GSelectionSet? = null,
		arguments: List<GArgument> = emptyList(),
		alias: String? = null,
		directives: List<GDirective> = emptyList(),
		extensions: (GNodeExtensionSet.Builder<GFieldDefinition>.() -> Unit)? = null
	) : this(
		name = GName(name),
		selectionSet = selectionSet,
		arguments = arguments,
		alias = alias?.let { GName(alias) },
		directives = directives,
		extensions = extensions.build()
	)


	override fun equalsNode(other: GNode, includingOrigin: Boolean) =
		this === other || (
			other is GFieldSelection &&
				aliasNode.equalsNode(other.aliasNode, includingOrigin = includingOrigin) &&
				arguments.equalsNode(other.arguments, includingOrigin = includingOrigin) &&
				directives.equalsNode(other.directives, includingOrigin = includingOrigin) &&
				nameNode.equalsNode(other.nameNode, includingOrigin = includingOrigin) &&
				selectionSet.equalsNode(other.selectionSet, includingOrigin = includingOrigin) &&
				(!includingOrigin || origin == other.origin)
			)


	companion object
}


// https://graphql.github.io/graphql-spec/draft/#sec-Float.Input-Coercion
object GFloatType : GScalarType(name = "Float")


class GFloatValue(
	val value: Double,
	origin: GDocumentPosition? = null,
	extensions: (GNodeExtensionSet.Builder<GFieldDefinition>.() -> Unit)? = null
) : GValue(
	extensions = extensions.build(),
	origin = origin
) {

	override val kind get() = Kind.FLOAT


	override fun equals(other: Any?) =
		this === other || (other is GFloatValue && value == other.value)


	override fun equalsNode(other: GNode, includingOrigin: Boolean) =
		this === other || (
			other is GFloatValue &&
				value == other.value &&
				(!includingOrigin || origin == other.origin)
			)


	override fun hashCode() =
		value.hashCode()


	override fun unwrap() =
		value


	companion object
}


class GFragmentDefinition(
	name: GName,
	val typeCondition: GNamedTypeRef,
	val selectionSet: GSelectionSet,
	override val variableDefinitions: List<GVariableDefinition> = emptyList(),
	override val directives: List<GDirective> = emptyList(),
	origin: GDocumentPosition? = null,
	extensions: (GNodeExtensionSet.Builder<GFieldDefinition>.() -> Unit)? = null
) :
	GExecutableDefinition(
		extensions = extensions.build(),
		origin = origin
	),
	GNode.WithDirectives,
	GNode.WithName,
	GNode.WithVariableDefinitions {

	override val nameNode = name


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


	override fun equalsNode(other: GNode, includingOrigin: Boolean) =
		this === other || (
			other is GFragmentDefinition &&
				directives.equalsNode(other.directives, includingOrigin = includingOrigin) &&
				nameNode.equalsNode(other.nameNode, includingOrigin = includingOrigin) &&
				selectionSet.equalsNode(other.selectionSet, includingOrigin = includingOrigin) &&
				typeCondition.equalsNode(other.typeCondition, includingOrigin = includingOrigin) &&
				variableDefinitions.equalsNode(other.variableDefinitions, includingOrigin = includingOrigin) &&
				(!includingOrigin || origin == other.origin)
			)


	companion object
}


class GFragmentSelection(
	name: GName,
	directives: List<GDirective> = emptyList(),
	origin: GDocumentPosition? = null,
	extensions: (GNodeExtensionSet.Builder<GFieldDefinition>.() -> Unit)? = null
) :
	GSelection(
		directives = directives,
		extensions = extensions.build(),
		origin = origin
	) {

	val name get() = nameNode.value
	val nameNode = name


	constructor(
		name: String,
		directives: List<GDirective> = emptyList(),
		extensions: (GNodeExtensionSet.Builder<GFieldDefinition>.() -> Unit)? = null
	) : this(
		name = GName(name),
		directives = directives,
		extensions = extensions.build()
	)


	override fun equalsNode(other: GNode, includingOrigin: Boolean) =
		this === other || (
			other is GFragmentSelection &&
				directives.equalsNode(other.directives, includingOrigin = includingOrigin) &&
				nameNode.equalsNode(other.nameNode, includingOrigin = includingOrigin) &&
				(!includingOrigin || origin == other.origin)
			)


	companion object
}


// https://graphql.github.io/graphql-spec/draft/#sec-ID.Input-Coercion
object GIdType : GScalarType(name = "ID")


class GInlineFragmentSelection(
	val selectionSet: GSelectionSet,
	val typeCondition: GNamedTypeRef?,
	directives: List<GDirective> = emptyList(),
	origin: GDocumentPosition? = null,
	extensions: (GNodeExtensionSet.Builder<GFieldDefinition>.() -> Unit)? = null
) : GSelection(
	directives = directives,
	extensions = extensions.build(),
	origin = origin
) {

	override fun equalsNode(other: GNode, includingOrigin: Boolean) =
		this === other || (
			other is GInlineFragmentSelection &&
				directives.equalsNode(other.directives, includingOrigin = includingOrigin) &&
				selectionSet.equalsNode(other.selectionSet, includingOrigin = includingOrigin) &&
				typeCondition.equalsNode(other.typeCondition, includingOrigin = includingOrigin) &&
				(!includingOrigin || origin == other.origin)
			)


	companion object
}


class GInputObjectArgumentDefinition(
	name: GName,
	type: GTypeRef,
	defaultValue: GValue? = null,
	description: GStringValue? = null,
	directives: List<GDirective> = emptyList(),
	origin: GDocumentPosition? = null,
	extensions: (GNodeExtensionSet.Builder<GFieldDefinition>.() -> Unit)? = null
) : GArgumentDefinition(
	name = name,
	type = type,
	defaultValue = defaultValue,
	description = description,
	directives = directives,
	origin = origin,
	extensions = extensions.build()
) {

	constructor(
		name: String,
		type: GTypeRef,
		defaultValue: GValue? = null,
		description: String? = null,
		directives: List<GDirective> = emptyList(),
		extensions: (GNodeExtensionSet.Builder<GFieldDefinition>.() -> Unit)? = null
	) : this(
		name = GName(name),
		type = type,
		defaultValue = defaultValue,
		description = description?.let(::GStringValue),
		directives = directives,
		extensions = extensions
	)

	companion object
}


// https://graphql.github.io/graphql-spec/June2018/#sec-Input-Objects
// https://graphql.github.io/graphql-spec/June2018/#sec-Input-Object
class GInputObjectType(
	name: GName,
	override val argumentDefinitions: List<GInputObjectArgumentDefinition>,
	description: GStringValue? = null,
	directives: List<GDirective> = emptyList(),
	origin: GDocumentPosition? = null,
	extensions: (GNodeExtensionSet.Builder<GFieldDefinition>.() -> Unit)? = null
) :
	GCompositeType(
		description = description,
		directives = directives,
		extensions = extensions.build(),
		kind = Kind.INPUT_OBJECT,
		name = name,
		origin = origin
	),
	GNode.WithArgumentDefinitions {

	constructor(
		name: String,
		argumentDefinitions: List<GInputObjectArgumentDefinition>,
		description: String? = null,
		directives: List<GDirective> = emptyList(),
		extensions: (GNodeExtensionSet.Builder<GFieldDefinition>.() -> Unit)? = null
	) : this(
		name = GName(name),
		argumentDefinitions = argumentDefinitions,
		description = description?.let(::GStringValue),
		directives = directives,
		extensions = extensions
	)


	override fun argumentDefinition(name: String) =
		argumentDefinitions.firstOrNull { it.name == name }


	override fun equalsNode(other: GNode, includingOrigin: Boolean) =
		this === other || (
			other is GInputObjectType &&
				argumentDefinitions.equalsNode(other.argumentDefinitions, includingOrigin = includingOrigin) &&
				descriptionNode.equalsNode(other.descriptionNode, includingOrigin = includingOrigin) &&
				directives.equalsNode(other.directives, includingOrigin = includingOrigin) &&
				nameNode.equalsNode(other.nameNode, includingOrigin = includingOrigin) &&
				(!includingOrigin || origin == other.origin)
			)


	override fun isSupertypeOf(other: GType) =
		other === this ||
			(other is GNonNullType && other.nullableType === this)


	companion object
}


class GInputObjectTypeExtension(
	name: GName,
	override val argumentDefinitions: List<GInputObjectArgumentDefinition> = emptyList(),
	directives: List<GDirective> = emptyList(),
	origin: GDocumentPosition? = null,
	extensions: (GNodeExtensionSet.Builder<GFieldDefinition>.() -> Unit)? = null
) :
	GTypeExtension(
		directives = directives,
		extensions = extensions.build(),
		name = name,
		origin = origin
	),
	GNode.WithArgumentDefinitions {

	constructor(
		name: String,
		argumentDefinitions: List<GInputObjectArgumentDefinition> = emptyList(),
		directives: List<GDirective> = emptyList(),
		extensions: (GNodeExtensionSet.Builder<GFieldDefinition>.() -> Unit)? = null
	) : this(
		name = GName(name),
		argumentDefinitions = argumentDefinitions,
		directives = directives,
		extensions = extensions
	)


	override fun equalsNode(other: GNode, includingOrigin: Boolean) =
		this === other || (
			other is GInputObjectTypeExtension &&
				argumentDefinitions.equalsNode(other.argumentDefinitions, includingOrigin = includingOrigin) &&
				directives.equalsNode(other.directives, includingOrigin = includingOrigin) &&
				nameNode.equalsNode(other.nameNode, includingOrigin = includingOrigin) &&
				(!includingOrigin || origin == other.origin)
			)


	companion object
}


// https://graphql.github.io/graphql-spec/draft/#sec-Int.Input-Coercion
object GIntType : GScalarType(name = "Int")


class GIntValue(
	val value: Int,
	origin: GDocumentPosition? = null,
	extensions: (GNodeExtensionSet.Builder<GFieldDefinition>.() -> Unit)? = null
) : GValue(
	extensions = extensions.build(),
	origin = origin
) {

	override val kind get() = Kind.INT


	override fun equals(other: Any?) =
		this === other || (other is GIntValue && value == other.value)


	override fun equalsNode(other: GNode, includingOrigin: Boolean) =
		this === other || (
			other is GIntValue &&
				value == other.value &&
				(!includingOrigin || origin == other.origin)
			)


	override fun hashCode() =
		value.hashCode()


	override fun unwrap() =
		value


	companion object
}


// https://graphql.github.io/graphql-spec/June2018/#sec-Interfaces
// https://graphql.github.io/graphql-spec/June2018/#sec-Interface
class GInterfaceType(
	name: GName,
	override val fieldDefinitions: List<GFieldDefinition>,
	override val interfaces: List<GNamedTypeRef> = emptyList(),
	description: GStringValue? = null,
	directives: List<GDirective> = emptyList(),
	origin: GDocumentPosition? = null,
	extensions: (GNodeExtensionSet.Builder<GFieldDefinition>.() -> Unit)? = null
) :
	GAbstractType(
		description = description,
		directives = directives,
		extensions = extensions.build(),
		kind = Kind.INTERFACE,
		name = name,
		origin = origin
	),
	GNode.WithFieldDefinitions,
	GNode.WithInterfaces {

	constructor(
		name: String,
		fields: List<GFieldDefinition>,
		interfaces: List<GNamedTypeRef> = emptyList(),
		description: String? = null,
		directives: List<GDirective> = emptyList(),
		extensions: (GNodeExtensionSet.Builder<GFieldDefinition>.() -> Unit)? = null
	) : this(
		name = GName(name),
		fieldDefinitions = fields,
		interfaces = interfaces,
		description = description?.let(::GStringValue),
		directives = directives,
		extensions = extensions
	)


	override fun equalsNode(other: GNode, includingOrigin: Boolean) =
		this === other || (
			other is GInterfaceType &&
				descriptionNode.equalsNode(other.descriptionNode, includingOrigin = includingOrigin) &&
				directives.equalsNode(other.directives, includingOrigin = includingOrigin) &&
				fieldDefinitions.equalsNode(other.fieldDefinitions, includingOrigin = includingOrigin) &&
				interfaces.equalsNode(other.interfaces, includingOrigin = includingOrigin) &&
				nameNode.equalsNode(other.nameNode, includingOrigin = includingOrigin) &&
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
	override val fieldDefinitions: List<GFieldDefinition> = emptyList(),
	override val interfaces: List<GNamedTypeRef> = emptyList(),
	directives: List<GDirective> = emptyList(),
	origin: GDocumentPosition? = null,
	extensions: (GNodeExtensionSet.Builder<GFieldDefinition>.() -> Unit)? = null
) :
	GTypeExtension(
		directives = directives,
		extensions = extensions.build(),
		name = name,
		origin = origin
	),
	GNode.WithFieldDefinitions,
	GNode.WithInterfaces {


	constructor(
		name: String,
		fields: List<GFieldDefinition> = emptyList(),
		interfaces: List<GNamedTypeRef> = emptyList(),
		directives: List<GDirective> = emptyList(),
		extensions: (GNodeExtensionSet.Builder<GFieldDefinition>.() -> Unit)? = null
	) : this(
		name = GName(name),
		fieldDefinitions = fields,
		interfaces = interfaces,
		directives = directives,
		extensions = extensions
	)


	override fun equalsNode(other: GNode, includingOrigin: Boolean) =
		this === other || (
			other is GInterfaceTypeExtension &&
				directives.equalsNode(other.directives, includingOrigin = includingOrigin) &&
				fieldDefinitions.equalsNode(other.fieldDefinitions, includingOrigin = includingOrigin) &&
				interfaces.equalsNode(other.interfaces, includingOrigin = includingOrigin) &&
				nameNode.equalsNode(other.nameNode, includingOrigin = includingOrigin) &&
				(!includingOrigin || origin == other.origin)
			)


	companion object
}


sealed class GLeafType(
	name: GName,
	description: GStringValue? = null,
	directives: List<GDirective> = emptyList(),
	extensions: GNodeExtensionSet,
	kind: Kind,
	origin: GDocumentPosition?
) : GNamedType(
	name = name,
	description = description,
	directives = directives,
	extensions = extensions,
	kind = kind,
	origin = origin
) {

	companion object
}


// https://graphql.github.io/graphql-spec/June2018/#sec-Type-System.List
// https://graphql.github.io/graphql-spec/June2018/#sec-Type-Kinds.List
class GListType(
	elementType: GType,
	extensions: (GNodeExtensionSet.Builder<GFieldDefinition>.() -> Unit)? = null
) : GWrappingType(
	extensions = extensions.build(),
	kind = Kind.LIST,
	wrappedType = elementType
) {

	val elementType get() = wrappedType

	override val name get() = "[${elementType.name}]"


	override fun equalsNode(other: GNode, includingOrigin: Boolean) =
		this === other || (
			other is GListType &&
				elementType.equalsNode(other.elementType, includingOrigin = includingOrigin)
			)


	override fun isSupertypeOf(other: GType): Boolean =
		other === this ||
			(other is GListType && elementType.isSupertypeOf(other.elementType)) ||
			(other is GNonNullType && isSupertypeOf(other.nullableType))


	override fun toRef() =
		GListTypeRef(elementType.toRef())


	companion object
}


class GListTypeRef(
	val elementType: GTypeRef,
	origin: GDocumentPosition? = null,
	extensions: (GNodeExtensionSet.Builder<GFieldDefinition>.() -> Unit)? = null
) : GTypeRef(
	extensions = extensions.build(),
	origin = origin
) {

	override val underlyingName get() = elementType.underlyingName


	override fun equalsNode(other: GNode, includingOrigin: Boolean) =
		this === other || (
			other is GListTypeRef &&
				elementType.equalsNode(other.elementType, includingOrigin = includingOrigin) &&
				(!includingOrigin || origin == other.origin)
			)


	companion object
}


fun GListTypeRef(
	name: String,
	extensions: (GNodeExtensionSet.Builder<GFieldDefinition>.() -> Unit)? = null
): GListTypeRef {
	@Suppress("NAME_SHADOWING")
	val extensions = extensions.build()

	return GListTypeRef(GNamedTypeRef(name, extensions = extensions), extensions = extensions)
}


class GListValue(
	val elements: List<GValue>,
	origin: GDocumentPosition? = null,
	extensions: (GNodeExtensionSet.Builder<GFieldDefinition>.() -> Unit)? = null
) : GValue(
	extensions = extensions.build(),
	origin = origin
) {

	override val kind get() = Kind.LIST


	override fun equals(other: Any?) =
		this === other || (other is GListValue && elements == other.elements)


	override fun equalsNode(other: GNode, includingOrigin: Boolean) =
		this === other || (
			other is GListValue &&
				elements.equalsNode(other.elements, includingOrigin = includingOrigin) &&
				(!includingOrigin || origin == other.origin)
			)


	override fun hashCode() =
		elements.hashCode()


	override fun unwrap() =
		elements.map { it.unwrap() }


	companion object
}


class GName(
	val value: String,
	origin: GDocumentPosition? = null,
	extensions: (GNodeExtensionSet.Builder<GFieldDefinition>.() -> Unit)? = null
) : GNode(
	extensions = extensions.build(),
	origin = origin
) {

	override fun equals(other: Any?) =
		this === other || (other is GName && value == other.value)


	override fun equalsNode(other: GNode, includingOrigin: Boolean) =
		this === other || (
			other is GName &&
				value == other.value &&
				(!includingOrigin || origin == other.origin)
			)


	override fun hashCode() =
		value.hashCode()


	companion object
}


sealed class GNamedType(
	description: GStringValue?,
	override val directives: List<GDirective>,
	extensions: GNodeExtensionSet,
	kind: Kind,
	name: GName,
	origin: GDocumentPosition?
) :
	GType(
		extensions = extensions,
		kind = kind,
		origin = origin
	),
	GNode.WithDirectives,
	GNode.WithName,
	GNode.WithOptionalDescription {

	final override val descriptionNode = description
	final override val nameNode = name
	final override val underlyingNamedType get() = this


	override fun toRef() =
		GTypeRef(name)


	companion object
}


class GNamedTypeRef(
	name: GName,
	origin: GDocumentPosition? = null,
	extensions: (GNodeExtensionSet.Builder<GFieldDefinition>.() -> Unit)? = null
) : GTypeRef(
	extensions = extensions.build(),
	origin = origin
) {

	val name get() = nameNode.value
	val nameNode = name

	override val underlyingName get() = name


	constructor(
		name: String,
		extensions: (GNodeExtensionSet.Builder<GFieldDefinition>.() -> Unit)? = null
	) : this(
		name = GName(name),
		extensions = extensions
	)


	override fun equalsNode(other: GNode, includingOrigin: Boolean) =
		this === other || (
			other is GNamedTypeRef &&
				nameNode.equalsNode(other.nameNode, includingOrigin = includingOrigin) &&
				(!includingOrigin || origin == other.origin)
			)


	companion object
}


// https://graphql.github.io/graphql-spec/June2018/#sec-Type-System.Non-Null
// https://graphql.github.io/graphql-spec/June2018/#sec-Type-Kinds.Non-Null
class GNonNullType(
	nullableType: GType,
	extensions: (GNodeExtensionSet.Builder<GFieldDefinition>.() -> Unit)? = null
) : GWrappingType(
	extensions = extensions.build(),
	kind = Kind.NON_NULL,
	wrappedType = nullableType
) {

	override val name get() = "${nullableType.name}!"
	override val nonNullable get() = this
	override val nullableType get() = wrappedType


	override fun equalsNode(other: GNode, includingOrigin: Boolean) =
		this === other || (
			other is GNonNullType &&
				nullableType.equalsNode(other.nullableType, includingOrigin = includingOrigin) &&
				(!includingOrigin || origin == other.origin)
			)


	override fun isSupertypeOf(other: GType) =
		other === this ||
			(other is GNonNullType && nullableType.isSupertypeOf(other.nullableType))


	override fun toRef() =
		GNonNullTypeRef(nullableType.toRef())


	companion object
}


class GNonNullTypeRef(
	override val nullableRef: GTypeRef,
	origin: GDocumentPosition? = null,
	extensions: (GNodeExtensionSet.Builder<GFieldDefinition>.() -> Unit)? = null
) : GTypeRef(
	extensions = extensions.build(),
	origin = origin
) {

	init {
		require(nullableRef !is GNonNullTypeRef) { "Cannot create non-null type ref to non-null type: $nullableRef" }
	}


	override val nonNullableRef get() = this
	override val underlyingName get() = nullableRef.underlyingName


	override fun equalsNode(other: GNode, includingOrigin: Boolean) =
		this === other || (
			other is GNonNullTypeRef &&
				nullableRef.equalsNode(other.nullableRef, includingOrigin = includingOrigin) &&
				(!includingOrigin || origin == other.origin)
			)


	companion object
}


fun GNonNullTypeRef(
	name: String,
	extensions: (GNodeExtensionSet.Builder<GFieldDefinition>.() -> Unit)? = null
): GNonNullTypeRef {
	@Suppress("NAME_SHADOWING")
	val extensions = extensions.build()

	return GNonNullTypeRef(GNamedTypeRef(name, extensions = extensions), extensions = extensions)
}


class GNullValue(
	origin: GDocumentPosition? = null,
	extensions: (GNodeExtensionSet.Builder<GFieldDefinition>.() -> Unit)? = null
) : GValue(
	extensions = extensions.build(),
	origin = origin
) {

	override val kind get() = Kind.NULL


	override fun equals(other: Any?) =
		this === other || other is GNullValue


	override fun equalsNode(other: GNode, includingOrigin: Boolean) =
		this === other || (
			other is GNullValue &&
				(!includingOrigin || origin == other.origin)
			)


	override fun hashCode() =
		0


	override fun unwrap(): Nothing? =
		null


	companion object {

		val withoutOrigin = GNullValue()
	}
}


// https://graphql.github.io/graphql-spec/June2018/#sec-Objects
// https://graphql.github.io/graphql-spec/June2018/#sec-Object
class GObjectType(
	name: GName,
	override val fieldDefinitions: List<GFieldDefinition>,
	override val interfaces: List<GNamedTypeRef> = emptyList(),
	description: GStringValue? = null,
	directives: List<GDirective> = emptyList(),
	origin: GDocumentPosition? = null,
	extensions: (GNodeExtensionSet.Builder<GFieldDefinition>.() -> Unit)? = null
) :
	GCompositeType(
		description = description,
		directives = directives,
		extensions = extensions.build(),
		kind = Kind.OBJECT,
		name = name,
		origin = origin
	),
	GNode.WithFieldDefinitions,
	GNode.WithInterfaces {

	constructor(
		name: String,
		fields: List<GFieldDefinition>,
		interfaces: List<GNamedTypeRef> = emptyList(),
		description: String? = null,
		directives: List<GDirective> = emptyList(),
		extensions: (GNodeExtensionSet.Builder<GFieldDefinition>.() -> Unit)? = null
	) : this(
		name = GName(name),
		fieldDefinitions = fields,
		interfaces = interfaces,
		description = description?.let(::GStringValue),
		directives = directives,
		extensions = extensions
	)


	override fun equalsNode(other: GNode, includingOrigin: Boolean) =
		this === other || (
			other is GObjectType &&
				descriptionNode.equalsNode(other.descriptionNode, includingOrigin = includingOrigin) &&
				directives.equalsNode(other.directives, includingOrigin = includingOrigin) &&
				fieldDefinitions.equalsNode(other.fieldDefinitions, includingOrigin = includingOrigin) &&
				interfaces.equalsNode(other.interfaces, includingOrigin = includingOrigin) &&
				nameNode.equalsNode(other.nameNode, includingOrigin = includingOrigin) &&
				(!includingOrigin || origin == other.origin)
			)


	override fun isSupertypeOf(other: GType): Boolean =
		other === this ||
			(other is GNonNullType && isSupertypeOf(other.nullableType))


	companion object
}


class GObjectTypeExtension(
	name: GName,
	override val fieldDefinitions: List<GFieldDefinition> = emptyList(),
	override val interfaces: List<GNamedTypeRef> = emptyList(),
	directives: List<GDirective> = emptyList(),
	origin: GDocumentPosition? = null,
	extensions: (GNodeExtensionSet.Builder<GFieldDefinition>.() -> Unit)? = null
) :
	GTypeExtension(
		directives = directives,
		extensions = extensions.build(),
		name = name,
		origin = origin
	),
	GNode.WithFieldDefinitions,
	GNode.WithInterfaces {

	constructor(
		name: String,
		fields: List<GFieldDefinition> = emptyList(),
		interfaces: List<GNamedTypeRef> = emptyList(),
		directives: List<GDirective> = emptyList(),
		extensions: (GNodeExtensionSet.Builder<GFieldDefinition>.() -> Unit)? = null
	) : this(
		name = GName(name),
		fieldDefinitions = fields,
		interfaces = interfaces,
		directives = directives,
		extensions = extensions
	)


	override fun equalsNode(other: GNode, includingOrigin: Boolean) =
		this === other || (
			other is GObjectTypeExtension &&
				directives.equalsNode(other.directives, includingOrigin = includingOrigin) &&
				fieldDefinitions.equalsNode(other.fieldDefinitions, includingOrigin = includingOrigin) &&
				interfaces.equalsNode(other.interfaces, includingOrigin = includingOrigin) &&
				nameNode.equalsNode(other.nameNode, includingOrigin = includingOrigin) &&
				(!includingOrigin || origin == other.origin)
			)


	companion object
}


class GObjectValue(
	override val arguments: List<GArgument>,
	origin: GDocumentPosition? = null,
	extensions: (GNodeExtensionSet.Builder<GFieldDefinition>.() -> Unit)? = null
) :
	GValue(
		extensions = extensions.build(),
		origin = origin
	),
	GNode.WithArguments {

	override val kind: Kind get() = Kind.OBJECT


	override fun equals(other: Any?): Boolean =
		this === other || (other is GObjectValue && arguments.equalsNode(other.arguments, includingOrigin = false))


	override fun equalsNode(other: GNode, includingOrigin: Boolean): Boolean =
		this === other || (
			other is GObjectValue &&
				arguments.equalsNode(other.arguments, includingOrigin = includingOrigin) &&
				(!includingOrigin || origin == other.origin)
			)


	override fun hashCode(): Int =
		arguments.hashCode()


	override fun unwrap(): Map<String, Any?> =
		arguments.associate { it.name to it.value.unwrap() }


	companion object
}


class GOperationDefinition(
	val type: GOperationType,
	name: GName? = null,
	val selectionSet: GSelectionSet,
	override val variableDefinitions: List<GVariableDefinition> = emptyList(),
	override val directives: List<GDirective> = emptyList(),
	origin: GDocumentPosition? = null,
	extensions: (GNodeExtensionSet.Builder<GFieldDefinition>.() -> Unit)? = null
) :
	GExecutableDefinition(
		extensions = extensions.build(),
		origin = origin
	),
	GNode.WithDirectives,
	GNode.WithOptionalName,
	GNode.WithVariableDefinitions {

	override val nameNode = name


	constructor(
		type: GOperationType,
		name: String? = null,
		selectionSet: GSelectionSet,
		variableDefinitions: List<GVariableDefinition> = emptyList(),
		directives: List<GDirective> = emptyList(),
		extensions: (GNodeExtensionSet.Builder<GFieldDefinition>.() -> Unit)? = null
	) : this(
		type = type,
		name = name?.let { GName(it) },
		selectionSet = selectionSet,
		variableDefinitions = variableDefinitions,
		directives = directives,
		extensions = extensions
	)


	override fun equalsNode(other: GNode, includingOrigin: Boolean) =
		this === other || (
			other is GOperationDefinition &&
				directives.equalsNode(other.directives, includingOrigin = includingOrigin) &&
				nameNode.equalsNode(other.nameNode, includingOrigin = includingOrigin) &&
				selectionSet.equalsNode(other.selectionSet, includingOrigin = includingOrigin) &&
				type == other.type &&
				variableDefinitions.equalsNode(other.variableDefinitions, includingOrigin = includingOrigin) &&
				(!includingOrigin || origin == other.origin)
			)


	companion object
}


class GOperationTypeDefinition(
	val operationType: GOperationType,
	val type: GNamedTypeRef,
	origin: GDocumentPosition? = null,
	extensions: (GNodeExtensionSet.Builder<GFieldDefinition>.() -> Unit)? = null
) : GNode(
	extensions = extensions.build(),
	origin = origin
) {

	override fun equalsNode(other: GNode, includingOrigin: Boolean) =
		this === other || (
			other is GOperationTypeDefinition &&
				operationType == other.operationType &&
				type.equalsNode(other.type, includingOrigin = includingOrigin) &&
				(!includingOrigin || origin == other.origin)
			)


	companion object
}


// https://graphql.github.io/graphql-spec/June2018/#sec-Scalars
// https://graphql.github.io/graphql-spec/June2018/#sec-Scalar
sealed class GScalarType(
	name: GName,
	description: GStringValue? = null,
	directives: List<GDirective> = emptyList(),
	origin: GDocumentPosition? = null,
	extensions: (GNodeExtensionSet.Builder<GFieldDefinition>.() -> Unit)? = null
) : GLeafType(
	description = description,
	directives = directives,
	extensions = extensions.build(),
	kind = Kind.SCALAR,
	name = name,
	origin = origin
) {

	constructor(
		name: String,
		description: String? = null,
		directives: List<GDirective> = emptyList(),
		extensions: (GNodeExtensionSet.Builder<GFieldDefinition>.() -> Unit)? = null
	) : this(
		name = GName(name),
		description = description?.let(::GStringValue),
		directives = directives,
		extensions = extensions
	)


	final override fun equalsNode(other: GNode, includingOrigin: Boolean) =
		this === other || (
			other is GScalarType &&
				descriptionNode.equalsNode(other.descriptionNode, includingOrigin = includingOrigin) &&
				directives.equalsNode(other.directives, includingOrigin = includingOrigin) &&
				nameNode.equalsNode(other.nameNode, includingOrigin = includingOrigin) &&
				(!includingOrigin || origin == other.origin)
			)


	final override fun isSupertypeOf(other: GType): Boolean =
		this == other ||
			(other is GNonNullType && isSupertypeOf(other.nullableType))


	companion object
}


class GScalarTypeExtension(
	name: GName,
	directives: List<GDirective> = emptyList(),
	origin: GDocumentPosition? = null,
	extensions: (GNodeExtensionSet.Builder<GFieldDefinition>.() -> Unit)? = null
) : GTypeExtension(
	directives = directives,
	extensions = extensions.build(),
	name = name,
	origin = origin
) {

	constructor(
		name: String,
		directives: List<GDirective> = emptyList(),
		extensions: (GNodeExtensionSet.Builder<GFieldDefinition>.() -> Unit)? = null
	) : this(
		name = GName(name),
		directives = directives,
		extensions = extensions
	)


	override fun equalsNode(other: GNode, includingOrigin: Boolean) =
		this === other || (
			other is GScalarTypeExtension &&
				directives.equalsNode(other.directives, includingOrigin = includingOrigin) &&
				nameNode.equalsNode(other.nameNode, includingOrigin = includingOrigin) &&
				(!includingOrigin || origin == other.origin)
			)


	companion object
}


class GSchemaDefinition(
	override val operationTypeDefinitions: List<GOperationTypeDefinition>,
	override val directives: List<GDirective> = emptyList(),
	origin: GDocumentPosition? = null,
	extensions: (GNodeExtensionSet.Builder<GFieldDefinition>.() -> Unit)? = null
) :
	GTypeSystemDefinition(
		extensions = extensions.build(),
		origin = origin
	),
	GNode.WithDirectives,
	GNode.WithOperationTypeDefinitions {

	override fun equalsNode(other: GNode, includingOrigin: Boolean) =
		this === other || (
			other is GSchemaDefinition &&
				directives.equalsNode(other.directives, includingOrigin = includingOrigin) &&
				operationTypeDefinitions.equalsNode(other.operationTypeDefinitions, includingOrigin = includingOrigin) &&
				(!includingOrigin || origin == other.origin)
			)


	companion object
}


class GSchemaExtension(
	override val operationTypeDefinitions: List<GOperationTypeDefinition> = emptyList(),
	override val directives: List<GDirective> = emptyList(),
	origin: GDocumentPosition? = null,
	extensions: (GNodeExtensionSet.Builder<GFieldDefinition>.() -> Unit)? = null
) :
	GTypeSystemExtension(
		extensions = extensions.build(),
		origin = origin
	),
	GNode.WithDirectives,
	GNode.WithOperationTypeDefinitions {

	override fun equalsNode(other: GNode, includingOrigin: Boolean) =
		this === other || (
			other is GSchemaExtension &&
				directives.equalsNode(other.directives, includingOrigin = includingOrigin) &&
				operationTypeDefinitions.equalsNode(other.operationTypeDefinitions, includingOrigin = includingOrigin) &&
				(!includingOrigin || origin == other.origin)
			)


	companion object
}


sealed class GSelection(
	override val directives: List<GDirective>,
	extensions: GNodeExtensionSet,
	origin: GDocumentPosition?
) :
	GNode(
		extensions = extensions.build(),
		origin = origin
	),
	GNode.WithDirectives {

	companion object
}


class GSelectionSet(
	val selections: List<GSelection>,
	origin: GDocumentPosition? = null,
	extensions: (GNodeExtensionSet.Builder<GFieldDefinition>.() -> Unit)? = null
) : GNode(
	extensions = extensions.build(),
	origin = origin
) {

	override fun equalsNode(other: GNode, includingOrigin: Boolean) =
		this === other || (
			other is GSelectionSet &&
				selections.equalsNode(other.selections, includingOrigin = includingOrigin) &&
				(!includingOrigin || origin == other.origin)
			)


	companion object
}


// https://graphql.github.io/graphql-spec/draft/#sec-String.Input-Coercion
object GStringType : GScalarType(name = "String")


class GStringValue(
	val value: String,
	val isBlock: Boolean = false,
	origin: GDocumentPosition? = null,
	extensions: (GNodeExtensionSet.Builder<GFieldDefinition>.() -> Unit)? = null
) : GValue(
	extensions = extensions.build(),
	origin = origin
) {

	constructor(value: String) : // needed so that ::GStringValue can be applied on Strings
		this(value = value, isBlock = false)


	override val kind get() = Kind.STRING


	override fun equals(other: Any?) =
		this === other || (other is GStringValue && value == other.value)


	override fun equalsNode(other: GNode, includingOrigin: Boolean) =
		this === other || (
			other is GStringValue &&
				value == other.value &&
				isBlock == other.isBlock &&
				(!includingOrigin || origin == other.origin)
			)


	override fun hashCode() =
		value.hashCode()


	override fun unwrap() =
		value


	companion object
}


// https://graphql.github.io/graphql-spec/June2018/#sec-Wrapping-Types
// https://graphql.github.io/graphql-spec/June2018/#sec-Types
// https://graphql.github.io/graphql-spec/June2018/#sec-The-__Type-Type
sealed class GType(
	extensions: GNodeExtensionSet,
	val kind: Kind,
	origin: GDocumentPosition?
) : GTypeSystemDefinition(
	extensions = extensions,
	origin = origin
) {

	abstract val name: String
	abstract val underlyingNamedType: GNamedType

	open val nonNullable get() = GNonNullType(this)
	open val nullableType get() = this


	abstract fun toRef(): GTypeRef


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
			GIdType,
			GIntType,
			GStringType
		)
	}


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
				ENUM -> "enum"
				INPUT_OBJECT -> "input object"
				INTERFACE -> "interface"
				LIST -> "list"
				NON_NULL -> "non-null"
				OBJECT -> "object"
				SCALAR -> "scalar"
				UNION -> "union"
			}


		companion object
	}
}


sealed class GTypeExtension(
	override val directives: List<GDirective>,
	extensions: GNodeExtensionSet,
	name: GName,
	origin: GDocumentPosition?
) :
	GTypeSystemExtension(
		extensions = extensions,
		origin = origin
	),
	GNode.WithDirectives,
	GNode.WithName {

	override val nameNode = name


	companion object
}


sealed class GTypeRef(
	extensions: GNodeExtensionSet,
	origin: GDocumentPosition?
) : GNode(
	extensions = extensions.build(),
	origin = origin
) {

	abstract val underlyingName: String

	open val nonNullableRef get() = GNonNullTypeRef(this)
	open val nullableRef get() = this


	override fun equals(other: Any?) =
		this === other || (other is GTypeRef && equalsNode(other, includingOrigin = false))


	companion object {

		fun parse(source: GDocumentSource.Parsable) =
			Parser.parseTypeReference(source)


		fun parse(content: String, name: String = "<type reference>") =
			parse(GDocumentSource.of(content = content, name = name))
	}
}


fun GTypeRef(
	name: String,
	extensions: (GNodeExtensionSet.Builder<GFieldDefinition>.() -> Unit)? = null
): GNamedTypeRef =
	GNamedTypeRef(name, extensions = extensions)


val GBooleanTypeRef: GNamedTypeRef = GTypeRef("Boolean")
val GFloatTypeRef: GNamedTypeRef = GTypeRef("Float")
val GIdTypeRef: GNamedTypeRef = GTypeRef("ID")
val GIntTypeRef: GNamedTypeRef = GTypeRef("Int")
val GStringTypeRef: GNamedTypeRef = GTypeRef("String")


sealed class GTypeSystemDefinition(
	extensions: GNodeExtensionSet,
	origin: GDocumentPosition?
) : GDefinition(
	extensions = extensions,
	origin = origin
) {

	companion object
}


sealed class GTypeSystemExtension(
	extensions: GNodeExtensionSet,
	origin: GDocumentPosition?
) : GDefinition(
	extensions = extensions,
	origin = origin
) {

	companion object
}


// https://graphql.github.io/graphql-spec/June2018/#sec-Unions
// https://graphql.github.io/graphql-spec/June2018/#sec-Union
class GUnionType(
	name: GName,
	val possibleTypes: List<GNamedTypeRef>,
	description: GStringValue? = null,
	directives: List<GDirective> = emptyList(),
	origin: GDocumentPosition? = null,
	extensions: (GNodeExtensionSet.Builder<GFieldDefinition>.() -> Unit)? = null
) : GAbstractType(
	description = description,
	directives = directives,
	extensions = extensions.build(),
	kind = Kind.UNION,
	name = name,
	origin = origin
) {

	constructor(
		name: String,
		possibleTypes: List<GNamedTypeRef>,
		description: String? = null,
		directives: List<GDirective> = emptyList(),
		extensions: (GNodeExtensionSet.Builder<GFieldDefinition>.() -> Unit)? = null
	) : this(
		name = GName(name),
		possibleTypes = possibleTypes,
		description = description?.let(::GStringValue),
		directives = directives,
		extensions = extensions
	)


	override fun equalsNode(other: GNode, includingOrigin: Boolean) =
		this === other || (
			other is GUnionType &&
				descriptionNode.equalsNode(other.descriptionNode, includingOrigin = includingOrigin) &&
				directives.equalsNode(other.directives, includingOrigin = includingOrigin) &&
				nameNode.equalsNode(other.nameNode, includingOrigin = includingOrigin) &&
				possibleTypes.equalsNode(other.possibleTypes, includingOrigin = includingOrigin) &&
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
	origin: GDocumentPosition? = null,
	extensions: (GNodeExtensionSet.Builder<GFieldDefinition>.() -> Unit)? = null
) : GTypeExtension(
	directives = directives,
	extensions = extensions.build(),
	name = name,
	origin = origin
) {

	constructor(
		name: String,
		possibleTypes: List<GNamedTypeRef> = emptyList(),
		directives: List<GDirective> = emptyList(),
		extensions: (GNodeExtensionSet.Builder<GFieldDefinition>.() -> Unit)? = null
	) : this(
		name = GName(name),
		possibleTypes = possibleTypes,
		directives = directives,
		extensions = extensions
	)


	override fun equalsNode(other: GNode, includingOrigin: Boolean) =
		this === other || (
			other is GUnionTypeExtension &&
				directives.equalsNode(other.directives, includingOrigin = includingOrigin) &&
				nameNode.equalsNode(other.nameNode, includingOrigin = includingOrigin) &&
				possibleTypes.equalsNode(other.possibleTypes, includingOrigin = includingOrigin) &&
				(!includingOrigin || origin == other.origin)
			)


	companion object
}


sealed class GValue(
	extensions: GNodeExtensionSet,
	origin: GDocumentPosition?
) : GNode(
	extensions = extensions,
	origin = origin
) {

	abstract val kind: Kind

	abstract fun unwrap(): Any? // FIXME not language layer


	companion object {

		// FIXME temporary -- improve
		fun of(value: Any?): GValue? =
			when (value) {
				null -> GNullValue.withoutOrigin
				is Boolean -> GBooleanValue(value)
				is Double -> GFloatValue(value)
				is Int -> GIntValue(value)
				is Map<*, *> -> GObjectValue(value.map { (fieldName, fieldValue) ->
					GArgument(
						name = fieldName as? String ?: return null,
						value = of(fieldValue) ?: return null
					)
				})
				is Collection<*> -> GListValue(value.map { of(it) ?: return null })
				is String -> GStringValue(value)
				else -> null
			}


		fun parse(source: GDocumentSource.Parsable) =
			Parser.parseValue(source)


		fun parse(content: String, name: String = "<value>") =
			parse(GDocumentSource.of(content = content, name = name))
	}


	enum class Kind {

		BOOLEAN,
		ENUM,
		FLOAT,
		INT,
		LIST,
		NULL,
		OBJECT,
		STRING,
		VARIABLE;


		override fun toString() = when (this) {
			BOOLEAN -> "boolean"
			ENUM -> "enum"
			FLOAT -> "float"
			INT -> "int"
			LIST -> "list"
			NULL -> "null"
			OBJECT -> "input object"
			STRING -> "string"
			VARIABLE -> "variable"
		}


		companion object
	}
}


class GVariableDefinition(
	name: GName,
	val type: GTypeRef,
	val defaultValue: GValue? = null,
	override val directives: List<GDirective> = emptyList(),
	origin: GDocumentPosition? = null,
	extensions: (GNodeExtensionSet.Builder<GFieldDefinition>.() -> Unit)? = null
) :
	GNode(
		extensions = extensions.build(),
		origin = origin
	),
	GNode.WithDirectives,
	GNode.WithName {

	override val nameNode = name


	constructor(
		name: String,
		type: GTypeRef,
		defaultValue: GValue? = null,
		directives: List<GDirective> = emptyList(),
		extensions: (GNodeExtensionSet.Builder<GFieldDefinition>.() -> Unit)? = null
	) : this(
		name = GName(name),
		type = type,
		defaultValue = defaultValue,
		directives = directives,
		extensions = extensions
	)


	override fun equalsNode(other: GNode, includingOrigin: Boolean) =
		this === other || (
			other is GVariableDefinition &&
				defaultValue.equalsNode(other.defaultValue, includingOrigin = includingOrigin) &&
				directives.equalsNode(other.directives, includingOrigin = includingOrigin) &&
				nameNode.equalsNode(other.nameNode, includingOrigin = includingOrigin) &&
				type.equalsNode(other.type, includingOrigin = includingOrigin) &&
				(!includingOrigin || origin == other.origin)
			)


	companion object
}


class GVariableRef(
	name: GName,
	origin: GDocumentPosition? = null,
	extensions: (GNodeExtensionSet.Builder<GFieldDefinition>.() -> Unit)? = null
) : GValue(
	extensions = extensions.build(),
	origin = origin
) {

	val name get() = nameNode.value
	val nameNode = name

	override val kind get() = Kind.VARIABLE


	constructor(
		name: String,
		extensions: (GNodeExtensionSet.Builder<GFieldDefinition>.() -> Unit)? = null
	) : this(
		name = GName(name),
		extensions = extensions
	)


	override fun equals(other: Any?) =
		this === other || (other is GVariableRef && name == other.name)


	override fun equalsNode(other: GNode, includingOrigin: Boolean) =
		this === other || (
			other is GVariableRef &&
				nameNode.equalsNode(other.nameNode, includingOrigin = includingOrigin) &&
				(!includingOrigin || origin == other.origin)
			)


	override fun hashCode() =
		name.hashCode()


	override fun unwrap() =
		error("Cannot unwrap a GraphQL variable: $name")


	companion object
}


// https://graphql.github.io/graphql-spec/June2018/#sec-Wrapping-Types
// https://graphql.github.io/graphql-spec/June2018/#sec-Types
sealed class GWrappingType(
	kind: Kind,
	val wrappedType: GType,
	extensions: (GNodeExtensionSet.Builder<GFieldDefinition>.() -> Unit)? = null // FIXME all <>
) : GType(
	extensions = extensions.build(),
	kind = kind,
	origin = null
) {

	final override val underlyingNamedType get() = wrappedType.underlyingNamedType


	override fun toString() =
		"${print(wrappedType)} <wrapped as $name>"


	companion object
}


private fun <Node : GNode> ((GNodeExtensionSet.Builder<Node>.() -> Unit)?).build() =
	when (this) {
		null -> GNodeExtensionSet.empty()
		is GNodeExtensionSet -> this
		else -> GNodeExtensionSet.Builder.default<Node>().apply(this).build()
	}
