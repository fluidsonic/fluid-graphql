package testing

// Expected errors for `ValueValidityRuleDocumentRejectionTest`, covering every `ID` case in its fixture.
// The strings are verbatim `GError.describe()` renderings and are position-sensitive: their
// `<document>:line:column` blocks and source excerpts index the single fixture declared in
// `ValueValidityRuleDocumentRejectionTest.kt`, so neither the fixture nor these strings can be edited
// in isolation. See docs/scout/testing/validation-test-harness.md for the paste-back workflow.

/** Errors reported for variable definition default values of type `ID`, in the order the rule reports them. */
internal val valueValidityRuleDocumentIdVariableErrors = listOf(
	"""
		ID cannot represent a non-string and non-integer value: true

		<document>:41:22
		40 |    ${'$'}variable39: Float! = ""
		41 |    ${'$'}variable40: ID = true
		   |                      ^
		42 |    ${'$'}variable41: ID = 1.0
	""",
	"""
		ID cannot represent a non-string and non-integer value: 1.0

		<document>:42:22
		41 |    ${'$'}variable40: ID = true
		42 |    ${'$'}variable41: ID = 1.0
		   |                      ^
		43 |    ${'$'}variable44: ID = VALUE
	""",
	"""
		ID cannot represent a non-string and non-integer value: VALUE

		<document>:43:22
		42 |    ${'$'}variable41: ID = 1.0
		43 |    ${'$'}variable44: ID = VALUE
		   |                      ^
		44 |    ${'$'}variable42: ID = []
	""",
	"""
		ID cannot represent a non-string and non-integer value: []

		<document>:44:22
		43 |    ${'$'}variable44: ID = VALUE
		44 |    ${'$'}variable42: ID = []
		   |                      ^
		45 |    ${'$'}variable43: ID = {}
	""",
	"""
		ID cannot represent a non-string and non-integer value: {}

		<document>:45:22
		44 |    ${'$'}variable42: ID = []
		45 |    ${'$'}variable43: ID = {}
		   |                      ^
		46 |    ${'$'}variable45: ID! = null
	""",
	"""
		Type 'ID!' does not allow value 'null'.

		<document>:46:23
		45 |    ${'$'}variable43: ID = {}
		46 |    ${'$'}variable45: ID! = null
		   |                       ^
		47 |    ${'$'}variable46: ID! = true
	""",
	"""
		ID cannot represent a non-string and non-integer value: true

		<document>:47:23
		46 |    ${'$'}variable45: ID! = null
		47 |    ${'$'}variable46: ID! = true
		   |                       ^
		48 |    ${'$'}variable47: ID! = 1.0
	""",
	"""
		ID cannot represent a non-string and non-integer value: 1.0

		<document>:48:23
		47 |    ${'$'}variable46: ID! = true
		48 |    ${'$'}variable47: ID! = 1.0
		   |                       ^
		49 |    ${'$'}variable48: ID! = VALUE
	""",
	"""
		ID cannot represent a non-string and non-integer value: VALUE

		<document>:49:23
		48 |    ${'$'}variable47: ID! = 1.0
		49 |    ${'$'}variable48: ID! = VALUE
		   |                       ^
		50 |    ${'$'}variable49: ID! = []
	""",
	"""
		ID cannot represent a non-string and non-integer value: []

		<document>:50:23
		49 |    ${'$'}variable48: ID! = VALUE
		50 |    ${'$'}variable49: ID! = []
		   |                       ^
		51 |    ${'$'}variable50: ID! = {}
	""",
	"""
		ID cannot represent a non-string and non-integer value: {}

		<document>:51:23
		50 |    ${'$'}variable49: ID! = []
		51 |    ${'$'}variable50: ID! = {}
		   |                       ^
		52 |    ${'$'}variable51: Input = true
	""",
)

