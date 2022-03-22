package testing

import io.fluidsonic.graphql.*


fun <T : GNode> ast(configure: AstBuilder.() -> T) =
	AstBuilder.run(configure)


fun documentAst(origin: IntRange, configure: AstBuilder.DocumentBuilder.() -> Unit) =
	AstBuilder.DocumentBuilder(origin).apply(configure).build()


@AstBuilderDsl
object AstBuilder {

	fun document(origin: IntRange, configure: DocumentBuilder.() -> Unit) =
		DocumentBuilder(origin).apply(configure).build()


	@AstBuilderDsl
	class ArgumentDefinitionBuilder(private val origin: IntRange) {

		private var defaultValue: GValue? = null
		private var name: GName? = null
		private var type: GTypeRef? = null


		fun buildForDirective() = GDirectiveArgumentDefinition(
			description = null,
			defaultValue = defaultValue,
			directives = emptyList(),
			name = name ?: error("name() missing"),
			origin = DocumentPosition(origin),
			type = type ?: error("type() missing")
		)


		fun buildForField() = GFieldArgumentDefinition(
			description = null,
			defaultValue = defaultValue,
			directives = emptyList(),
			name = name ?: error("name() missing"),
			origin = DocumentPosition(origin),
			type = type ?: error("type() missing")
		)


		fun buildForInputObject() = GInputObjectArgumentDefinition(
			description = null,
			defaultValue = defaultValue,
			directives = emptyList(),
			name = name ?: error("name() missing"),
			origin = DocumentPosition(origin),
			type = type ?: error("type() missing")
		)


		fun defaultValue(configure: ValueBuilder.() -> Unit) {
			defaultValue = ValueBuilder().apply(configure).build()
		}


		fun name(origin: IntRange, configure: () -> String) {
			name = GName(
				origin = DocumentPosition(origin),
				value = configure()
			)
		}


		fun type(configure: TypeReferenceBuilder.() -> Unit) {
			type = TypeReferenceBuilder().apply(configure).build()
		}
	}


	@AstBuilderDsl
	class DirectiveDefinitionBuilder(private val origin: IntRange) {

		private var isRepeatable = false
		private val locations = mutableListOf<GName>()
		private var name: GName? = null


		fun build() = GDirectiveDefinition(
			argumentDefinitions = emptyList(),
			description = null,
			isRepeatable = isRepeatable,
			locations = locations,
			name = name ?: error("name() missing"),
			origin = DocumentPosition(origin)
		)


		fun location(origin: IntRange, configure: () -> String) {
			locations += GName(
				origin = DocumentPosition(origin),
				value = configure()
			)
		}


		fun name(origin: IntRange, configure: () -> String) {
			name = GName(
				origin = DocumentPosition(origin),
				value = configure()
			)
		}


		fun repeatable() {
			isRepeatable = true
		}
	}


	@AstBuilderDsl
	class DocumentBuilder(private val origin: IntRange) {

		private val definitions = mutableListOf<GDefinition>()


		fun build() = GDocument(
			definitions = definitions,
			origin = DocumentPosition(origin)
		)


		fun directiveDefinition(origin: IntRange, configure: DirectiveDefinitionBuilder.() -> Unit) {
			definitions += DirectiveDefinitionBuilder(origin).apply(configure).build()
		}


		fun enumTypeDefinition(origin: IntRange, configure: EnumTypeDefinitionBuilder.() -> Unit) {
			definitions += EnumTypeDefinitionBuilder(origin).apply(configure).build()
		}


//		fun fragmentDefinition(origin: IntRange, configure: FragmentDefinitionBuilder.() -> Unit) {
//			definitions += FragmentDefinitionBuilder(origin).apply(configure).build()
//		}


		fun inputObjectTypeDefinition(origin: IntRange, configure: InputObjectTypeDefinitionBuilder.() -> Unit) {
			definitions += InputObjectTypeDefinitionBuilder(origin).apply(configure).build()
		}


		fun interfaceTypeDefinition(origin: IntRange, configure: InterfaceTypeDefinitionBuilder.() -> Unit) {
			definitions += InterfaceTypeDefinitionBuilder(origin).apply(configure).build()
		}


		fun objectTypeDefinition(origin: IntRange, configure: ObjectTypeDefinitionBuilder.() -> Unit) {
			definitions += ObjectTypeDefinitionBuilder(origin).apply(configure).build()
		}


//		fun operationDefinition(origin: IntRange, configure: OperationDefinitionBuilder.() -> Unit) {
//			definitions += OperationDefinitionBuilder(origin).apply(configure).build()
//		}


