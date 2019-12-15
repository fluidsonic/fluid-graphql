package io.fluidsonic.graphql


internal sealed class AstNode {

	abstract val sourceLocation: SourceLocation


	data class Argument(
		val name: Name,
		override val sourceLocation: SourceLocation,
		val value: Value
	) : AstNode()


	data class ArgumentDefinition(
		val defaultValue: Value?,
		val description: Value.String?,
		val directives: List<Directive>,
		val name: Name,
		override val sourceLocation: SourceLocation,
		val type: TypeReference
	) : AstNode()


	sealed class Definition : AstNode() {

		data class Fragment(
			val directives: List<Directive>,
			val name: Name,
			val selectionSet: SelectionSet,
			override val sourceLocation: SourceLocation,
			val typeCondition: TypeReference.Named
		) : Definition()


		data class Operation(
			val directives: List<Directive>,
			val name: Name?,
			val selectionSet: SelectionSet,
			override val sourceLocation: SourceLocation,
			val type: GOperationType,
			val variableDefinitions: List<VariableDefinition>
		) : Definition()


		sealed class TypeSystem : Definition() {

			data class Directive(
				val arguments: List<ArgumentDefinition>,
				val description: Value.String?,
				val isRepeatable: Boolean,
				val locations: List<Name>,
				val name: Name,
				override val sourceLocation: SourceLocation
			) : TypeSystem()


			data class Schema(
				val directives: List<AstNode.Directive>,
				val operationTypes: List<OperationTypeDefinition>,
				override val sourceLocation: SourceLocation
			) : TypeSystem()


			sealed class Type : TypeSystem() {

				abstract val description: Value.String?
				abstract val directives: List<AstNode.Directive>
				abstract val name: Name


				data class Enum(
					override val description: Value.String?,
					override val directives: List<AstNode.Directive>,
					override val name: Name,
					override val sourceLocation: SourceLocation,
					val values: List<EnumValueDefinition>
				) : Type()


				data class InputObject(
					val arguments: List<ArgumentDefinition>,
					override val description: Value.String?,
					override val directives: List<AstNode.Directive>,
					override val name: Name,
					override val sourceLocation: SourceLocation
				) : Type()


				data class Interface(
					override val description: Value.String?,
					override val directives: List<AstNode.Directive>,
					val fields: List<FieldDefinition>,
					override val name: Name,
					val interfaces: List<TypeReference.Named>,
					override val sourceLocation: SourceLocation
				) : Type()


				data class Object(
					override val description: Value.String?,
					override val directives: List<AstNode.Directive>,
					val fields: List<FieldDefinition>,
					val interfaces: List<TypeReference.Named>,
					override val name: Name,
					override val sourceLocation: SourceLocation
				) : Type()


				data class Scalar(
					override val description: Value.String?,
					override val directives: List<AstNode.Directive>,
					override val name: Name,
					override val sourceLocation: SourceLocation
				) : Type()


				data class Union(
					override val description: Value.String?,
					override val directives: List<AstNode.Directive>,
					override val name: Name,
					override val sourceLocation: SourceLocation,
					val types: List<TypeReference.Named>
				) : Type()
			}
		}


		sealed class TypeSystemExtension : Definition() {

			data class Schema(
				val directives: List<Directive>,
				val operationTypes: List<OperationTypeDefinition>,
				override val sourceLocation: SourceLocation
			) : TypeSystemExtension()


			sealed class Type : TypeSystemExtension() {

				abstract val directives: List<Directive>
				abstract val name: Name


				data class Enum(
					override val directives: List<Directive>,
					override val name: Name,
					override val sourceLocation: SourceLocation,
					val values: List<EnumValueDefinition>
				) : Type()


				data class InputObject(
					val arguments: List<ArgumentDefinition>,
					override val directives: List<Directive>,
					override val name: Name,
					override val sourceLocation: SourceLocation
				) : Type()


				data class Interface(
					override val directives: List<Directive>,
					val fields: List<FieldDefinition>,
					val interfaces: List<TypeReference.Named>,
					override val name: Name,
					override val sourceLocation: SourceLocation
				) : Type()


