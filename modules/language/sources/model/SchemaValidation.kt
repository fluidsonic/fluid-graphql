package io.fluidsonic.graphql

/**
 * Throws unless this schema satisfies every specification rule [validate] checks.
 *
 * @throws GErrorException carrying exactly the errors [validate] reports, in the same order.
 *   `assertValidSchema()` in graphql-js flattens them into one plain error instead, but only because its
 *   `Error` cannot carry a list; [GErrorException] can, and flattening would discard the nodes that give
 *   each error its source excerpt and caret — which is what makes the thrown message worth reading.
 */
public fun GSchema.assertValid() {
	val errors = validate()
	if (errors.isEmpty()) {
		return
	}

	throw GErrorException(errors)
}

/**
 * Returns every violation of the GraphQL specification this schema commits, or an empty list if it commits
 * none.
 *
 * Structural problems are never reported here — a duplicate or reserved type name makes the [GSchema] factory
 * throw, so no such schema exists to be validated. What remains are the type-system rules a schema can
 * violate while still being a well-formed set of definitions:
 *
 * - a query root operation type is missing, or a root operation type does not name an object type,
 * - a type does not provide a field of an interface it declares, or fails to declare an interface that one of
 *   its declared interfaces implements,
 * - a field of a `@oneOf` input object is non-null or carries a default value.
 *
 * These are a subset of the rules `validateSchema()` checks in graphql-js — notably, fluid does not yet
 * require a type to define at least one member, nor check that an implementing field's type and arguments are
 * compatible with the interface's. A schema this function accepts is therefore not guaranteed to satisfy
 * every rule of the specification.
 *
 * A [GSchema] is immutable, so the answer is computed on the first call and that exact list — the same
 * instance — is returned by every later call. There is no cache to invalidate: what is true once stays true.
 */
public fun GSchema.validate(): List<GError> = validationErrors

/**
 * Reports every specification violation of [schema] that fluid checks, in the order the schema defines the
 * offending types, root operation types first.
 *
 * Only *spec-rule* violations are reported. A structural problem — a duplicate or reserved type name — never
 * reaches this function because the [GSchema] factory refuses to build such a schema.
 */
// https://spec.graphql.org/draft/#sec-Type-System
internal fun validateSchema(schema: GSchema): List<GError> {
	val errors = mutableListOf<GError>()

	for (operationType in GOperationType.entries) {
		rootOperationTypeError(schema = schema, operationType = operationType)?.let { errors += it }
	}

	for (type in schema.types) {
		when (type) {
			is GInputObjectType -> validateOneOfInputObjectType(type = type, errors = errors)
			is GInterfaceType -> validateInterfaces(schema = schema, type = type, errors = errors)
			is GObjectType -> validateInterfaces(schema = schema, type = type, errors = errors)

			is GEnumType,
			is GScalarType,
			is GUnionType,
			-> Unit
		}
	}

	return errors
}

/**
 * Returns the problem with the root operation type [schema] declares for [operationType], or `null` if there
 * is none.
 *
 * A mutation and a subscription root type are optional, so a missing one is not a problem; a declared name
 * that does not resolve to an object type always is.
 */
// https://spec.graphql.org/draft/#sec-Root-Operation-Types
private fun rootOperationTypeError(schema: GSchema, operationType: GOperationType): GError? {
	val ref = schema.rootTypeRefs[operationType]
	val type = ref?.let { schema.resolveType(it) }
	val label = operationType.name.replaceFirstChar { it.uppercaseChar() }

	return when {
		type is GObjectType -> null

		type !== null -> GError(
			message = when (operationType) {
				GOperationType.query -> "Query root type must be Object type, it cannot be ${type.name}."
				else -> "$label root type must be Object type if provided, it cannot be ${type.name}."
			},
			nodes = listOfNotNull(ref),
		)

		operationType == GOperationType.query -> GError(message = "Query root type must be provided.")

		else -> null
	}
}

/**
 * Reports, for a `@oneOf` input object, every field that is non-null and every field that carries a default
 * value; reports nothing for an input object that is not `@oneOf`.
 *
 * A `@oneOf` field must be nullable and must not carry a default value: exactly one field is supplied per
 * value, so a non-null or defaulted field could never be left out. Note the deliberate asymmetry with the
 * *variable* supplying such a field, which must be declared non-null — see `VariablesInAllowedPositionRule`.
 */
// https://spec.graphql.org/draft/#sec-Input-Objects.Type-Validation
private fun validateOneOfInputObjectType(type: GInputObjectType, errors: MutableList<GError>) {
	if (type.directive(GLanguage.defaultOneOfDirective.name) === null) {
		return
	}

	for (field in type.argumentDefinitions) {
		if (field.type is GNonNullTypeRef) {
			errors += GError(message = "OneOf input field ${type.name}.${field.name} must be nullable.", nodes = listOf(field.nameNode))
		}

		if (field.defaultValue !== null) {
			errors += GError(message = "OneOf input field ${type.name}.${field.name} cannot have a default value.", nodes = listOf(field.nameNode))
		}
	}
}

/**
 * Reports, for every interface [type] declares, an interface that [type] must also declare because the
 * declared one implements it, and every field of the declared interface that [type] does not provide.
 *
 * A reference that does not resolve to an interface type is skipped: that is a different problem, and
 * reporting it here would double up on it.
 */
// https://spec.graphql.org/draft/#sec-Interfaces.Type-Validation
private fun <TypeT> validateInterfaces(
	schema: GSchema,
	type: TypeT,
	errors: MutableList<GError>,
) where TypeT : GNamedType, TypeT : GNode.WithFieldDefinitions, TypeT : GNode.WithInterfaces {
	val declaredInterfaceNames = type.interfaces.mapTo(hashSetOf()) { it.name }

	for (interfaceRef in type.interfaces) {
		val interfaceType = schema.resolveType(interfaceRef) as? GInterfaceType
		if (interfaceType === null || interfaceType.name == type.name) {
			continue
		}

		for (ancestorRef in interfaceType.interfaces) {
			if (ancestorRef.name != type.name && ancestorRef.name !in declaredInterfaceNames) {
				errors += GError(
					message = "Type ${type.name} must implement ${ancestorRef.name} because it is implemented by ${interfaceType.name}.",
					nodes = listOf(interfaceRef, type.nameNode),
				)
			}
		}

		for (interfaceField in interfaceType.fieldDefinitions) {
			if (type.fieldDefinition(interfaceField.name) === null) {
				errors += GError(
					message = "Interface field ${interfaceType.name}.${interfaceField.name} expected but ${type.name} does not provide it.",
					nodes = listOf(interfaceField.nameNode, type.nameNode),
				)
			}
		}
	}
}
