package io.fluidsonic.graphql


// TODO add acceptChildren
sealed class GAst {

	abstract val origin: GOrigin


	abstract fun <Result, Data> accept(visitor: GAstVisitor<Result, Data>, data: Data): Result


	fun accept(visitor: GAstVoidVisitor) =
		accept(visitor, data = null)


	@Suppress("UNCHECKED_CAST")
	fun <Node : GAst, Data> transform(visitor: GAstTransformer<Data>, data: Data) =
		accept(visitor, data) as Node


	companion object {

		fun parseDocument(source: GSource.Parsable) =
			Parser.parseDocument(source)


		fun parseDocument(content: String, name: String = "<document>") =
			parseDocument(GSource.of(content = content, name = name))


		fun parseTypeReference(source: GSource.Parsable) =
			Parser.parseTypeReference(source)


		fun parseTypeReference(content: String, name: String = "<type reference>") =
			parseTypeReference(GSource.of(content = content, name = name))


		fun parseValue(source: GSource.Parsable) =
			Parser.parseValue(source)


		fun parseValue(content: String, name: String = "<value>") =
			parseValue(GSource.of(content = content, name = name))


		fun print(ast: GAst, indent: String = "\t") =
			Printer.print(ast = ast, indent = indent)
	}


	data class Argument(
		val name: Name,
		override val origin: GOrigin,
		val value: Value
	) : GAst() {

		override fun <Result, Data> accept(visitor: GAstVisitor<Result, Data>, data: Data) =
			visitor.visitArgument(this, data)
	}


	data class ArgumentDefinition(
		val defaultValue: Value?,
		val description: Value.String?,
		val directives: List<Directive>,
		val name: Name,
		override val origin: GOrigin,
		val type: TypeReference
	) : GAst() {

		override fun <Result, Data> accept(visitor: GAstVisitor<Result, Data>, data: Data) =
			visitor.visitArgumentDefinition(this, data)
	}


	sealed class Definition : GAst() {

		data class Fragment(
			val directives: List<Directive>,
			val name: Name,
			override val origin: GOrigin,
			val selectionSet: SelectionSet,
			val typeCondition: TypeReference.Named,
			val variableDefinitions: List<VariableDefinition>
		) : Definition() {

			override fun <Result, Data> accept(visitor: GAstVisitor<Result, Data>, data: Data) =
				visitor.visitFragmentDefinition(this, data)
		}


		data class Operation(
			val directives: List<Directive>,
			val name: Name?,
			override val origin: GOrigin,
			val selectionSet: SelectionSet,
			val type: GOperationType,
			val variableDefinitions: List<VariableDefinition>
		) : Definition() {

			override fun <Result, Data> accept(visitor: GAstVisitor<Result, Data>, data: Data) =
				visitor.visitOperationDefinition(this, data)
		}


		sealed class TypeSystem : Definition() {

			data class Directive(
				val arguments: List<ArgumentDefinition>,
				val description: Value.String?,
				val isRepeatable: Boolean,
				val locations: List<Name>,
				val name: Name,
				override val origin: GOrigin
			) : TypeSystem() {

				override fun <Result, Data> accept(visitor: GAstVisitor<Result, Data>, data: Data) =
					visitor.visitDirectiveDefinition(this, data)
			}


			data class Schema(
				val directives: List<GAst.Directive>,
				val operationTypes: List<OperationTypeDefinition>,
				override val origin: GOrigin
			) : TypeSystem() {

				override fun <Result, Data> accept(visitor: GAstVisitor<Result, Data>, data: Data) =
					visitor.visitSchemaDefinition(this, data)
			}


			sealed class Type : TypeSystem() {

				abstract val description: Value.String?
				abstract val directives: List<GAst.Directive>
				abstract val name: Name


				data class Enum(
					override val description: Value.String?,
					override val directives: List<GAst.Directive>,
					override val name: Name,
					override val origin: GOrigin,
					val values: List<EnumValueDefinition>
				) : Type() {

					override fun <Result, Data> accept(visitor: GAstVisitor<Result, Data>, data: Data) =
						visitor.visitEnumTypeDefinition(this, data)
				}


