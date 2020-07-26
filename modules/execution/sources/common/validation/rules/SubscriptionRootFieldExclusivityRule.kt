package io.fluidsonic.graphql


// https://graphql.github.io/graphql-spec/draft/#sec-Single-root-field
@Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")
internal object SubscriptionRootFieldExclusivityRule : ValidationRule.Singleton() {

	override fun onOperationDefinition(definition: GOperationDefinition, data: ValidationContext, visit: Visit) {
		if (definition.type != GOperationType.subscription)
			return

		// FIXME
//		val fieldByResponseName: Map<String, List<GFieldSelection>> = collectFields(
//			data.schema.subscriptionType, // FIXME check
//			selectionSet,
//			emptyMap()
//		)
//
//		if (fieldByResponseName.size != 1) {
//			val name = definition.name
//
//			data.reportError(
//				message = (
//					if (name != null) "Subscription '$name' must select exactly one top-level field."
//					else "Anonymous Subscription must select exactly one top-level field."),
//				nodes = fieldByResponseName.values.flatten()
//			)
//		}
	}
}
