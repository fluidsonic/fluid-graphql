package io.fluidsonic.graphql


@GraphQLMarker
public /* sealed */ interface GraphQLArgumentsContainer : GraphQLArgumentsContainerScope {

	@GraphQLMarker
	public fun arguments(arguments: List<GArgument>)
}


@GraphQLMarker
public /* sealed */ interface GraphQLArgumentsContainerScope


internal interface GraphQLArgumentsContainerInternal : GraphQLArgumentsContainer {

	var arguments: List<GArgument>?


	override fun arguments(arguments: List<GArgument>) {
		check(this.arguments == null) { "Cannot specify multiple 'arguments' blocks." }

		this.arguments = arguments
	}
}


@GraphQLMarker
public inline fun GraphQLArgumentsContainerScope.arguments(configure: GraphQLArgumentsBuilderScope.() -> Unit) {
	when (this) {
		is GraphQLArgumentsContainer ->
			arguments(GraphQLArgumentsBuilder().apply(configure).build())
	}
}
