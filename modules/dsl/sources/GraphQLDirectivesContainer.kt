package io.fluidsonic.graphql


@GraphQLMarker
public sealed interface GraphQLDirectivesContainer : GraphQLDirectivesContainerScope {

	@GraphQLMarker
	public fun directives(directives: List<GDirective>)
}


@GraphQLMarker
public sealed interface GraphQLDirectivesContainerScope


internal sealed interface GraphQLDirectivesContainerInternal : GraphQLDirectivesContainer {

	var directives: List<GDirective>?


	override fun directives(directives: List<GDirective>) {
		check(this.directives == null) { "Cannot specify multiple 'directives' blocks." }

		this.directives = directives
	}
}


@GraphQLMarker
public inline fun GraphQLDirectivesContainerScope.directives(configure: GraphQLDirectivesBuilderScope.() -> Unit) {
	when (this) {
		is GraphQLDirectivesContainer ->
			directives(GraphQLDirectivesBuilder().apply(configure).build())
	}
}
