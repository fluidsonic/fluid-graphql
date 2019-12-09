package io.fluidsonic.graphql


object GQLInput {

	class Argument(
		val name: String,
		val value: GValue
	) {

		companion object
	}


	class Directive(
		val name: String,
		val arguments: List<Argument>
	) {

		companion object
	}


	class DirectiveDefinition(
		val name: String,
		val arguments: List<InputValue>,
		val locations: List<GDirectiveLocation>,
		val description: String? = null
	) {

		companion object
	}


	class EnumValue(
		val name: String,
		val description: String? = null,
		val directives: List<Directive> = emptyList(),
		val isDeprecated: Boolean = false,
		val deprecationReason: String? = null
	) {

		companion object
	}


	class Field(
		val name: String,
		val type: GTypeRef,
		val args: List<InputValue>,
		val description: String? = null,
		val directives: List<Directive> = emptyList(),
		val isDeprecated: Boolean = false,
		val deprecationReason: String? = null
	) {

		companion object
	}


	class InputValue(
		val name: String,
		val type: GTypeRef,
		val defaultValue: GValue? = null,
		val description: String? = null,
		val directives: List<Directive> = emptyList()
	) {

		companion object
	}


	data class Schema(
		val types: List<Type>,
		val queryType: String? = null,
		val mutationType: String? = null,
		val subscriptionType: String? = null,
		val directives: List<DirectiveDefinition> = emptyList()
	) {

		companion object
	}


	sealed class Type {

		abstract val directives: List<Directive>
		abstract val name: String


		companion object;


		data class Enum(
			override val name: String,
			val values: List<EnumValue>,
			val description: String? = null,
			override val directives: List<Directive> = emptyList()
		) : Type() {

			companion object
		}


		data class InputObject(
			override val name: String,
			val fields: List<InputValue>,
			val description: String? = null,
			override val directives: List<Directive> = emptyList()
		) : Type() {

			companion object
		}


		data class Interface(
			override val name: String,
			val fields: List<Field>,
			val description: String? = null,
			override val directives: List<Directive> = emptyList()
		) : Type() {

			companion object
		}


		data class Object(
			override val name: String,
			val fields: List<Field>,
			val interfaces: List<GTypeRef> = emptyList(),
			val description: String? = null,
			override val directives: List<Directive> = emptyList()
		) : Type() {

			companion object
		}


		data class Scalar(
			override val name: String,
			val description: String? = null,
			override val directives: List<Directive> = emptyList()
		) : Type() {

			companion object
		}


		data class Union(
			override val name: String,
			val possibleTypes: List<GTypeRef>,
			val description: String? = null,
			override val directives: List<Directive> = emptyList()
		) : Type() {

			companion object
		}
	}
}
