package io.fluidsonic.graphql


// https://graphql.github.io/graphql-spec/draft/#sec-Single-root-field
internal object SubscriptionSelectsOnlyOneRootFieldRule : ValidationRule {

	override fun validateOperationDefinition(definition: GOperationDefinition, context: ValidationContext) {
		if (definition.type != GOperationType.subscription)
			return

		// FIXME
//		val fieldByResponseName: Map<String, List<GFieldSelection>> = collectFields(
//			context.schema.subscriptionType, // FIXME check
//			selectionSet,
//			emptyMap()
//		)
//
//		if (fieldByResponseName.size != 1) {
//			val name = definition.name
//
//			context.reportError(
//				message = (
//					if (name != null) "Subscription '$name' must select exactly one top-level field."
//					else "Anonymous Subscription must select exactly one top-level field."),
//				nodes = fieldByResponseName.values.flatten()
//			)
//		}
	}
}
