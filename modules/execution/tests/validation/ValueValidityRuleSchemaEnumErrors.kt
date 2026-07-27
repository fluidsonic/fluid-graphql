package testing

// Expected errors for `ValueValidityRuleSchemaRejectionTest`, covering every `Enum` case in its fixture.
// The strings are verbatim `GError.describe()` renderings and are position-sensitive: their
// `<document>:line:column` blocks and source excerpts index the single fixture declared in
// `ValueValidityRuleSchemaRejectionTest.kt`, so neither the fixture nor these strings can be edited
// in isolation. See docs/scout/testing/validation-test-harness.md for the paste-back workflow.

/** Errors reported for argument definition default values of type `Enum`, in the order the rule reports them. */
internal val valueValidityRuleSchemaEnumArgumentErrors = listOf(
	"""
		Type 'Enum' does not allow value 'true'.

		<document>:16:26
		15 |       argument13: Boolean! = ""
		16 |       argument14: Enum = true
		   |                          ^
		17 |       argument15: Enum = value
	""",
	"""
		Type 'Enum' does not allow value 'value'.

		<document>:17:26
		16 |       argument14: Enum = true
		17 |       argument15: Enum = value
		   |                          ^
		18 |       argument16: Enum = 1.0
	""",
	"""
		Type 'Enum' does not allow value '1.0'.

		<document>:18:26
		17 |       argument15: Enum = value
		18 |       argument16: Enum = 1.0
		   |                          ^
		19 |       argument17: Enum = 1
	""",
	"""
		Type 'Enum' does not allow value '1'.

		<document>:19:26
		18 |       argument16: Enum = 1.0
		19 |       argument17: Enum = 1
		   |                          ^
		20 |       argument18: Enum = []
	""",
	"""
		Type 'Enum' does not allow a list value.

		<document>:20:26
		19 |       argument17: Enum = 1
		20 |       argument18: Enum = []
		   |                          ^
		21 |       argument19: Enum = {}
	""",
	"""
		Type 'Enum' does not allow an input object value.

		<document>:21:26
		20 |       argument18: Enum = []
		21 |       argument19: Enum = {}
		   |                          ^
		22 |       argument20: Enum = ""
	""",
	"""
		Type 'Enum' does not allow value '""'.

		<document>:22:26
		21 |       argument19: Enum = {}
		22 |       argument20: Enum = ""
		   |                          ^
		23 |       argument21: Enum! = null
	""",
	"""
		Type 'Enum!' does not allow value 'null'.

		<document>:23:27
		22 |       argument20: Enum = ""
		23 |       argument21: Enum! = null
		   |                           ^
		24 |       argument22: Enum! = value
	""",
	"""
		Type 'Enum!' does not allow value 'value'.

		<document>:24:27
		23 |       argument21: Enum! = null
		24 |       argument22: Enum! = value
		   |                           ^
		25 |       argument23: Enum! = true
	""",
	"""
		Type 'Enum!' does not allow value 'true'.

		<document>:25:27
		24 |       argument22: Enum! = value
		25 |       argument23: Enum! = true
		   |                           ^
		26 |       argument24: Enum! = 1.0
	""",
	"""
		Type 'Enum!' does not allow value '1.0'.

		<document>:26:27
		25 |       argument23: Enum! = true
		26 |       argument24: Enum! = 1.0
		   |                           ^
		27 |       argument25: Enum! = 1
	""",
	"""
		Type 'Enum!' does not allow value '1'.

		<document>:27:27
		26 |       argument24: Enum! = 1.0
		27 |       argument25: Enum! = 1
		   |                           ^
		28 |       argument26: Enum! = []
	""",
	"""
		Type 'Enum!' does not allow a list value.

		<document>:28:27
		27 |       argument25: Enum! = 1
		28 |       argument26: Enum! = []
		   |                           ^
		29 |       argument27: Enum! = {}
	""",
	"""
		Type 'Enum!' does not allow an input object value.

		<document>:29:27
		28 |       argument26: Enum! = []
		29 |       argument27: Enum! = {}
		   |                           ^
		30 |       argument28: Enum! = ""
	""",
	"""
		Type 'Enum!' does not allow value '""'.

		<document>:30:27
		29 |       argument27: Enum! = {}
		30 |       argument28: Enum! = ""
		   |                           ^
		31 |       argument29: Float = true
	""",
)

