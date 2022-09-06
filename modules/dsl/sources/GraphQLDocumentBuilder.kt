package io.fluidsonic.graphql

import kotlin.js.*


@GraphQLMarker
public /* sealed */ interface GraphQLDocumentBuilder : GraphQLDocumentBuilderScope, GraphQLFragmentDefinitionContainer {

	public fun build(): GDocument

	@GraphQLMarker
	public fun operation(definition: GOperationDefinition)
}


@GraphQLMarker
public /* sealed */ interface GraphQLDocumentBuilderScope : GraphQLFragmentDefinitionContainerScope


@GraphQLMarker
public inline fun GraphQLDocumentBuilderScope.mutation(
	name: String? = null,
	configure: GraphQLOperationBuilderScope.() -> Unit,
) {
	when (this) {
		is GraphQLDocumentBuilder -> operation(GraphQLOperationBuilder(name = name, type = GOperationType.mutation).apply(configure).build())
	}
}


@GraphQLMarker
public inline fun GraphQLDocumentBuilderScope.query(
	name: String? = null,
	configure: GraphQLOperationBuilderScope.() -> Unit,
) {
	when (this) {
		is GraphQLDocumentBuilder -> operation(GraphQLOperationBuilder(name = name, type = GOperationType.query).apply(configure).build())
	}
}


@GraphQLMarker
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


@JsName("_GraphQLDocumentBuilder")
public fun GraphQLDocumentBuilder(): GraphQLDocumentBuilder =
	GraphQLDocumentBuilderImpl()


@GraphQLMarker
public inline fun GraphQL(configure: GraphQLDocumentBuilderScope.() -> Unit): GDocument =
	GraphQLDocumentBuilder().apply(configure).build()


@GraphQLMarker
@Suppress("UnusedReceiverParameter")
public inline fun GraphQL.document(configure: GraphQLDocumentBuilderScope.() -> Unit): GDocument =
	GraphQLDocumentBuilder().apply(configure).build()
