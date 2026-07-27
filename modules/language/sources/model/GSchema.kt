package io.fluidsonic.graphql

import kotlin.coroutines.cancellation.CancellationException

/**
 * A resolved GraphQL schema, containing all type definitions, directive definitions,
 * and root operation types derived from a [GDocument].
 *
 * Use [GSchema.parse] to create a schema from a GraphQL SDL string, or call the
 * [GSchema] factory function to build one from an already-parsed [GDocument].
 *
 * The standard directives (`@deprecated`, `@include`, `@oneOf`, `@skip`, `@specifiedBy`) are added
 * automatically, as are the built-in scalar types (`Boolean`, `Float`, `ID`, `Int`, `String`) that
 * something refers to — see [types].
 *
 * @see GSchema.parse
 */
// https://graphql.github.io/graphql-spec/June2018/#sec-Schema-Introspection
public class GSchema internal constructor(
	public val directiveDefinitions: List<GDirectiveDefinition>,
	public val document: GDocument,
	queryType: GNamedTypeRef? = null,
	mutationType: GNamedTypeRef? = null,
	subscriptionType: GNamedTypeRef? = null,
	types: List<GNamedType>,
) {

	/**
	 * Every named type of this schema, in the order the document defines them, followed by the built-in
	 * scalar types this schema refers to and the eight introspection types.
	 *
	 * A type the document *defines* is always listed, whether or not anything refers to it. A built-in
	 * scalar is listed only when a type or directive definition refers to it, so a schema that never
	 * mentions `Float` does not list `Float`. `Boolean` and `String` are therefore always listed: the
	 * built-in directives take arguments of those types, as do the introspection types.
	 *
	 * The introspection types (`__Schema`, `__Type`, …) are always listed. They are ordinary members of the
	 * type map, so `__type(name: "__Type")` and a fragment on `__Type` resolve through the same lookup as
	 * any user type — there is one type-identity domain, not one per schema plus one for introspection.
	 *
	 * Every name occurs at most once. A definition that shares a built-in scalar's or an introspection
	 * type's name is dropped in favour of the library's — the public [GSchema] factory rejects such a
	 * definition outright, so only a schema built through the internal factory can carry one.
	 */
	public val types: List<GNamedType> = run {
		@OptIn(InternalGraphqlApi::class)
		val introspectionTypes = GIntrospection.types()
		val referencedNames = referencedTypeNames(types = types + introspectionTypes, directiveDefinitions = directiveDefinitions)

		types.filterNot { it.name in builtinScalarTypeNames || it.name in introspectionTypeNames } +
			GType.defaultTypes().filter { it.name in referencedNames } +
			introspectionTypes
	}

	/**
	 * The description written on this schema's `schema { … }` definition, or `null` if it has none.
	 *
	 * Read from the document rather than stored, because a schema definition is optional: a document that
	 * relies on the conventional root type names has no node to carry a description.
	 */
	public val description: String? = mergeSchemaExtensions(
		schemaDefinition = document.definitions.filterIsInstance<GSchemaDefinition>().firstOrNull(),
		schemaExtensions = document.definitions.filterIsInstance<GSchemaExtension>(),
	)?.description

	private val typesByName: Map<String, GNamedType> = this.types.associateByTo(hashMapOf()) { it.name }

	public val queryType: GObjectType? = queryType?.let { typesByName[it.name] as? GObjectType }
	public val mutationType: GObjectType? = mutationType?.let { typesByName[it.name] as? GObjectType }
	public val subscriptionType: GObjectType? = subscriptionType?.let { typesByName[it.name] as? GObjectType }

	/**
	 * The type reference declared for each root operation type, whatever it resolves to.
	 *
	 * Kept alongside the resolved [queryType], [mutationType] and [subscriptionType] so that schema
	 * validation can tell "no root type declared" from "the declared root type is not an object type" — the
	 * resolved properties are `null` for both.
	 */
	internal val rootTypeRefs: Map<GOperationType, GNamedTypeRef> = buildMap {
		mutationType?.let { put(GOperationType.mutation, it) }
		queryType?.let { put(GOperationType.query, it) }
		subscriptionType?.let { put(GOperationType.subscription, it) }
	}

	/** The memoized backing of [validate]; computed once because a [GSchema] never changes. */
	internal val validationErrors: List<GError> by lazy { validateSchema(this) }

	private val possibleTypesByType: Map<String, List<GObjectType>> =
		types
			.filterIsInstance<GObjectType>()
			.filter { it.interfaces.isNotEmpty() }
			.flatMap { type -> type.interfaces.map { it.name to type } }
			.groupBy(keySelector = { it.first }, valueTransform = { it.second }) +
			types
				.filterIsInstance<GUnionType>()
				.associate { union -> union.name to union.possibleTypes.mapNotNull { typesByName[it.name] as? GObjectType } }

	/** Returns the directive definition with the given [name], or `null` if not found. */
	public fun directiveDefinition(name: String): GDirectiveDefinition? = directiveDefinitions.firstOrNull { it.name == name }

	/**
	 * Returns the concrete object types that are possible for [type].
	 *
	 * For a [GObjectType], returns a list containing only that type itself.
	 * For a [GInterfaceType] or [GUnionType], returns the object types that implement or belong to it.
	 */
	public fun getPossibleTypes(type: GCompositeType): List<GObjectType> = if (type is GObjectType) {
		listOf(type)
	} else {
		possibleTypesByType[type.name].orEmpty()
	}

	/**
	 * Resolves a type reference to a [GType], following list and non-null wrappers.
	 *
	 * Returns `null` if the underlying named type is not defined in this schema.
	 */
	public fun resolveType(ref: GTypeRef): GType? = when (ref) {
		is GListTypeRef -> resolveType(ref.elementType)?.let { GListType(elementType = it) }
		is GNamedTypeRef -> resolveType(ref)
		is GNonNullTypeRef -> resolveType(ref.nullableRef)?.let { GNonNullType(nullableType = it) }
	}

	/** Resolves a named type reference to a [GNamedType], or `null` if not found. */
	public fun resolveType(ref: GNamedTypeRef): GNamedType? = resolveType(ref.name)

	/** Resolves a type by name, or `null` if not found. */
	public fun resolveType(name: String): GNamedType? = typesByName[name]

	/** Returns the root object type for the given [operationType] (query, mutation, or subscription). */
	public fun rootTypeForOperationType(operationType: GOperationType): GObjectType? = when (operationType) {
		GOperationType.mutation -> mutationType
		GOperationType.query -> queryType
		GOperationType.subscription -> subscriptionType
	}

	/**
	 * Returns this schema as SDL, exactly as [printSchema] does but indented with tabs to match
	 * [GNode.toString].
	 */
	override fun toString(): String = printSchema(schema = this, indent = "\t")

	/**
	 * Validates that [value] is a legal GraphQL value for [type].
	 *
	 * Returns a list of [GError]s describing any violations; an empty list means the value is valid.
	 * [GVariableRef] values (`$var`) are not validated and are silently skipped.
	 *
	 * A scalar literal is checked by coercing it with the coercion the scalar defines itself. An executor
	 * that lets a coercion be attached to a type must use [validateValueForExecution] instead, or it accepts
	 * literals that execution then rejects.
	 */
	public fun validateValue(value: GValue, type: GType): List<GError> =
		validateValue(value = value, typeRef = null, type = type, scalarLiteralCoercionOverride = noScalarLiteralCoercionOverride).orEmpty()

	/**
	 * Validates that [value] is a legal GraphQL value for the type identified by [typeRef].
	 *
	 * A scalar literal is checked by coercing it with the coercion the scalar defines itself. An executor
	 * that lets a coercion be attached to a type must use [validateValueForExecution] instead, or it accepts
	 * literals that execution then rejects.
	 */
	public fun validateValue(value: GValue, typeRef: GTypeRef): List<GError> =
		validateValue(value = value, typeRef = typeRef, type = null, scalarLiteralCoercionOverride = noScalarLiteralCoercionOverride).orEmpty()

	/**
	 * Validates that [value] is a legal GraphQL value for [type], or for the type [typeRef] identifies when
	 * no [type] is given, checking every scalar literal with the coercion the executor will apply to it.
	 *
	 * Behaves like [validateValue] in every other respect. Give either [type] or [typeRef]; the value is
	 * accepted unchecked when neither resolves to a type.
	 *
	 * @param scalarLiteralCoercionOverride Returns the coercion that takes precedence over the one the given
	 *   scalar defines itself, or `null` to use the scalar's own. Must answer exactly what the caller's
	 *   execution path applies, or validation and execution disagree about which literals are legal.
	 */
	@InternalGraphqlApi
	public fun validateValueForExecution(
		value: GValue,
		type: GType?,
		typeRef: GTypeRef?,
		scalarLiteralCoercionOverride: (type: GScalarType) -> ((value: GValue) -> Any?)?,
	): List<GError> = validateValue(value = value, typeRef = typeRef, type = type, scalarLiteralCoercionOverride = scalarLiteralCoercionOverride).orEmpty()

	// FIXME not here
	@Suppress("NAME_SHADOWING")
	private fun validateValue(
		value: GValue,
		typeRef: GTypeRef?,
		type: GType?,
		scalarLiteralCoercionOverride: (type: GScalarType) -> ((value: GValue) -> Any?)?,
		fullyWrappedTypeRef: GTypeRef? = typeRef,
		errors: MutableList<GError>? = null,
	): List<GError>? {
		val type = type
			?: typeRef?.let { resolveType(it) } // FIXME resolve in execution context
			?: return null // We don't check types - only values.

		var errors = errors

		// no inline: https://youtrack.jetbrains.com/issue/KT-31371
		/* inline */
		fun addError(error: GError) {
			(errors ?: mutableListOf<GError>().also { errors = it }).add(error)
		}

		fun reportError(message: String? = null, nodeInsteadOfTypeRef: GNode? = null) {
			val message = message ?: run {
				val valueText = when (value) {
					is GListValue -> "a list value"
					is GObjectValue -> "an input object value"
					else -> "value '$value'"
				}

				"Type '${fullyWrappedTypeRef?.underlyingName ?: type.name}' does not allow $valueText."
			}

			addError(
				GError(
					message = message,
					nodes = listOfNotNull(value, nodeInsteadOfTypeRef ?: fullyWrappedTypeRef),
				),
			)
		}

		// The generic catch is deliberate: a scalar coercion is arbitrary code, and letting a non-GraphQL
		// throwable escape would break the contract of *returning* the problems found. The executor does the
		// opposite and hands such a throwable to `GExceptionHandler`, which has no counterpart here.
		// no inline: https://youtrack.jetbrains.com/issue/KT-31371
		/* inline */
		@Suppress("TooGenericExceptionCaught")
		fun reportRejectedScalarLiteral(scalarType: GScalarType) {
			val coerce = scalarLiteralCoercionOverride(scalarType)
				?: scalarType.coerceInputLiteral
				// `GNullValue` is answered before a literal reaches here, so `unwrap` cannot yield null.
				?: { literal -> scalarType.coerceInputValue(checkNotNull(literal.unwrap()) { "A null literal must not reach scalar coercion." }) }

			try {
				coerce(value)
			} catch (exception: GErrorException) {
				// The error is kept whole — its extensions carry the machine-readable half of what a coercion
				// reports, and rebuilding it from the message alone would discard exactly that while keeping
				// the prose. Only the location is added, because that is the one thing a coercion cannot know:
				// it sees a value, never where in the document the value was written. A coercion that did set
				// its own nodes is left alone.
				for (error in exception.errors) {
					addError(
						when {
							error.nodes.isEmpty() -> error.copy(nodes = listOfNotNull(value, fullyWrappedTypeRef))
							else -> error
						},
					)
				}
			} catch (exception: CancellationException) {
				// Cancellation is not a coercion failure: swallowing it would keep a cancelled caller working.
				throw exception
			} catch (exception: Throwable) {
				// `message` is null for exceptions carrying none, e.g. a bare `NullPointerException`, where the
				// class name is the only thing left worth reporting.
				val described = exception.message ?: exception::class.simpleName ?: "unknown error"

				reportError(message = "Expected value of type \"${scalarType.name}\", but encountered error \"$described\"; found: $value.")
			}
		}

		// We don't support variables here yet.
		if (value is GVariableRef) {
			return null
		}

		if (type is GNonNullType && value is GNullValue) {
			reportError()

			return errors
		}

		val isValidValue = when (val namedType = type.nullableType) {
			is GEnumType ->
				when (value) {
					is GEnumValue ->
						namedType.value(value.name) !== null

					is GNullValue ->
						true

					is GBooleanValue,
					is GFloatValue,
					is GIntValue,
					is GListValue,
					is GObjectValue,
					is GStringValue,
					is GVariableRef,
					-> false
				}

			is GInputObjectType ->
				when (value) {
					is GObjectValue -> {
						// Only syntactic fields can be counted here. The runtime value of a variable is unknown at
						// validation time and is therefore checked again during coercion.
						// https://spec.graphql.org/draft/#sec-OneOf-Input-Objects.Input-Coercion
						if (namedType.directive(GLanguage.defaultOneOfDirective.name) !== null) {
							val field = value.arguments.distinctBy { it.name }.singleOrNull()
							if (field === null || field.value is GNullValue) {
								reportError(message = oneOfViolationMessage(typeName = namedType.name))
							}
						}

						for (argumentDefinition in namedType.argumentDefinitions) {
							if (argumentDefinition.isRequired()) {
								if (value.argument(argumentDefinition.name) === null) {
									reportError(
										message = "Required field '${argumentDefinition.name}' of type '${namedType.name}' is missing.",
										nodeInsteadOfTypeRef = argumentDefinition.nameNode,
									)
								}
							}
						}

						for (field in value.arguments) {
							val argumentDefinition = namedType.argumentDefinition(field.name)
							if (argumentDefinition !== null) {
								validateValue(
									value = field.value,
									typeRef = argumentDefinition.type,
									type = null,
									scalarLiteralCoercionOverride = scalarLiteralCoercionOverride,
									errors = errors ?: mutableListOf<GError>().also { errors = it },
								)
							} else {
								reportError()
							}
						}

						true
					}

					is GNullValue ->
						true

					is GBooleanValue,
					is GEnumValue,
					is GFloatValue,
					is GIntValue,
					is GListValue,
					is GStringValue,
					is GVariableRef,
					-> false
				}

			is GListType ->
				when (value) {
					is GListValue -> {
						for (element in value.elements) {
							validateValue(
								value = element,
								typeRef = (typeRef?.nullableRef as GListTypeRef?)?.elementType,
								type = namedType.elementType,
								scalarLiteralCoercionOverride = scalarLiteralCoercionOverride,
								fullyWrappedTypeRef = fullyWrappedTypeRef,
								errors = errors ?: mutableListOf<GError>().also { errors = it },
							)
						}

						true
					}

					is GBooleanValue,
					is GEnumValue,
					is GFloatValue,
					is GIntValue,
					is GObjectValue,
					is GStringValue,
					-> {
						validateValue(
							value = value,
							typeRef = (typeRef?.nullableRef as GListTypeRef?)?.elementType,
							type = namedType.elementType,
							scalarLiteralCoercionOverride = scalarLiteralCoercionOverride,
							fullyWrappedTypeRef = fullyWrappedTypeRef,
							errors = errors ?: mutableListOf<GError>().also { errors = it },
						)

						true
					}

					is GNullValue ->
						true

					is GVariableRef ->
						false
				}

			is GNonNullType ->
				error("Impossible.")

			// Every literal kind is listed rather than defaulted: a new one must be decided on here instead
			// of silently reaching the coercion, and no scalar can end up in an accept-everything arm that
			// was written for custom scalars.
			is GScalarType ->
				when (value) {
					is GNullValue ->
						true

					is GVariableRef ->
						false

					is GBooleanValue,
					is GEnumValue,
					is GFloatValue,
					is GIntValue,
					is GListValue,
					is GObjectValue,
					is GStringValue,
					-> {
						// A rejection is reported with the scalar's own wording, leaving nothing for the
						// generic message below to add.
						reportRejectedScalarLiteral(namedType)

						true
					}
				}

			is GInterfaceType,
			is GObjectType,
			is GUnionType,
			->
				true // We don't check types - only values.
		}

		if (!isValidValue) {
			reportError()
		}

		return errors
	}

	public companion object {

		/**
		 * Parses a GraphQL SDL document from [source] and builds a [GSchema].
		 *
		 * Returns a [GResult.Success] with the schema, or a [GResult.Failure] with parse errors. Only
		 * *parse* errors travel in the result: a document that parses but cannot be turned into a schema
		 * throws, exactly as the [GSchema] factory does. A schema that merely violates a specification rule
		 * is returned successfully — call [validate] or [assertValid] on it.
		 *
		 * @throws GErrorException if the parsed document defines two types of the same name, or a type whose
		 *   name is reserved.
		 */
		public fun parse(source: GDocumentSource.Parsable): GResult<GSchema> = GDocument.parse(source).mapValue(::GSchema)

		/**
		 * Parses a GraphQL SDL string and builds a [GSchema].
		 *
		 * Behaves exactly like the [parse] overload taking a [GDocumentSource.Parsable], including its
		 * throwing behaviour.
		 *
		 * @throws GErrorException if the parsed document defines two types of the same name, or a type whose
		 *   name is reserved.
		 */
		public fun parse(content: String, name: String = "<document>"): GResult<GSchema> = parse(GDocumentSource.of(content = content, name = name))
	}
}

