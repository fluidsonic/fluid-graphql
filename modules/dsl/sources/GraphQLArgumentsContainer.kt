package io.fluidsonic.graphql


/**
 * Mixin interface for builders that accept an `arguments { ... }` block.
 *
 * Call [arguments] once to set the list of [GArgument] instances.
 */
@GraphQLMarker
public sealed interface GraphQLArgumentsContainer : GraphQLArgumentsContainerScope {

	/**
	 * Sets the arguments list for this element.
	 *
	 * Can only be called once per builder; subsequent calls throw.
	 */
	@GraphQLMarker
	public fun arguments(arguments: List<GArgument>)
}


/** Scope interface for [GraphQLArgumentsContainer]. */
@GraphQLMarker
public sealed interface GraphQLArgumentsContainerScope


internal interface GraphQLArgumentsContainerInternal : GraphQLArgumentsContainer {

	var arguments: List<GArgument>?


	override fun arguments(arguments: List<GArgument>) {
		check(this.arguments == null) { "Cannot specify multiple 'arguments' blocks." }

		this.arguments = arguments
	}
}


/**
 * Applies an `arguments { ... }` block to this element using the [GraphQLArgumentsBuilder] DSL.
 */
@GraphQLMarker
public inline fun GraphQLArgumentsContainerScope.arguments(configure: GraphQLArgumentsBuilderScope.() -> Unit) {
	when (this) {
		is GraphQLArgumentsContainer ->
			arguments(GraphQLArgumentsBuilder().apply(configure).build())
	}
}
