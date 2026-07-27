package testing

// Expected errors for `ValueValidityRuleDocumentRejectionTest`, covering every `Input` case in its fixture.
// The strings are verbatim `GError.describe()` renderings and are position-sensitive: their
// `<document>:line:column` blocks and source excerpts index the single fixture declared in
// `ValueValidityRuleDocumentRejectionTest.kt`, so neither the fixture nor these strings can be edited
// in isolation. See docs/scout/testing/validation-test-harness.md for the paste-back workflow.

/** Errors reported for variable definition default values of type `Input`, in the order the rule reports them. */
internal val valueValidityRuleDocumentInputObjectVariableErrors = listOf(
	"""
		Type 'Input' does not allow value 'true'.

		<document>:52:25
		51 |    ${'$'}variable50: ID! = {}
		52 |    ${'$'}variable51: Input = true
		   |                         ^
		53 |    ${'$'}variable52: Input = VALUE
	""",
	"""
		Type 'Input' does not allow value 'VALUE'.

		<document>:53:25
		52 |    ${'$'}variable51: Input = true
		53 |    ${'$'}variable52: Input = VALUE
		   |                         ^
		54 |    ${'$'}variable53: Input = 1.0
	""",
	"""
		Type 'Input' does not allow value '1.0'.

		<document>:54:25
		53 |    ${'$'}variable52: Input = VALUE
		54 |    ${'$'}variable53: Input = 1.0
		   |                         ^
		55 |    ${'$'}variable54: Input = 1
	""",
	"""
		Type 'Input' does not allow value '1'.

		<document>:55:25
		54 |    ${'$'}variable53: Input = 1.0
		55 |    ${'$'}variable54: Input = 1
		   |                         ^
		56 |    ${'$'}variable55: Input = []
	""",
	"""
		Type 'Input' does not allow a list value.

		<document>:56:25
		55 |    ${'$'}variable54: Input = 1
		56 |    ${'$'}variable55: Input = []
		   |                         ^
		57 |    ${'$'}variable56: Input = ""
	""",
	"""
		Type 'Input' does not allow value '""'.

		<document>:57:25
		56 |    ${'$'}variable55: Input = []
		57 |    ${'$'}variable56: Input = ""
		   |                         ^
		58 |    ${'$'}variable57: Input! = null
	""",
	"""
		Type 'Input!' does not allow value 'null'.

		<document>:58:26
		57 |    ${'$'}variable56: Input = ""
		58 |    ${'$'}variable57: Input! = null
		   |                          ^
		59 |    ${'$'}variable58: Input! = true
	""",
	"""
		Type 'Input!' does not allow value 'true'.

		<document>:59:26
		58 |    ${'$'}variable57: Input! = null
		59 |    ${'$'}variable58: Input! = true
		   |                          ^
		60 |    ${'$'}variable59: Input! = VALUE
	""",
	"""
		Type 'Input!' does not allow value 'VALUE'.

		<document>:60:26
		59 |    ${'$'}variable58: Input! = true
		60 |    ${'$'}variable59: Input! = VALUE
		   |                          ^
		61 |    ${'$'}variable60: Input! = 1.0
	""",
	"""
		Type 'Input!' does not allow value '1.0'.

		<document>:61:26
		60 |    ${'$'}variable59: Input! = VALUE
		61 |    ${'$'}variable60: Input! = 1.0
		   |                          ^
		62 |    ${'$'}variable61: Input! = 1
	""",
	"""
		Type 'Input!' does not allow value '1'.

		<document>:62:26
		61 |    ${'$'}variable60: Input! = 1.0
		62 |    ${'$'}variable61: Input! = 1
		   |                          ^
		63 |    ${'$'}variable62: Input! = []
	""",
	"""
		Type 'Input!' does not allow a list value.

		<document>:63:26
		62 |    ${'$'}variable61: Input! = 1
		63 |    ${'$'}variable62: Input! = []
		   |                          ^
		64 |    ${'$'}variable63: Input! = ""
	""",
	"""
		Type 'Input!' does not allow value '""'.

		<document>:64:26
		63 |    ${'$'}variable62: Input! = []
		64 |    ${'$'}variable63: Input! = ""
		   |                          ^
		65 |    ${'$'}variable64: Int = true
	""",
)