				data class InputObject(
					val arguments: List<ArgumentDefinition>,
					override val description: Value.String?,
					override val directives: List<GAst.Directive>,
					override val name: Name,
					override val origin: GOrigin
				) : Type() {

					override fun <Result, Data> accept(visitor: GAstVisitor<Result, Data>, data: Data) =
						visitor.visitInputObjectTypeDefinition(this, data)
				}


				data class Interface(
					override val description: Value.String?,
					override val directives: List<GAst.Directive>,
					val fields: List<FieldDefinition>,
					val interfaces: List<TypeReference.Named>,
					override val name: Name,
					override val origin: GOrigin
				) : Type() {

					override fun <Result, Data> accept(visitor: GAstVisitor<Result, Data>, data: Data) =
						visitor.visitInterfaceTypeDefinition(this, data)
				}


				data class Object(
					override val description: Value.String?,
					override val directives: List<GAst.Directive>,
					val fields: List<FieldDefinition>,
					val interfaces: List<TypeReference.Named>,
					override val name: Name,
					override val origin: GOrigin
				) : Type() {

					override fun <Result, Data> accept(visitor: GAstVisitor<Result, Data>, data: Data) =
						visitor.visitObjectTypeDefinition(this, data)
				}


				data class Scalar(
					override val description: Value.String?,
					override val directives: List<GAst.Directive>,
					override val name: Name,
					override val origin: GOrigin
				) : Type() {

					override fun <Result, Data> accept(visitor: GAstVisitor<Result, Data>, data: Data) =
						visitor.visitScalarTypeDefinition(this, data)
				}


				data class Union(
					override val description: Value.String?,
					override val directives: List<GAst.Directive>,
					override val name: Name,
					override val origin: GOrigin,
					val types: List<TypeReference.Named>
				) : Type() {

					override fun <Result, Data> accept(visitor: GAstVisitor<Result, Data>, data: Data) =
						visitor.visitUnionTypeDefinition(this, data)
				}
			}
		}


		sealed class TypeSystemExtension : Definition() {

			data class Schema(
				val directives: List<Directive>,
				val operationTypes: List<OperationTypeDefinition>,
				override val origin: GOrigin
			) : TypeSystemExtension() {

				override fun <Result, Data> accept(visitor: GAstVisitor<Result, Data>, data: Data) =
					visitor.visitSchemaExtension(this, data)
			}


			sealed class Type : TypeSystemExtension() {

				abstract val directives: List<Directive>
				abstract val name: Name


				data class Enum(
					override val directives: List<Directive>,
					override val name: Name,
					override val origin: GOrigin,
					val values: List<EnumValueDefinition>
				) : Type() {

					override fun <Result, Data> accept(visitor: GAstVisitor<Result, Data>, data: Data) =
						visitor.visitEnumTypeExtension(this, data)
				}


				data class InputObject(
					val arguments: List<ArgumentDefinition>,
					override val directives: List<Directive>,
					override val name: Name,
					override val origin: GOrigin
				) : Type() {

					override fun <Result, Data> accept(visitor: GAstVisitor<Result, Data>, data: Data) =
						visitor.visitInputObjectTypeExtension(this, data)
				}


				data class Interface(
					override val directives: List<Directive>,
					val fields: List<FieldDefinition>,
					val interfaces: List<TypeReference.Named>,
					override val name: Name,
					override val origin: GOrigin
				) : Type() {

					override fun <Result, Data> accept(visitor: GAstVisitor<Result, Data>, data: Data) =
						visitor.visitInterfaceTypeExtension(this, data)
				}


				data class Object(
					override val directives: List<Directive>,
					val fields: List<FieldDefinition>,
					val interfaces: List<TypeReference.Named>,
					override val name: Name,
					override val origin: GOrigin
				) : Type() {

					override fun <Result, Data> accept(visitor: GAstVisitor<Result, Data>, data: Data) =
						visitor.visitObjectTypeExtension(this, data)
				}


