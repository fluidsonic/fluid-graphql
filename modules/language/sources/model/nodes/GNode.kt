package io.fluidsonic.graphql


/**
 * The sealed base class for every GraphQL AST node.
 *
 * Every node carries:
 * - [origin] — the location in the source document where the node was parsed, or `null` if not available.
 * - [extensions] — a typed key-value map for attaching arbitrary metadata without modifying the AST.
 *
 * Child traversal is available via [children], [childAt], [countChildren], and [hasChildren].
 * Structural equality (ignoring origin) is supported via [equalsNode].
 * Serialisation back to a GraphQL string is available via [GNode.print] or [toString].
 *
 * Access extension values directly with `node[MyKey]` using a [GNodeExtensionKey].
 *
 * Nested `With*` interfaces declare optional capabilities such as having a name, directives,
 * field definitions, etc. They are mixed into concrete node classes.
 */
public sealed class GNode(
	public val extensions: GNodeExtensionSet<GNode>,
	public val origin: GDocumentPosition?,
) {

	/** Returns the child node at the given [index], or `null` if the index is out of range. */
	public fun childAt(index: Int): GNode? {
		var childIndex = 0

		forEachChild { child ->
			if (childIndex == index)
				return child

			childIndex += 1
		}

		return null
	}


	/** Returns all direct children of this node as a list. */
	public fun children(): List<GNode> {
		var list: MutableList<GNode>? = null

		forEachChild { child ->
			(list ?: mutableListOf<GNode>().also { list = it })
				.add(child)
		}

		return list.orEmpty()
	}


	/** Returns the number of direct children of this node. */
	public fun countChildren(): Int {
		var count = 0
		forEachChild { count += 1 }

		return count
	}


	/**
	 * Compares this node to [other] for structural equality.
	 *
	 * When [includingOrigin] is `true`, source positions are also compared;
	 * when `false` (the default), only the logical content is compared.
	 */
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


	/** Returns the extension value for [extensionKey], or `null` if not set. */
	public operator fun <Value : Any> get(extensionKey: GNodeExtensionKey<Value>): Value? =
		extensions[extensionKey]


	/** Returns `true` if this node has at least one direct child. */
	public fun hasChildren(): Boolean {
		forEachChild { return true }

		return false
	}


	override fun toString(): String =
		print(this)


	public companion object {

		/** Serialises [node] to a GraphQL string, using [indent] for indentation. */
		public fun print(node: GNode, indent: String = "\t"): String =
			Printer.print(node = node, indent = indent)
	}


	/** Marks a node that carries a list of [GArgument]s. */
	public interface WithArguments {

		public val arguments: List<GArgument>

		/** Returns the argument with the given [name], or `null` if not present. */
		public fun argument(name: String): GArgument? =
			arguments.firstOrNull { it.name == name }
	}


	/** Marks a node that declares a list of [GArgumentDefinition]s. */
	public interface WithArgumentDefinitions {

		public val argumentDefinitions: List<GArgumentDefinition>


		/** Returns the argument definition with the given [name], or `null` if not found. */
		public fun argumentDefinition(name: String): GArgumentDefinition? =
			argumentDefinitions.firstOrNull { it.name == name }
	}


	/** Marks a node ([GVariableDefinition] or [GArgumentDefinition]) that may carry a default value and a [GTypeRef]. */
	public interface WithDefaultValue : WithDirectives, WithType {

		public val defaultValue: GValue?


		/** Returns `true` if supplying a value at runtime is optional. */
		public fun isOptional(): Boolean =
			!isRequired()


		/**
		 * Returns `true` if a value must be supplied at runtime.
		 *
		 * A definition is required when its [type] is [GNonNullTypeRef], it has no [defaultValue],
		 * and it is not annotated with `@optional`.
		 */
		public fun isRequired(): Boolean =
			type is GNonNullTypeRef && defaultValue === null && directive(GLanguage.defaultOptionalDirective.name) == null
	}


	/** Marks a node that carries a list of applied [GDirective]s. */
	public interface WithDirectives {

		public val directives: List<GDirective>


		/** Returns the first directive with the given [name], or `null` if not present. */
		public fun directive(name: String): GDirective? =
			directives.firstOrNull { it.name == name }


		/** Returns all directives with the given [name]. */
		public fun directives(name: String): List<GDirective> =
			directives.filter { it.name == name }
	}


	/** Marks a node (object or interface type) that exposes a list of [GFieldDefinition]s. */
	public interface WithFieldDefinitions {

		public val fieldDefinitions: List<GFieldDefinition>


		/** Returns the field definition with the given [name], or `null` if not found. */
		public fun fieldDefinition(name: String): GFieldDefinition? =
			fieldDefinitions.firstOrNull { it.name == name }
	}


	/** Marks a node (object or interface type) that declares implemented interfaces. */
	public interface WithInterfaces {

		public val interfaces: List<GNamedTypeRef>
	}


	/** Marks a node with a required, non-nullable name. */
	public interface WithName : WithOptionalName {

		override val name: String
			get() = nameNode.value


		override val nameNode: GName
	}


	/** Marks a node that maps operation types to their root object types. */
	public interface WithOperationTypeDefinitions {

		public val operationTypeDefinitions: List<GOperationTypeDefinition>


		/** Returns the operation type definition for the given [operationType], or `null` if not defined. */
		public fun operationTypeDefinition(operationType: GOperationType): GOperationTypeDefinition? =
			operationTypeDefinitions.firstOrNull { it.operationType == operationType }
	}


	/** Marks a named, directive-carrying node that may be annotated with `@deprecated`. */
	public interface WithOptionalDeprecation : WithDirectives, WithName {

		/** Returns the `@deprecated` directive if present, or `null`. */
		public val deprecation: GDirective?
			get() = directive(GLanguage.defaultDeprecatedDirective.name)


		public val deprecationReason: String?
			get() = (deprecation?.argument("reason")?.value as? GStringValue)?.value
	}


	/** Marks a node (type or field definition) that may carry a description string. */
	public interface WithOptionalDescription {

		public val description: String?
			get() = descriptionNode?.value


		public val descriptionNode: GStringValue?
	}


	/** Marks a node with an optional name (e.g. an anonymous operation). */
	public interface WithOptionalName {

		public val name: String?
			get() = nameNode?.value


		public val nameNode: GName?
	}


	/** Marks a node that references a [GTypeRef] (e.g. a variable or argument definition). */
	public interface WithType {

		public val type: GTypeRef
	}


	/** Marks an executable definition that declares variables. */
	public interface WithVariableDefinitions {

		public val variableDefinitions: List<GVariableDefinition>


		/** Returns the variable definition with the given [name], or `null` if not found. */
		public fun variableDefinition(name: String): GVariableDefinition? =
			variableDefinitions.firstOrNull { it.name == name }
	}
}


