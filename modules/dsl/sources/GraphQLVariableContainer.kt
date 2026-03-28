package io.fluidsonic.graphql

import io.fluidsonic.graphql.GraphQLVariableContainer.*
import kotlin.properties.*
import kotlin.reflect.*


/**
 * Container that accepts variable definitions and tracks references to them.
 *
 * Variable definitions added here are included in the resulting [GOperationDefinition] or
 * [GFragmentDefinition].
 */
@GraphQLMarker
public sealed interface GraphQLVariableContainer : GraphQLVariableContainerScope {

	/** Adds a pre-built [GVariableDefinition] and returns a reference to it. */
	public fun variable(definition: GVariableDefinition): GVariableRef


	/**
	 * A property delegate provider that registers a variable definition using the delegating
	 * property's name as the variable name.
	 *
	 * ```kotlin
	 * val limit by variable(Int)
	 * ```
	 *
	 * Must be used with `by` delegation — calling the result without delegation will throw.
	 */
	@GraphQLMarker
	public class RefFactory internal constructor(
		private val register: (factory: RefFactory, name: String) -> GVariableRef,
	) : PropertyDelegateProvider<Nothing?, ReadOnlyProperty<Nothing?, GVariableRef>> {

		private val notDelegatedError: Throwable = IllegalStateException("This variable() overload must be delegated to using 'by'.")


		internal fun throwNotDelegatedError(): Nothing {
			throw notDelegatedError
		}


		override fun provideDelegate(thisRef: Nothing?, property: KProperty<*>): ReadOnlyProperty<Nothing?, GVariableRef> {
			val ref = register(this, property.name)

			return ReadOnlyProperty { _, _ -> ref }
		}
	}
}


/**
 * Scope interface for declaring variables in an operation or fragment builder.
 *
 * The two-argument [variable] overloads (with `name` and `type`) register variables with an explicit name,
 * while the single-argument [variable] overloads (with `type` only) return a [RefFactory] that derives
 * the variable name from a `by`-delegated property.
 */
@GraphQLMarker
public sealed interface GraphQLVariableContainerScope : GraphQLTypeContainerScope {

	/** Declares a variable whose name is derived from a `by`-delegated property. */
	public fun variable(type: String): RefFactory =
		variable(type(type))

	/** Declares a variable whose name is derived from a `by`-delegated property. */
	public fun variable(type: String, configure: GraphQLVariableBuilder.() -> Unit): RefFactory =
		variable(type(type), configure = configure)

	/** Declares a variable with an explicit [name] and a type given by its string name. */
	public fun variable(name: String, type: String): GVariableRef =
		variable(name = name, type = type(type))

	/** Declares a variable with an explicit [name] and a type given by its string name. */
	public fun variable(name: String, type: String, configure: GraphQLVariableBuilder.() -> Unit): GVariableRef =
		variable(name = name, type = type(type), configure = configure)

	/** Declares a variable with an explicit [name] and [type]. */
	public fun variable(name: String, type: GTypeRef): GVariableRef =
		variable(name = name, type = type) {}

	/** Declares a variable with an explicit [name] and [type]. */
	public fun variable(name: String, type: GTypeRef, configure: GraphQLVariableBuilder.() -> Unit): GVariableRef

	/** Declares a variable whose name is derived from a `by`-delegated property. */
	public fun variable(type: GTypeRef): RefFactory =
		variable(type = type) {}

	/** Declares a variable whose name is derived from a `by`-delegated property. */
	public fun variable(type: GTypeRef, configure: GraphQLVariableBuilder.() -> Unit): RefFactory
}


internal interface GraphQLVariableContainerInternal : GraphQLVariableContainer {

	val unusedVariableRefFactories: MutableList<RefFactory>
	val variableDefinitions: MutableList<GVariableDefinition>


	fun finalize() {
		unusedVariableRefFactories.firstOrNull()?.throwNotDelegatedError()
	}


	override fun variable(definition: GVariableDefinition): GVariableRef {
		val name = definition.name
		check(GLanguage.isValidTypeName(name)) { "Invalid variable name: $name" }
		check(variableDefinitions.none { it.name == name }) { "Cannot specify multiple variables with the same name: $name" }

		variableDefinitions += definition

		return GVariableRef(name)
	}


	// TODO Move to extension and inline once we have context receivers.
	override fun variable(name: String, type: GTypeRef, configure: GraphQLVariableBuilder.() -> Unit): GVariableRef =
		variable(GraphQLVariableBuilder(name = name, type = type).apply(configure).build())


	override fun variable(type: GTypeRef, configure: GraphQLVariableBuilder.() -> Unit): RefFactory =
		RefFactory { factory, name ->
			unusedVariableRefFactories -= factory

			variable(name = name, type = type, configure = configure)
		}.also { unusedVariableRefFactories += it }
}
