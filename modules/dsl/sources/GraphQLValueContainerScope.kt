package io.fluidsonic.graphql


@GraphQLMarker
public /* sealed */ interface GraphQLValueContainerScope {

	@GraphQLMarker
	public fun enum(name: String): GEnumValue {
		check(GLanguage.isValidEnumValue(name)) { "Invalid enum value: $name" }

		return GEnumValue(name)
	}


	@GraphQLMarker
	public fun variable(name: String): GVariableRef {
		check(GLanguage.isValidName(name)) { "Invalid variable name: $name" }

		return GVariableRef(name)
	}
}


@GraphQLMarker
@Suppress("UnusedReceiverParameter")
public inline fun GraphQLValueContainerScope.list(configure: GraphQLValueListBuilder.() -> Unit): GListValue =
	GraphQLValueListBuilder().apply(configure).build()


@GraphQLMarker
@Suppress("UnusedReceiverParameter")
public inline fun GraphQLValueContainerScope.obj(configure: GraphQLArgumentsBuilder.() -> Unit): GObjectValue =
	GObjectValue(GraphQLArgumentsBuilder().apply(configure).build())