/** Errors reported for field argument values of type `ID`, in the order the rule reports them. */
internal val valueValidityRuleDocumentIdArgumentErrors = listOf(
	"""
		ID cannot represent a non-string and non-integer value: true

		<document>:161:19
		160 |       argument39: ""
		161 |       argument40: true
		    |                   ^
		162 |       argument41: 1.0

		<document>:42:19
		41 |       argument39: Float! = ""
		42 |       argument40: ID = true
		   |                   ^
		43 |       argument41: ID = 1.0
	""",
	"""
		ID cannot represent a non-string and non-integer value: 1.0

		<document>:162:19
		161 |       argument40: true
		162 |       argument41: 1.0
		    |                   ^
		163 |       argument44: VALUE

		<document>:43:19
		42 |       argument40: ID = true
		43 |       argument41: ID = 1.0
		   |                   ^
		44 |       argument44: ID = VALUE
	""",
	"""
		ID cannot represent a non-string and non-integer value: VALUE

		<document>:163:19
		162 |       argument41: 1.0
		163 |       argument44: VALUE
		    |                   ^
		164 |       argument42: []

		<document>:44:19
		43 |       argument41: ID = 1.0
		44 |       argument44: ID = VALUE
		   |                   ^
		45 |       argument42: ID = []
	""",
	"""
		ID cannot represent a non-string and non-integer value: []

		<document>:164:19
		163 |       argument44: VALUE
		164 |       argument42: []
		    |                   ^
		165 |       argument43: {}

		<document>:45:19
		44 |       argument44: ID = VALUE
		45 |       argument42: ID = []
		   |                   ^
		46 |       argument43: ID = {}
	""",
	"""
		ID cannot represent a non-string and non-integer value: {}

		<document>:165:19
		164 |       argument42: []
		165 |       argument43: {}
		    |                   ^
		166 |       argument45: null

		<document>:46:19
		45 |       argument42: ID = []
		46 |       argument43: ID = {}
		   |                   ^
		47 |       argument45: ID! = null
	""",
	"""
		Type 'ID' does not allow value 'null'.

		<document>:166:19
		165 |       argument43: {}
		166 |       argument45: null
		    |                   ^
		167 |       argument46: true

		<document>:47:19
		46 |       argument43: ID = {}
		47 |       argument45: ID! = null
		   |                   ^
		48 |       argument46: ID! = true
	""",
	"""
		ID cannot represent a non-string and non-integer value: true

		<document>:167:19
		166 |       argument45: null
		167 |       argument46: true
		    |                   ^
		168 |       argument47: 1.0

		<document>:48:19
		47 |       argument45: ID! = null
		48 |       argument46: ID! = true
		   |                   ^
		49 |       argument47: ID! = 1.0
	""",
	"""
		ID cannot represent a non-string and non-integer value: 1.0

		<document>:168:19
		167 |       argument46: true
		168 |       argument47: 1.0
		    |                   ^
		169 |       argument48: VALUE

		<document>:49:19
		48 |       argument46: ID! = true
		49 |       argument47: ID! = 1.0
		   |                   ^
		50 |       argument48: ID! = VALUE
	""",
	"""
		ID cannot represent a non-string and non-integer value: VALUE

		<document>:169:19
		168 |       argument47: 1.0
		169 |       argument48: VALUE
		    |                   ^
		170 |       argument49: []

		<document>:50:19
		49 |       argument47: ID! = 1.0
		50 |       argument48: ID! = VALUE
		   |                   ^
		51 |       argument49: ID! = []
	""",
	"""
		ID cannot represent a non-string and non-integer value: []

		<document>:170:19
		169 |       argument48: VALUE
		170 |       argument49: []
		    |                   ^
		171 |       argument50: {}

		<document>:51:19
		50 |       argument48: ID! = VALUE
		51 |       argument49: ID! = []
		   |                   ^
		52 |       argument50: ID! = {}
	""",
	"""
		ID cannot represent a non-string and non-integer value: {}

		<document>:171:19
		170 |       argument49: []
		171 |       argument50: {}
		    |                   ^
		172 |       argument51: true

		<document>:52:19
		51 |       argument49: ID! = []
		52 |       argument50: ID! = {}
		   |                   ^
		53 |       argument51: Input = true
	""",
)

