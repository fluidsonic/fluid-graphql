package testing

// Expected errors for `ValueValidityRuleDocumentRejectionTest`, covering every `Enum` case in its fixture.
// The strings are verbatim `GError.describe()` renderings and are position-sensitive: their
// `<document>:line:column` blocks and source excerpts index the single fixture declared in
// `ValueValidityRuleDocumentRejectionTest.kt`, so neither the fixture nor these strings can be edited
// in isolation. See docs/scout/testing/validation-test-harness.md for the paste-back workflow.

/** Errors reported for variable definition default values of type `Enum`, in the order the rule reports them. */
internal val valueValidityRuleDocumentEnumVariableErrors = listOf(
	"""
		Type 'Enum' does not allow value 'true'.

		<document>:15:24
		14 |    ${'$'}variable13: Boolean! = ""
		15 |    ${'$'}variable14: Enum = true
		   |                        ^
		16 |    ${'$'}variable15: Enum = value
	""",
	"""
		Type 'Enum' does not allow value 'value'.

		<document>:16:24
		15 |    ${'$'}variable14: Enum = true
		16 |    ${'$'}variable15: Enum = value
		   |                        ^
		17 |    ${'$'}variable16: Enum = 1.0
	""",
	"""
		Type 'Enum' does not allow value '1.0'.

		<document>:17:24
		16 |    ${'$'}variable15: Enum = value
		17 |    ${'$'}variable16: Enum = 1.0
		   |                        ^
		18 |    ${'$'}variable17: Enum = 1
	""",
	"""
		Type 'Enum' does not allow value '1'.

		<document>:18:24
		17 |    ${'$'}variable16: Enum = 1.0
		18 |    ${'$'}variable17: Enum = 1
		   |                        ^
		19 |    ${'$'}variable18: Enum = []
	""",
	"""
		Type 'Enum' does not allow a list value.

		<document>:19:24
		18 |    ${'$'}variable17: Enum = 1
		19 |    ${'$'}variable18: Enum = []
		   |                        ^
		20 |    ${'$'}variable19: Enum = {}
	""",
	"""
		Type 'Enum' does not allow an input object value.

		<document>:20:24
		19 |    ${'$'}variable18: Enum = []
		20 |    ${'$'}variable19: Enum = {}
		   |                        ^
		21 |    ${'$'}variable20: Enum = ""
	""",
	"""
		Type 'Enum' does not allow value '""'.

		<document>:21:24
		20 |    ${'$'}variable19: Enum = {}
		21 |    ${'$'}variable20: Enum = ""
		   |                        ^
		22 |    ${'$'}variable21: Enum! = null
	""",
	"""
		Type 'Enum!' does not allow value 'null'.

		<document>:22:25
		21 |    ${'$'}variable20: Enum = ""
		22 |    ${'$'}variable21: Enum! = null
		   |                         ^
		23 |    ${'$'}variable22: Enum! = value
	""",
	"""
		Type 'Enum!' does not allow value 'value'.

		<document>:23:25
		22 |    ${'$'}variable21: Enum! = null
		23 |    ${'$'}variable22: Enum! = value
		   |                         ^
		24 |    ${'$'}variable23: Enum! = true
	""",
	"""
		Type 'Enum!' does not allow value 'true'.

		<document>:24:25
		23 |    ${'$'}variable22: Enum! = value
		24 |    ${'$'}variable23: Enum! = true
		   |                         ^
		25 |    ${'$'}variable24: Enum! = 1.0
	""",
	"""
		Type 'Enum!' does not allow value '1.0'.

		<document>:25:25
		24 |    ${'$'}variable23: Enum! = true
		25 |    ${'$'}variable24: Enum! = 1.0
		   |                         ^
		26 |    ${'$'}variable25: Enum! = 1
	""",
	"""
		Type 'Enum!' does not allow value '1'.

		<document>:26:25
		25 |    ${'$'}variable24: Enum! = 1.0
		26 |    ${'$'}variable25: Enum! = 1
		   |                         ^
		27 |    ${'$'}variable26: Enum! = []
	""",
	"""
		Type 'Enum!' does not allow a list value.

		<document>:27:25
		26 |    ${'$'}variable25: Enum! = 1
		27 |    ${'$'}variable26: Enum! = []
		   |                         ^
		28 |    ${'$'}variable27: Enum! = {}
	""",
	"""
		Type 'Enum!' does not allow an input object value.

		<document>:28:25
		27 |    ${'$'}variable26: Enum! = []
		28 |    ${'$'}variable27: Enum! = {}
		   |                         ^
		29 |    ${'$'}variable28: Enum! = ""
	""",
	"""
		Type 'Enum!' does not allow value '""'.

		<document>:29:25
		28 |    ${'$'}variable27: Enum! = {}
		29 |    ${'$'}variable28: Enum! = ""
		   |                         ^
		30 |    ${'$'}variable29: Float = true
	""",
)

