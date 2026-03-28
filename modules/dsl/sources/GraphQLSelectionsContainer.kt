package io.fluidsonic.graphql


/**
 * Mixin interface for builders that hold a list of [GSelection] instances.
 *
 * Adds concrete implementations for field, fragment spread, and inline fragment selections.
 */
@GraphQLMarker
public sealed interface GraphQLSelectionsContainer : GraphQLSelectionsContainerScope {

	// TODO Move to extension and inline.
	public override fun fragment(name: GFragmentRef, configure: GraphQLFragmentSelectionBuilder.() -> Unit) {
		selection(GraphQLFragmentSelectionBuilder(name = name.name).apply(configure).build())
	}


	// TODO Move to extension and inline.
	public override fun on(type: GNamedTypeRef, configure: GraphQLInlineFragmentSelectionBuilder.() -> Unit) {
		selection(GraphQLInlineFragmentSelectionBuilder(type = type).apply(configure).build())
	}


	/** Adds a pre-built [GSelection] to this selection set. */
	public fun selection(selection: GSelection)


	// TODO Move to extension and inline once we have context receivers.
	public override operator fun String.invoke(alias: String?, configure: GraphQLFieldSelectionBuilder.() -> Unit) {
		selection(GraphQLFieldSelectionBuilder(alias = alias, name = this).apply(configure).build())
	}
}


/**
 * Scope interface for building a [GSelectionSet].
 *
 * Use `"fieldName"()` for field selections, [fragment] for fragment spreads, and
 * [on] for inline fragments.
 */
@GraphQLMarker
public sealed interface GraphQLSelectionsContainerScope {

	/** Adds an inline fragment selection for the given type name (string). */
	public fun on(type: String, configure: GraphQLInlineFragmentSelectionBuilder.() -> Unit) {
		on(type = GNamedTypeRef(type), configure = configure)
	}

	/** Adds an inline fragment selection for the given [type] condition. */
	public fun on(type: GNamedTypeRef, configure: GraphQLInlineFragmentSelectionBuilder.() -> Unit)

	/** Adds a fragment spread for the named fragment (no directives). */
	public fun fragment(name: String) {
		fragment(name = GFragmentRef(name))
	}

	/** Adds a fragment spread with no directives. */
	public fun fragment(name: GFragmentRef) {
		fragment(name = name) {}
	}


	// TODO Move to extension and inline.
	/** Adds a fragment spread by string name with optional directives. */
	public fun fragment(name: String, configure: GraphQLFragmentSelectionBuilder.() -> Unit) {
		fragment(name = GFragmentRef(name), configure = configure)
	}


	// TODO Move to extension and inline.
	/** Adds a fragment spread with optional directives. */
	public fun fragment(name: GFragmentRef, configure: GraphQLFragmentSelectionBuilder.() -> Unit)


	/** Adds a `__typename` field selection. */
	@Suppress("FunctionName")
	public fun __typename() {
		// TODO Define constant somewhere else and use reference.
		"__typename"()
	}


	/** Adds a fragment spread with no directives. */
	public operator fun GFragmentRef.invoke() {
		fragment(name = this) {}
	}


	// TODO Move to extension and inline.
	/** Adds a fragment spread with optional directives. */
	public operator fun GFragmentRef.invoke(configure: GraphQLFragmentSelectionBuilder.() -> Unit) {
		fragment(name = this, configure = configure)
	}


	/** Adds a field selection with the given name, optional alias, and no sub-selections. */
	public operator fun String.invoke(
		alias: String? = null,
	) {
		invoke(alias = alias) {}
	}


	// TODO Move to extension and inline.
	/**
	 * Adds a field selection with the given name, optional alias, and sub-selections/arguments
	 * configured in [configure].
	 */
	public operator fun String.invoke(alias: String? = null, configure: GraphQLFieldSelectionBuilder.() -> Unit)
}


internal interface GraphQLSelectionsContainerInternal : GraphQLSelectionsContainer {

	val selections: MutableList<GSelection>


	override fun selection(selection: GSelection) {
		selections += when (selection) {
			is GFieldSelection -> {
				val effectiveName = selection.alias ?: selection.name

				check(selections.none { it is GFieldSelection && (it.alias ?: it.name) == effectiveName }) {
					"Cannot specify multiple selections with the same name: $effectiveName"
				}

				selection
			}

			is GFragmentSelection, is GInlineFragmentSelection ->
				selection
		}
	}
}
