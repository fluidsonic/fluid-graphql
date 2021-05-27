package io.fluidsonic.graphql


public sealed class GNode(
	public val extensions: GNodeExtensionSet<GNode>,
	public val origin: GDocumentPosition?,
) {

	public fun childAt(index: Int): GNode? {
		var childIndex = 0

		forEachChild { child ->
			if (childIndex == index)
				return child

			childIndex += 1
		}

		return null
	}


	public fun children(): List<GNode> {
		var list: MutableList<GNode>? = null

		forEachChild { child ->
			(list ?: mutableListOf<GNode>().also { list = it })
				.add(child)
		}

		return list.orEmpty()
	}


	public fun countChildren(): Int {
		var count = 0
		forEachChild { count += 1 }

		return count
	}


	public abstract fun equalsNode(other: GNode, includingOrigin: Boolean = false): Boolean


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


	public operator fun <Value : Any> get(extensionKey: GNodeExtensionKey<Value>): Value? =
		extensions[extensionKey]


	public fun hasChildren(): Boolean {
		forEachChild { return true }

		return false
	}


	override fun toString(): String =
		print(this)


	public companion object {

		public fun print(node: GNode, indent: String = "\t"): String =
			Printer.print(node = node, indent = indent)
	}


	public interface WithArguments {

		public val arguments: List<GArgument>

		public fun argument(name: String): GArgument? =
			arguments.firstOrNull { it.name == name }
	}


	public interface WithArgumentDefinitions {

		public val argumentDefinitions: List<GArgumentDefinition>


		public fun argumentDefinition(name: String): GArgumentDefinition? =
			argumentDefinitions.firstOrNull { it.name == name }
	}


	public interface WithDefaultValue : WithType {

		public val defaultValue: GValue?


		public fun isOptional(): Boolean =
			!isRequired()


		public fun isRequired(): Boolean =
			type is GNonNullTypeRef && defaultValue === null
	}


	public interface WithDirectives {

		public val directives: List<GDirective>


		public fun directive(name: String): GDirective? =
			directives.firstOrNull { it.name == name }


		public fun directives(name: String): List<GDirective> =
			directives.filter { it.name == name }
	}


	public interface WithFieldDefinitions {

		public val fieldDefinitions: List<GFieldDefinition>


		public fun fieldDefinition(name: String): GFieldDefinition? =
			fieldDefinitions.firstOrNull { it.name == name }
	}


	public interface WithInterfaces {

		public val interfaces: List<GNamedTypeRef>
	}


	public interface WithName : WithOptionalName {

		override val name: String
			get() = nameNode.value


		override val nameNode: GName
	}


	public interface WithOperationTypeDefinitions {

		public val operationTypeDefinitions: List<GOperationTypeDefinition>


		public fun operationTypeDefinition(operationType: GOperationType): GOperationTypeDefinition? =
			operationTypeDefinitions.firstOrNull { it.operationType == operationType }
	}


	public interface WithOptionalDeprecation : WithDirectives, WithName {

		public val deprecation: GDirective?
			get() = directive(GLanguage.defaultDeprecatedDirective.name)


		public val deprecationReason: String?
			get() = (deprecation?.argument("reason")?.value as? GStringValue)?.value
	}


	public interface WithOptionalDescription {

		public val description: String?
			get() = descriptionNode?.value


		public val descriptionNode: GStringValue?
	}


	public interface WithOptionalName {

		public val name: String?
			get() = nameNode?.value


		public val nameNode: GName?
	}


	public interface WithType {

		public val type: GTypeRef
	}


	public interface WithVariableDefinitions {

		public val variableDefinitions: List<GVariableDefinition>


		public fun variableDefinition(name: String): GVariableDefinition? =
			variableDefinitions.firstOrNull { it.name == name }
	}
}


public fun GNode?.equalsNode(other: GNode?, includingOrigin: Boolean = false): Boolean =
	this === other || (this !== null && other !== null && equalsNode(other, includingOrigin = includingOrigin))


