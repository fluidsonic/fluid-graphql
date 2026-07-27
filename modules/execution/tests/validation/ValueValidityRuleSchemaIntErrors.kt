package testing

// Expected errors for `ValueValidityRuleSchemaRejectionTest`, covering every `Int` case in its fixture.
// The strings are verbatim `GError.describe()` renderings and are position-sensitive: their
// `<document>:line:column` blocks and source excerpts index the single fixture declared in
// `ValueValidityRuleSchemaRejectionTest.kt`, so neither the fixture nor these strings can be edited
// in isolation. See docs/scout/testing/validation-test-harness.md for the paste-back workflow.

/** Errors reported for argument definition default values of type `Int`, in the order the rule reports them. */
internal val valueValidityRuleSchemaIntArgumentErrors = listOf(
	"""
		Int cannot represent non-integer value: true

		<document>:66:25
		65 |       argument63: Input! = ""
		66 |       argument64: Int = true
		   |                         ^
		67 |       argument65: Int = VALUE
	""",
	"""
		Int cannot represent non-integer value: VALUE

		<document>:67:25
		66 |       argument64: Int = true
		67 |       argument65: Int = VALUE
		   |                         ^
		68 |       argument66: Int = 1.0
	""",
	"""
		Int cannot represent non-integer value: 1.0

		<document>:68:25
		67 |       argument65: Int = VALUE
		68 |       argument66: Int = 1.0
		   |                         ^
		69 |       argument67: Int = {}
	""",
	"""
		Int cannot represent non-integer value: {}

		<document>:69:25
		68 |       argument66: Int = 1.0
		69 |       argument67: Int = {}
		   |                         ^
		70 |       argument68: Int = []
	""",
	"""
		Int cannot represent non-integer value: []

		<document>:70:25
		69 |       argument67: Int = {}
		70 |       argument68: Int = []
		   |                         ^
		71 |       argument69: Int = ""
	""",
	"""
		Int cannot represent non-integer value: ""

		<document>:71:25
		70 |       argument68: Int = []
		71 |       argument69: Int = ""
		   |                         ^
		72 |       argument70: Int! = null
	""",
	"""
		Type 'Int!' does not allow value 'null'.

		<document>:72:26
		71 |       argument69: Int = ""
		72 |       argument70: Int! = null
		   |                          ^
		73 |       argument71: Int! = true
	""",
	"""
		Int cannot represent non-integer value: true

		<document>:73:26
		72 |       argument70: Int! = null
		73 |       argument71: Int! = true
		   |                          ^
		74 |       argument72: Int! = VALUE
	""",
	"""
		Int cannot represent non-integer value: VALUE

		<document>:74:26
		73 |       argument71: Int! = true
		74 |       argument72: Int! = VALUE
		   |                          ^
		75 |       argument73: Int! = 1.0
	""",
	"""
		Int cannot represent non-integer value: 1.0

		<document>:75:26
		74 |       argument72: Int! = VALUE
		75 |       argument73: Int! = 1.0
		   |                          ^
		76 |       argument74: Int! = {}
	""",
	"""
		Int cannot represent non-integer value: {}

		<document>:76:26
		75 |       argument73: Int! = 1.0
		76 |       argument74: Int! = {}
		   |                          ^
		77 |       argument75: Int! = []
	""",
	"""
		Int cannot represent non-integer value: []

		<document>:77:26
		76 |       argument74: Int! = {}
		77 |       argument75: Int! = []
		   |                          ^
		78 |       argument76: Int! = ""
	""",
	"""
		Int cannot represent non-integer value: ""

		<document>:78:26
		77 |       argument75: Int! = []
		78 |       argument76: Int! = ""
		   |                          ^
		79 |       argument77: [Int] = true
	""",
)

/** Errors reported for input field definition default values of type `Int`, in the order the rule reports them. */
internal val valueValidityRuleSchemaIntInputFieldErrors = listOf(
	"""
		Int cannot represent non-integer value: true

		<document>:189:19
		188 |    field63: Input! = ""
		189 |    field64: Int = true
		    |                   ^
		190 |    field65: Int = VALUE
	""",
	"""
		Int cannot represent non-integer value: VALUE

		<document>:190:19
		189 |    field64: Int = true
		190 |    field65: Int = VALUE
		    |                   ^
		191 |    field66: Int = 1.0
	""",
	"""
		Int cannot represent non-integer value: 1.0

		<document>:191:19
		190 |    field65: Int = VALUE
		191 |    field66: Int = 1.0
		    |                   ^
		192 |    field67: Int = {}
	""",
	"""
		Int cannot represent non-integer value: {}

		<document>:192:19
		191 |    field66: Int = 1.0
		192 |    field67: Int = {}
		    |                   ^
		193 |    field68: Int = []
	""",
	"""
		Int cannot represent non-integer value: []

		<document>:193:19
		192 |    field67: Int = {}
		193 |    field68: Int = []
		    |                   ^
		194 |    field69: Int = ""
	""",
	"""
		Int cannot represent non-integer value: ""

		<document>:194:19
		193 |    field68: Int = []
		194 |    field69: Int = ""
		    |                   ^
		195 |    field70: Int! = null
	""",
	"""
		Type 'Int!' does not allow value 'null'.

		<document>:195:20
		194 |    field69: Int = ""
		195 |    field70: Int! = null
		    |                    ^
		196 |    field71: Int! = true
	""",
	"""
		Int cannot represent non-integer value: true

		<document>:196:20
		195 |    field70: Int! = null
		196 |    field71: Int! = true
		    |                    ^
		197 |    field72: Int! = VALUE
	""",
	"""
		Int cannot represent non-integer value: VALUE

		<document>:197:20
		196 |    field71: Int! = true
		197 |    field72: Int! = VALUE
		    |                    ^
		198 |    field73: Int! = 1.0
	""",
	"""
		Int cannot represent non-integer value: 1.0

		<document>:198:20
		197 |    field72: Int! = VALUE
		198 |    field73: Int! = 1.0
		    |                    ^
		199 |    field74: Int! = {}
	""",
	"""
		Int cannot represent non-integer value: {}

		<document>:199:20
		198 |    field73: Int! = 1.0
		199 |    field74: Int! = {}
		    |                    ^
		200 |    field75: Int! = []
	""",
	"""
		Int cannot represent non-integer value: []

		<document>:200:20
		199 |    field74: Int! = {}
		200 |    field75: Int! = []
		    |                    ^
		201 |    field76: Int! = ""
	""",
	"""
		Int cannot represent non-integer value: ""

		<document>:201:20
		200 |    field75: Int! = []
		201 |    field76: Int! = ""
		    |                    ^
		202 |    field77: [Int] = true
	""",
)