/** Leaves every scalar literal to the coercion the scalar defines itself. */
private val noScalarLiteralCoercionOverride: (type: GScalarType) -> ((value: GValue) -> Any?)? = { null }

/** The names the specification reserves for the introspection types. */
@OptIn(InternalGraphqlApi::class)
private val introspectionTypeNames = GIntrospection.typeNames.toSet()

/**
 * Verifies that no type of [types] carries a name the specification reserves and that no name is defined
 * twice.
 *
 * Reserved are the five built-in scalar names and every name beginning with `__`, which covers the eight
 * introspection type names. Directive names are *not* subject to this rule: a user-declared `@deprecated`
 * legitimately replaces the built-in one.
 *
 * @throws GErrorException describing the first violation found.
 */
// https://spec.graphql.org/draft/#sec-Schema
private fun validateTypeNames(types: List<GNamedType>) {
	val definedNames = hashSetOf<String>()

	for (type in types) {
		val message = when {
			// Deliberately stricter than graphql-js, which reports this from `validateSchema()` instead of
			// refusing to build the schema.
			GLanguage.isValidIntrospectionName(type.name) ->
				"Name \"${type.name}\" must not begin with \"__\", which is reserved by GraphQL introspection."

			// Deliberately stricter than graphql-js, which discards an SDL redefinition silently and throws
			// programmatically only for the built-ins its introspection types happen to make reachable.
			type.name in builtinScalarTypeNames ->
				"Cannot redefine built-in scalar type \"${type.name}\"."

			!definedNames.add(type.name) ->
				"There can be only one type named \"${type.name}\"."

			else -> continue
		}

		GError(message = message, nodes = listOf(type.nameNode)).throwException()
	}
}