		fun scalarTypeDefinition(origin: IntRange, configure: ScalarTypeDefinitionBuilder.() -> Unit) {
			definitions += ScalarTypeDefinitionBuilder(origin).apply(configure).build()
		}


		fun schemaDefinition(origin: IntRange, configure: SchemaDefinitionBuilder.() -> Unit) {
			definitions += SchemaDefinitionBuilder(origin).apply(configure).build()
		}


		fun unionTypeDefinition(origin: IntRange, configure: UnionTypeDefinitionBuilder.() -> Unit) {
			definitions += UnionTypeDefinitionBuilder(origin).apply(configure).build()
		}
	}


	@AstBuilderDsl
	class EnumTypeDefinitionBuilder(private val origin: IntRange) {

		private var description: GStringValue? = null
		private val values = mutableListOf<GEnumValueDefinition>()
		private var name: GName? = null


		fun build() = GEnumType(
			description = description,
			directives = emptyList(),
			name = name ?: error("name() missing"),
			origin = DocumentPosition(origin),
			values = values
		)


		fun description(origin: IntRange, isBlock: Boolean = false, configure: () -> String) {
			description = GStringValue(
				origin = DocumentPosition(origin),
				value = configure(),
				isBlock = isBlock
			)
		}


		fun name(origin: IntRange, configure: () -> String) {
			name = GName(
				origin = DocumentPosition(origin),
				value = configure()
			)
		}


		fun value(origin: IntRange, configure: () -> String) {
			values += GEnumValueDefinition(
				description = null,
				directives = emptyList(),
				name = GName(
					origin = DocumentPosition(origin),
					value = configure()
				),
				origin = DocumentPosition(origin)
			)
		}
	}


	@AstBuilderDsl
	class FieldDefinitionBuilder(private val origin: IntRange) {

		private val arguments = mutableListOf<GFieldArgumentDefinition>()
		private var name: GName? = null
		private var type: GTypeRef? = null


		fun build() = GFieldDefinition(
			argumentDefinitions = arguments,
			description = null,
			directives = emptyList(),
			name = name ?: error("name() missing"),
			origin = DocumentPosition(origin),
			type = type ?: error("type() missing")
		)


		fun argument(origin: IntRange, configure: ArgumentDefinitionBuilder.() -> Unit) {
			arguments += ArgumentDefinitionBuilder(origin).apply(configure).buildForField()
		}


		fun name(origin: IntRange, configure: () -> String) {
			name = GName(
				origin = DocumentPosition(origin),
				value = configure()
			)
		}


		fun type(configure: TypeReferenceBuilder.() -> Unit) {
			type = TypeReferenceBuilder().apply(configure).build()
		}
	}


	@AstBuilderDsl
	class FragmentDefinitionBuilder


	@AstBuilderDsl
	class InputObjectTypeDefinitionBuilder(private val origin: IntRange) {

		private val arguments = mutableListOf<GInputObjectArgumentDefinition>()
		private var description: GStringValue? = null
		private var name: GName? = null


		fun argument(origin: IntRange, configure: ArgumentDefinitionBuilder.() -> Unit) {
			arguments += ArgumentDefinitionBuilder(origin).apply(configure).buildForInputObject()
		}


		fun build() = GInputObjectType(
			argumentDefinitions = arguments,
			description = description,
			directives = emptyList(),
			name = name ?: error("name() missing"),
			origin = DocumentPosition(origin)
		)


		fun description(origin: IntRange, isBlock: Boolean = false, configure: () -> String) {
			description = GStringValue(
				origin = DocumentPosition(origin),
				value = configure(),
				isBlock = isBlock
			)
		}


		fun name(origin: IntRange, configure: () -> String) {
			name = GName(
				origin = DocumentPosition(origin),
				value = configure()
			)
		}
	}


	@AstBuilderDsl
	class InterfaceTypeDefinitionBuilder(private val origin: IntRange) {

		private var description: GStringValue? = null
		private val fields = mutableListOf<GFieldDefinition>()
		private val interfaces = mutableListOf<GNamedTypeRef>()
		private var name: GName? = null


		fun build() = GInterfaceType(
			description = description,
			directives = emptyList(),
			fieldDefinitions = fields,
			interfaces = interfaces,
			name = name ?: error("name() missing"),
			origin = DocumentPosition(origin)
		)


		fun description(origin: IntRange, isBlock: Boolean = false, configure: () -> String) {
			description = GStringValue(
				origin = DocumentPosition(origin),
				value = configure(),
				isBlock = isBlock
			)
		}