/** Errors reported for input object field values of type `ID`, in the order the rule reports them. */
internal val valueValidityRuleDocumentIdInputFieldErrors = listOf(
	"""
		ID cannot represent a non-string and non-integer value: true

		<document>:280:19
		279 |          field39: ""
		280 |          field40: true
		    |                   ^
		281 |          field41: 1.0

		<document>:165:13
		164 |    field39: Float! = ""
		165 |    field40: ID = true
		    |             ^
		166 |    field41: ID = 1.0
	""",
	"""
		ID cannot represent a non-string and non-integer value: 1.0

		<document>:281:19
		280 |          field40: true
		281 |          field41: 1.0
		    |                   ^
		282 |          field44: VALUE

		<document>:166:13
		165 |    field40: ID = true
		166 |    field41: ID = 1.0
		    |             ^
		167 |    field44: ID = VALUE
	""",
	"""
		ID cannot represent a non-string and non-integer value: VALUE

		<document>:282:19
		281 |          field41: 1.0
		282 |          field44: VALUE
		    |                   ^
		283 |          field42: []

		<document>:167:13
		166 |    field41: ID = 1.0
		167 |    field44: ID = VALUE
		    |             ^
		168 |    field42: ID = []
	""",
	"""
		ID cannot represent a non-string and non-integer value: []

		<document>:283:19
		282 |          field44: VALUE
		283 |          field42: []
		    |                   ^
		284 |          field43: {}

		<document>:168:13
		167 |    field44: ID = VALUE
		168 |    field42: ID = []
		    |             ^
		169 |    field43: ID = {}
	""",
	"""
		ID cannot represent a non-string and non-integer value: {}

		<document>:284:19
		283 |          field42: []
		284 |          field43: {}
		    |                   ^
		285 |          field45: null

		<document>:169:13
		168 |    field42: ID = []
		169 |    field43: ID = {}
		    |             ^
		170 |    field45: ID! = null
	""",
	"""
		Type 'ID' does not allow value 'null'.

		<document>:285:19
		284 |          field43: {}
		285 |          field45: null
		    |                   ^
		286 |          field46: true

		<document>:170:13
		169 |    field43: ID = {}
		170 |    field45: ID! = null
		    |             ^
		171 |    field46: ID! = true
	""",
	"""
		ID cannot represent a non-string and non-integer value: true

		<document>:286:19
		285 |          field45: null
		286 |          field46: true
		    |                   ^
		287 |          field47: 1.0

		<document>:171:13
		170 |    field45: ID! = null
		171 |    field46: ID! = true
		    |             ^
		172 |    field47: ID! = 1.0
	""",
	"""
		ID cannot represent a non-string and non-integer value: 1.0

		<document>:287:19
		286 |          field46: true
		287 |          field47: 1.0
		    |                   ^
		288 |          field48: VALUE

		<document>:172:13
		171 |    field46: ID! = true
		172 |    field47: ID! = 1.0
		    |             ^
		173 |    field48: ID! = VALUE
	""",
	"""
		ID cannot represent a non-string and non-integer value: VALUE

		<document>:288:19
		287 |          field47: 1.0
		288 |          field48: VALUE
		    |                   ^
		289 |          field49: []

		<document>:173:13
		172 |    field47: ID! = 1.0
		173 |    field48: ID! = VALUE
		    |             ^
		174 |    field49: ID! = []
	""",
	"""
		ID cannot represent a non-string and non-integer value: []

		<document>:289:19
		288 |          field48: VALUE
		289 |          field49: []
		    |                   ^
		290 |          field50: {}

		<document>:174:13
		173 |    field48: ID! = VALUE
		174 |    field49: ID! = []
		    |             ^
		175 |    field50: ID! = {}
	""",
	"""
		ID cannot represent a non-string and non-integer value: {}

		<document>:290:19
		289 |          field49: []
		290 |          field50: {}
		    |                   ^
		291 |          field51: true

		<document>:175:13
		174 |    field49: ID! = []
		175 |    field50: ID! = {}
		    |             ^
		176 |    field51: Input = true
	""",
)
