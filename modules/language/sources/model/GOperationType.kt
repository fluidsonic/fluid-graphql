package io.fluidsonic.graphql


/** The three kinds of GraphQL operations: query, mutation, and subscription. */
public enum class GOperationType {

	query,
	mutation,
	subscription;


	/** The conventional root type name for this operation (`Query`, `Mutation`, or `Subscription`). */
	public val defaultObjectTypeName: String
		get() = when (this) {
			query -> GLanguage.defaultQueryTypeName
			mutation -> GLanguage.defaultMutationTypeName
			subscription -> GLanguage.defaultSubscriptionTypeName
		}
}
