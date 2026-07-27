package testing

// Expected errors for `ValueValidityRuleDocumentRejectionTest`, covering every `Float` case in its fixture.
// The strings are verbatim `GError.describe()` renderings and are position-sensitive: their
// `<document>:line:column` blocks and source excerpts index the single fixture declared in
// `ValueValidityRuleDocumentRejectionTest.kt`, so neither the fixture nor these strings can be edited
// in isolation. See docs/scout/testing/validation-test-harness.md for the paste-back workflow.

/** Errors reported for variable definition default values of type `Float`, in the order the rule reports them. */
internal val valueValidityRuleDocumentFloatVariableErrors = listOf(
	"""
		Float cannot represent non numeric value: true

		<document>:30:25
		29 |    ${'$'}variable28: Enum! = ""
		30 |    ${'$'}variable29: Float = true
		   |                         ^
		31 |    ${'$'}variable30: Float = VALUE
	""",
	"""
		Float cannot represent non numeric value: VALUE

		<document>:31:25
		30 |    ${'$'}variable29: Float = true
		31 |    ${'$'}variable30: Float = VALUE
		   |                         ^
		32 |    ${'$'}variable31: Float = []
	""",
	"""
		Float cannot represent non numeric value: []

		<document>:32:25
		31 |    ${'$'}variable30: Float = VALUE
		32 |    ${'$'}variable31: Float = []
		   |                         ^
		33 |    ${'$'}variable32: Float = {}
	""",
	"""
		Float cannot represent non numeric value: {}

		<document>:33:25
		32 |    ${'$'}variable31: Float = []
		33 |    ${'$'}variable32: Float = {}
		   |                         ^
		34 |    ${'$'}variable33: Float = ""
	""",
	"""
		Float cannot represent non numeric value: ""

		<document>:34:25
		33 |    ${'$'}variable32: Float = {}
		34 |    ${'$'}variable33: Float = ""
		   |                         ^
		35 |    ${'$'}variable34: Float! = null
	""",
	"""
		Type 'Float!' does not allow value 'null'.

		<document>:35:26
		34 |    ${'$'}variable33: Float = ""
		35 |    ${'$'}variable34: Float! = null
		   |                          ^
		36 |    ${'$'}variable35: Float! = true
	""",
	"""
		Float cannot represent non numeric value: true

		<document>:36:26
		35 |    ${'$'}variable34: Float! = null
		36 |    ${'$'}variable35: Float! = true
		   |                          ^
		37 |    ${'$'}variable36: Float! = VALUE
	""",
	"""
		Float cannot represent non numeric value: VALUE

		<document>:37:26
		36 |    ${'$'}variable35: Float! = true
		37 |    ${'$'}variable36: Float! = VALUE
		   |                          ^
		38 |    ${'$'}variable37: Float! = []
	""",
	"""
		Float cannot represent non numeric value: []

		<document>:38:26
		37 |    ${'$'}variable36: Float! = VALUE
		38 |    ${'$'}variable37: Float! = []
		   |                          ^
		39 |    ${'$'}variable38: Float! = {}
	""",
	"""
		Float cannot represent non numeric value: {}

		<document>:39:26
		38 |    ${'$'}variable37: Float! = []
		39 |    ${'$'}variable38: Float! = {}
		   |                          ^
		40 |    ${'$'}variable39: Float! = ""
	""",
	"""
		Float cannot represent non numeric value: ""

		<document>:40:26
		39 |    ${'$'}variable38: Float! = {}
		40 |    ${'$'}variable39: Float! = ""
		   |                          ^
		41 |    ${'$'}variable40: ID = true
	""",
)

/** Errors reported for field argument values of type `Float`, in the order the rule reports them. */
internal val valueValidityRuleDocumentFloatArgumentErrors = listOf(
	"""
		Float cannot represent non numeric value: true

		<document>:150:19
		149 |       argument28: ""
		150 |       argument29: true
		    |                   ^
		151 |       argument30: VALUE

		<document>:31:19
		30 |       argument28: Enum! = ""
		31 |       argument29: Float = true
		   |                   ^
		32 |       argument30: Float = VALUE
	""",
	"""
		Float cannot represent non numeric value: VALUE

		<document>:151:19
		150 |       argument29: true
		151 |       argument30: VALUE
		    |                   ^
		152 |       argument31: []

		<document>:32:19
		31 |       argument29: Float = true
		32 |       argument30: Float = VALUE
		   |                   ^
		33 |       argument31: Float = []
	""",
	"""
		Float cannot represent non numeric value: []

		<document>:152:19
		151 |       argument30: VALUE
		152 |       argument31: []
		    |                   ^
		153 |       argument32: {}

		<document>:33:19
		32 |       argument30: Float = VALUE
		33 |       argument31: Float = []
		   |                   ^
		34 |       argument32: Float = {}
	""",
	"""
		Float cannot represent non numeric value: {}

		<document>:153:19
		152 |       argument31: []
		153 |       argument32: {}
		    |                   ^
		154 |       argument33: ""

		<document>:34:19
		33 |       argument31: Float = []
		34 |       argument32: Float = {}
		   |                   ^
		35 |       argument33: Float = ""
	""",
	"""
		Float cannot represent non numeric value: ""

		<document>:154:19
		153 |       argument32: {}
		154 |       argument33: ""
		    |                   ^
		155 |       argument34: null

		<document>:35:19
		34 |       argument32: Float = {}
		35 |       argument33: Float = ""
		   |                   ^
		36 |       argument34: Float! = null
	""",
	"""
		Type 'Float' does not allow value 'null'.

		<document>:155:19
		154 |       argument33: ""
		155 |       argument34: null
		    |                   ^
		156 |       argument35: true

		<document>:36:19
		35 |       argument33: Float = ""
		36 |       argument34: Float! = null
		   |                   ^
		37 |       argument35: Float! = true
	""",
	"""
		Float cannot represent non numeric value: true

		<document>:156:19
		155 |       argument34: null
		156 |       argument35: true
		    |                   ^
		157 |       argument36: VALUE

		<document>:37:19
		36 |       argument34: Float! = null
		37 |       argument35: Float! = true
		   |                   ^
		38 |       argument36: Float! = VALUE
	""",
	"""
		Float cannot represent non numeric value: VALUE

		<document>:157:19
		156 |       argument35: true
		157 |       argument36: VALUE
		    |                   ^
		158 |       argument37: []

		<document>:38:19
		37 |       argument35: Float! = true
		38 |       argument36: Float! = VALUE
		   |                   ^
		39 |       argument37: Float! = []
	""",
	"""
		Float cannot represent non numeric value: []

		<document>:158:19
		157 |       argument36: VALUE
		158 |       argument37: []
		    |                   ^
		159 |       argument38: {}

		<document>:39:19
		38 |       argument36: Float! = VALUE
		39 |       argument37: Float! = []
		   |                   ^
		40 |       argument38: Float! = {}
	""",
	"""
		Float cannot represent non numeric value: {}

		<document>:159:19
		158 |       argument37: []
		159 |       argument38: {}
		    |                   ^
		160 |       argument39: ""

		<document>:40:19
		39 |       argument37: Float! = []
		40 |       argument38: Float! = {}
		   |                   ^
		41 |       argument39: Float! = ""
	""",
	"""
		Float cannot represent non numeric value: ""

		<document>:160:19
		159 |       argument38: {}
		160 |       argument39: ""
		    |                   ^
		161 |       argument40: true

		<document>:41:19
		40 |       argument38: Float! = {}
		41 |       argument39: Float! = ""
		   |                   ^
		42 |       argument40: ID = true
	""",
)

