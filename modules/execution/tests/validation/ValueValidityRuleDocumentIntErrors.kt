package testing

// Expected errors for `ValueValidityRuleDocumentRejectionTest`, covering every `Int` case in its fixture.
// The strings are verbatim `GError.describe()` renderings and are position-sensitive: their
// `<document>:line:column` blocks and source excerpts index the single fixture declared in
// `ValueValidityRuleDocumentRejectionTest.kt`, so neither the fixture nor these strings can be edited
// in isolation. See docs/scout/testing/validation-test-harness.md for the paste-back workflow.

/** Errors reported for variable definition default values of type `Int`, in the order the rule reports them. */
internal val valueValidityRuleDocumentIntVariableErrors = listOf(
	"""
		Int cannot represent non-integer value: true

		<document>:65:23
		64 |    ${'$'}variable63: Input! = ""
		65 |    ${'$'}variable64: Int = true
		   |                       ^
		66 |    ${'$'}variable65: Int = VALUE
	""",
	"""
		Int cannot represent non-integer value: VALUE

		<document>:66:23
		65 |    ${'$'}variable64: Int = true
		66 |    ${'$'}variable65: Int = VALUE
		   |                       ^
		67 |    ${'$'}variable66: Int = 1.0
	""",
	"""
		Int cannot represent non-integer value: 1.0

		<document>:67:23
		66 |    ${'$'}variable65: Int = VALUE
		67 |    ${'$'}variable66: Int = 1.0
		   |                       ^
		68 |    ${'$'}variable67: Int = {}
	""",
	"""
		Int cannot represent non-integer value: {}

		<document>:68:23
		67 |    ${'$'}variable66: Int = 1.0
		68 |    ${'$'}variable67: Int = {}
		   |                       ^
		69 |    ${'$'}variable68: Int = []
	""",
	"""
		Int cannot represent non-integer value: []

		<document>:69:23
		68 |    ${'$'}variable67: Int = {}
		69 |    ${'$'}variable68: Int = []
		   |                       ^
		70 |    ${'$'}variable69: Int = ""
	""",
	"""
		Int cannot represent non-integer value: ""

		<document>:70:23
		69 |    ${'$'}variable68: Int = []
		70 |    ${'$'}variable69: Int = ""
		   |                       ^
		71 |    ${'$'}variable70: Int! = null
	""",
	"""
		Type 'Int!' does not allow value 'null'.

		<document>:71:24
		70 |    ${'$'}variable69: Int = ""
		71 |    ${'$'}variable70: Int! = null
		   |                        ^
		72 |    ${'$'}variable71: Int! = true
	""",
	"""
		Int cannot represent non-integer value: true

		<document>:72:24
		71 |    ${'$'}variable70: Int! = null
		72 |    ${'$'}variable71: Int! = true
		   |                        ^
		73 |    ${'$'}variable72: Int! = VALUE
	""",
	"""
		Int cannot represent non-integer value: VALUE

		<document>:73:24
		72 |    ${'$'}variable71: Int! = true
		73 |    ${'$'}variable72: Int! = VALUE
		   |                        ^
		74 |    ${'$'}variable73: Int! = 1.0
	""",
	"""
		Int cannot represent non-integer value: 1.0

		<document>:74:24
		73 |    ${'$'}variable72: Int! = VALUE
		74 |    ${'$'}variable73: Int! = 1.0
		   |                        ^
		75 |    ${'$'}variable74: Int! = {}
	""",
	"""
		Int cannot represent non-integer value: {}

		<document>:75:24
		74 |    ${'$'}variable73: Int! = 1.0
		75 |    ${'$'}variable74: Int! = {}
		   |                        ^
		76 |    ${'$'}variable75: Int! = []
	""",
	"""
		Int cannot represent non-integer value: []

		<document>:76:24
		75 |    ${'$'}variable74: Int! = {}
		76 |    ${'$'}variable75: Int! = []
		   |                        ^
		77 |    ${'$'}variable76: Int! = ""
	""",
	"""
		Int cannot represent non-integer value: ""

		<document>:77:24
		76 |    ${'$'}variable75: Int! = []
		77 |    ${'$'}variable76: Int! = ""
		   |                        ^
		78 |    ${'$'}variable77: [Int] = true
	""",
)

