package io.fluidsonic.graphql


internal abstract class ValidationRule : Visitor.Hierarchical<Unit, ValidationContext>() {

	override fun onAny(node: GNode, data: ValidationContext, visit: Visit) =
		Unit


	abstract class Factory(private val create: () -> ValidationRule) : Provider {

		override fun provide() =
			create()
	}


	interface Provider {

		fun provide(): ValidationRule
	}


	abstract class Singleton : ValidationRule(), Provider {

		@Deprecated(message = "Don't call this.", level = DeprecationLevel.HIDDEN)
		override fun provide() =
			this
	}
}