		fun field(origin: IntRange, configure: FieldDefinitionBuilder.() -> Unit) {
			fields += FieldDefinitionBuilder(origin).apply(configure).build()
		}


		fun inherits(origin: IntRange, configure: () -> String) {
			interfaces += GNamedTypeRef(
				name = GName(
					origin = DocumentPosition(origin),
					value = configure()
				),
				origin = DocumentPosition(origin)
			)
		}


		fun name(origin: IntRange, configure: () -> String) {
			name = GName(
				origin = DocumentPosition(origin),
				value = configure()
			)
		}
	}


	@AstBuilderDsl
	class ObjectTypeDefinitionBuilder(private val origin: IntRange) {

		private var description: GStringValue? = null
		private val fields = mutableListOf<GFieldDefinition>()
		private val interfaces = mutableListOf<GNamedTypeRef>()
		private var name: GName? = null


		fun build() = GObjectType(
			description = description,
			directives = emptyList(),
			fieldDefinitions = fields,
			interfaces = interfaces,
			name = name ?: error("name() missing"),
			origin = DocumentPosition(origin)
		)


		fun description(origin: IntRange, isBlock: Boolean = false, configure: () -> String) {
			description = GStringValue(
				origin = DocumentPosition(origin),
				value = configure(),
				isBlock = isBlock
			)
		}


		fun field(origin: IntRange, configure: FieldDefinitionBuilder.() -> Unit) {
			fields += FieldDefinitionBuilder(origin).apply(configure).build()
		}


		fun implements(origin: IntRange, configure: () -> String) {
			interfaces += GNamedTypeRef(
				name = GName(
					origin = DocumentPosition(origin),
					value = configure()
				),
				origin = DocumentPosition(origin)
			)
		}


		fun name(origin: IntRange, configure: () -> String) {
			name = GName(
				origin = DocumentPosition(origin),
				value = configure()
			)
		}
	}


	@AstBuilderDsl
	class ListTypeReferenceBuilder(private val origin: IntRange) {

		private var elementType: GTypeRef? = null


		fun build() =
			GListTypeRef(
				elementType = elementType ?: error("name(), list() or nonNull() missing"),
				origin = DocumentPosition(origin)
			)


		fun list(origin: IntRange, configure: ListTypeReferenceBuilder.() -> Unit) {
			elementType = ListTypeReferenceBuilder(origin).apply(configure).build()
		}


		fun name(origin: IntRange, configure: () -> String) {
			elementType = GNamedTypeRef(
				name = GName(
					origin = DocumentPosition(origin),
					value = configure()
				),
				origin = DocumentPosition(origin)
			)
		}


		fun nonNull(origin: IntRange, configure: NonNullTypeReferenceBuilder.() -> Unit) {
			elementType = NonNullTypeReferenceBuilder(origin).apply(configure).build()
		}
	}


	@AstBuilderDsl
	class NonNullTypeReferenceBuilder(private val origin: IntRange) {

		private var nullableType: GTypeRef? = null


		fun build() =
			GNonNullTypeRef(
				nullableRef = nullableType ?: error("name() or list() missing"),
				origin = DocumentPosition(origin)
			)


		fun list(origin: IntRange, configure: ListTypeReferenceBuilder.() -> Unit) {
			nullableType = ListTypeReferenceBuilder(origin).apply(configure).build()
		}


		fun name(origin: IntRange, configure: () -> String) {
			nullableType = GNamedTypeRef(
				name = GName(
					origin = DocumentPosition(origin),
					value = configure()
				),
				origin = DocumentPosition(origin)
			)
		}
	}


	@AstBuilderDsl
	class OperationDefinitionBuilder


	data class DocumentPosition(
		override val startPosition: Int,
		override val endPosition: Int,
		override val line: Int = -1,
		override val column: Int = -1,
	) : GDocumentPosition {

		constructor(range: IntRange) :
			this(startPosition = range.first, endPosition = range.last)


		override fun equals(other: Any?) =
			this === other || (
				other is DocumentPosition &&
					startPosition == other.startPosition &&
					endPosition == other.endPosition &&
					(column < 0 || other.column < 0 || column == other.column) &&
					(line < 0 || other.line < 0 || line == other.line)
				)


		override fun hashCode(): Int {
			var result = startPosition
			result = 31 * result + endPosition

			return result
		}


		override val source: GDocumentSource
			get() = nullSource


		override fun toString() =
			"$startPosition .. $endPosition"


		companion object {

			private val nullSource = object : GDocumentSource {

				override val content: String? get() = null
				override val name = "<test>"
			}
		}
	}