/** Errors reported for field argument values of type `Int`, in the order the rule reports them. */
internal val valueValidityRuleDocumentIntArgumentErrors = listOf(
	"""
		Int cannot represent non-integer value: true

		<document>:185:19
		184 |       argument63: ""
		185 |       argument64: true
		    |                   ^
		186 |       argument65: VALUE

		<document>:66:19
		65 |       argument63: Input! = ""
		66 |       argument64: Int = true
		   |                   ^
		67 |       argument65: Int = VALUE
	""",
	"""
		Int cannot represent non-integer value: VALUE

		<document>:186:19
		185 |       argument64: true
		186 |       argument65: VALUE
		    |                   ^
		187 |       argument66: 1.0

		<document>:67:19
		66 |       argument64: Int = true
		67 |       argument65: Int = VALUE
		   |                   ^
		68 |       argument66: Int = 1.0
	""",
	"""
		Int cannot represent non-integer value: 1.0

		<document>:187:19
		186 |       argument65: VALUE
		187 |       argument66: 1.0
		    |                   ^
		188 |       argument67: {}

		<document>:68:19
		67 |       argument65: Int = VALUE
		68 |       argument66: Int = 1.0
		   |                   ^
		69 |       argument67: Int = {}
	""",
	"""
		Int cannot represent non-integer value: {}

		<document>:188:19
		187 |       argument66: 1.0
		188 |       argument67: {}
		    |                   ^
		189 |       argument68: []

		<document>:69:19
		68 |       argument66: Int = 1.0
		69 |       argument67: Int = {}
		   |                   ^
		70 |       argument68: Int = []
	""",
	"""
		Int cannot represent non-integer value: []

		<document>:189:19
		188 |       argument67: {}
		189 |       argument68: []
		    |                   ^
		190 |       argument69: ""

		<document>:70:19
		69 |       argument67: Int = {}
		70 |       argument68: Int = []
		   |                   ^
		71 |       argument69: Int = ""
	""",
	"""
		Int cannot represent non-integer value: ""

		<document>:190:19
		189 |       argument68: []
		190 |       argument69: ""
		    |                   ^
		191 |       argument70: null

		<document>:71:19
		70 |       argument68: Int = []
		71 |       argument69: Int = ""
		   |                   ^
		72 |       argument70: Int! = null
	""",
	"""
		Type 'Int' does not allow value 'null'.

		<document>:191:19
		190 |       argument69: ""
		191 |       argument70: null
		    |                   ^
		192 |       argument71: true

		<document>:72:19
		71 |       argument69: Int = ""
		72 |       argument70: Int! = null
		   |                   ^
		73 |       argument71: Int! = true
	""",
	"""
		Int cannot represent non-integer value: true

		<document>:192:19
		191 |       argument70: null
		192 |       argument71: true
		    |                   ^
		193 |       argument72: VALUE

		<document>:73:19
		72 |       argument70: Int! = null
		73 |       argument71: Int! = true
		   |                   ^
		74 |       argument72: Int! = VALUE
	""",
	"""
		Int cannot represent non-integer value: VALUE

		<document>:193:19
		192 |       argument71: true
		193 |       argument72: VALUE
		    |                   ^
		194 |       argument73: 1.0

		<document>:74:19
		73 |       argument71: Int! = true
		74 |       argument72: Int! = VALUE
		   |                   ^
		75 |       argument73: Int! = 1.0
	""",
	"""
		Int cannot represent non-integer value: 1.0

		<document>:194:19
		193 |       argument72: VALUE
		194 |       argument73: 1.0
		    |                   ^
		195 |       argument74: {}

		<document>:75:19
		74 |       argument72: Int! = VALUE
		75 |       argument73: Int! = 1.0
		   |                   ^
		76 |       argument74: Int! = {}
	""",
	"""
		Int cannot represent non-integer value: {}

		<document>:195:19
		194 |       argument73: 1.0
		195 |       argument74: {}
		    |                   ^
		196 |       argument75: []

		<document>:76:19
		75 |       argument73: Int! = 1.0
		76 |       argument74: Int! = {}
		   |                   ^
		77 |       argument75: Int! = []
	""",
	"""
		Int cannot represent non-integer value: []

		<document>:196:19
		195 |       argument74: {}
		196 |       argument75: []
		    |                   ^
		197 |       argument76: ""

		<document>:77:19
		76 |       argument74: Int! = {}
		77 |       argument75: Int! = []
		   |                   ^
		78 |       argument76: Int! = ""
	""",
	"""
		Int cannot represent non-integer value: ""

		<document>:197:19
		196 |       argument75: []
		197 |       argument76: ""
		    |                   ^
		198 |       argument77: true

		<document>:78:19
		77 |       argument75: Int! = []
		78 |       argument76: Int! = ""
		   |                   ^
		79 |       argument77: [Int] = true
	""",
)