/**
 * Collects the names of all types [types] and [directiveDefinitions] refer to.
 *
 * A name is referred to when it is the underlying named type of a type reference written anywhere in those
 * definitions — a field's type, an argument's type, an input field's type, an implemented interface, a union
 * member. Defining a type of that name is not a reference to it.
 */
private fun referencedTypeNames(types: List<GNamedType>, directiveDefinitions: List<GDirectiveDefinition>): Set<String> {
	val names = hashSetOf<String>()

	fun collect(node: GNode) {
		if (node is GTypeRef) {
			names += node.underlyingName
		}

		node.children().forEach(::collect)
	}

	types.forEach(::collect)
	directiveDefinitions.forEach(::collect)

	return names
}

/**
 * Builds a [GSchema] from an already-parsed [GDocument].
 *
 * Extracts type and directive definitions, resolves root operation types (from an explicit
 * `schema { ... }` definition or by convention: `Query`, `Mutation`, `Subscription`), and
 * adds the standard built-in directives if they are not already defined in the document.
 *
 * Type system extensions (`extend type Foo { … }`, `extend schema { … }`, …) are merged into the
 * definitions they extend; see [mergeTypeExtensions]. An extension of a definition the document does
 * not contain is silently ignored. An operation type declared by a schema extension overrides the one
 * declared by the `schema { … }` definition, or the conventional root type name when there is none.
 *
 * @throws GErrorException if the document defines two types of the same name, or a type whose name is
 *   reserved: one of the built-in scalars (`Boolean`, `Float`, `ID`, `Int`, `String`) or any name that
 *   begins with `__`.
 */