/** Errors reported for input object field values of type `Float`, in the order the rule reports them. */
internal val valueValidityRuleDocumentFloatInputFieldErrors = listOf(
	"""
		Float cannot represent non numeric value: true

		<document>:269:19
		268 |          field28: ""
		269 |          field29: true
		    |                   ^
		270 |          field30: VALUE

		<document>:154:13
		153 |    field28: Enum! = ""
		154 |    field29: Float = true
		    |             ^
		155 |    field30: Float = VALUE
	""",
	"""
		Float cannot represent non numeric value: VALUE

		<document>:270:19
		269 |          field29: true
		270 |          field30: VALUE
		    |                   ^
		271 |          field31: []

		<document>:155:13
		154 |    field29: Float = true
		155 |    field30: Float = VALUE
		    |             ^
		156 |    field31: Float = []
	""",
	"""
		Float cannot represent non numeric value: []

		<document>:271:19
		270 |          field30: VALUE
		271 |          field31: []
		    |                   ^
		272 |          field32: {}

		<document>:156:13
		155 |    field30: Float = VALUE
		156 |    field31: Float = []
		    |             ^
		157 |    field32: Float = {}
	""",
	"""
		Float cannot represent non numeric value: {}

		<document>:272:19
		271 |          field31: []
		272 |          field32: {}
		    |                   ^
		273 |          field33: ""

		<document>:157:13
		156 |    field31: Float = []
		157 |    field32: Float = {}
		    |             ^
		158 |    field33: Float = ""
	""",
	"""
		Float cannot represent non numeric value: ""

		<document>:273:19
		272 |          field32: {}
		273 |          field33: ""
		    |                   ^
		274 |          field34: null

		<document>:158:13
		157 |    field32: Float = {}
		158 |    field33: Float = ""
		    |             ^
		159 |    field34: Float! = null
	""",
	"""
		Type 'Float' does not allow value 'null'.

		<document>:274:19
		273 |          field33: ""
		274 |          field34: null
		    |                   ^
		275 |          field35: true

		<document>:159:13
		158 |    field33: Float = ""
		159 |    field34: Float! = null
		    |             ^
		160 |    field35: Float! = true
	""",
	"""
		Float cannot represent non numeric value: true

		<document>:275:19
		274 |          field34: null
		275 |          field35: true
		    |                   ^
		276 |          field36: VALUE

		<document>:160:13
		159 |    field34: Float! = null
		160 |    field35: Float! = true
		    |             ^
		161 |    field36: Float! = VALUE
	""",
	"""
		Float cannot represent non numeric value: VALUE

		<document>:276:19
		275 |          field35: true
		276 |          field36: VALUE
		    |                   ^
		277 |          field37: []

		<document>:161:13
		160 |    field35: Float! = true
		161 |    field36: Float! = VALUE
		    |             ^
		162 |    field37: Float! = []
	""",
	"""
		Float cannot represent non numeric value: []

		<document>:277:19
		276 |          field36: VALUE
		277 |          field37: []
		    |                   ^
		278 |          field38: {}

		<document>:162:13
		161 |    field36: Float! = VALUE
		162 |    field37: Float! = []
		    |             ^
		163 |    field38: Float! = {}
	""",
	"""
		Float cannot represent non numeric value: {}

		<document>:278:19
		277 |          field37: []
		278 |          field38: {}
		    |                   ^
		279 |          field39: ""

		<document>:163:13
		162 |    field37: Float! = []
		163 |    field38: Float! = {}
		    |             ^
		164 |    field39: Float! = ""
	""",
	"""
		Float cannot represent non numeric value: ""

		<document>:279:19
		278 |          field38: {}
		279 |          field39: ""
		    |                   ^
		280 |          field40: true

		<document>:164:13
		163 |    field38: Float! = {}
		164 |    field39: Float! = ""
		    |             ^
		165 |    field40: ID = true
	""",
)