/** Errors reported for field argument values of type `Input`, in the order the rule reports them. */
internal val valueValidityRuleDocumentInputObjectArgumentErrors = listOf(
	"""
		Type 'Input' does not allow value 'true'.

		<document>:172:19
		171 |       argument50: {}
		172 |       argument51: true
		    |                   ^
		173 |       argument52: VALUE

		<document>:53:19
		52 |       argument50: ID! = {}
		53 |       argument51: Input = true
		   |                   ^
		54 |       argument52: Input = VALUE
	""",
	"""
		Type 'Input' does not allow value 'VALUE'.

		<document>:173:19
		172 |       argument51: true
		173 |       argument52: VALUE
		    |                   ^
		174 |       argument53: 1.0

		<document>:54:19
		53 |       argument51: Input = true
		54 |       argument52: Input = VALUE
		   |                   ^
		55 |       argument53: Input = 1.0
	""",
	"""
		Type 'Input' does not allow value '1.0'.

		<document>:174:19
		173 |       argument52: VALUE
		174 |       argument53: 1.0
		    |                   ^
		175 |       argument54: 1

		<document>:55:19
		54 |       argument52: Input = VALUE
		55 |       argument53: Input = 1.0
		   |                   ^
		56 |       argument54: Input = 1
	""",
	"""
		Type 'Input' does not allow value '1'.

		<document>:175:19
		174 |       argument53: 1.0
		175 |       argument54: 1
		    |                   ^
		176 |       argument55: []

		<document>:56:19
		55 |       argument53: Input = 1.0
		56 |       argument54: Input = 1
		   |                   ^
		57 |       argument55: Input = []
	""",
	"""
		Type 'Input' does not allow a list value.

		<document>:176:19
		175 |       argument54: 1
		176 |       argument55: []
		    |                   ^
		177 |       argument56: ""

		<document>:57:19
		56 |       argument54: Input = 1
		57 |       argument55: Input = []
		   |                   ^
		58 |       argument56: Input = ""
	""",
	"""
		Type 'Input' does not allow value '""'.

		<document>:177:19
		176 |       argument55: []
		177 |       argument56: ""
		    |                   ^
		178 |       argument57: null

		<document>:58:19
		57 |       argument55: Input = []
		58 |       argument56: Input = ""
		   |                   ^
		59 |       argument57: Input! = null
	""",
	"""
		Type 'Input' does not allow value 'null'.

		<document>:178:19
		177 |       argument56: ""
		178 |       argument57: null
		    |                   ^
		179 |       argument58: true

		<document>:59:19
		58 |       argument56: Input = ""
		59 |       argument57: Input! = null
		   |                   ^
		60 |       argument58: Input! = true
	""",
	"""
		Type 'Input' does not allow value 'true'.

		<document>:179:19
		178 |       argument57: null
		179 |       argument58: true
		    |                   ^
		180 |       argument59: VALUE

		<document>:60:19
		59 |       argument57: Input! = null
		60 |       argument58: Input! = true
		   |                   ^
		61 |       argument59: Input! = VALUE
	""",
	"""
		Type 'Input' does not allow value 'VALUE'.

		<document>:180:19
		179 |       argument58: true
		180 |       argument59: VALUE
		    |                   ^
		181 |       argument60: 1.0

		<document>:61:19
		60 |       argument58: Input! = true
		61 |       argument59: Input! = VALUE
		   |                   ^
		62 |       argument60: Input! = 1.0
	""",
	"""
		Type 'Input' does not allow value '1.0'.

		<document>:181:19
		180 |       argument59: VALUE
		181 |       argument60: 1.0
		    |                   ^
		182 |       argument61: 1

		<document>:62:19
		61 |       argument59: Input! = VALUE
		62 |       argument60: Input! = 1.0
		   |                   ^
		63 |       argument61: Input! = 1
	""",
	"""
		Type 'Input' does not allow value '1'.

		<document>:182:19
		181 |       argument60: 1.0
		182 |       argument61: 1
		    |                   ^
		183 |       argument62: []

		<document>:63:19
		62 |       argument60: Input! = 1.0
		63 |       argument61: Input! = 1
		   |                   ^
		64 |       argument62: Input! = []
	""",
	"""
		Type 'Input' does not allow a list value.

		<document>:183:19
		182 |       argument61: 1
		183 |       argument62: []
		    |                   ^
		184 |       argument63: ""

		<document>:64:19
		63 |       argument61: Input! = 1
		64 |       argument62: Input! = []
		   |                   ^
		65 |       argument63: Input! = ""
	""",
	"""
		Type 'Input' does not allow value '""'.

		<document>:184:19
		183 |       argument62: []
		184 |       argument63: ""
		    |                   ^
		185 |       argument64: true

		<document>:65:19
		64 |       argument62: Input! = []
		65 |       argument63: Input! = ""
		   |                   ^
		66 |       argument64: Int = true
	""",
)

