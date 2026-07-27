package testing

// Expected errors for `ValueValidityRuleSchemaRejectionTest`, covering every `Scalar` case in its fixture.
// The strings are verbatim `GError.describe()` renderings and are position-sensitive: their
// `<document>:line:column` blocks and source excerpts index the single fixture declared in
// `ValueValidityRuleSchemaRejectionTest.kt`, so neither the fixture nor these strings can be edited
// in isolation. See docs/scout/testing/validation-test-harness.md for the paste-back workflow.

/** Errors reported for argument definition default values of type `Scalar`, in the order the rule reports them. */
internal val valueValidityRuleSchemaCustomScalarArgumentErrors = listOf(
	"""
		Type 'Scalar!' does not allow value 'null'.

		<document>:104:30
		103 |       argument101: Scalar = []
		104 |       argument102: Scalar! = null
		    |                              ^
		105 |       argument103: Scalar! = VALUE
	""",
)

/** Errors reported for input field definition default values of type `Scalar`, in the order the rule reports them. */
internal val valueValidityRuleSchemaCustomScalarInputFieldErrors = listOf(
	"""
		Type 'Scalar!' does not allow value 'null'.

		<document>:227:24
		226 |    field101: Scalar = []
		227 |    field102: Scalar! = null
		    |                        ^
		228 |    field103: Scalar! = VALUE
	""",
)