				data class Scalar(
					override val directives: List<Directive>,
					override val name: Name,
					override val origin: GOrigin
				) : Type() {

					override fun <Result, Data> accept(visitor: GAstVisitor<Result, Data>, data: Data) =
						visitor.visitScalarTypeExtension(this, data)
				}


				data class Union(
					override val directives: List<Directive>,
					override val name: Name,
					override val origin: GOrigin,
					val types: List<TypeReference.Named>
				) : Type() {

					override fun <Result, Data> accept(visitor: GAstVisitor<Result, Data>, data: Data) =
						visitor.visitUnionTypeExtension(this, data)
				}
			}
		}
	}


	data class Directive(
		val arguments: List<Argument>,
		val name: Name,
		override val origin: GOrigin
	) : GAst() {

		override fun <Result, Data> accept(visitor: GAstVisitor<Result, Data>, data: Data) =
			visitor.visitDirective(this, data)
	}


	data class Document(
		val definitions: List<Definition>,
		override val origin: GOrigin
	) : GAst() {

		override fun <Result, Data> accept(visitor: GAstVisitor<Result, Data>, data: Data) =
			visitor.visitDocument(this, data)
	}


	data class EnumValueDefinition(
		val description: Value.String?,
		val directives: List<Directive>,
		val name: Name,
		override val origin: GOrigin
	) : GAst() {

		override fun <Result, Data> accept(visitor: GAstVisitor<Result, Data>, data: Data) =
			visitor.visitEnumValueDefinition(this, data)
	}


	data class FieldDefinition(
		val arguments: List<ArgumentDefinition>,
		val description: Value.String?,
		val directives: List<Directive>,
		val name: Name,
		override val origin: GOrigin,
		val type: TypeReference
	) : GAst() {

		override fun <Result, Data> accept(visitor: GAstVisitor<Result, Data>, data: Data) =
			visitor.visitFieldDefinition(this, data)
	}


	data class Name(
		override val origin: GOrigin,
		val value: String
	) : GAst() {

		override fun <Result, Data> accept(visitor: GAstVisitor<Result, Data>, data: Data) =
			visitor.visitName(this, data)
	}


	data class OperationTypeDefinition(
		val operation: GOperationType,
		override val origin: GOrigin,
		val type: TypeReference.Named
	) : GAst() {

		override fun <Result, Data> accept(visitor: GAstVisitor<Result, Data>, data: Data) =
			visitor.visitOperationTypeDefinition(this, data)
	}


	sealed class Selection : GAst() {

		data class Field(
			val alias: Name?,
			val arguments: List<Argument>,
			val directives: List<Directive>,
			val name: Name,
			override val origin: GOrigin,
			val selectionSet: SelectionSet?
		) : Selection() {

			override fun <Result, Data> accept(visitor: GAstVisitor<Result, Data>, data: Data) =
				visitor.visitFieldSelection(this, data)
		}


		data class InlineFragment(
			val directives: List<Directive>,
			override val origin: GOrigin,
			val selectionSet: SelectionSet,
			val typeCondition: TypeReference.Named?
		) : Selection() {

			override fun <Result, Data> accept(visitor: GAstVisitor<Result, Data>, data: Data) =
				visitor.visitInlineFragmentSelection(this, data)
		}


		data class Fragment(
			val directives: List<Directive>,
			val name: Name,
			override val origin: GOrigin
		) : Selection() {

			override fun <Result, Data> accept(visitor: GAstVisitor<Result, Data>, data: Data) =
				visitor.visitFragmentSelection(this, data)
		}
	}


	data class SelectionSet(
		override val origin: GOrigin,
		val selections: List<Selection>
	) : GAst() {

		override fun <Result, Data> accept(visitor: GAstVisitor<Result, Data>, data: Data) =
			visitor.visitSelectionSet(this, data)
	}


	sealed class TypeReference : GAst() {

		data class List(
			val elementType: TypeReference,
			override val origin: GOrigin
		) : TypeReference() {

			override fun <Result, Data> accept(visitor: GAstVisitor<Result, Data>, data: Data) =
				visitor.visitListTypeReference(this, data)
		}


