package tests

import io.fluidsonic.graphql.*
import io.fluidsonic.graphql.GAst.*


fun <T : GAst> ast(configure: AstBuilder.() -> T) =
	AstBuilder.run(configure)


fun documentAst(origin: IntRange, configure: AstBuilder.DocumentBuilder.() -> Unit) =
	AstBuilder.DocumentBuilder(origin).apply(configure).build()


@AstBuilderDsl
object AstBuilder {

	fun document(origin: IntRange, configure: DocumentBuilder.() -> Unit) =
		DocumentBuilder(origin).apply(configure).build()


//	fun schema(configure: SchemaBuilder.() -> Unit) =
//		SchemaBuilder().apply(configure).build()


	@AstBuilderDsl
	class ArgumentDefinitionBuilder(private val origin: IntRange) {

		private var defaultValue: Value? = null
		private var name: Name? = null
		private var type: TypeReference? = null


		fun build() = ArgumentDefinition(
			description = null,
			defaultValue = defaultValue,
			directives = emptyList(),
			name = name ?: error("name() missing"),
			origin = Origin(origin),
			type = type ?: error("type() missing")
		)


		fun defaultValue(configure: ValueBuilder.() -> Unit) {
			defaultValue = ValueBuilder().apply(configure).build()
		}


		fun name(origin: IntRange, configure: () -> String) {
			name = Name(
				origin = Origin(origin),
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
		private val locations = mutableListOf<Name>()
		private var name: Name? = null


		fun build() = Definition.TypeSystem.Directive(
			arguments = emptyList(),
			description = null,
			isRepeatable = isRepeatable,
			locations = locations,
			name = name ?: error("name() missing"),
			origin = Origin(origin)
		)


		fun location(origin: IntRange, configure: () -> String) {
			locations += Name(
				origin = Origin(origin),
				value = configure()
			)
		}


		fun name(origin: IntRange, configure: () -> String) {
			name = Name(
				origin = Origin(origin),
				value = configure()
			)
		}


		fun repeatable() {
			isRepeatable = true
		}
	}


	@AstBuilderDsl
	class DocumentBuilder(private val origin: IntRange) {

		private val definitions = mutableListOf<Definition>()


		fun build() = Document(
			definitions = definitions,
			origin = Origin(origin)
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


		fun unionTypeDefinition(origin: IntRange, configure: UnionTypeDefinitionBuilder.() -> Unit) {
			definitions += UnionTypeDefinitionBuilder(origin).apply(configure).build()
		}
	}


	@AstBuilderDsl
	class EnumTypeDefinitionBuilder(private val origin: IntRange) {

		private var description: Value.String? = null
		private val values = mutableListOf<EnumValueDefinition>()
		private var name: Name? = null


		fun build() = Definition.TypeSystem.Type.Enum(
			description = description,
			directives = emptyList(),
			name = name ?: error("name() missing"),
			origin = Origin(origin),
			values = values
		)


		fun description(origin: IntRange, isBlock: Boolean = false, configure: () -> String) {
			description = Value.String(
				origin = Origin(origin),
				value = configure(),
				isBlock = isBlock
			)
		}


		fun name(origin: IntRange, configure: () -> String) {
			name = Name(
				origin = Origin(origin),
				value = configure()
			)
		}


		fun value(origin: IntRange, configure: () -> String) {
			values += EnumValueDefinition(
				description = null,
				directives = emptyList(),
				name = Name(
					origin = Origin(origin),
					value = configure()
				),
				origin = Origin(origin)
			)
		}
	}


	@AstBuilderDsl
	class FieldDefinitionBuilder(private val origin: IntRange) {

		private val arguments = mutableListOf<ArgumentDefinition>()
		private var name: Name? = null
		private var type: TypeReference? = null


		fun build() = FieldDefinition(
			arguments = arguments,
			description = null,
			directives = emptyList(),
			name = name ?: error("name() missing"),
			origin = Origin(origin),
			type = type ?: error("type() missing")
		)


		fun argument(origin: IntRange, configure: ArgumentDefinitionBuilder.() -> Unit) {
			arguments += ArgumentDefinitionBuilder(origin).apply(configure).build()
		}


		fun name(origin: IntRange, configure: () -> String) {
			name = Name(
				origin = Origin(origin),
				value = configure()
			)
		}


		fun type(configure: TypeReferenceBuilder.() -> Unit) {
			type = TypeReferenceBuilder().apply(configure).build()
		}
	}


	@AstBuilderDsl
	class FragmentDefinitionBuilder {


	}


	@AstBuilderDsl
	class InputObjectTypeDefinitionBuilder(private val origin: IntRange) {

		private val arguments = mutableListOf<ArgumentDefinition>()
		private var description: Value.String? = null
		private var name: Name? = null


		fun argument(origin: IntRange, configure: ArgumentDefinitionBuilder.() -> Unit) {
			arguments += ArgumentDefinitionBuilder(origin).apply(configure).build()
		}


		fun build() = Definition.TypeSystem.Type.InputObject(
			arguments = arguments,
			description = description,
			directives = emptyList(),
			name = name ?: error("name() missing"),
			origin = Origin(origin)
		)


		fun description(origin: IntRange, isBlock: Boolean = false, configure: () -> String) {
			description = Value.String(
				origin = Origin(origin),
				value = configure(),
				isBlock = isBlock
			)
		}


		fun name(origin: IntRange, configure: () -> String) {
			name = Name(
				origin = Origin(origin),
				value = configure()
			)
		}
	}


	@AstBuilderDsl
	class InterfaceTypeDefinitionBuilder(private val origin: IntRange) {

		private var description: Value.String? = null
		private val fields = mutableListOf<FieldDefinition>()
		private val interfaces = mutableListOf<TypeReference.Named>()
		private var name: Name? = null


		fun build() = Definition.TypeSystem.Type.Interface(
			description = description,
			directives = emptyList(),
			fields = fields,
			interfaces = interfaces,
			name = name ?: error("name() missing"),
			origin = Origin(origin)
		)


		fun description(origin: IntRange, isBlock: Boolean = false, configure: () -> String) {
			description = Value.String(
				origin = Origin(origin),
				value = configure(),
				isBlock = isBlock
			)
		}


		fun field(origin: IntRange, configure: FieldDefinitionBuilder.() -> Unit) {
			fields += FieldDefinitionBuilder(origin).apply(configure).build()
		}


		fun inherits(origin: IntRange, configure: () -> String) {
			interfaces += TypeReference.Named(
				name = Name(
					origin = Origin(origin),
					value = configure()
				),
				origin = Origin(origin)
			)
		}


		fun name(origin: IntRange, configure: () -> String) {
			name = Name(
				origin = Origin(origin),
				value = configure()
			)
		}
	}


	@AstBuilderDsl
	class ObjectTypeDefinitionBuilder(private val origin: IntRange) {

		private var description: Value.String? = null
		private val fields = mutableListOf<FieldDefinition>()
		private val interfaces = mutableListOf<TypeReference.Named>()
		private var name: Name? = null


		fun build() = Definition.TypeSystem.Type.Object(
			description = description,
			directives = emptyList(),
			fields = fields,
			interfaces = interfaces,
			name = name ?: error("name() missing"),
			origin = Origin(origin)
		)


		fun description(origin: IntRange, isBlock: Boolean = false, configure: () -> String) {
			description = Value.String(
				origin = Origin(origin),
				value = configure(),
				isBlock = isBlock
			)
		}


		fun field(origin: IntRange, configure: FieldDefinitionBuilder.() -> Unit) {
			fields += FieldDefinitionBuilder(origin).apply(configure).build()
		}


		fun implements(origin: IntRange, configure: () -> String) {
			interfaces += TypeReference.Named(
				name = Name(
					origin = Origin(origin),
					value = configure()
				),
				origin = Origin(origin)
			)
		}


		fun name(origin: IntRange, configure: () -> String) {
			name = Name(
				origin = Origin(origin),
				value = configure()
			)
		}
	}


	@AstBuilderDsl
	class ListTypeReferenceBuilder(private val origin: IntRange) {

		private var elementType: TypeReference? = null


		fun build() =
			TypeReference.List(
				elementType = elementType ?: error("name(), list() or nonNull() missing"),
				origin = Origin(origin)
			)


		fun list(origin: IntRange, configure: ListTypeReferenceBuilder.() -> Unit) {
			elementType = ListTypeReferenceBuilder(origin).apply(configure).build()
		}


		fun name(origin: IntRange, configure: () -> String) {
			elementType = TypeReference.Named(
				name = Name(
					origin = Origin(origin),
					value = configure()
				),
				origin = Origin(origin)
			)
		}


		fun nonNull(origin: IntRange, configure: NonNullTypeReferenceBuilder.() -> Unit) {
			elementType = NonNullTypeReferenceBuilder(origin).apply(configure).build()
		}
	}


	@AstBuilderDsl
	class NonNullTypeReferenceBuilder(private val origin: IntRange) {

		private var nullableType: TypeReference? = null


		fun build() =
			TypeReference.NonNull(
				nullableType = nullableType ?: error("name() or list() missing"),
				origin = Origin(origin)
			)


		fun list(origin: IntRange, configure: ListTypeReferenceBuilder.() -> Unit) {
			nullableType = ListTypeReferenceBuilder(origin).apply(configure).build()
		}


		fun name(origin: IntRange, configure: () -> String) {
			nullableType = TypeReference.Named(
				name = Name(
					origin = Origin(origin),
					value = configure()
				),
				origin = Origin(origin)
			)
		}
	}


	@AstBuilderDsl
	class OperationDefinitionBuilder {


	}


	data class Origin(
		override val startPosition: Int,
		override val endPosition: Int,
		override val line: Int = -1,
		override val column: Int = -1
	) : GOrigin {

		constructor(range: IntRange) :
			this(startPosition = range.first, endPosition = range.last)


		override val source: GSource
			get() = nullSource


		override fun toString() =
			"$startPosition .. $endPosition"


		companion object {

			private val nullSource = object : GSource {

				override val content: String? get() = null
				override val name = "<test>"
			}
		}
	}


	@AstBuilderDsl
	class SchemaBuilder {

	}


	@AstBuilderDsl
	class TypeReferenceBuilder {

		private var type: TypeReference? = null


		fun build() =
			type ?: error("name(), list() or nonNull() missing")


		fun list(origin: IntRange, configure: ListTypeReferenceBuilder.() -> Unit) {
			type = ListTypeReferenceBuilder(origin).apply(configure).build()
		}


		fun name(origin: IntRange, configure: () -> String) {
			type = TypeReference.Named(
				name = Name(
					origin = Origin(origin),
					value = configure()
				),
				origin = Origin(origin)
			)
		}


		fun nonNull(origin: IntRange, configure: NonNullTypeReferenceBuilder.() -> Unit) {
			type = NonNullTypeReferenceBuilder(origin).apply(configure).build()
		}
	}


	@AstBuilderDsl
	class ScalarTypeDefinitionBuilder(private val origin: IntRange) {

		private var description: Value.String? = null
		private var name: Name? = null


		fun build() = Definition.TypeSystem.Type.Scalar(
			description = description,
			directives = emptyList(),
			name = name ?: error("name() missing"),
			origin = Origin(origin)
		)


		fun description(origin: IntRange, isBlock: Boolean = false, configure: () -> String) {
			description = Value.String(
				origin = Origin(origin),
				value = configure(),
				isBlock = isBlock
			)
		}


		fun name(origin: IntRange, configure: () -> String) {
			name = Name(
				origin = Origin(origin),
				value = configure()
			)
		}
	}


	@AstBuilderDsl
	class UnionTypeDefinitionBuilder(private val origin: IntRange) {

		private var description: Value.String? = null
		private val possibleTypes = mutableListOf<TypeReference.Named>()
		private var name: Name? = null


		fun build() = Definition.TypeSystem.Type.Union(
			description = description,
			directives = emptyList(),
			name = name ?: error("name() missing"),
			origin = Origin(origin),
			types = possibleTypes
		)


		fun description(origin: IntRange, isBlock: Boolean = false, configure: () -> String) {
			description = Value.String(
				origin = Origin(origin),
				value = configure(),
				isBlock = isBlock
			)
		}


		fun name(origin: IntRange, configure: () -> String) {
			name = Name(
				origin = Origin(origin),
				value = configure()
			)
		}


		fun possibleType(origin: IntRange, configure: () -> String) {
			possibleTypes += TypeReference.Named(
				name = Name(
					origin = Origin(origin),
					value = configure()
				),
				origin = Origin(origin)
			)
		}
	}


	@AstBuilderDsl
	class ValueBuilder {

		private var value: Value? = null


		fun build() =
			value ?: error("boolean(), or …FIXME missing")


		fun boolean(origin: IntRange, configure: () -> Boolean) {
			value = Value.Boolean(
				origin = Origin(origin),
				value = configure()
			)
		}
	}
}


@DslMarker
annotation class AstBuilderDsl