/**
 * Compares two nullable nodes for structural equality, returning `true` if both are `null`
 * or if they are [GNode.equalsNode]-equal with the given [includingOrigin] setting.
 */
public fun GNode?.equalsNode(other: GNode?, includingOrigin: Boolean = false): Boolean =
	this === other || (this !== null && other !== null && equalsNode(other, includingOrigin = includingOrigin))


/** Compares two lists of nullable nodes element-by-element using [GNode.equalsNode]. */
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


/**
 * Sealed base class for abstract GraphQL types: [GInterfaceType] and [GUnionType].
 *
 * Abstract types can appear in selection sets but their possible concrete types must be
 * resolved via [GSchema.getPossibleTypes].
 */
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


/**
 * A named argument passed to a field or directive (`name: value`).
 *
 * The argument's definition (if any) is found via [GNode.WithArgumentDefinitions.argumentDefinition].
 */
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


/**
 * Sealed base class for input argument definitions.
 *
 * Concrete subclasses: [GFieldArgumentDefinition] (for object/interface field arguments),
 * [GDirectiveArgumentDefinition] (for directive arguments), and
 * [GInputObjectArgumentDefinition] (for input object fields).
 */
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
	GNode.WithOptionalDeprecation,
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


/** The built-in GraphQL `Boolean` scalar type. */
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


/**
 * Sealed base class for composite GraphQL types: [GObjectType], [GInterfaceType], [GUnionType],
 * and [GInputObjectType].
 *
 * Composite types can have selection sets applied to them in queries.
 */
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


/**
 * A user-defined (custom) GraphQL scalar type.
 *
 * Unlike the built-in scalars, custom scalars require coercion logic to be registered
 * in the execution layer.
 */
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


