package io.fluidsonic.graphql


@GraphQLMarker
public sealed interface GraphQLSelectionsContainer : GraphQLSelectionsContainerScope {

	// TODO Move to extension and inline.
	@GraphQLMarker
	public override fun fragment(name: GFragmentRef, configure: GraphQLFragmentSelectionBuilder.() -> Unit) {
		selection(GraphQLFragmentSelectionBuilder(name = name.name).apply(configure).build())
	}


	// TODO Move to extension and inline.
	@GraphQLMarker
	public override fun on(type: GNamedTypeRef, configure: GraphQLInlineFragmentSelectionBuilder.() -> Unit) {
		selection(GraphQLInlineFragmentSelectionBuilder(type = type).apply(configure).build())
	}


	@GraphQLMarker
	public fun selection(selection: GSelection)


	// TODO Move to extension and inline once we have context receivers.
	@GraphQLMarker
	public override operator fun String.invoke(alias: String?, configure: GraphQLFieldSelectionBuilder.() -> Unit) {
		selection(GraphQLFieldSelectionBuilder(alias = alias, name = this).apply(configure).build())
	}
}


@GraphQLMarker
public sealed interface GraphQLSelectionsContainerScope {

	@GraphQLMarker
	public fun on(type: String, configure: GraphQLInlineFragmentSelectionBuilder.() -> Unit) {
		on(type = GNamedTypeRef(type), configure = configure)
	}


	@GraphQLMarker
	public fun on(type: GNamedTypeRef, configure: GraphQLInlineFragmentSelectionBuilder.() -> Unit)


	@GraphQLMarker
	public fun fragment(name: String) {
		fragment(name = GFragmentRef(name))
	}


	@GraphQLMarker
	public fun fragment(name: GFragmentRef) {
		fragment(name = name) {}
	}


	// TODO Move to extension and inline.
	@GraphQLMarker
	public fun fragment(name: String, configure: GraphQLFragmentSelectionBuilder.() -> Unit) {
		fragment(name = GFragmentRef(name), configure = configure)
	}


	// TODO Move to extension and inline.
	@GraphQLMarker
	public fun fragment(name: GFragmentRef, configure: GraphQLFragmentSelectionBuilder.() -> Unit)


	@GraphQLMarker
	@Suppress("FunctionName")
	public fun __typename() {
		// TODO Define constant somewhere else and use reference.
		"__typename"()
	}


	@GraphQLMarker
	public operator fun GFragmentRef.invoke() {
		fragment(name = this) {}
	}


	// TODO Move to extension and inline.
	@GraphQLMarker
	public operator fun GFragmentRef.invoke(configure: GraphQLFragmentSelectionBuilder.() -> Unit) {
		fragment(name = this, configure = configure)
	}


	@GraphQLMarker
	public operator fun String.invoke(
		alias: String? = null,
	) {
		invoke(alias = alias) {}
	}


	// TODO Move to extension and inline.
	@GraphQLMarker
	public operator fun String.invoke(alias: String? = null, configure: GraphQLFieldSelectionBuilder.() -> Unit)
}


internal sealed interface GraphQLSelectionsContainerInternal : GraphQLSelectionsContainer {

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
