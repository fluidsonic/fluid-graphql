package io.fluidsonic.graphql


enum class GOperationType {

	query,
	mutation,
	subscription;


	val defaultObjectTypeName
		get() = when (this) {
			query -> GLanguage.defaultQueryTypeName
			mutation -> GLanguage.defaultMutationTypeName
			subscription -> GLanguage.defaultSubscriptionTypeName
		}
}