/**
 * Sealed base class for all top-level definitions in a [GDocument].
 *
 * Subclasses: [GExecutableDefinition] (operations and fragments) and
 * [GTypeSystemDefinition] (type, directive, and schema definitions).
 */
public sealed class GDefinition(
	extensions: GNodeExtensionSet<GDefinition>,
	origin: GDocumentPosition?,
) : GNode(
	extensions = extensions,
	origin = origin
) {

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


/**
 * Sealed base class for executable definitions: [GOperationDefinition] and [GFragmentDefinition].
 *
 * These appear in query documents sent to a GraphQL server.
 */
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


/**
 * The definition of a field on an object or interface type in the schema.
 *
 * Includes the field's return [type], optional [argumentDefinitions], optional [description],
 * and any applied [directives] (e.g. `@deprecated`).
 */
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


/** The built-in GraphQL `Float` scalar type. */
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


/** The built-in GraphQL `ID` scalar type. */
// https://graphql.github.io/graphql-spec/draft/#sec-ID.Input-Coercion
public object GIdType : GScalarType(name = "ID")


/**
 * An inline fragment spread (`... on Type { ... }` or `... { ... }`) within a selection set.
 *
 * When [typeCondition] is absent, the fragment applies to the enclosing type unconditionally.
 */
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


/** The built-in GraphQL `Int` scalar type. */
// https://graphql.github.io/graphql-spec/draft/#sec-Int.Input-Coercion
public object GIntType : GScalarType(name = "Int")


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


/**
 * Sealed base class for GraphQL leaf types: [GScalarType] and [GEnumType].
 *
 * Leaf types have no selection set — fields of these types resolve to concrete values.
 */
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


/**
 * A type reference representing a GraphQL list type (`[ElementType]`).
 *
 * Use [GListTypeRef] factory function to create one from a type name.
 */
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


/**
 * Sealed base class for all named (i.e. non-wrapping) GraphQL types.
 *
 * Subclasses: [GScalarType], [GObjectType], [GInterfaceType], [GUnionType],
 * [GEnumType], and [GInputObjectType].
 *
 * Named types are identified by [name] and are registered in the schema's type map.
 */
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


	override val name: String
		get() = nameNode.value


	override fun toRef(): GNamedTypeRef =
		GTypeRef(name)


	public companion object
}


/**
 * A type reference to a named type by name (e.g. `String`, `MyObject`).
 *
 * Create with [GTypeRef]`(name)` or [GNamedTypeRef]`(name)`.
 */
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


/**
 * A type reference representing a non-null type (`Type!`).
 *
 * The wrapped [nullableRef] must itself not be a [GNonNullTypeRef].
 * Create with [GNonNullTypeRef]`(name)` or via [GTypeRef.nonNullableRef].
 */
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


/**
 * An input object value literal, represented as a list of field-value pairs (`{ field: value }`).
 *
 * Each field is stored as a [GArgument]. Use [GNode.WithArguments.argument] to look up a field by name.
 * [unwrap] returns a `Map<String, Any?>` of the field values.
 */
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


/**
 * Maps a [GOperationType] (query/mutation/subscription) to its root object type inside a
 * `schema { ... }` definition.
 */
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


/**
 * Sealed base class for GraphQL scalar types.
 *
 * Built-in singletons: [GBooleanType], [GFloatType], [GIdType], [GIntType], [GStringType].
 * User-defined scalars use [GCustomScalarType].
 */
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


/** The built-in GraphQL `String` scalar type. */
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


