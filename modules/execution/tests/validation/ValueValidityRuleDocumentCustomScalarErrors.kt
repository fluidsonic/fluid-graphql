package testing

// Expected errors for `ValueValidityRuleDocumentRejectionTest`, covering every `Scalar` case in its fixture.
// The strings are verbatim `GError.describe()` renderings and are position-sensitive: their
// `<document>:line:column` blocks and source excerpts index the single fixture declared in
// `ValueValidityRuleDocumentRejectionTest.kt`, so neither the fixture nor these strings can be edited
// in isolation. See docs/scout/testing/validation-test-harness.md for the paste-back workflow.

/** Errors reported for variable definition default values of type `Scalar`, in the order the rule reports them. */
internal val valueValidityRuleDocumentCustomScalarVariableErrors = listOf(
	"""
		Type 'Scalar!' does not allow value 'null'.

		<document>:103:28
		102 |    ${'$'}variable101: Scalar = []
		103 |    ${'$'}variable102: Scalar! = null
		    |                            ^
		104 |    ${'$'}variable103: Scalar! = VALUE
	""",
)

/** Errors reported for field argument values of type `Scalar`, in the order the rule reports them. */
internal val valueValidityRuleDocumentCustomScalarArgumentErrors = listOf(
	"""
		Type 'Scalar' does not allow value 'null'.

		<document>:223:20
		222 |       argument101: []
		223 |       argument102: null
		    |                    ^
		224 |       argument103: VALUE

		<document>:104:20
		103 |       argument101: Scalar = []
		104 |       argument102: Scalar! = null
		    |                    ^
		105 |       argument103: Scalar! = VALUE
	""",
)

/** Errors reported for input object field values of type `Scalar`, in the order the rule reports them. */
internal val valueValidityRuleDocumentCustomScalarInputFieldErrors = listOf(
	"""
		Type 'Scalar' does not allow value 'null'.

		<document>:342:20
		341 |          field101: []
		342 |          field102: null
		    |                    ^
		343 |          field103: VALUE

		<document>:227:14
		226 |    field101: Scalar = []
		227 |    field102: Scalar! = null
		    |              ^
		228 |    field103: Scalar! = VALUE
	""",
)