// https://spec.graphql.org/draft/#sec-Schema
public fun GSchema(document: GDocument): GSchema = GSchema(document = document, allowsReservedTypeNames = false)

/**
 * Builds a [GSchema] from an already-parsed [GDocument], optionally permitting reserved type names.
 *
 * Behaves exactly like the [GSchema] factory taking only a document, except that [allowsReservedTypeNames]
 * turns off the check for reserved and duplicate type names.
 *
 * This exists to reach the deduplication [GSchema.types] performs, which no document the public factory
 * accepts can trigger: a definition that shadows a built-in scalar or an introspection type is dropped in
 * favour of the library's own type.
 *
 * @param allowsReservedTypeNames Whether to accept type names the specification reserves.
 * @throws GErrorException if the document defines a reserved or duplicate type name and
 *   [allowsReservedTypeNames] is `false`.
 */
internal fun GSchema(document: GDocument, allowsReservedTypeNames: Boolean): GSchema {
	val typeSystemDefinitions = document.definitions.filterIsInstance<GTypeSystemDefinition>()
	val typeSystemExtensions = document.definitions.filterIsInstance<GTypeSystemExtension>()

	val directiveDefinitions = typeSystemDefinitions.filterIsInstance<GDirectiveDefinition>().toMutableList()

	// Specified directives are added only when the document does not declare one of the same name.
	// https://spec.graphql.org/draft/#sec-Type-System.Directives.Built-in-Directives
	val defaultDirectiveDefinitions = listOf(
		GLanguage.defaultDeprecatedDirective,
		GLanguage.defaultIncludeDirective,
		GLanguage.defaultOneOfDirective,
		GLanguage.defaultSkipDirective,
		GLanguage.defaultSpecifiedByDirective,
	)

	for (defaultDirectiveDefinition in defaultDirectiveDefinitions) {
		if (directiveDefinitions.none { it.name == defaultDirectiveDefinition.name }) {
			directiveDefinitions += defaultDirectiveDefinition
		}
	}

	val schemaDefinition = typeSystemDefinitions.filterIsInstance<GSchemaDefinition>()
		.singleOrNull() // FIXME
	val schemaExtensions = typeSystemExtensions.filterIsInstance<GSchemaExtension>()

	// Extensions must be merged here rather than in the constructor: the constructor derives interface and
	// union membership from the types it receives, which would go out of sync with the merged ones.
	val typeDefinitions = mergeTypeExtensions(
		typeDefinitions = typeSystemDefinitions.filterIsInstance<GNamedType>(),
		typeExtensions = typeSystemExtensions.filterIsInstance<GTypeExtension>(),
	)

	if (!allowsReservedTypeNames) {
		validateTypeNames(typeDefinitions)
	}

	fun rootTypeRef(operationType: GOperationType, conventionalTypeName: String): GNamedTypeRef? =
		schemaExtensions.asReversed().firstNotNullOfOrNull { it.operationTypeDefinition(operationType) }?.type
			?: when (schemaDefinition) {
				null -> GTypeRef(conventionalTypeName)
				else -> schemaDefinition.operationTypeDefinition(operationType)?.type
			}

	return GSchema(
		directiveDefinitions = directiveDefinitions,
		document = document,
		mutationType = rootTypeRef(operationType = GOperationType.mutation, conventionalTypeName = GLanguage.defaultMutationTypeName),
		queryType = rootTypeRef(operationType = GOperationType.query, conventionalTypeName = GLanguage.defaultQueryTypeName),
		subscriptionType = rootTypeRef(operationType = GOperationType.subscription, conventionalTypeName = GLanguage.defaultSubscriptionTypeName),
		types = typeDefinitions,
	)
}