	@AstBuilderDsl
	class SchemaBuilder


	@AstBuilderDsl
	class TypeReferenceBuilder {

		private var type: GTypeRef? = null


		fun build() =
			type ?: error("name(), list() or nonNull() missing")


		fun list(origin: IntRange, configure: ListTypeReferenceBuilder.() -> Unit) {
			type = ListTypeReferenceBuilder(origin).apply(configure).build()
		}


		fun name(origin: IntRange, configure: () -> String) {
			type = GNamedTypeRef(
				name = GName(
					origin = DocumentPosition(origin),
					value = configure()
				),
				origin = DocumentPosition(origin)
			)
		}


		fun nonNull(origin: IntRange, configure: NonNullTypeReferenceBuilder.() -> Unit) {
			type = NonNullTypeReferenceBuilder(origin).apply(configure).build()
		}
	}


	@AstBuilderDsl
	class ScalarTypeDefinitionBuilder(private val origin: IntRange) {

		private var description: GStringValue? = null
		private var name: GName? = null


		fun build() = GCustomScalarType(
			description = description,
			directives = emptyList(),
			name = name ?: error("name() missing"),
			origin = DocumentPosition(origin)
		)


		fun description(origin: IntRange, isBlock: Boolean = false, configure: () -> String) {
			description = GStringValue(
				origin = DocumentPosition(origin),
				value = configure(),
				isBlock = isBlock
			)
		}


		fun name(origin: IntRange, configure: () -> String) {
			name = GName(
				origin = DocumentPosition(origin),
				value = configure()
			)
		}
	}


	@AstBuilderDsl
	class SchemaDefinitionBuilder(private val origin: IntRange) {

		private var description: GStringValue? = null
		private val operationTypeDefinitions = mutableListOf<GOperationTypeDefinition>()


		fun build() = GSchemaDefinition(
			descriptionNode = description,
			directives = emptyList(),
			operationTypeDefinitions = operationTypeDefinitions,
			origin = DocumentPosition(origin)
		)


		fun description(origin: IntRange, isBlock: Boolean = false, configure: () -> String) {
			description = GStringValue(
				origin = DocumentPosition(origin),
				value = configure(),
				isBlock = isBlock
			)
		}


		fun query(origin: IntRange, configure: QueryBuilder.() -> Unit) {
			operationTypeDefinitions += GOperationTypeDefinition(
				operationType = GOperationType.query,
				origin = DocumentPosition(origin),
				type = QueryBuilder().apply(configure).build(),
			)
		}


		class QueryBuilder {

			private var name: GName? = null


			fun build(): GNamedTypeRef {
				val name = checkNotNull(name)

				return GNamedTypeRef(name = name, origin = name.origin)
			}


			fun typeRef(origin: IntRange, configure: () -> String) {
				name = GName(value = configure(), origin = DocumentPosition(origin))
			}
		}
	}


	@AstBuilderDsl
	class UnionTypeDefinitionBuilder(private val origin: IntRange) {

		private var description: GStringValue? = null
		private val possibleTypes = mutableListOf<GNamedTypeRef>()
		private var name: GName? = null


		fun build() = GUnionType(
			description = description,
			directives = emptyList(),
			name = name ?: error("name() missing"),
			origin = DocumentPosition(origin),
			possibleTypes = possibleTypes
		)


		fun description(origin: IntRange, isBlock: Boolean = false, configure: () -> String) {
			description = GStringValue(
				origin = DocumentPosition(origin),
				value = configure(),
				isBlock = isBlock
			)
		}


		fun name(origin: IntRange, configure: () -> String) {
			name = GName(
				origin = DocumentPosition(origin),
				value = configure()
			)
		}


		fun possibleType(origin: IntRange, configure: () -> String) {
			possibleTypes += GNamedTypeRef(
				name = GName(
					origin = DocumentPosition(origin),
					value = configure()
				),
				origin = DocumentPosition(origin)
			)
		}
	}


	@AstBuilderDsl
	class ValueBuilder {

		private var value: GValue? = null


		fun build() =
			value ?: error("boolean(), or …FIXME missing")


		fun boolean(origin: IntRange, configure: () -> Boolean) {
			value = GBooleanValue(
				origin = DocumentPosition(origin),
				value = configure()
			)
		}
	}
}


@DslMarker
annotation class AstBuilderDsl
