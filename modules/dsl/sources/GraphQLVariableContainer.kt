package io.fluidsonic.graphql

import io.fluidsonic.graphql.GraphQLVariableContainer.*
import kotlin.properties.*
import kotlin.reflect.*


@GraphQLMarker
public sealed interface GraphQLVariableContainer : GraphQLVariableContainerScope {

	@GraphQLMarker
	public fun variable(definition: GVariableDefinition): GVariableRef


	@GraphQLMarker
	public class RefFactory internal constructor(
		private val register: (factory: RefFactory, name: String) -> GVariableRef,
	) : PropertyDelegateProvider<Nothing?, ReadOnlyProperty<Nothing?, GVariableRef>> {

		private val notDelegatedError: Throwable = IllegalStateException("This variable() overload must be delegated to using 'by'.")


		internal fun throwNotDelegatedError(): Nothing {
			throw notDelegatedError
		}


		@GraphQLMarker
		override fun provideDelegate(thisRef: Nothing?, property: KProperty<*>): ReadOnlyProperty<Nothing?, GVariableRef> {
			val ref = register(this, property.name)

			return ReadOnlyProperty { _, _ -> ref }
		}
	}
}


@GraphQLMarker
public sealed interface GraphQLVariableContainerScope : GraphQLTypeContainerScope {

	@GraphQLMarker
	public fun variable(type: String): RefFactory =
		variable(type(type))


	@GraphQLMarker
	public fun variable(type: String, configure: GraphQLVariableBuilder.() -> Unit): RefFactory =
		variable(type(type), configure = configure)


	@GraphQLMarker
	public fun variable(name: String, type: String): GVariableRef =
		variable(name = name, type = type(type))


	@GraphQLMarker
	public fun variable(name: String, type: String, configure: GraphQLVariableBuilder.() -> Unit): GVariableRef =
		variable(name = name, type = type(type), configure = configure)


	@GraphQLMarker
	public fun variable(name: String, type: GTypeRef): GVariableRef =
		variable(name = name, type = type) {}


	@GraphQLMarker
	public fun variable(name: String, type: GTypeRef, configure: GraphQLVariableBuilder.() -> Unit): GVariableRef


	@GraphQLMarker
	public fun variable(type: GTypeRef): RefFactory =
		variable(type = type) {}


	@GraphQLMarker
	public fun variable(type: GTypeRef, configure: GraphQLVariableBuilder.() -> Unit): RefFactory
}


internal sealed interface GraphQLVariableContainerInternal : GraphQLVariableContainer {

	val unusedVariableRefFactories: MutableList<RefFactory>
	val variableDefinitions: MutableList<GVariableDefinition>


	fun finalize() {
		unusedVariableRefFactories.firstOrNull()?.throwNotDelegatedError()
	}


	@GraphQLMarker
	override fun variable(definition: GVariableDefinition): GVariableRef {
		val name = definition.name
		check(GLanguage.isValidTypeName(name)) { "Invalid variable name: $name" }
		check(variableDefinitions.none { it.name == name }) { "Cannot specify multiple variables with the same name: $name" }

		variableDefinitions += definition

		return GVariableRef(name)
	}


	// TODO Move to extension and inline once we have context receivers.
	@GraphQLMarker
	override fun variable(name: String, type: GTypeRef, configure: GraphQLVariableBuilder.() -> Unit): GVariableRef =
		variable(GraphQLVariableBuilder(name = name, type = type).apply(configure).build())


	@GraphQLMarker
	override fun variable(type: GTypeRef, configure: GraphQLVariableBuilder.() -> Unit): RefFactory =
		RefFactory { factory, name ->
			unusedVariableRefFactories -= factory

			variable(name = name, type = type, configure = configure)
		}.also { unusedVariableRefFactories += it }
}
