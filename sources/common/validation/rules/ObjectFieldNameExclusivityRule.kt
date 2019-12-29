package io.fluidsonic.graphql


// https://graphql.github.io/graphql-spec/draft/#sec-Input-Object-Field-Uniqueness
internal object ObjectFieldNameExclusivityRule : ValidationRule {

	override fun validateValue(value: GValue, context: ValidationContext) {
		if (value !is GObjectValue)
			return // Irrelevant.

		value.fields
			.groupBy { it.name }
			.filter { (_, fields) -> fields.size > 1 }
			.forEach { (name, field) ->
				context.reportError(
					message = "An input object can only have a single field named '$name'.",
					nodes = field.map { it.nameNode }
				)
			}
	}
}
