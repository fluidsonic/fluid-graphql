package io.fluidsonic.graphql


/**
 * Builder for a list of directive applications.
 *
 * Use `"directiveName" { ... }` syntax to add directives, or pass pre-built [GDirective]
 * instances via [directive].
 */
@GraphQLMarker
public sealed interface GraphQLDirectivesBuilder : GraphQLDirectivesBuilderScope {

	/** Builds and returns the list of [GDirective] instances. */
	public fun build(): List<GDirective>

	/** Adds a pre-built [GDirective]. */
	@GraphQLMarker
	public fun directive(directive: GDirective)


	// TODO Move to extension and inline.
	@GraphQLMarker
	public override operator fun String.invoke(configure: GraphQLDirectiveBuilder.() -> Unit) {
		directive(GraphQLDirectiveBuilder(name = this).apply(configure).build())
	}
}


/**
 * Scope interface for [GraphQLDirectivesBuilder].
 *
 * Invoke a string as a function to apply a directive by name:
 * ```kotlin
 * directives {
 *     "skip" { arguments { "if" to true } }
 *     "deprecated"()
 * }
 * ```
 */
@GraphQLMarker
public sealed interface GraphQLDirectivesBuilderScope : GraphQLValueContainerScope {

	/** Applies the directive with this name and no arguments. */
	@GraphQLMarker
	public operator fun String.invoke() {
		this {}
	}

	/** Applies the directive with this name, configuring its arguments via [configure]. */
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


/** Creates a new [GraphQLDirectivesBuilder]. */
public fun GraphQLDirectivesBuilder(): GraphQLDirectivesBuilder =
	GraphQLDirectivesBuilderImpl()