				data class Object(
					override val directives: List<Directive>,
					val fields: List<FieldDefinition>,
					val interfaces: List<TypeReference.Named>,
					override val name: Name,
					override val sourceLocation: SourceLocation
				) : Type()


				data class Scalar(
					override val directives: List<Directive>,
					override val name: Name,
					override val sourceLocation: SourceLocation
				) : Type()


				data class Union(
					override val directives: List<Directive>,
					override val name: Name,
					override val sourceLocation: SourceLocation,
					val types: List<TypeReference.Named>
				) : Type()
			}
		}
	}


	data class Directive(
		val arguments: List<Argument>,
		val name: Name,
		override val sourceLocation: SourceLocation
	) : AstNode()


	data class Document(
		val definitions: List<Definition>,
		override val sourceLocation: SourceLocation
	) : AstNode()


	data class EnumValueDefinition(
		val description: Value.String?,
		val directives: List<Directive>,
		val name: Name,
		override val sourceLocation: SourceLocation
	) : AstNode()


	data class FieldDefinition(
		val arguments: List<ArgumentDefinition>,
		val description: Value.String?,
		val directives: List<Directive>,
		val name: Name,
		override val sourceLocation: SourceLocation,
		val type: TypeReference
	) : AstNode()


	data class Name(
		override val sourceLocation: SourceLocation,
		val value: String
	) : AstNode()


	data class OperationTypeDefinition(
		val operation: GOperationType,
		override val sourceLocation: SourceLocation,
		val type: TypeReference.Named
	) : AstNode()


	sealed class Selection : AstNode() {

		data class Field(
			val alias: Name?,
			val arguments: List<Argument>,
			val directives: List<Directive>,
			val name: Name,
			val selectionSet: SelectionSet?,
			override val sourceLocation: SourceLocation
		) : Selection()


		data class InlineFragment(
			val directives: List<Directive>,
			val selectionSet: SelectionSet,
			override val sourceLocation: SourceLocation,
			val typeCondition: TypeReference.Named?
		) : Selection()


		data class FragmentSpread(
			val directives: List<Directive>,
			val name: Name,
			override val sourceLocation: SourceLocation
		) : Selection()
	}


	data class SelectionSet(
		val selections: List<Selection>,
		override val sourceLocation: SourceLocation
	) : AstNode()


	sealed class TypeReference : AstNode() {

		data class List(
			val elementType: TypeReference,
			override val sourceLocation: SourceLocation
		) : TypeReference()


		data class Named(
			val name: Name,
			override val sourceLocation: SourceLocation
		) : TypeReference()


		data class NonNull(
			val nullableType: TypeReference,
			override val sourceLocation: SourceLocation
		) : TypeReference()
	}


	sealed class Value : AstNode() {

		data class Boolean(
			override val sourceLocation: SourceLocation,
			val value: kotlin.Boolean
		) : Value()


		data class Enum(
			val name: kotlin.String,
			override val sourceLocation: SourceLocation
		) : Value()


		data class Float(
			override val sourceLocation: SourceLocation,
			val value: kotlin.String
		) : Value()


		data class Int(
			override val sourceLocation: SourceLocation,
			val value: kotlin.String
		) : Value()


		data class List(
			override val sourceLocation: SourceLocation,
			val elements: kotlin.collections.List<Value>
		) : Value()


		data class Null(
			override val sourceLocation: SourceLocation
		) : Value()


		data class Object(
			val fields: kotlin.collections.List<Field>,
			override val sourceLocation: SourceLocation
		) : Value() {

			data class Field(
				val name: Name,
				override val sourceLocation: SourceLocation,
				val value: Value
			) : AstNode()
		}


		data class String(
			val isBlock: kotlin.Boolean,
			override val sourceLocation: SourceLocation,
			val value: kotlin.String
		) : Value()


		data class Variable(
			val name: Name,
			override val sourceLocation: SourceLocation
		) : Value()
	}


	data class VariableDefinition(
		val defaultValue: Value?,
		val directives: List<Directive>,
		override val sourceLocation: SourceLocation,
		val type: TypeReference,
		val variable: Value.Variable
	) : AstNode()
}