/** Errors reported for input object field values of type `Input`, in the order the rule reports them. */
internal val valueValidityRuleDocumentInputObjectInputFieldErrors = listOf(
	"""
		Type 'Input' does not allow value 'true'.

		<document>:291:19
		290 |          field50: {}
		291 |          field51: true
		    |                   ^
		292 |          field52: VALUE

		<document>:176:13
		175 |    field50: ID! = {}
		176 |    field51: Input = true
		    |             ^
		177 |    field52: Input = VALUE
	""",
	"""
		Type 'Input' does not allow value 'VALUE'.

		<document>:292:19
		291 |          field51: true
		292 |          field52: VALUE
		    |                   ^
		293 |          field53: 1.0

		<document>:177:13
		176 |    field51: Input = true
		177 |    field52: Input = VALUE
		    |             ^
		178 |    field53: Input = 1.0
	""",
	"""
		Type 'Input' does not allow value '1.0'.

		<document>:293:19
		292 |          field52: VALUE
		293 |          field53: 1.0
		    |                   ^
		294 |          field54: 1

		<document>:178:13
		177 |    field52: Input = VALUE
		178 |    field53: Input = 1.0
		    |             ^
		179 |    field54: Input = 1
	""",
	"""
		Type 'Input' does not allow value '1'.

		<document>:294:19
		293 |          field53: 1.0
		294 |          field54: 1
		    |                   ^
		295 |          field55: []

		<document>:179:13
		178 |    field53: Input = 1.0
		179 |    field54: Input = 1
		    |             ^
		180 |    field55: Input = []
	""",
	"""
		Type 'Input' does not allow a list value.

		<document>:295:19
		294 |          field54: 1
		295 |          field55: []
		    |                   ^
		296 |          field56: ""

		<document>:180:13
		179 |    field54: Input = 1
		180 |    field55: Input = []
		    |             ^
		181 |    field56: Input = ""
	""",
	"""
		Type 'Input' does not allow value '""'.

		<document>:296:19
		295 |          field55: []
		296 |          field56: ""
		    |                   ^
		297 |          field57: null

		<document>:181:13
		180 |    field55: Input = []
		181 |    field56: Input = ""
		    |             ^
		182 |    field57: Input! = null
	""",
	"""
		Type 'Input' does not allow value 'null'.

		<document>:297:19
		296 |          field56: ""
		297 |          field57: null
		    |                   ^
		298 |          field58: true

		<document>:182:13
		181 |    field56: Input = ""
		182 |    field57: Input! = null
		    |             ^
		183 |    field58: Input! = true
	""",
	"""
		Type 'Input' does not allow value 'true'.

		<document>:298:19
		297 |          field57: null
		298 |          field58: true
		    |                   ^
		299 |          field59: VALUE

		<document>:183:13
		182 |    field57: Input! = null
		183 |    field58: Input! = true
		    |             ^
		184 |    field59: Input! = VALUE
	""",
	"""
		Type 'Input' does not allow value 'VALUE'.

		<document>:299:19
		298 |          field58: true
		299 |          field59: VALUE
		    |                   ^
		300 |          field60: 1.0

		<document>:184:13
		183 |    field58: Input! = true
		184 |    field59: Input! = VALUE
		    |             ^
		185 |    field60: Input! = 1.0
	""",
	"""
		Type 'Input' does not allow value '1.0'.

		<document>:300:19
		299 |          field59: VALUE
		300 |          field60: 1.0
		    |                   ^
		301 |          field61: 1

		<document>:185:13
		184 |    field59: Input! = VALUE
		185 |    field60: Input! = 1.0
		    |             ^
		186 |    field61: Input! = 1
	""",
	"""
		Type 'Input' does not allow value '1'.

		<document>:301:19
		300 |          field60: 1.0
		301 |          field61: 1
		    |                   ^
		302 |          field62: []

		<document>:186:13
		185 |    field60: Input! = 1.0
		186 |    field61: Input! = 1
		    |             ^
		187 |    field62: Input! = []
	""",
	"""
		Type 'Input' does not allow a list value.

		<document>:302:19
		301 |          field61: 1
		302 |          field62: []
		    |                   ^
		303 |          field63: ""

		<document>:187:13
		186 |    field61: Input! = 1
		187 |    field62: Input! = []
		    |             ^
		188 |    field63: Input! = ""
	""",
	"""
		Type 'Input' does not allow value '""'.

		<document>:303:19
		302 |          field62: []
		303 |          field63: ""
		    |                   ^
		304 |          field64: true

		<document>:188:13
		187 |    field62: Input! = []
		188 |    field63: Input! = ""
		    |             ^
		189 |    field64: Int = true
	""",
)

/** The error reported for the input object argument whose value omits a required field. */
internal val valueValidityRuleDocumentInputObjectMissingFieldErrors = listOf(
	"""
		Required field 'field119' of type 'Input' is missing.

		<document>:240:20
		239 |       argument118: []
		240 |       argument119: {
		    |                    ^
		241 |          field1: VALUE

		<document>:244:4
		243 |    field118: String! = []
		244 |    field119: String!
		    |    ^
		245 | }
	""",
)
