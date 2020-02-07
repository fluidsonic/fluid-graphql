package io.fluidsonic.graphql


enum class GOperationType {

	query,
	mutation,
	subscription;


	val defaultObjectTypeName
		get() = when (this) {
			query -> GSpecification.defaultQueryTypeName
			mutation -> GSpecification.defaultMutationTypeName
			subscription -> GSpecification.defaultSubscriptionTypeName
		}
}
