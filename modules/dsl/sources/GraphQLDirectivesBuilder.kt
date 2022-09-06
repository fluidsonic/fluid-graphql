package io.fluidsonic.graphql

import kotlin.js.*


@GraphQLMarker
public /* sealed */ interface GraphQLDirectivesBuilder : GraphQLDirectivesBuilderScope {

	public fun build(): List<GDirective>

	@GraphQLMarker
	public fun directive(directive: GDirective)


	// TODO Move to extension and inline.
	@GraphQLMarker
	public override operator fun String.invoke(configure: GraphQLDirectiveBuilder.() -> Unit) {
		directive(GraphQLDirectiveBuilder(name = this).apply(configure).build())
	}
}


@GraphQLMarker
public /* sealed */ interface GraphQLDirectivesBuilderScope : GraphQLValueContainerScope {

	@GraphQLMarker
	public operator fun String.invoke() {
		this {}
	}


	@GraphQLMarker
	public operator fun String.invoke(configure: GraphQLDirectiveBuilder.() -> Unit)
}


private class GraphQLDirectivesBuilderImpl : GraphQLDirectivesBuilder {

	private val directives = mutableListOf<GDirective>()


	override fun directive(directive: GDirective) {
		val name = directive.name

		check(GLanguage.isValidName(name)) { "Invalid directive name: $name" }

		directives += directive
	}


	override fun build(): List<GDirective> =
		directives.toList()
}


@JsName("_GraphQLDirectivesBuilder")
public fun GraphQLDirectivesBuilder(): GraphQLDirectivesBuilder =
	GraphQLDirectivesBuilderImpl()
