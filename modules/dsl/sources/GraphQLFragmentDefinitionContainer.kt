package io.fluidsonic.graphql

import io.fluidsonic.graphql.GraphQLFragmentDefinitionContainer.*
import kotlin.properties.*
import kotlin.reflect.*


@GraphQLMarker
public sealed interface GraphQLFragmentDefinitionContainer : GraphQLFragmentDefinitionContainerScope {

	@GraphQLMarker
	public fun fragment(definition: GFragmentDefinition): GFragmentRef


	@GraphQLMarker
	public class RefFactory internal constructor(
		private val register: (factory: RefFactory, name: String) -> GFragmentRef,
	) : PropertyDelegateProvider<Nothing?, ReadOnlyProperty<Nothing?, GFragmentRef>> {

		private val notDelegatedError: Throwable = IllegalStateException("This fragment() overload must be delegated to using 'by'.")


		internal fun throwNotDelegatedError(): Nothing {
			throw notDelegatedError
		}


		@GraphQLMarker
		override fun provideDelegate(thisRef: Nothing?, property: KProperty<*>): ReadOnlyProperty<Nothing?, GFragmentRef> {
			val ref = register(this, property.name)

			return ReadOnlyProperty { _, _ -> ref }
		}
	}
}


@GraphQLMarker
public sealed interface GraphQLFragmentDefinitionContainerScope : GraphQLTypeContainerScope {

	@GraphQLMarker
	public fun fragment(typeCondition: String, configure: GraphQLFragmentDefinitionBuilder.() -> Unit): RefFactory =
		fragment(type(typeCondition), configure = configure)


	@GraphQLMarker
	public fun fragment(name: String, typeCondition: String, configure: GraphQLFragmentDefinitionBuilder.() -> Unit): GFragmentRef =
		fragment(name = name, typeCondition = type(typeCondition), configure = configure)


	@GraphQLMarker
	public fun fragment(name: String, typeCondition: GNamedTypeRef, configure: GraphQLFragmentDefinitionBuilder.() -> Unit): GFragmentRef


	@GraphQLMarker
	public fun fragment(typeCondition: GNamedTypeRef, configure: GraphQLFragmentDefinitionBuilder.() -> Unit): RefFactory
}


internal sealed interface GraphQLFragmentDefinitionContainerInternal : GraphQLFragmentDefinitionContainer {

	val definitions: MutableList<GDefinition>
	val unusedFragmentDefinitionRefFactories: MutableList<RefFactory>


	fun finalize() {
		unusedFragmentDefinitionRefFactories.firstOrNull()?.throwNotDelegatedError()
	}


	@GraphQLMarker
	override fun fragment(definition: GFragmentDefinition): GFragmentRef {
		val name = definition.name
		check(GLanguage.isValidFragmentName(name)) { "Invalid fragment name: $name" }
		check(definitions.none { it is GFragmentDefinition && it.name == name }) { "Cannot specify multiple fragments with the same name: $name" }

		definitions += definition

		return GFragmentRef(name)
	}


	// TODO Move to extension and inline once we have context receivers.
	@GraphQLMarker
	override fun fragment(name: String, typeCondition: GNamedTypeRef, configure: GraphQLFragmentDefinitionBuilder.() -> Unit): GFragmentRef =
		fragment(GraphQLFragmentDefinitionBuilder(name = name, typeCondition = typeCondition).apply(configure).build())


	@GraphQLMarker
	override fun fragment(typeCondition: GNamedTypeRef, configure: GraphQLFragmentDefinitionBuilder.() -> Unit): RefFactory =
		RefFactory { factory, name ->
			unusedFragmentDefinitionRefFactories -= factory

			fragment(name = name, typeCondition = typeCondition, configure = configure)
		}.also { unusedFragmentDefinitionRefFactories += it }
}