/** Errors reported for input object field values of type `Int`, in the order the rule reports them. */
internal val valueValidityRuleDocumentIntInputFieldErrors = listOf(
	"""
		Int cannot represent non-integer value: true

		<document>:304:19
		303 |          field63: ""
		304 |          field64: true
		    |                   ^
		305 |          field65: VALUE

		<document>:189:13
		188 |    field63: Input! = ""
		189 |    field64: Int = true
		    |             ^
		190 |    field65: Int = VALUE
	""",
	"""
		Int cannot represent non-integer value: VALUE

		<document>:305:19
		304 |          field64: true
		305 |          field65: VALUE
		    |                   ^
		306 |          field66: 1.0

		<document>:190:13
		189 |    field64: Int = true
		190 |    field65: Int = VALUE
		    |             ^
		191 |    field66: Int = 1.0
	""",
	"""
		Int cannot represent non-integer value: 1.0

		<document>:306:19
		305 |          field65: VALUE
		306 |          field66: 1.0
		    |                   ^
		307 |          field67: {}

		<document>:191:13
		190 |    field65: Int = VALUE
		191 |    field66: Int = 1.0
		    |             ^
		192 |    field67: Int = {}
	""",
	"""
		Int cannot represent non-integer value: {}

		<document>:307:19
		306 |          field66: 1.0
		307 |          field67: {}
		    |                   ^
		308 |          field68: []

		<document>:192:13
		191 |    field66: Int = 1.0
		192 |    field67: Int = {}
		    |             ^
		193 |    field68: Int = []
	""",
	"""
		Int cannot represent non-integer value: []

		<document>:308:19
		307 |          field67: {}
		308 |          field68: []
		    |                   ^
		309 |          field69: ""

		<document>:193:13
		192 |    field67: Int = {}
		193 |    field68: Int = []
		    |             ^
		194 |    field69: Int = ""
	""",
	"""
		Int cannot represent non-integer value: ""

		<document>:309:19
		308 |          field68: []
		309 |          field69: ""
		    |                   ^
		310 |          field70: null

		<document>:194:13
		193 |    field68: Int = []
		194 |    field69: Int = ""
		    |             ^
		195 |    field70: Int! = null
	""",
	"""
		Type 'Int' does not allow value 'null'.

		<document>:310:19
		309 |          field69: ""
		310 |          field70: null
		    |                   ^
		311 |          field71: true

		<document>:195:13
		194 |    field69: Int = ""
		195 |    field70: Int! = null
		    |             ^
		196 |    field71: Int! = true
	""",
	"""
		Int cannot represent non-integer value: true

		<document>:311:19
		310 |          field70: null
		311 |          field71: true
		    |                   ^
		312 |          field72: VALUE

		<document>:196:13
		195 |    field70: Int! = null
		196 |    field71: Int! = true
		    |             ^
		197 |    field72: Int! = VALUE
	""",
	"""
		Int cannot represent non-integer value: VALUE

		<document>:312:19
		311 |          field71: true
		312 |          field72: VALUE
		    |                   ^
		313 |          field73: 1.0

		<document>:197:13
		196 |    field71: Int! = true
		197 |    field72: Int! = VALUE
		    |             ^
		198 |    field73: Int! = 1.0
	""",
	"""
		Int cannot represent non-integer value: 1.0

		<document>:313:19
		312 |          field72: VALUE
		313 |          field73: 1.0
		    |                   ^
		314 |          field74: {}

		<document>:198:13
		197 |    field72: Int! = VALUE
		198 |    field73: Int! = 1.0
		    |             ^
		199 |    field74: Int! = {}
	""",
	"""
		Int cannot represent non-integer value: {}

		<document>:314:19
		313 |          field73: 1.0
		314 |          field74: {}
		    |                   ^
		315 |          field75: []

		<document>:199:13
		198 |    field73: Int! = 1.0
		199 |    field74: Int! = {}
		    |             ^
		200 |    field75: Int! = []
	""",
	"""
		Int cannot represent non-integer value: []

		<document>:315:19
		314 |          field74: {}
		315 |          field75: []
		    |                   ^
		316 |          field76: ""

		<document>:200:13
		199 |    field74: Int! = {}
		200 |    field75: Int! = []
		    |             ^
		201 |    field76: Int! = ""
	""",
	"""
		Int cannot represent non-integer value: ""

		<document>:316:19
		315 |          field75: []
		316 |          field76: ""
		    |                   ^
		317 |          field77: true

		<document>:201:13
		200 |    field75: Int! = []
		201 |    field76: Int! = ""
		    |             ^
		202 |    field77: [Int] = true
	""",
)