public fun List<GNode?>.equalsNode(other: List<GNode?>, includingOrigin: Boolean): Boolean {
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


public sealed class GAbstractType(
	description: GStringValue?,
	directives: List<GDirective>,
	extensions: GNodeExtensionSet<GAbstractType>,
	kind: Kind,
	name: GName,
	origin: GDocumentPosition?,
) : GCompositeType(
	description = description,
	directives = directives,
	extensions = extensions,
	kind = kind,
	name = name,
	origin = origin
) {

	public companion object
}


public class GArgument(
	name: GName,
	public val value: GValue,
	origin: GDocumentPosition? = null,
	extensions: GNodeExtensionSet<GArgument> = GNodeExtensionSet.empty(),
) :
	GNode(
		extensions = extensions,
		origin = origin
	),
	GNode.WithName {

	override val nameNode: GName = name


	public constructor(
		name: String,
		value: GValue,
		extensions: GNodeExtensionSet<GArgument> = GNodeExtensionSet.empty(),
	) : this(
		name = GName(name),
		value = value,
		extensions = extensions
	)


	override fun equalsNode(other: GNode, includingOrigin: Boolean): Boolean =
		this === other || (
			other is GArgument &&
				nameNode.equalsNode(other.nameNode, includingOrigin = includingOrigin) &&
				value.equalsNode(other.value, includingOrigin = includingOrigin) &&
				(!includingOrigin || origin == other.origin)
			)


	public companion object
}


public sealed class GArgumentDefinition(
	override val defaultValue: GValue?,
	description: GStringValue?,
	override val directives: List<GDirective>,
	extensions: GNodeExtensionSet<GArgumentDefinition>,
	name: GName,
	origin: GDocumentPosition?,
	override val type: GTypeRef,
) :
	GNode(
		extensions = extensions,
		origin = origin
	),
	GNode.WithDefaultValue,
	GNode.WithDirectives,
	GNode.WithName,
	GNode.WithOptionalDescription {

	override val descriptionNode: GStringValue? = description
	override val nameNode: GName = name


	override fun equalsNode(other: GNode, includingOrigin: Boolean): Boolean =
		this === other || (
			other is GArgumentDefinition &&
				defaultValue.equalsNode(other.defaultValue, includingOrigin = includingOrigin) &&
				descriptionNode.equalsNode(other.descriptionNode, includingOrigin = includingOrigin) &&
				directives.equalsNode(other.directives, includingOrigin = includingOrigin) &&
				nameNode.equalsNode(other.nameNode, includingOrigin = includingOrigin) &&
				type.equalsNode(other.type, includingOrigin = includingOrigin) &&
				(!includingOrigin || origin == other.origin)
			)


	public companion object
}


// https://graphql.github.io/graphql-spec/draft/#sec-Boolean.Input-Coercion
public object GBooleanType : GScalarType(name = "Boolean")


public class GBooleanValue(
	public val value: Boolean,
	origin: GDocumentPosition? = null,
	extensions: GNodeExtensionSet<GBooleanValue> = GNodeExtensionSet.empty(),
) : GValue(
	extensions = extensions,
	origin = origin
) {

	override val kind: Kind get() = Kind.BOOLEAN


	override fun equals(other: Any?): Boolean =
		this === other || (other is GBooleanValue && value == other.value)


	override fun equalsNode(other: GNode, includingOrigin: Boolean): Boolean =
		this === other || (
			other is GBooleanValue &&
				value == other.value &&
				(!includingOrigin || origin == other.origin)
			)


	override fun hashCode(): Int =
		value.hashCode()


	override fun unwrap(): Boolean =
		value


	public companion object
}


public fun GBooleanValue(value: Boolean): GBooleanValue =
	GBooleanValue(value = value, origin = null)


public sealed class GCompositeType(
	description: GStringValue?,
	directives: List<GDirective>,
	extensions: GNodeExtensionSet<GCompositeType>,
	kind: Kind,
	name: GName,
	origin: GDocumentPosition?,
) : GNamedType(
	description = description,
	directives = directives,
	extensions = extensions,
	kind = kind,
	name = name,
	origin = origin
) {

	public companion object
}


// https://graphql.github.io/graphql-spec/draft/#sec-Scalars.Input-Coercion
public class GCustomScalarType(
	name: GName,
	description: GStringValue? = null,
	directives: List<GDirective> = emptyList(),
	origin: GDocumentPosition? = null,
	extensions: GNodeExtensionSet<GCustomScalarType> = GNodeExtensionSet.empty(),
) : GScalarType(
	description = description,
	directives = directives,
	extensions = extensions,
	name = name,
	origin = origin
) {

	public constructor(
		name: String,
		description: String? = null,
		directives: List<GDirective> = emptyList(),
		extensions: GNodeExtensionSet<GCustomScalarType> = GNodeExtensionSet.empty(),
	) : this(
		name = GName(name),
		description = description?.let(::GStringValue),
		directives = directives,
		extensions = extensions
	)


	public companion object
}


public sealed class GDefinition(
	extensions: GNodeExtensionSet<GDefinition>,
	origin: GDocumentPosition?,
) : GNode(
	extensions = extensions,
	origin = origin
) {

	public companion object
}


public class GDirective(
	name: GName,
	override val arguments: List<GArgument> = emptyList(),
	origin: GDocumentPosition? = null,
	extensions: GNodeExtensionSet<GDirective> = GNodeExtensionSet.empty(),
) :
	GNode(
		extensions = extensions,
		origin = origin
	),
	GNode.WithArguments,
	GNode.WithName {

	override val nameNode: GName = name


	public constructor(
		name: String,
		arguments: List<GArgument> = emptyList(),
		extensions: GNodeExtensionSet<GDirective> = GNodeExtensionSet.empty(),
	) : this(
		name = GName(name),
		arguments = arguments,
		extensions = extensions
	)


	override fun equalsNode(other: GNode, includingOrigin: Boolean): Boolean =
		this === other || (
			other is GDirective &&
				arguments.equalsNode(other.arguments, includingOrigin = includingOrigin) &&
				nameNode.equalsNode(other.nameNode, includingOrigin = includingOrigin) &&
				(!includingOrigin || origin == other.origin)
			)


	public companion object
}


public class GDirectiveArgumentDefinition(
	name: GName,
	type: GTypeRef,
	defaultValue: GValue? = null,
	description: GStringValue? = null,
	directives: List<GDirective> = emptyList(),
	origin: GDocumentPosition? = null,
	extensions: GNodeExtensionSet<GDirectiveArgumentDefinition> = GNodeExtensionSet.empty(),
) : GArgumentDefinition(
	defaultValue = defaultValue,
	description = description,
	directives = directives,
	extensions = extensions,
	name = name,
	type = type,
	origin = origin
) {

	public constructor(
		name: String,
		type: GTypeRef,
		defaultValue: GValue? = null,
		description: String? = null,
		directives: List<GDirective> = emptyList(),
		extensions: GNodeExtensionSet<GDirectiveArgumentDefinition> = GNodeExtensionSet.empty(),
	) : this(
		name = GName(name),
		type = type,
		defaultValue = defaultValue,
		description = description?.let(::GStringValue),
		directives = directives,
		extensions = extensions
	)

	public companion object
}


public class GDirectiveDefinition(
	name: GName,
	locations: List<GName>,
	public val isRepeatable: Boolean = false,
	override val argumentDefinitions: List<GDirectiveArgumentDefinition> = emptyList(),
	description: GStringValue? = null,
	origin: GDocumentPosition? = null,
	extensions: GNodeExtensionSet<GDirectiveDefinition> = GNodeExtensionSet.empty(),
) :
	GTypeSystemDefinition(
		extensions = extensions,
		origin = origin
	),
	GNode.WithArgumentDefinitions,
	GNode.WithName,
	GNode.WithOptionalDescription {

	public val locations: Set<GDirectiveLocation> = locations.mapNotNullTo(mutableSetOf()) { node ->
		GDirectiveLocation.values().firstOrNull { it.name == node.value }
	}
	public val locationNodes: List<GName> = locations

	override val descriptionNode: GStringValue? = description
	override val nameNode: GName = name


	public constructor(
		name: String,
		locations: Set<GDirectiveLocation>,
		isRepeatable: Boolean = false,
		argumentDefinitions: List<GDirectiveArgumentDefinition> = emptyList(),
		description: String? = null,
		extensions: GNodeExtensionSet<GDirectiveDefinition> = GNodeExtensionSet.empty(),
	) : this(
		name = GName(name),
		locations = locations.map { GName(it.name) },
		isRepeatable = isRepeatable,
		argumentDefinitions = argumentDefinitions,
		description = description?.let(::GStringValue),
		extensions = extensions
	)


	override fun argumentDefinition(name: String): GDirectiveArgumentDefinition? =
		argumentDefinitions.firstOrNull { it.name == name }


	override fun equalsNode(other: GNode, includingOrigin: Boolean): Boolean =
		this === other || (
			other is GDirectiveDefinition &&
				argumentDefinitions.equalsNode(other.argumentDefinitions, includingOrigin = includingOrigin) &&
				descriptionNode.equalsNode(other.descriptionNode, includingOrigin = includingOrigin) &&
				isRepeatable == other.isRepeatable &&
				locationNodes.equalsNode(other.locationNodes, includingOrigin = includingOrigin) &&
				nameNode.equalsNode(other.nameNode, includingOrigin = includingOrigin) &&
				(!includingOrigin || origin == other.origin)
			)


	public companion object
}


public class GDocument(
	public val definitions: List<GDefinition>,
	origin: GDocumentPosition? = null,
	extensions: GNodeExtensionSet<GDocument> = GNodeExtensionSet.empty(),
) : GNode(
	extensions = extensions,
	origin = origin
) {

	override fun equalsNode(other: GNode, includingOrigin: Boolean): Boolean =
		this === other || (
			other is GDocument &&
				definitions.equalsNode(other.definitions, includingOrigin = includingOrigin) &&
				(!includingOrigin || origin == other.origin)
			)


	public fun fragment(name: String): GFragmentDefinition? {
		for (definition in definitions)
			if (definition is GFragmentDefinition && definition.name == name)
				return definition

		return null
	}


	public fun operation(name: String?): GOperationDefinition? {
		for (definition in definitions)
			if (definition is GOperationDefinition && definition.name == name)
				return definition

		return null
	}


	public companion object {

		public fun parse(source: GDocumentSource.Parsable): GResult<GDocument> =
			Parser.parseDocument(source)


		public fun parse(content: String, name: String = "<document>"): GResult<GDocument> =
			parse(GDocumentSource.of(content = content, name = name))
	}
}


// https://graphql.github.io/graphql-spec/June2018/#sec-Enums
// https://graphql.github.io/graphql-spec/draft/#sec-Enums.Input-Coercion
public class GEnumType(
	name: GName,
	public val values: List<GEnumValueDefinition>,
	description: GStringValue? = null,
	directives: List<GDirective> = emptyList(),
	origin: GDocumentPosition? = null,
	extensions: GNodeExtensionSet<GEnumType> = GNodeExtensionSet.empty(),
) : GLeafType(
	description = description,
	directives = directives,
	extensions = extensions,
	kind = Kind.ENUM,
	name = name,
	origin = origin
) {

	public constructor(
		name: String,
		values: List<GEnumValueDefinition>,
		description: String? = null,
		directives: List<GDirective> = emptyList(),
		extensions: GNodeExtensionSet<GEnumType> = GNodeExtensionSet.empty(),
	) : this(
		name = GName(name),
		values = values,
		description = description?.let(::GStringValue),
		directives = directives,
		extensions = extensions
	)


	override fun equalsNode(other: GNode, includingOrigin: Boolean): Boolean =
		this === other || (
			other is GEnumType &&
				descriptionNode.equalsNode(other.descriptionNode, includingOrigin = includingOrigin) &&
				directives.equalsNode(other.directives, includingOrigin = includingOrigin) &&
				nameNode.equalsNode(other.nameNode, includingOrigin = includingOrigin) &&
				values.equalsNode(other.values, includingOrigin = includingOrigin) &&
				(!includingOrigin || origin == other.origin)
			)


	override fun isSupertypeOf(other: GType): Boolean =
		other === this ||
			(other is GNonNullType && other.nullableType === this)


	public fun value(name: String): GEnumValueDefinition? =
		values.firstOrNull { it.name == name }


	public companion object
}


public class GEnumTypeExtension(
	name: GName,
	public val values: List<GEnumValueDefinition>,
	directives: List<GDirective> = emptyList(),
	origin: GDocumentPosition? = null,
	extensions: GNodeExtensionSet<GEnumTypeExtension> = GNodeExtensionSet.empty(),
) : GTypeExtension(
	directives = directives,
	extensions = extensions,
	name = name,
	origin = origin
) {

	public constructor(
		name: String,
		values: List<GEnumValueDefinition>,
		directives: List<GDirective> = emptyList(),
		extensions: GNodeExtensionSet<GEnumTypeExtension> = GNodeExtensionSet.empty(),
	) : this(
		name = GName(name),
		values = values,
		directives = directives,
		extensions = extensions
	)


	override fun equalsNode(other: GNode, includingOrigin: Boolean): Boolean =
		this === other || (
			other is GEnumTypeExtension &&
				directives.equalsNode(other.directives, includingOrigin = includingOrigin) &&
				nameNode.equalsNode(other.nameNode, includingOrigin = includingOrigin) &&
				values.equalsNode(other.values, includingOrigin = includingOrigin) &&
				(!includingOrigin || origin == other.origin)
			)


	public fun value(name: String): GEnumValueDefinition? =
		values.firstOrNull { it.name == name }


	public companion object
}


public class GEnumValue(
	public val name: String,
	origin: GDocumentPosition? = null,
	extensions: GNodeExtensionSet<GEnumValue> = GNodeExtensionSet.empty(),
) : GValue(
	extensions = extensions,
	origin = origin
) {

	override val kind: Kind get() = Kind.ENUM


	override fun equals(other: Any?): Boolean =
		this === other || (other is GEnumValue && name == other.name)


	override fun equalsNode(other: GNode, includingOrigin: Boolean): Boolean =
		this === other || (
			other is GEnumValue &&
				name == other.name &&
				(!includingOrigin || origin == other.origin)
			)


	override fun hashCode(): Int =
		name.hashCode()


	override fun unwrap(): String =
		name


	public companion object
}


public fun GEnumValue(name: String): GEnumValue =
	GEnumValue(name = name, origin = null)


public class GEnumValueDefinition(
	name: GName,
	description: GStringValue? = null,
	override val directives: List<GDirective> = emptyList(),
	origin: GDocumentPosition? = null,
	extensions: GNodeExtensionSet<GEnumValueDefinition> = GNodeExtensionSet.empty(),
) :
	GNode(
		extensions = extensions,
		origin = origin
	),
	GNode.WithOptionalDeprecation,
	GNode.WithOptionalDescription {

	override val descriptionNode: GStringValue? = description
	override val nameNode: GName = name


	public constructor(
		name: String,
		description: String? = null,
		directives: List<GDirective> = emptyList(),
		extensions: GNodeExtensionSet<GEnumValueDefinition> = GNodeExtensionSet.empty(),
	) : this(
		name = GName(name),
		description = description?.let(::GStringValue),
		directives = directives,
		extensions = extensions
	)


	override fun equalsNode(other: GNode, includingOrigin: Boolean): Boolean =
		this === other || (
			other is GEnumValueDefinition &&
				descriptionNode.equalsNode(other.descriptionNode, includingOrigin = includingOrigin) &&
				directives.equalsNode(other.directives, includingOrigin = includingOrigin) &&
				nameNode.equalsNode(other.nameNode, includingOrigin = includingOrigin) &&
				(!includingOrigin || origin == other.origin)
			)


	public companion object
}


public sealed class GExecutableDefinition(
	extensions: GNodeExtensionSet<GExecutableDefinition>,
	origin: GDocumentPosition?,
) : GDefinition(
	extensions = extensions,
	origin = origin
) {

	public companion object
}


public class GFieldArgumentDefinition(
	name: GName,
	type: GTypeRef,
	defaultValue: GValue? = null,
	description: GStringValue? = null,
	directives: List<GDirective> = emptyList(),
	origin: GDocumentPosition? = null,
	extensions: GNodeExtensionSet<GFieldArgumentDefinition> = GNodeExtensionSet.empty(),
) : GArgumentDefinition(
	name = name,
	type = type,
	defaultValue = defaultValue,
	description = description,
	directives = directives,
	origin = origin,
	extensions = extensions
) {

	public constructor(
		name: String,
		type: GTypeRef,
		defaultValue: GValue? = null,
		description: String? = null,
		directives: List<GDirective> = emptyList(),
		extensions: GNodeExtensionSet<GFieldArgumentDefinition> = GNodeExtensionSet.empty(),
	) : this(
		name = GName(name),
		type = type,
		defaultValue = defaultValue,
		description = description?.let(::GStringValue),
		directives = directives,
		extensions = extensions
	)

	public companion object
}


public class GFieldDefinition(
	name: GName,
	public val type: GTypeRef,
	override val argumentDefinitions: List<GFieldArgumentDefinition> = emptyList(),
	description: GStringValue? = null,
	override val directives: List<GDirective> = emptyList(),
	origin: GDocumentPosition? = null,
	extensions: GNodeExtensionSet<GFieldDefinition> = GNodeExtensionSet.empty(),
) :
	GNode(
		extensions = extensions,
		origin = origin
	),
	GNode.WithArgumentDefinitions,
	GNode.WithOptionalDescription,
	GNode.WithOptionalDeprecation {

	override val descriptionNode: GStringValue? = description
	override val nameNode: GName = name


	public constructor(
		name: String,
		type: GTypeRef,
		argumentDefinitions: List<GFieldArgumentDefinition> = emptyList(),
		description: String? = null,
		directives: List<GDirective> = emptyList(),
		extensions: GNodeExtensionSet<GFieldDefinition> = GNodeExtensionSet.empty(),
	) : this(
		name = GName(name),
		type = type,
		argumentDefinitions = argumentDefinitions,
		description = description?.let(::GStringValue),
		directives = directives,
		extensions = extensions
	)


	override fun argumentDefinition(name: String): GFieldArgumentDefinition? =
		argumentDefinitions.firstOrNull { it.name == name }


	override fun equalsNode(other: GNode, includingOrigin: Boolean): Boolean =
		this === other || (
			other is GFieldDefinition &&
				argumentDefinitions.equalsNode(other.argumentDefinitions, includingOrigin = includingOrigin) &&
				descriptionNode.equalsNode(other.descriptionNode, includingOrigin = includingOrigin) &&
				directives.equalsNode(other.directives, includingOrigin = includingOrigin) &&
				nameNode.equalsNode(other.nameNode, includingOrigin = includingOrigin) &&
				type.equalsNode(other.type, includingOrigin = includingOrigin) &&
				(!includingOrigin || origin == other.origin)
			)


	public companion object
}


public class GFieldSelection(
	name: GName,
	public val selectionSet: GSelectionSet? = null,
	override val arguments: List<GArgument> = emptyList(),
	alias: GName? = null,
	directives: List<GDirective> = emptyList(),
	origin: GDocumentPosition? = null,
	extensions: GNodeExtensionSet<GFieldSelection> = GNodeExtensionSet.empty(),
) :
	GSelection(
		directives = directives,
		extensions = extensions,
		origin = origin
	),
	GNode.WithArguments {

	public val alias: String? get() = aliasNode?.value
	public val aliasNode: GName? = alias
	public val name: String get() = nameNode.value
	public val nameNode: GName = name


	public constructor(
		name: String,
		selectionSet: GSelectionSet? = null,
		arguments: List<GArgument> = emptyList(),
		alias: String? = null,
		directives: List<GDirective> = emptyList(),
		extensions: GNodeExtensionSet<GFieldSelection> = GNodeExtensionSet.empty(),
	) : this(
		name = GName(name),
		selectionSet = selectionSet,
		arguments = arguments,
		alias = alias?.let(::GName),
		directives = directives,
		extensions = extensions
	)


	override fun equalsNode(other: GNode, includingOrigin: Boolean): Boolean =
		this === other || (
			other is GFieldSelection &&
				aliasNode.equalsNode(other.aliasNode, includingOrigin = includingOrigin) &&
				arguments.equalsNode(other.arguments, includingOrigin = includingOrigin) &&
				directives.equalsNode(other.directives, includingOrigin = includingOrigin) &&
				nameNode.equalsNode(other.nameNode, includingOrigin = includingOrigin) &&
				selectionSet.equalsNode(other.selectionSet, includingOrigin = includingOrigin) &&
				(!includingOrigin || origin == other.origin)
			)


	public companion object
}


// https://graphql.github.io/graphql-spec/draft/#sec-Float.Input-Coercion
public object GFloatType : GScalarType(name = "Float")


public class GFloatValue(
	public val value: Double,
	origin: GDocumentPosition? = null,
	extensions: GNodeExtensionSet<GFloatValue> = GNodeExtensionSet.empty(),
) : GValue(
	extensions = extensions,
	origin = origin
) {

	init {
		check(value.isFinite()) { "'$value' is not a valid GraphQL Float value." }
	}


	override val kind: Kind get() = Kind.FLOAT


	override fun equals(other: Any?): Boolean =
		this === other || (other is GFloatValue && value == other.value)


	override fun equalsNode(other: GNode, includingOrigin: Boolean): Boolean =
		this === other || (
			other is GFloatValue &&
				value == other.value &&
				(!includingOrigin || origin == other.origin)
			)


	override fun hashCode(): Int =
		value.hashCode()


	override fun unwrap(): Double =
		value


	public companion object
}


public fun GFloatValue(value: Double): GFloatValue =
	GFloatValue(value = value, origin = null)


public fun GFloatValue(value: Float): GFloatValue =
	GFloatValue(value.toDouble())


public fun GFloatValue(value: Int): GFloatValue =
	GFloatValue(value.toDouble())


public class GFragmentDefinition(
	name: GName,
	public val typeCondition: GNamedTypeRef,
	public val selectionSet: GSelectionSet,
	override val variableDefinitions: List<GVariableDefinition> = emptyList(),
	override val directives: List<GDirective> = emptyList(),
	origin: GDocumentPosition? = null,
	extensions: GNodeExtensionSet<GFragmentDefinition> = GNodeExtensionSet.empty(),
) :
	GExecutableDefinition(
		extensions = extensions,
		origin = origin
	),
	GNode.WithDirectives,
	GNode.WithName,
	GNode.WithVariableDefinitions {

	override val nameNode: GName = name


	public constructor(
		name: String,
		typeCondition: GNamedTypeRef,
		selectionSet: GSelectionSet,
		variableDefinitions: List<GVariableDefinition> = emptyList(),
		directives: List<GDirective> = emptyList(),
		extensions: GNodeExtensionSet<GFragmentDefinition> = GNodeExtensionSet.empty(),
	) : this(
		name = GName(name),
		typeCondition = typeCondition,
		selectionSet = selectionSet,
		variableDefinitions = variableDefinitions,
		directives = directives,
		extensions = extensions
	)


	override fun equalsNode(other: GNode, includingOrigin: Boolean): Boolean =
		this === other || (
			other is GFragmentDefinition &&
				directives.equalsNode(other.directives, includingOrigin = includingOrigin) &&
				nameNode.equalsNode(other.nameNode, includingOrigin = includingOrigin) &&
				selectionSet.equalsNode(other.selectionSet, includingOrigin = includingOrigin) &&
				typeCondition.equalsNode(other.typeCondition, includingOrigin = includingOrigin) &&
				variableDefinitions.equalsNode(other.variableDefinitions, includingOrigin = includingOrigin) &&
				(!includingOrigin || origin == other.origin)
			)


	public companion object
}


public class GFragmentSelection(
	name: GName,
	directives: List<GDirective> = emptyList(),
	origin: GDocumentPosition? = null,
	extensions: GNodeExtensionSet<GFragmentSelection> = GNodeExtensionSet.empty(),
) :
	GSelection(
		directives = directives,
		extensions = extensions,
		origin = origin
	) {

	public val name: String get() = nameNode.value
	public val nameNode: GName = name


	public constructor(
		name: String,
		directives: List<GDirective> = emptyList(),
		extensions: GNodeExtensionSet<GFragmentSelection> = GNodeExtensionSet.empty(),
	) : this(
		name = GName(name),
		directives = directives,
		extensions = extensions
	)


	override fun equalsNode(other: GNode, includingOrigin: Boolean): Boolean =
		this === other || (
			other is GFragmentSelection &&
				directives.equalsNode(other.directives, includingOrigin = includingOrigin) &&
				nameNode.equalsNode(other.nameNode, includingOrigin = includingOrigin) &&
				(!includingOrigin || origin == other.origin)
			)


	public companion object
}


// https://graphql.github.io/graphql-spec/draft/#sec-ID.Input-Coercion
public object GIdType : GScalarType(name = "ID")


public class GInlineFragmentSelection(
	public val selectionSet: GSelectionSet,
	public val typeCondition: GNamedTypeRef?,
	directives: List<GDirective> = emptyList(),
	origin: GDocumentPosition? = null,
	extensions: GNodeExtensionSet<GInlineFragmentSelection> = GNodeExtensionSet.empty(),
) : GSelection(
	directives = directives,
	extensions = extensions,
	origin = origin
) {

	override fun equalsNode(other: GNode, includingOrigin: Boolean): Boolean =
		this === other || (
			other is GInlineFragmentSelection &&
				directives.equalsNode(other.directives, includingOrigin = includingOrigin) &&
				selectionSet.equalsNode(other.selectionSet, includingOrigin = includingOrigin) &&
				typeCondition.equalsNode(other.typeCondition, includingOrigin = includingOrigin) &&
				(!includingOrigin || origin == other.origin)
			)


	public companion object
}


public class GInputObjectArgumentDefinition(
	name: GName,
	type: GTypeRef,
	defaultValue: GValue? = null,
	description: GStringValue? = null,
	directives: List<GDirective> = emptyList(),
	origin: GDocumentPosition? = null,
	extensions: GNodeExtensionSet<GInputObjectArgumentDefinition> = GNodeExtensionSet.empty(),
) : GArgumentDefinition(
	name = name,
	type = type,
	defaultValue = defaultValue,
	description = description,
	directives = directives,
	origin = origin,
	extensions = extensions
) {

	public constructor(
		name: String,
		type: GTypeRef,
		defaultValue: GValue? = null,
		description: String? = null,
		directives: List<GDirective> = emptyList(),
		extensions: GNodeExtensionSet<GInputObjectArgumentDefinition> = GNodeExtensionSet.empty(),
	) : this(
		name = GName(name),
		type = type,
		defaultValue = defaultValue,
		description = description?.let(::GStringValue),
		directives = directives,
		extensions = extensions
	)

	public companion object
}


// https://graphql.github.io/graphql-spec/June2018/#sec-Input-Objects
// https://graphql.github.io/graphql-spec/June2018/#sec-Input-Object
public class GInputObjectType(
	name: GName,
	override val argumentDefinitions: List<GInputObjectArgumentDefinition>,
	description: GStringValue? = null,
	directives: List<GDirective> = emptyList(),
	origin: GDocumentPosition? = null,
	extensions: GNodeExtensionSet<GInputObjectType> = GNodeExtensionSet.empty(),
) :
	GCompositeType(
		description = description,
		directives = directives,
		extensions = extensions,
		kind = Kind.INPUT_OBJECT,
		name = name,
		origin = origin
	),
	GNode.WithArgumentDefinitions {

	public constructor(
		name: String,
		argumentDefinitions: List<GInputObjectArgumentDefinition>,
		description: String? = null,
		directives: List<GDirective> = emptyList(),
		extensions: GNodeExtensionSet<GInputObjectType> = GNodeExtensionSet.empty(),
	) : this(
		name = GName(name),
		argumentDefinitions = argumentDefinitions,
		description = description?.let(::GStringValue),
		directives = directives,
		extensions = extensions
	)


	override fun argumentDefinition(name: String): GInputObjectArgumentDefinition? =
		argumentDefinitions.firstOrNull { it.name == name }


	override fun equalsNode(other: GNode, includingOrigin: Boolean): Boolean =
		this === other || (
			other is GInputObjectType &&
				argumentDefinitions.equalsNode(other.argumentDefinitions, includingOrigin = includingOrigin) &&
				descriptionNode.equalsNode(other.descriptionNode, includingOrigin = includingOrigin) &&
				directives.equalsNode(other.directives, includingOrigin = includingOrigin) &&
				nameNode.equalsNode(other.nameNode, includingOrigin = includingOrigin) &&
				(!includingOrigin || origin == other.origin)
			)


	override fun isSupertypeOf(other: GType): Boolean =
		other === this ||
			(other is GNonNullType && other.nullableType === this)


	public companion object
}


public class GInputObjectTypeExtension(
	name: GName,
	override val argumentDefinitions: List<GInputObjectArgumentDefinition> = emptyList(),
	directives: List<GDirective> = emptyList(),
	origin: GDocumentPosition? = null,
	extensions: GNodeExtensionSet<GInputObjectTypeExtension> = GNodeExtensionSet.empty(),
) :
	GTypeExtension(
		directives = directives,
		extensions = extensions,
		name = name,
		origin = origin
	),
	GNode.WithArgumentDefinitions {

	public constructor(
		name: String,
		argumentDefinitions: List<GInputObjectArgumentDefinition> = emptyList(),
		directives: List<GDirective> = emptyList(),
		extensions: GNodeExtensionSet<GInputObjectTypeExtension> = GNodeExtensionSet.empty(),
	) : this(
		name = GName(name),
		argumentDefinitions = argumentDefinitions,
		directives = directives,
		extensions = extensions
	)


	override fun equalsNode(other: GNode, includingOrigin: Boolean): Boolean =
		this === other || (
			other is GInputObjectTypeExtension &&
				argumentDefinitions.equalsNode(other.argumentDefinitions, includingOrigin = includingOrigin) &&
				directives.equalsNode(other.directives, includingOrigin = includingOrigin) &&
				nameNode.equalsNode(other.nameNode, includingOrigin = includingOrigin) &&
				(!includingOrigin || origin == other.origin)
			)


	public companion object
}


// https://graphql.github.io/graphql-spec/draft/#sec-Int.Input-Coercion
public object GIntType : GScalarType(name = "Int")


public class GIntValue(
	public val value: Int,
	origin: GDocumentPosition? = null,
	extensions: GNodeExtensionSet<GIntValue> = GNodeExtensionSet.empty(),
) : GValue(
	extensions = extensions,
	origin = origin
) {

	override val kind: Kind get() = Kind.INT


	override fun equals(other: Any?): Boolean =
		this === other || (other is GIntValue && value == other.value)


	override fun equalsNode(other: GNode, includingOrigin: Boolean): Boolean =
		this === other || (
			other is GIntValue &&
				value == other.value &&
				(!includingOrigin || origin == other.origin)
			)


	override fun hashCode(): Int =
		value.hashCode()


	override fun unwrap(): Int =
		value


	public companion object
}


public fun GIntValue(value: Int): GIntValue =
	GIntValue(value = value, origin = null)


// https://graphql.github.io/graphql-spec/June2018/#sec-Interfaces
// https://graphql.github.io/graphql-spec/June2018/#sec-Interface
public class GInterfaceType(
	name: GName,
	override val fieldDefinitions: List<GFieldDefinition>,
	override val interfaces: List<GNamedTypeRef> = emptyList(),
	description: GStringValue? = null,
	directives: List<GDirective> = emptyList(),
	origin: GDocumentPosition? = null,
	extensions: GNodeExtensionSet<GInterfaceType> = GNodeExtensionSet.empty(),
) :
	GAbstractType(
		description = description,
		directives = directives,
		extensions = extensions,
		kind = Kind.INTERFACE,
		name = name,
		origin = origin
	),
	GNode.WithFieldDefinitions,
	GNode.WithInterfaces {

	public constructor(
		name: String,
		fieldDefinitions: List<GFieldDefinition>,
		interfaces: List<GNamedTypeRef> = emptyList(),
		description: String? = null,
		directives: List<GDirective> = emptyList(),
		extensions: GNodeExtensionSet<GInterfaceType> = GNodeExtensionSet.empty(),
	) : this(
		name = GName(name),
		fieldDefinitions = fieldDefinitions,
		interfaces = interfaces,
		description = description?.let(::GStringValue),
		directives = directives,
		extensions = extensions
	)


	override fun equalsNode(other: GNode, includingOrigin: Boolean): Boolean =
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


	public companion object
}


public class GInterfaceTypeExtension(
	name: GName,
	override val fieldDefinitions: List<GFieldDefinition> = emptyList(),
	override val interfaces: List<GNamedTypeRef> = emptyList(),
	directives: List<GDirective> = emptyList(),
	origin: GDocumentPosition? = null,
	extensions: GNodeExtensionSet<GInterfaceTypeExtension> = GNodeExtensionSet.empty(),
) :
	GTypeExtension(
		directives = directives,
		extensions = extensions,
		name = name,
		origin = origin
	),
	GNode.WithFieldDefinitions,
	GNode.WithInterfaces {


	public constructor(
		name: String,
		fields: List<GFieldDefinition> = emptyList(),
		interfaces: List<GNamedTypeRef> = emptyList(),
		directives: List<GDirective> = emptyList(),
		extensions: GNodeExtensionSet<GInterfaceTypeExtension> = GNodeExtensionSet.empty(),
	) : this(
		name = GName(name),
		fieldDefinitions = fields,
		interfaces = interfaces,
		directives = directives,
		extensions = extensions
	)


	override fun equalsNode(other: GNode, includingOrigin: Boolean): Boolean =
		this === other || (
			other is GInterfaceTypeExtension &&
				directives.equalsNode(other.directives, includingOrigin = includingOrigin) &&
				fieldDefinitions.equalsNode(other.fieldDefinitions, includingOrigin = includingOrigin) &&
				interfaces.equalsNode(other.interfaces, includingOrigin = includingOrigin) &&
				nameNode.equalsNode(other.nameNode, includingOrigin = includingOrigin) &&
				(!includingOrigin || origin == other.origin)
			)


	public companion object
}


public sealed class GLeafType(
	name: GName,
	description: GStringValue? = null,
	directives: List<GDirective> = emptyList(),
	extensions: GNodeExtensionSet<GLeafType>,
	kind: Kind,
	origin: GDocumentPosition?,
) : GNamedType(
	name = name,
	description = description,
	directives = directives,
	extensions = extensions,
	kind = kind,
	origin = origin
) {

	public companion object
}


// https://graphql.github.io/graphql-spec/June2018/#sec-Type-System.List
// https://graphql.github.io/graphql-spec/June2018/#sec-Type-Kinds.List
public class GListType(
	elementType: GType,
	extensions: GNodeExtensionSet<GListType> = GNodeExtensionSet.empty(),
) : GWrappingType(
	extensions = extensions,
	kind = Kind.LIST,
	wrappedType = elementType
) {

	public val elementType: GType get() = wrappedType

	override val name: String get() = "[${elementType.name}]"


	override fun equalsNode(other: GNode, includingOrigin: Boolean): Boolean =
		this === other || (
			other is GListType &&
				elementType.equalsNode(other.elementType, includingOrigin = includingOrigin)
			)


	override fun isSupertypeOf(other: GType): Boolean =
		other === this ||
			(other is GListType && elementType.isSupertypeOf(other.elementType)) ||
			(other is GNonNullType && isSupertypeOf(other.nullableType))


	override fun toRef(): GListTypeRef =
		GListTypeRef(elementType.toRef())


	public companion object
}


public class GListTypeRef(
	public val elementType: GTypeRef,
	origin: GDocumentPosition? = null,
	extensions: GNodeExtensionSet<GListTypeRef> = GNodeExtensionSet.empty(),
) : GTypeRef(
	extensions = extensions,
	origin = origin
) {

	override val underlyingName: String get() = elementType.underlyingName


	override fun equals(other: Any?): Boolean =
		this === other || (other is GListTypeRef && elementType == other.elementType)


	override fun hashCode(): Int =
		elementType.hashCode()


	override fun equalsNode(other: GNode, includingOrigin: Boolean): Boolean =
		this === other || (
			other is GListTypeRef &&
				elementType.equalsNode(other.elementType, includingOrigin = includingOrigin) &&
				(!includingOrigin || origin == other.origin)
			)


	public companion object
}


public fun GListTypeRef(
	name: String,
	extensions: GNodeExtensionSet<GListTypeRef> = GNodeExtensionSet.empty(),
): GListTypeRef =
	GListTypeRef(GTypeRef(name), extensions = extensions)


public class GListValue(
	public val elements: List<GValue>,
	origin: GDocumentPosition? = null,
	extensions: GNodeExtensionSet<GListValue> = GNodeExtensionSet.empty(),
) : GValue(
	extensions = extensions,
	origin = origin
) {

	override val kind: Kind get() = Kind.LIST


	override fun equals(other: Any?): Boolean =
		this === other || (other is GListValue && elements == other.elements)


	override fun equalsNode(other: GNode, includingOrigin: Boolean): Boolean =
		this === other || (
			other is GListValue &&
				elements.equalsNode(other.elements, includingOrigin = includingOrigin) &&
				(!includingOrigin || origin == other.origin)
			)


	override fun hashCode(): Int =
		elements.hashCode()


	override fun unwrap(): List<Any?> =
		elements.map { it.unwrap() }


	public companion object
}


public fun GListValue(elements: List<GValue>): GListValue =
	GListValue(elements = elements, origin = null)


public class GName(
	public val value: String,
	origin: GDocumentPosition? = null,
	extensions: GNodeExtensionSet<GName> = GNodeExtensionSet.empty(),
) : GNode(
	extensions = extensions,
	origin = origin
) {

	override fun equals(other: Any?): Boolean =
		this === other || (other is GName && value == other.value)


	override fun equalsNode(other: GNode, includingOrigin: Boolean): Boolean =
		this === other || (
			other is GName &&
				value == other.value &&
				(!includingOrigin || origin == other.origin)
			)


	override fun hashCode(): Int =
		value.hashCode()


	public companion object
}


public fun GName(value: String): GName =
	GName(value = value, origin = null)


public sealed class GNamedType(
	description: GStringValue?,
	override val directives: List<GDirective>,
	extensions: GNodeExtensionSet<GNamedType>,
	kind: Kind,
	name: GName,
	origin: GDocumentPosition?,
) :
	GType(
		extensions = extensions,
		kind = kind,
		origin = origin
	),
	GNode.WithDirectives,
	GNode.WithName,
	GNode.WithOptionalDescription {

	final override val descriptionNode: GStringValue? = description
	final override val nameNode: GName = name
	final override val underlyingNamedType: GNamedType get() = this


	override fun toRef(): GNamedTypeRef =
		GTypeRef(name)


	public companion object
}


public class GNamedTypeRef(
	name: GName,
	origin: GDocumentPosition? = null,
	extensions: GNodeExtensionSet<GNamedTypeRef> = GNodeExtensionSet.empty(),
) : GTypeRef(
	extensions = extensions,
	origin = origin
) {

	public val name: String get() = nameNode.value
	public val nameNode: GName = name

	override val underlyingName: String get() = name


	override fun equals(other: Any?): Boolean =
		this === other || (other is GNamedTypeRef && name == other.name)


	override fun hashCode(): Int =
		name.hashCode()


	public constructor(
		name: String,
		extensions: GNodeExtensionSet<GNamedTypeRef> = GNodeExtensionSet.empty(),
	) : this(
		name = GName(name),
		extensions = extensions
	)


	override fun equalsNode(other: GNode, includingOrigin: Boolean): Boolean =
		this === other || (
			other is GNamedTypeRef &&
				nameNode.equalsNode(other.nameNode, includingOrigin = includingOrigin) &&
				(!includingOrigin || origin == other.origin)
			)


	public companion object
}


// https://graphql.github.io/graphql-spec/June2018/#sec-Type-System.Non-Null
// https://graphql.github.io/graphql-spec/June2018/#sec-Type-Kinds.Non-Null
public class GNonNullType(
	nullableType: GType,
	extensions: GNodeExtensionSet<GNonNullType> = GNodeExtensionSet.empty(),
) : GWrappingType(
	extensions = extensions,
	kind = Kind.NON_NULL,
	wrappedType = nullableType
) {

	override val name: String get() = "${nullableType.name}!"
	override val nonNullable: GNonNullType get() = this
	override val nullableType: GType get() = wrappedType


	override fun equalsNode(other: GNode, includingOrigin: Boolean): Boolean =
		this === other || (
			other is GNonNullType &&
				nullableType.equalsNode(other.nullableType, includingOrigin = includingOrigin) &&
				(!includingOrigin || origin == other.origin)
			)


	override fun isSupertypeOf(other: GType): Boolean =
		other === this ||
			(other is GNonNullType && nullableType.isSupertypeOf(other.nullableType))


	override fun toRef(): GNonNullTypeRef =
		GNonNullTypeRef(nullableType.toRef())


	public companion object
}


public class GNonNullTypeRef(
	override val nullableRef: GTypeRef,
	origin: GDocumentPosition? = null,
	extensions: GNodeExtensionSet<GNonNullTypeRef> = GNodeExtensionSet.empty(),
) : GTypeRef(
	extensions = extensions,
	origin = origin
) {

	init {
		require(nullableRef !is GNonNullTypeRef) { "Cannot create non-null type ref to non-null type: $nullableRef" }
	}


	override val nonNullableRef: GNonNullTypeRef get() = this
	override val underlyingName: String get() = nullableRef.underlyingName


	override fun equals(other: Any?): Boolean =
		this === other || (other is GNonNullTypeRef && nullableRef == other.nullableRef)


	override fun hashCode(): Int =
		nullableRef.hashCode()


	override fun equalsNode(other: GNode, includingOrigin: Boolean): Boolean =
		this === other || (
			other is GNonNullTypeRef &&
				nullableRef.equalsNode(other.nullableRef, includingOrigin = includingOrigin) &&
				(!includingOrigin || origin == other.origin)
			)


	public companion object
}


public fun GNonNullTypeRef(
	name: String,
	extensions: GNodeExtensionSet<GNonNullTypeRef> = GNodeExtensionSet.empty(),
): GNonNullTypeRef =
	GNonNullTypeRef(GTypeRef(name), extensions = extensions)


public class GNullValue(
	origin: GDocumentPosition? = null,
	extensions: GNodeExtensionSet<GNullValue> = GNodeExtensionSet.empty(),
) : GValue(
	extensions = extensions,
	origin = origin
) {

	override val kind: Kind get() = Kind.NULL


	override fun equals(other: Any?): Boolean =
		this === other || other is GNullValue


	override fun equalsNode(other: GNode, includingOrigin: Boolean): Boolean =
		this === other || (
			other is GNullValue &&
				(!includingOrigin || origin == other.origin)
			)


	override fun hashCode(): Int =
		0


	override fun unwrap(): Nothing? =
		null


	public companion object {

		public val withoutOrigin: GNullValue = GNullValue()
	}
}


// https://graphql.github.io/graphql-spec/June2018/#sec-Objects
// https://graphql.github.io/graphql-spec/June2018/#sec-Object
public class GObjectType(
	name: GName,
	override val fieldDefinitions: List<GFieldDefinition>,
	override val interfaces: List<GNamedTypeRef> = emptyList(),
	description: GStringValue? = null,
	directives: List<GDirective> = emptyList(),
	origin: GDocumentPosition? = null,
	extensions: GNodeExtensionSet<GObjectType> = GNodeExtensionSet.empty(),
) :
	GCompositeType(
		description = description,
		directives = directives,
		extensions = extensions,
		kind = Kind.OBJECT,
		name = name,
		origin = origin
	),
	GNode.WithFieldDefinitions,
	GNode.WithInterfaces {

	public constructor(
		name: String,
		fieldDefinitions: List<GFieldDefinition>,
		interfaces: List<GNamedTypeRef> = emptyList(),
		description: String? = null,
		directives: List<GDirective> = emptyList(),
		extensions: GNodeExtensionSet<GObjectType> = GNodeExtensionSet.empty(),
	) : this(
		name = GName(name),
		fieldDefinitions = fieldDefinitions,
		interfaces = interfaces,
		description = description?.let(::GStringValue),
		directives = directives,
		extensions = extensions
	)


	override fun equalsNode(other: GNode, includingOrigin: Boolean): Boolean =
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


	public companion object
}


public class GObjectTypeExtension(
	name: GName,
	override val fieldDefinitions: List<GFieldDefinition> = emptyList(),
	override val interfaces: List<GNamedTypeRef> = emptyList(),
	directives: List<GDirective> = emptyList(),
	origin: GDocumentPosition? = null,
	extensions: GNodeExtensionSet<GObjectTypeExtension> = GNodeExtensionSet.empty(),
) :
	GTypeExtension(
		directives = directives,
		extensions = extensions,
		name = name,
		origin = origin
	),
	GNode.WithFieldDefinitions,
	GNode.WithInterfaces {

	public constructor(
		name: String,
		fields: List<GFieldDefinition> = emptyList(),
		interfaces: List<GNamedTypeRef> = emptyList(),
		directives: List<GDirective> = emptyList(),
		extensions: GNodeExtensionSet<GObjectTypeExtension> = GNodeExtensionSet.empty(),
	) : this(
		name = GName(name),
		fieldDefinitions = fields,
		interfaces = interfaces,
		directives = directives,
		extensions = extensions
	)


	override fun equalsNode(other: GNode, includingOrigin: Boolean): Boolean =
		this === other || (
			other is GObjectTypeExtension &&
				directives.equalsNode(other.directives, includingOrigin = includingOrigin) &&
				fieldDefinitions.equalsNode(other.fieldDefinitions, includingOrigin = includingOrigin) &&
				interfaces.equalsNode(other.interfaces, includingOrigin = includingOrigin) &&
				nameNode.equalsNode(other.nameNode, includingOrigin = includingOrigin) &&
				(!includingOrigin || origin == other.origin)
			)


	public companion object
}


public class GObjectValue(
	override val arguments: List<GArgument>,
	origin: GDocumentPosition? = null,
	extensions: GNodeExtensionSet<GObjectValue> = GNodeExtensionSet.empty(),
) :
	GValue(
		extensions = extensions,
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


	public companion object
}


public class GOperationDefinition(
	public val type: GOperationType,
	name: GName? = null,
	public val selectionSet: GSelectionSet,
	override val variableDefinitions: List<GVariableDefinition> = emptyList(),
	override val directives: List<GDirective> = emptyList(),
	origin: GDocumentPosition? = null,
	extensions: GNodeExtensionSet<GOperationDefinition> = GNodeExtensionSet.empty(),
) :
	GExecutableDefinition(
		extensions = extensions,
		origin = origin
	),
	GNode.WithDirectives,
	GNode.WithOptionalName,
	GNode.WithVariableDefinitions {

	override val nameNode: GName? = name


	public constructor(
		type: GOperationType,
		name: String? = null,
		selectionSet: GSelectionSet,
		variableDefinitions: List<GVariableDefinition> = emptyList(),
		directives: List<GDirective> = emptyList(),
		extensions: GNodeExtensionSet<GOperationDefinition> = GNodeExtensionSet.empty(),
	) : this(
		type = type,
		name = name?.let(::GName),
		selectionSet = selectionSet,
		variableDefinitions = variableDefinitions,
		directives = directives,
		extensions = extensions
	)


	override fun equalsNode(other: GNode, includingOrigin: Boolean): Boolean =
		this === other || (
			other is GOperationDefinition &&
				directives.equalsNode(other.directives, includingOrigin = includingOrigin) &&
				nameNode.equalsNode(other.nameNode, includingOrigin = includingOrigin) &&
				selectionSet.equalsNode(other.selectionSet, includingOrigin = includingOrigin) &&
				type == other.type &&
				variableDefinitions.equalsNode(other.variableDefinitions, includingOrigin = includingOrigin) &&
				(!includingOrigin || origin == other.origin)
			)


	public companion object
}


public class GOperationTypeDefinition(
	public val operationType: GOperationType,
	public val type: GNamedTypeRef,
	origin: GDocumentPosition? = null,
	extensions: GNodeExtensionSet<GOperationTypeDefinition> = GNodeExtensionSet.empty(),
) : GNode(
	extensions = extensions,
	origin = origin
) {

	override fun equalsNode(other: GNode, includingOrigin: Boolean): Boolean =
		this === other || (
			other is GOperationTypeDefinition &&
				operationType == other.operationType &&
				type.equalsNode(other.type, includingOrigin = includingOrigin) &&
				(!includingOrigin || origin == other.origin)
			)


	public companion object
}


// https://graphql.github.io/graphql-spec/June2018/#sec-Scalars
// https://graphql.github.io/graphql-spec/June2018/#sec-Scalar
public sealed class GScalarType(
	name: GName,
	description: GStringValue?,
	directives: List<GDirective>,
	origin: GDocumentPosition?,
	extensions: GNodeExtensionSet<GScalarType>,
) : GLeafType(
	description = description,
	directives = directives,
	extensions = extensions,
	kind = Kind.SCALAR,
	name = name,
	origin = origin
) {

	protected constructor(
		// https://youtrack.jetbrains.com/issue/KT-46999
		name: String,
		description: String? = null,
		directives: List<GDirective> = emptyList(),
		extensions: GNodeExtensionSet<GScalarType> = GNodeExtensionSet.empty(),
	) : this(
		name = GName(name),
		description = description?.let(::GStringValue),
		directives = directives,
		origin = null,
		extensions = extensions
	)


	final override fun equalsNode(other: GNode, includingOrigin: Boolean): Boolean =
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


	public companion object
}


public class GScalarTypeExtension(
	name: GName,
	directives: List<GDirective> = emptyList(),
	origin: GDocumentPosition? = null,
	extensions: GNodeExtensionSet<GScalarTypeExtension> = GNodeExtensionSet.empty(),
) : GTypeExtension(
	directives = directives,
	extensions = extensions,
	name = name,
	origin = origin
) {

	public constructor(
		name: String,
		directives: List<GDirective> = emptyList(),
		extensions: GNodeExtensionSet<GScalarTypeExtension> = GNodeExtensionSet.empty(),
	) : this(
		name = GName(name),
		directives = directives,
		extensions = extensions
	)


	override fun equalsNode(other: GNode, includingOrigin: Boolean): Boolean =
		this === other || (
			other is GScalarTypeExtension &&
				directives.equalsNode(other.directives, includingOrigin = includingOrigin) &&
				nameNode.equalsNode(other.nameNode, includingOrigin = includingOrigin) &&
				(!includingOrigin || origin == other.origin)
			)


	public companion object
}


public class GSchemaDefinition(
	override val operationTypeDefinitions: List<GOperationTypeDefinition>,
	override val directives: List<GDirective> = emptyList(),
	origin: GDocumentPosition? = null,
	extensions: GNodeExtensionSet<GSchemaDefinition> = GNodeExtensionSet.empty(),
) :
	GTypeSystemDefinition(
		extensions = extensions,
		origin = origin
	),
	GNode.WithDirectives,
	GNode.WithOperationTypeDefinitions {

	override fun equalsNode(other: GNode, includingOrigin: Boolean): Boolean =
		this === other || (
			other is GSchemaDefinition &&
				directives.equalsNode(other.directives, includingOrigin = includingOrigin) &&
				operationTypeDefinitions.equalsNode(other.operationTypeDefinitions, includingOrigin = includingOrigin) &&
				(!includingOrigin || origin == other.origin)
			)


	public companion object
}


public class GSchemaExtension(
	override val operationTypeDefinitions: List<GOperationTypeDefinition> = emptyList(),
	override val directives: List<GDirective> = emptyList(),
	origin: GDocumentPosition? = null,
	extensions: GNodeExtensionSet<GSchemaExtension> = GNodeExtensionSet.empty(),
) :
	GTypeSystemExtension(
		extensions = extensions,
		origin = origin
	),
	GNode.WithDirectives,
	GNode.WithOperationTypeDefinitions {

	override fun equalsNode(other: GNode, includingOrigin: Boolean): Boolean =
		this === other || (
			other is GSchemaExtension &&
				directives.equalsNode(other.directives, includingOrigin = includingOrigin) &&
				operationTypeDefinitions.equalsNode(other.operationTypeDefinitions, includingOrigin = includingOrigin) &&
				(!includingOrigin || origin == other.origin)
			)


	public companion object
}


public sealed class GSelection(
	override val directives: List<GDirective>,
	extensions: GNodeExtensionSet<GSelection>,
	origin: GDocumentPosition?,
) :
	GNode(
		extensions = extensions,
		origin = origin
	),
	GNode.WithDirectives {

	public companion object
}


public class GSelectionSet(
	public val selections: List<GSelection>,
	origin: GDocumentPosition? = null,
	extensions: GNodeExtensionSet<GSelectionSet> = GNodeExtensionSet.empty(),
) : GNode(
	extensions = extensions,
	origin = origin
) {

	override fun equalsNode(other: GNode, includingOrigin: Boolean): Boolean =
		this === other || (
			other is GSelectionSet &&
				selections.equalsNode(other.selections, includingOrigin = includingOrigin) &&
				(!includingOrigin || origin == other.origin)
			)


	public companion object
}


// https://graphql.github.io/graphql-spec/draft/#sec-String.Input-Coercion
public object GStringType : GScalarType(name = "String")


public class GStringValue(
	public val value: String,
	public val isBlock: Boolean = false,
	origin: GDocumentPosition? = null,
	extensions: GNodeExtensionSet<GStringValue> = GNodeExtensionSet.empty(),
) : GValue(
	extensions = extensions,
	origin = origin
) {

	override val kind: Kind get() = Kind.STRING


	override fun equals(other: Any?): Boolean =
		this === other || (other is GStringValue && value == other.value)


	override fun equalsNode(other: GNode, includingOrigin: Boolean): Boolean =
		this === other || (
			other is GStringValue &&
				value == other.value &&
				isBlock == other.isBlock &&
				(!includingOrigin || origin == other.origin)
			)


	override fun hashCode(): Int =
		value.hashCode()


	override fun unwrap(): String =
		value


	public companion object
}


public fun GStringValue(value: String): GStringValue =
	GStringValue(value = value, isBlock = false)


// https://graphql.github.io/graphql-spec/June2018/#sec-Wrapping-Types
// https://graphql.github.io/graphql-spec/June2018/#sec-Types
// https://graphql.github.io/graphql-spec/June2018/#sec-The-__Type-Type
public sealed class GType(
	extensions: GNodeExtensionSet<GType>,
	public val kind: Kind,
	origin: GDocumentPosition?,
) : GTypeSystemDefinition(
	extensions = extensions,
	origin = origin
) {

	public abstract val name: String
	public abstract val underlyingNamedType: GNamedType

	public open val nonNullable: GNonNullType get() = GNonNullType(this)
	public open val nullableType: GType get() = this


	public abstract fun toRef(): GTypeRef


	// https://graphql.github.io/graphql-spec/June2018/#IsInputType()
	public fun isInputType(): Boolean =
		when (this) {
			is GWrappingType -> wrappedType.isInputType()
			is GScalarType, is GEnumType, is GInputObjectType -> true
			else -> false
		}


	// https://graphql.github.io/graphql-spec/June2018/#IsOutputType()
	public fun isOutputType(): Boolean =
		when (this) {
			is GWrappingType -> wrappedType.isOutputType()
			is GScalarType, is GObjectType, is GInterfaceType, is GUnionType, is GEnumType -> true
			else -> false
		}


	public fun isSubtypeOf(other: GType): Boolean =
		other.isSupertypeOf(this)


	public abstract fun isSupertypeOf(other: GType): Boolean


	public companion object {

		public val defaultTypes: Set<GNamedType> = setOf<GNamedType>(
			GBooleanType,
			GFloatType,
			GIdType,
			GIntType,
			GStringType
		)
	}


	// https://graphql.github.io/graphql-spec/June2018/#sec-Schema-Introspection
	// https://graphql.github.io/graphql-spec/June2018/#sec-Type-Kinds
	public enum class Kind {

		ENUM,
		INPUT_OBJECT,
		INTERFACE,
		LIST,
		NON_NULL,
		OBJECT,
		SCALAR,
		UNION;


		override fun toString(): String =
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


		public companion object
	}
}


public sealed class GTypeExtension(
	override val directives: List<GDirective>,
	extensions: GNodeExtensionSet<GTypeExtension>,
	name: GName,
	origin: GDocumentPosition?,
) :
	GTypeSystemExtension(
		extensions = extensions,
		origin = origin
	),
	GNode.WithDirectives,
	GNode.WithName {

	override val nameNode: GName = name


	public companion object
}


public sealed class GTypeRef(
	extensions: GNodeExtensionSet<GTypeRef>,
	origin: GDocumentPosition?,
) : GNode(
	extensions = extensions,
	origin = origin
) {

	public abstract val underlyingName: String

	public open val nonNullableRef: GNonNullTypeRef get() = GNonNullTypeRef(this)
	public open val nullableRef: GTypeRef get() = this


	abstract override fun equals(other: Any?): Boolean
	abstract override fun hashCode(): Int


	public companion object {

		public fun parse(source: GDocumentSource.Parsable): GResult<GTypeRef> =
			Parser.parseTypeReference(source)


		public fun parse(content: String, name: String = "<type reference>"): GResult<GTypeRef> =
			parse(GDocumentSource.of(content = content, name = name))
	}
}


public fun GTypeRef(name: String): GNamedTypeRef =
	GNamedTypeRef(name)


public val GBooleanTypeRef: GNamedTypeRef = GTypeRef("Boolean")
public val GFloatTypeRef: GNamedTypeRef = GTypeRef("Float")
public val GIdTypeRef: GNamedTypeRef = GTypeRef("ID")
public val GIntTypeRef: GNamedTypeRef = GTypeRef("Int")
public val GStringTypeRef: GNamedTypeRef = GTypeRef("String")


public sealed class GTypeSystemDefinition(
	extensions: GNodeExtensionSet<GTypeSystemDefinition>,
	origin: GDocumentPosition?,
) : GDefinition(
	extensions = extensions,
	origin = origin
) {

	public companion object
}


public sealed class GTypeSystemExtension(
	extensions: GNodeExtensionSet<GTypeSystemExtension>,
	origin: GDocumentPosition?,
) : GDefinition(
	extensions = extensions,
	origin = origin
) {

	public companion object
}


// https://graphql.github.io/graphql-spec/June2018/#sec-Unions
// https://graphql.github.io/graphql-spec/June2018/#sec-Union
public class GUnionType(
	name: GName,
	public val possibleTypes: List<GNamedTypeRef>,
	description: GStringValue? = null,
	directives: List<GDirective> = emptyList(),
	origin: GDocumentPosition? = null,
	extensions: GNodeExtensionSet<GUnionType> = GNodeExtensionSet.empty(),
) : GAbstractType(
	description = description,
	directives = directives,
	extensions = extensions,
	kind = Kind.UNION,
	name = name,
	origin = origin
) {

	public constructor(
		name: String,
		possibleTypes: List<GNamedTypeRef>,
		description: String? = null,
		directives: List<GDirective> = emptyList(),
		extensions: GNodeExtensionSet<GUnionType> = GNodeExtensionSet.empty(),
	) : this(
		name = GName(name),
		possibleTypes = possibleTypes,
		description = description?.let(::GStringValue),
		directives = directives,
		extensions = extensions
	)


	override fun equalsNode(other: GNode, includingOrigin: Boolean): Boolean =
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


	public companion object
}


public class GUnionTypeExtension(
	name: GName,
	public val possibleTypes: List<GNamedTypeRef> = emptyList(),
	directives: List<GDirective> = emptyList(),
	origin: GDocumentPosition? = null,
	extensions: GNodeExtensionSet<GUnionTypeExtension> = GNodeExtensionSet.empty(),
) : GTypeExtension(
	directives = directives,
	extensions = extensions,
	name = name,
	origin = origin
) {

	public constructor(
		name: String,
		possibleTypes: List<GNamedTypeRef> = emptyList(),
		directives: List<GDirective> = emptyList(),
		extensions: GNodeExtensionSet<GUnionTypeExtension> = GNodeExtensionSet.empty(),
	) : this(
		name = GName(name),
		possibleTypes = possibleTypes,
		directives = directives,
		extensions = extensions
	)


	override fun equalsNode(other: GNode, includingOrigin: Boolean): Boolean =
		this === other || (
			other is GUnionTypeExtension &&
				directives.equalsNode(other.directives, includingOrigin = includingOrigin) &&
				nameNode.equalsNode(other.nameNode, includingOrigin = includingOrigin) &&
				possibleTypes.equalsNode(other.possibleTypes, includingOrigin = includingOrigin) &&
				(!includingOrigin || origin == other.origin)
			)


	public companion object
}


public sealed class GValue(
	extensions: GNodeExtensionSet<GValue>,
	origin: GDocumentPosition?,
) : GNode(
	extensions = extensions,
	origin = origin
) {

	public abstract val kind: Kind

	public abstract fun unwrap(): Any? // FIXME not language module


	public companion object {

		public fun parse(source: GDocumentSource.Parsable): GResult<GValue> =
			Parser.parseValue(source)


		public fun parse(content: String, name: String = "<value>"): GResult<GValue> =
			parse(GDocumentSource.of(content = content, name = name))
	}


	public enum class Kind {

		BOOLEAN,
		ENUM,
		FLOAT,
		INT,
		LIST,
		NULL,
		OBJECT,
		STRING,
		VARIABLE;


		override fun toString(): String = when (this) {
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


		public companion object
	}
}


public class GVariableDefinition(
	name: GName,
	override val type: GTypeRef,
	override val defaultValue: GValue? = null,
	override val directives: List<GDirective> = emptyList(),
	origin: GDocumentPosition? = null,
	extensions: GNodeExtensionSet<GVariableDefinition> = GNodeExtensionSet.empty(),
) :
	GNode(
		extensions = extensions,
		origin = origin
	),
	GNode.WithDefaultValue,
	GNode.WithDirectives,
	GNode.WithName {

	override val nameNode: GName = name


	public constructor(
		name: String,
		type: GTypeRef,
		defaultValue: GValue? = null,
		directives: List<GDirective> = emptyList(),
		extensions: GNodeExtensionSet<GVariableDefinition> = GNodeExtensionSet.empty(),
	) : this(
		name = GName(name),
		type = type,
		defaultValue = defaultValue,
		directives = directives,
		extensions = extensions
	)


	override fun equalsNode(other: GNode, includingOrigin: Boolean): Boolean =
		this === other || (
			other is GVariableDefinition &&
				defaultValue.equalsNode(other.defaultValue, includingOrigin = includingOrigin) &&
				directives.equalsNode(other.directives, includingOrigin = includingOrigin) &&
				nameNode.equalsNode(other.nameNode, includingOrigin = includingOrigin) &&
				type.equalsNode(other.type, includingOrigin = includingOrigin) &&
				(!includingOrigin || origin == other.origin)
			)


	public companion object
}


public class GVariableRef(
	name: GName,
	origin: GDocumentPosition? = null,
	extensions: GNodeExtensionSet<GVariableRef> = GNodeExtensionSet.empty(),
) : GValue(
	extensions = extensions,
	origin = origin
) {

	public val name: String get() = nameNode.value
	public val nameNode: GName = name

	override val kind: Kind get() = Kind.VARIABLE


	public constructor(
		name: String,
		extensions: GNodeExtensionSet<GVariableRef> = GNodeExtensionSet.empty(),
	) : this(
		name = GName(name),
		extensions = extensions
	)


	override fun equals(other: Any?): Boolean =
		this === other || (other is GVariableRef && name == other.name)


	override fun equalsNode(other: GNode, includingOrigin: Boolean): Boolean =
		this === other || (
			other is GVariableRef &&
				nameNode.equalsNode(other.nameNode, includingOrigin = includingOrigin) &&
				(!includingOrigin || origin == other.origin)
			)


	override fun hashCode(): Int =
		name.hashCode()


	override fun unwrap(): Nothing =
		error("Cannot unwrap a GraphQL variable: $name")


	public companion object
}


// https://graphql.github.io/graphql-spec/June2018/#sec-Wrapping-Types
// https://graphql.github.io/graphql-spec/June2018/#sec-Types
public sealed class GWrappingType(
	kind: Kind,
	public val wrappedType: GType,
	extensions: GNodeExtensionSet<GWrappingType>,
) : GType(
	extensions = extensions,
	kind = kind,
	origin = null
) {

	final override val underlyingNamedType: GNamedType get() = wrappedType.underlyingNamedType


	override fun toString(): String =
		"${print(wrappedType)} <wrapped as $name>"


	public companion object
}


private fun <Node : GNode> ((GNodeExtensionSet.Builder<Node>.() -> Unit)?).build(): GNodeExtensionSet<Node> =
	when (this) {
		null -> GNodeExtensionSet.empty()
		else -> GNodeExtensionSet.Builder.default<Node>().apply(this).build()
	}
