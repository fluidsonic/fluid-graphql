package io.fluidsonic.graphql


public enum class GOperationType {

	query,
	mutation,
	subscription;


	public val defaultObjectTypeName: String
		get() = when (this) {
			query -> GLanguage.defaultQueryTypeName
			mutation -> GLanguage.defaultMutationTypeName
			subscription -> GLanguage.defaultSubscriptionTypeName
		}
}