/** Errors reported for field argument values of type `Enum`, in the order the rule reports them. */
internal val valueValidityRuleDocumentEnumArgumentErrors = listOf(
	"""
		Type 'Enum' does not allow value 'true'.

		<document>:135:19
		134 |       argument13: ""
		135 |       argument14: true
		    |                   ^
		136 |       argument15: value

		<document>:16:19
		15 |       argument13: Boolean! = ""
		16 |       argument14: Enum = true
		   |                   ^
		17 |       argument15: Enum = value
	""",
	"""
		Type 'Enum' does not allow value 'value'.

		<document>:136:19
		135 |       argument14: true
		136 |       argument15: value
		    |                   ^
		137 |       argument16: 1.0

		<document>:17:19
		16 |       argument14: Enum = true
		17 |       argument15: Enum = value
		   |                   ^
		18 |       argument16: Enum = 1.0
	""",
	"""
		Type 'Enum' does not allow value '1.0'.

		<document>:137:19
		136 |       argument15: value
		137 |       argument16: 1.0
		    |                   ^
		138 |       argument17: 1

		<document>:18:19
		17 |       argument15: Enum = value
		18 |       argument16: Enum = 1.0
		   |                   ^
		19 |       argument17: Enum = 1
	""",
	"""
		Type 'Enum' does not allow value '1'.

		<document>:138:19
		137 |       argument16: 1.0
		138 |       argument17: 1
		    |                   ^
		139 |       argument18: []

		<document>:19:19
		18 |       argument16: Enum = 1.0
		19 |       argument17: Enum = 1
		   |                   ^
		20 |       argument18: Enum = []
	""",
	"""
		Type 'Enum' does not allow a list value.

		<document>:139:19
		138 |       argument17: 1
		139 |       argument18: []
		    |                   ^
		140 |       argument19: {}

		<document>:20:19
		19 |       argument17: Enum = 1
		20 |       argument18: Enum = []
		   |                   ^
		21 |       argument19: Enum = {}
	""",
	"""
		Type 'Enum' does not allow an input object value.

		<document>:140:19
		139 |       argument18: []
		140 |       argument19: {}
		    |                   ^
		141 |       argument20: ""

		<document>:21:19
		20 |       argument18: Enum = []
		21 |       argument19: Enum = {}
		   |                   ^
		22 |       argument20: Enum = ""
	""",
	"""
		Type 'Enum' does not allow value '""'.

		<document>:141:19
		140 |       argument19: {}
		141 |       argument20: ""
		    |                   ^
		142 |       argument21: null

		<document>:22:19
		21 |       argument19: Enum = {}
		22 |       argument20: Enum = ""
		   |                   ^
		23 |       argument21: Enum! = null
	""",
	"""
		Type 'Enum' does not allow value 'null'.

		<document>:142:19
		141 |       argument20: ""
		142 |       argument21: null
		    |                   ^
		143 |       argument22: value

		<document>:23:19
		22 |       argument20: Enum = ""
		23 |       argument21: Enum! = null
		   |                   ^
		24 |       argument22: Enum! = value
	""",
	"""
		Type 'Enum' does not allow value 'value'.

		<document>:143:19
		142 |       argument21: null
		143 |       argument22: value
		    |                   ^
		144 |       argument23: true

		<document>:24:19
		23 |       argument21: Enum! = null
		24 |       argument22: Enum! = value
		   |                   ^
		25 |       argument23: Enum! = true
	""",
	"""
		Type 'Enum' does not allow value 'true'.

		<document>:144:19
		143 |       argument22: value
		144 |       argument23: true
		    |                   ^
		145 |       argument24: 1.0

		<document>:25:19
		24 |       argument22: Enum! = value
		25 |       argument23: Enum! = true
		   |                   ^
		26 |       argument24: Enum! = 1.0
	""",
	"""
		Type 'Enum' does not allow value '1.0'.

		<document>:145:19
		144 |       argument23: true
		145 |       argument24: 1.0
		    |                   ^
		146 |       argument25: 1

		<document>:26:19
		25 |       argument23: Enum! = true
		26 |       argument24: Enum! = 1.0
		   |                   ^
		27 |       argument25: Enum! = 1
	""",
	"""
		Type 'Enum' does not allow value '1'.

		<document>:146:19
		145 |       argument24: 1.0
		146 |       argument25: 1
		    |                   ^
		147 |       argument26: []

		<document>:27:19
		26 |       argument24: Enum! = 1.0
		27 |       argument25: Enum! = 1
		   |                   ^
		28 |       argument26: Enum! = []
	""",
	"""
		Type 'Enum' does not allow a list value.

		<document>:147:19
		146 |       argument25: 1
		147 |       argument26: []
		    |                   ^
		148 |       argument27: {}

		<document>:28:19
		27 |       argument25: Enum! = 1
		28 |       argument26: Enum! = []
		   |                   ^
		29 |       argument27: Enum! = {}
	""",
	"""
		Type 'Enum' does not allow an input object value.

		<document>:148:19
		147 |       argument26: []
		148 |       argument27: {}
		    |                   ^
		149 |       argument28: ""

		<document>:29:19
		28 |       argument26: Enum! = []
		29 |       argument27: Enum! = {}
		   |                   ^
		30 |       argument28: Enum! = ""
	""",
	"""
		Type 'Enum' does not allow value '""'.

		<document>:149:19
		148 |       argument27: {}
		149 |       argument28: ""
		    |                   ^
		150 |       argument29: true

		<document>:30:19
		29 |       argument27: Enum! = {}
		30 |       argument28: Enum! = ""
		   |                   ^
		31 |       argument29: Float = true
	""",
)

