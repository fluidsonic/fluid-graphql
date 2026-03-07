package io.fluidsonic.graphql


/**
 * Mixin interface for builders that accept a `directives { ... }` block.
 *
 * Call [directives] once to attach a list of [GDirective] instances.
 */
@GraphQLMarker
public sealed interface GraphQLDirectivesContainer : GraphQLDirectivesContainerScope {

	/**
	 * Sets the directives list for this element.
	 *
	 * Can only be called once per builder; subsequent calls throw.
	 */
	@GraphQLMarker
	public fun directives(directives: List<GDirective>)
}


/** Scope interface for [GraphQLDirectivesContainer]. */
@GraphQLMarker
public sealed interface GraphQLDirectivesContainerScope


internal interface GraphQLDirectivesContainerInternal : GraphQLDirectivesContainer {

	var directives: List<GDirective>?


	override fun directives(directives: List<GDirective>) {
		check(this.directives == null) { "Cannot specify multiple 'directives' blocks." }

		this.directives = directives
	}
}


/**
 * Applies a `directives { ... }` block to this element using the [GraphQLDirectivesBuilder] DSL.
 */
@GraphQLMarker
public inline fun GraphQLDirectivesContainerScope.directives(configure: GraphQLDirectivesBuilderScope.() -> Unit) {
	when (this) {
		is GraphQLDirectivesContainer ->
			directives(GraphQLDirectivesBuilder().apply(configure).build())
	}
}