		data class Named(
			val name: Name,
			override val origin: GOrigin
		) : TypeReference() {

			override fun <Result, Data> accept(visitor: GAstVisitor<Result, Data>, data: Data) =
				visitor.visitNamedTypeReference(this, data)
		}


		data class NonNull(
			val nullableType: TypeReference,
			override val origin: GOrigin
		) : TypeReference() {

			override fun <Result, Data> accept(visitor: GAstVisitor<Result, Data>, data: Data) =
				visitor.visitNonNullTypeReference(this, data)
		}
	}


	sealed class Value : GAst() {

		// FIXME too naive (what about enum values, variables and type conversions?)
		abstract fun toKotlin(): Any?


		data class Boolean(
			override val origin: GOrigin,
			val value: kotlin.Boolean
		) : Value() {

			override fun <Result, Data> accept(visitor: GAstVisitor<Result, Data>, data: Data) =
				visitor.visitBooleanValue(this, data)


			override fun toKotlin() =
				value
		}


		data class Enum(
			val name: kotlin.String,
			override val origin: GOrigin
		) : Value() {

			override fun <Result, Data> accept(visitor: GAstVisitor<Result, Data>, data: Data) =
				visitor.visitEnumValue(this, data)


			override fun toKotlin() =
				name
		}


		data class Float(
			override val origin: GOrigin,
			val value: kotlin.String
		) : Value() {

			override fun <Result, Data> accept(visitor: GAstVisitor<Result, Data>, data: Data) =
				visitor.visitFloatValue(this, data)


			override fun toKotlin() =
				value
		}


		data class Int(
			override val origin: GOrigin,
			val value: kotlin.String
		) : Value() {

			override fun <Result, Data> accept(visitor: GAstVisitor<Result, Data>, data: Data) =
				visitor.visitIntValue(this, data)


			override fun toKotlin() =
				value
		}


		data class List(
			override val origin: GOrigin,
			val elements: kotlin.collections.List<Value>
		) : Value() {

			override fun <Result, Data> accept(visitor: GAstVisitor<Result, Data>, data: Data) =
				visitor.visitListValue(this, data)


			override fun toKotlin() =
				elements.map(Value::toKotlin)
		}


		data class Null(
			override val origin: GOrigin
		) : Value() {

			override fun <Result, Data> accept(visitor: GAstVisitor<Result, Data>, data: Data) =
				visitor.visitNullValue(this, data)


			override fun toKotlin(): Nothing? =
				null
		}


		data class Object(
			val fields: kotlin.collections.List<Field>,
			override val origin: GOrigin
		) : Value() {

			override fun <Result, Data> accept(visitor: GAstVisitor<Result, Data>, data: Data) =
				visitor.visitObjectValue(this, data)


			override fun toKotlin() =
				fields.associate { field -> field.name to field.value.toKotlin() }


			data class Field(
				val name: Name,
				override val origin: GOrigin,
				val value: Value
			) : GAst() {

				override fun <Result, Data> accept(visitor: GAstVisitor<Result, Data>, data: Data) =
					visitor.visitObjectValueField(this, data)
			}
		}


		data class String(
			val isBlock: kotlin.Boolean,
			override val origin: GOrigin,
			val value: kotlin.String
		) : Value() {

			override fun <Result, Data> accept(visitor: GAstVisitor<Result, Data>, data: Data) =
				visitor.visitStringValue(this, data)


			override fun toKotlin() =
				value
		}


		data class Variable(
			val name: Name,
			override val origin: GOrigin
		) : Value() {

			override fun <Result, Data> accept(visitor: GAstVisitor<Result, Data>, data: Data) =
				visitor.visitVariableValue(this, data)


			override fun toKotlin() =
				"$$name"
		}
	}


	data class VariableDefinition(
		val defaultValue: Value?,
		val directives: List<Directive>,
		override val origin: GOrigin,
		val type: TypeReference,
		val variable: Value.Variable
	) : GAst() {

		override fun <Result, Data> accept(visitor: GAstVisitor<Result, Data>, data: Data) =
			visitor.visitVariableDefinition(this, data)
	}
}