/** Errors reported for input object field values of type `Enum`, in the order the rule reports them. */
internal val valueValidityRuleDocumentEnumInputFieldErrors = listOf(
	"""
		Type 'Enum' does not allow value 'true'.

		<document>:254:19
		253 |          field13: ""
		254 |          field14: true
		    |                   ^
		255 |          field15: value

		<document>:139:13
		138 |    field13: Boolean! = ""
		139 |    field14: Enum = true
		    |             ^
		140 |    field15: Enum = value
	""",
	"""
		Type 'Enum' does not allow value 'value'.

		<document>:255:19
		254 |          field14: true
		255 |          field15: value
		    |                   ^
		256 |          field16: 1.0

		<document>:140:13
		139 |    field14: Enum = true
		140 |    field15: Enum = value
		    |             ^
		141 |    field16: Enum = 1.0
	""",
	"""
		Type 'Enum' does not allow value '1.0'.

		<document>:256:19
		255 |          field15: value
		256 |          field16: 1.0
		    |                   ^
		257 |          field17: 1

		<document>:141:13
		140 |    field15: Enum = value
		141 |    field16: Enum = 1.0
		    |             ^
		142 |    field17: Enum = 1
	""",
	"""
		Type 'Enum' does not allow value '1'.

		<document>:257:19
		256 |          field16: 1.0
		257 |          field17: 1
		    |                   ^
		258 |          field18: []

		<document>:142:13
		141 |    field16: Enum = 1.0
		142 |    field17: Enum = 1
		    |             ^
		143 |    field18: Enum = []
	""",
	"""
		Type 'Enum' does not allow a list value.

		<document>:258:19
		257 |          field17: 1
		258 |          field18: []
		    |                   ^
		259 |          field19: {}

		<document>:143:13
		142 |    field17: Enum = 1
		143 |    field18: Enum = []
		    |             ^
		144 |    field19: Enum = {}
	""",
	"""
		Type 'Enum' does not allow an input object value.

		<document>:259:19
		258 |          field18: []
		259 |          field19: {}
		    |                   ^
		260 |          field20: ""

		<document>:144:13
		143 |    field18: Enum = []
		144 |    field19: Enum = {}
		    |             ^
		145 |    field20: Enum = ""
	""",
	"""
		Type 'Enum' does not allow value '""'.

		<document>:260:19
		259 |          field19: {}
		260 |          field20: ""
		    |                   ^
		261 |          field21: null

		<document>:145:13
		144 |    field19: Enum = {}
		145 |    field20: Enum = ""
		    |             ^
		146 |    field21: Enum! = null
	""",
	"""
		Type 'Enum' does not allow value 'null'.

		<document>:261:19
		260 |          field20: ""
		261 |          field21: null
		    |                   ^
		262 |          field22: value

		<document>:146:13
		145 |    field20: Enum = ""
		146 |    field21: Enum! = null
		    |             ^
		147 |    field22: Enum! = value
	""",
	"""
		Type 'Enum' does not allow value 'value'.

		<document>:262:19
		261 |          field21: null
		262 |          field22: value
		    |                   ^
		263 |          field23: true

		<document>:147:13
		146 |    field21: Enum! = null
		147 |    field22: Enum! = value
		    |             ^
		148 |    field23: Enum! = true
	""",
	"""
		Type 'Enum' does not allow value 'true'.

		<document>:263:19
		262 |          field22: value
		263 |          field23: true
		    |                   ^
		264 |          field24: 1.0

		<document>:148:13
		147 |    field22: Enum! = value
		148 |    field23: Enum! = true
		    |             ^
		149 |    field24: Enum! = 1.0
	""",
	"""
		Type 'Enum' does not allow value '1.0'.

		<document>:264:19
		263 |          field23: true
		264 |          field24: 1.0
		    |                   ^
		265 |          field25: 1

		<document>:149:13
		148 |    field23: Enum! = true
		149 |    field24: Enum! = 1.0
		    |             ^
		150 |    field25: Enum! = 1
	""",
	"""
		Type 'Enum' does not allow value '1'.

		<document>:265:19
		264 |          field24: 1.0
		265 |          field25: 1
		    |                   ^
		266 |          field26: []

		<document>:150:13
		149 |    field24: Enum! = 1.0
		150 |    field25: Enum! = 1
		    |             ^
		151 |    field26: Enum! = []
	""",
	"""
		Type 'Enum' does not allow a list value.

		<document>:266:19
		265 |          field25: 1
		266 |          field26: []
		    |                   ^
		267 |          field27: {}

		<document>:151:13
		150 |    field25: Enum! = 1
		151 |    field26: Enum! = []
		    |             ^
		152 |    field27: Enum! = {}
	""",
	"""
		Type 'Enum' does not allow an input object value.

		<document>:267:19
		266 |          field26: []
		267 |          field27: {}
		    |                   ^
		268 |          field28: ""

		<document>:152:13
		151 |    field26: Enum! = []
		152 |    field27: Enum! = {}
		    |             ^
		153 |    field28: Enum! = ""
	""",
	"""
		Type 'Enum' does not allow value '""'.

		<document>:268:19
		267 |          field27: {}
		268 |          field28: ""
		    |                   ^
		269 |          field29: true

		<document>:153:13
		152 |    field27: Enum! = {}
		153 |    field28: Enum! = ""
		    |             ^
		154 |    field29: Float = true
	""",
)
