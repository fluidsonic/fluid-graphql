package io.fluidsonic.graphql


/**
 * Builder for a [GDocument].
 *
 * Add operations via [query], [mutation], [subscription], and fragment definitions via [fragment].
 * Call [build] when done to produce the [GDocument].
 */
@GraphQLMarker
public sealed interface GraphQLDocumentBuilder : GraphQLDocumentBuilderScope, GraphQLFragmentDefinitionContainer {

	/** Builds and returns the [GDocument] containing all added definitions. */
	public fun build(): GDocument

	/** Adds a pre-built [GOperationDefinition] to the document. */
	public fun operation(definition: GOperationDefinition)
}


/**
 * Scope interface for [GraphQLDocumentBuilder], providing [query], [mutation], [subscription],
 * and [fragment] DSL functions.
 */
@GraphQLMarker
public sealed interface GraphQLDocumentBuilderScope : GraphQLFragmentDefinitionContainerScope


/**
 * Adds a mutation operation to the document.
 *
 * @param name optional operation name.
 */
public inline fun GraphQLDocumentBuilderScope.mutation(
	name: String? = null,
	configure: GraphQLOperationBuilderScope.() -> Unit,
) {
	when (this) {
		is GraphQLDocumentBuilder -> operation(GraphQLOperationBuilder(name = name, type = GOperationType.mutation).apply(configure).build())
	}
}


/**
 * Adds a query operation to the document.
 *
 * @param name optional operation name.
 */
public inline fun GraphQLDocumentBuilderScope.query(
	name: String? = null,
	configure: GraphQLOperationBuilderScope.() -> Unit,
) {
	when (this) {
		is GraphQLDocumentBuilder -> operation(GraphQLOperationBuilder(name = name, type = GOperationType.query).apply(configure).build())
	}
}


/**
 * Adds a subscription operation to the document.
 *
 * @param name optional operation name.
 */
public inline fun GraphQLDocumentBuilderScope.subscription(
	name: String? = null,
	configure: GraphQLOperationBuilderScope.() -> Unit,
) {
	when (this) {
		is GraphQLDocumentBuilder -> operation(GraphQLOperationBuilder(name = name, type = GOperationType.subscription).apply(configure).build())
	}
}


@GraphQLMarker
internal class GraphQLDocumentBuilderImpl : GraphQLDocumentBuilder, GraphQLFragmentDefinitionContainerInternal {

	override val definitions: MutableList<GDefinition> = mutableListOf()

	override val unusedFragmentDefinitionRefFactories: MutableList<GraphQLFragmentDefinitionContainer.RefFactory> = mutableListOf()


	override fun build(): GDocument {
		super.finalize()

		check(definitions.isNotEmpty()) { "Document must contain at least one definition." }

		return GDocument(definitions = definitions.toList())
	}


	override fun operation(definition: GOperationDefinition) {
		definitions += definition
	}
}


/** Creates a new [GraphQLDocumentBuilder]. */
public fun GraphQLDocumentBuilder(): GraphQLDocumentBuilder =
	GraphQLDocumentBuilderImpl()


/**
 * Builds a [GDocument] using a lambda, without an explicit receiver.
 *
 * Prefer [GraphQL.document] for readability.
 */
public inline fun GraphQL(configure: GraphQLDocumentBuilderScope.() -> Unit): GDocument =
	GraphQLDocumentBuilder().apply(configure).build()


/**
 * Builds a [GDocument] using the document builder DSL.
 *
 * ```kotlin
 * val doc = GraphQL.document {
 *     query("GetUser") {
 *         "user"(alias = "currentUser") {
 *             "id"()
 *             "name"()
 *         }
 *     }
 * }
 * ```
 */
@Suppress("UnusedReceiverParameter")
public inline fun GraphQL.document(configure: GraphQLDocumentBuilderScope.() -> Unit): GDocument =
	GraphQLDocumentBuilder().apply(configure).build()