/** Errors reported for input field definition default values of type `Enum`, in the order the rule reports them. */
internal val valueValidityRuleSchemaEnumInputFieldErrors = listOf(
	"""
		Type 'Enum' does not allow value 'true'.

		<document>:139:20
		138 |    field13: Boolean! = ""
		139 |    field14: Enum = true
		    |                    ^
		140 |    field15: Enum = value
	""",
	"""
		Type 'Enum' does not allow value 'value'.

		<document>:140:20
		139 |    field14: Enum = true
		140 |    field15: Enum = value
		    |                    ^
		141 |    field16: Enum = 1.0
	""",
	"""
		Type 'Enum' does not allow value '1.0'.

		<document>:141:20
		140 |    field15: Enum = value
		141 |    field16: Enum = 1.0
		    |                    ^
		142 |    field17: Enum = 1
	""",
	"""
		Type 'Enum' does not allow value '1'.

		<document>:142:20
		141 |    field16: Enum = 1.0
		142 |    field17: Enum = 1
		    |                    ^
		143 |    field18: Enum = []
	""",
	"""
		Type 'Enum' does not allow a list value.

		<document>:143:20
		142 |    field17: Enum = 1
		143 |    field18: Enum = []
		    |                    ^
		144 |    field19: Enum = {}
	""",
	"""
		Type 'Enum' does not allow an input object value.

		<document>:144:20
		143 |    field18: Enum = []
		144 |    field19: Enum = {}
		    |                    ^
		145 |    field20: Enum = ""
	""",
	"""
		Type 'Enum' does not allow value '""'.

		<document>:145:20
		144 |    field19: Enum = {}
		145 |    field20: Enum = ""
		    |                    ^
		146 |    field21: Enum! = null
	""",
	"""
		Type 'Enum!' does not allow value 'null'.

		<document>:146:21
		145 |    field20: Enum = ""
		146 |    field21: Enum! = null
		    |                     ^
		147 |    field22: Enum! = value
	""",
	"""
		Type 'Enum!' does not allow value 'value'.

		<document>:147:21
		146 |    field21: Enum! = null
		147 |    field22: Enum! = value
		    |                     ^
		148 |    field23: Enum! = true
	""",
	"""
		Type 'Enum!' does not allow value 'true'.

		<document>:148:21
		147 |    field22: Enum! = value
		148 |    field23: Enum! = true
		    |                     ^
		149 |    field24: Enum! = 1.0
	""",
	"""
		Type 'Enum!' does not allow value '1.0'.

		<document>:149:21
		148 |    field23: Enum! = true
		149 |    field24: Enum! = 1.0
		    |                     ^
		150 |    field25: Enum! = 1
	""",
	"""
		Type 'Enum!' does not allow value '1'.

		<document>:150:21
		149 |    field24: Enum! = 1.0
		150 |    field25: Enum! = 1
		    |                     ^
		151 |    field26: Enum! = []
	""",
	"""
		Type 'Enum!' does not allow a list value.

		<document>:151:21
		150 |    field25: Enum! = 1
		151 |    field26: Enum! = []
		    |                     ^
		152 |    field27: Enum! = {}
	""",
	"""
		Type 'Enum!' does not allow an input object value.

		<document>:152:21
		151 |    field26: Enum! = []
		152 |    field27: Enum! = {}
		    |                     ^
		153 |    field28: Enum! = ""
	""",
	"""
		Type 'Enum!' does not allow value '""'.

		<document>:153:21
		152 |    field27: Enum! = {}
		153 |    field28: Enum! = ""
		    |                     ^
		154 |    field29: Float = true
	""",
)
