package io.fluidsonic.graphql

import io.fluidsonic.graphql.GraphQLFragmentDefinitionContainer.*
import kotlin.properties.*
import kotlin.reflect.*


/**
 * Container that accepts fragment definitions and tracks references to them.
 *
 * Fragment definitions added here are included in the resulting [GDocument].
 */
@GraphQLMarker
public sealed interface GraphQLFragmentDefinitionContainer : GraphQLFragmentDefinitionContainerScope {

	/** Adds a pre-built [GFragmentDefinition] and returns a reference to it. */
	@GraphQLMarker
	public fun fragment(definition: GFragmentDefinition): GFragmentRef


	/**
	 * A property delegate provider that registers a fragment definition using the delegating
	 * property's name as the fragment name.
	 *
	 * ```kotlin
	 * val UserFields by fragment("User") {
	 *     "id"()
	 *     "name"()
	 * }
	 * ```
	 *
	 * Must be used with `by` delegation — calling the result without delegation will throw.
	 */
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


/**
 * Scope for defining fragment definitions within a document or operation builder.
 *
 * The two-argument [fragment] overloads (with `name` and `typeCondition`) register fragments with
 * an explicit name, while the single-argument [fragment] overloads (with `typeCondition` only)
 * return a [RefFactory] that derives the fragment name from a `by`-delegated property.
 */
@GraphQLMarker
public sealed interface GraphQLFragmentDefinitionContainerScope : GraphQLTypeContainerScope {

	/** Defines a fragment on [typeCondition] whose name is derived from a `by`-delegated property. */
	@GraphQLMarker
	public fun fragment(typeCondition: String, configure: GraphQLFragmentDefinitionBuilder.() -> Unit): RefFactory =
		fragment(type(typeCondition), configure = configure)

	/** Defines a fragment with an explicit [name] on a type given by its string [typeCondition]. */
	@GraphQLMarker
	public fun fragment(name: String, typeCondition: String, configure: GraphQLFragmentDefinitionBuilder.() -> Unit): GFragmentRef =
		fragment(name = name, typeCondition = type(typeCondition), configure = configure)

	/** Defines a fragment with an explicit [name] and [typeCondition]. */
	@GraphQLMarker
	public fun fragment(name: String, typeCondition: GNamedTypeRef, configure: GraphQLFragmentDefinitionBuilder.() -> Unit): GFragmentRef

	/** Defines a fragment on [typeCondition] whose name is derived from a `by`-delegated property. */
	@GraphQLMarker
	public fun fragment(typeCondition: GNamedTypeRef, configure: GraphQLFragmentDefinitionBuilder.() -> Unit): RefFactory
}


internal interface GraphQLFragmentDefinitionContainerInternal : GraphQLFragmentDefinitionContainer {

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