/**
 * Sealed base class for all GraphQL types used during schema resolution and execution.
 *
 * Unlike [GTypeRef] (which is just a syntactic reference), a [GType] is a fully resolved type.
 * The type hierarchy is:
 * - [GNamedType]: [GScalarType], [GObjectType], [GInterfaceType], [GUnionType], [GEnumType], [GInputObjectType]
 * - [GWrappingType]: [GListType], [GNonNullType]
 *
 * Use [isInputType] / [isOutputType] to check usage context, and [isSubtypeOf] / [isSupertypeOf]
 * to test subtype relationships. The companion [GType.defaultTypes] lists the five built-in scalars.
 */
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


	/** Returns `true` if this type can be used as an input type ([GScalarType], [GEnumType], [GInputObjectType], and [GWrappingType] wrappers thereof). */
	// https://graphql.github.io/graphql-spec/June2018/#IsInputType()
	public fun isInputType(): Boolean =
		when (this) {
			is GWrappingType -> wrappedType.isInputType()
			is GScalarType, is GEnumType, is GInputObjectType -> true
			else -> false
		}


	/** Returns `true` if this type can be used as an output type ([GScalarType], [GObjectType], [GInterfaceType], [GUnionType], [GEnumType], and [GWrappingType] wrappers thereof). */
	// https://graphql.github.io/graphql-spec/June2018/#IsOutputType()
	public fun isOutputType(): Boolean =
		when (this) {
			is GWrappingType -> wrappedType.isOutputType()
			is GScalarType, is GObjectType, is GInterfaceType, is GUnionType, is GEnumType -> true
			else -> false
		}


	/** Returns `true` if this type is a subtype of [other]. */
	public fun isSubtypeOf(other: GType): Boolean =
		other.isSupertypeOf(this)


	/** Returns `true` if [other] is a subtype of this type. */
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


	/** Identifies the kind of a [GType] at runtime, matching the `__TypeKind` introspection enum. */
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


/**
 * Sealed base class for type extension definitions (`extend type Foo { ... }`).
 *
 * Concrete subclasses mirror each concrete [GNamedType] with an `Extension` suffix.
 */
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


/**
 * Sealed base class for GraphQL type references — the syntactic representation of a type
 * as it appears in a schema or query document.
 *
 * Concrete subclasses: [GNamedTypeRef], [GListTypeRef], [GNonNullTypeRef].
 *
 * Use [GTypeRef.parse] to parse a type reference from a string, and [GSchema.resolveType] to
 * resolve it to a concrete [GType].
 *
 * Convenience ref constants: [GBooleanTypeRef], [GFloatTypeRef], [GIdTypeRef], [GIntTypeRef], [GStringTypeRef].
 */
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

		/**
		 * Parses a type reference string (e.g. `"String!"`, `"[Int]"`) from [source].
		 *
		 * Returns a [GResult.Success] with the parsed type reference, or a [GResult.Failure] with parse errors.
		 */
		public fun parse(source: GDocumentSource.Parsable): GResult<GTypeRef> =
			Parser.parseTypeReference(source)


		public fun parse(content: String, name: String = "<type reference>"): GResult<GTypeRef> =
			parse(GDocumentSource.of(content = content, name = name))
	}
}


/** Creates a [GNamedTypeRef] for the given type [name]. Shorthand for [GNamedTypeRef]`(name)`. */
public fun GTypeRef(name: String): GNamedTypeRef =
	GNamedTypeRef(name)


/** Reusable type reference for the built-in `Boolean` scalar. */
public val GBooleanTypeRef: GNamedTypeRef = GTypeRef("Boolean")

/** Reusable type reference for the built-in `Float` scalar. */
public val GFloatTypeRef: GNamedTypeRef = GTypeRef("Float")

/** Reusable type reference for the built-in `ID` scalar. */
public val GIdTypeRef: GNamedTypeRef = GTypeRef("ID")

/** Reusable type reference for the built-in `Int` scalar. */
public val GIntTypeRef: GNamedTypeRef = GTypeRef("Int")

/** Reusable type reference for the built-in `String` scalar. */
public val GStringTypeRef: GNamedTypeRef = GTypeRef("String")


/**
 * Sealed base class for type system definitions in an SDL document.
 *
 * Subclasses: [GType] subclasses, [GDirectiveDefinition], and [GSchemaDefinition].
 */
public sealed class GTypeSystemDefinition(
	extensions: GNodeExtensionSet<GTypeSystemDefinition>,
	origin: GDocumentPosition?,
) : GDefinition(
	extensions = extensions,
	origin = origin
) {

	public companion object
}


/**
 * Sealed base class for type system extension definitions (`extend type/schema ...`).
 *
 * Subclasses: [GTypeExtension] and [GSchemaExtension].
 */
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


/**
 * Sealed base class for wrapping types that modify another type: [GListType] and [GNonNullType].
 *
 * The wrapped [wrappedType] is accessible directly; use [GType.underlyingNamedType] to unwrap fully.
 */
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
