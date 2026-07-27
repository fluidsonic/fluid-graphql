package testing

// Expected errors for `ValueValidityRuleDocumentRejectionTest`, covering every `Boolean` case in its fixture.
// The strings are verbatim `GError.describe()` renderings and are position-sensitive: their
// `<document>:line:column` blocks and source excerpts index the single fixture declared in
// `ValueValidityRuleDocumentRejectionTest.kt`, so neither the fixture nor these strings can be edited
// in isolation. See docs/scout/testing/validation-test-harness.md for the paste-back workflow.

/** Errors reported for variable definition default values of type `Boolean`, in the order the rule reports them. */
internal val valueValidityRuleDocumentBooleanVariableErrors = listOf(
	"""
		Boolean cannot represent a non boolean value: VALUE

		<document>:2:26
		1 | query someQuery(
		2 |    ${'$'}variable1: Boolean = VALUE
		  |                          ^
		3 |    ${'$'}variable2: Boolean = 1.0
	""",
	"""
		Boolean cannot represent a non boolean value: 1.0

		<document>:3:26
		2 |    ${'$'}variable1: Boolean = VALUE
		3 |    ${'$'}variable2: Boolean = 1.0
		  |                          ^
		4 |    ${'$'}variable3: Boolean = 1
	""",
	"""
		Boolean cannot represent a non boolean value: 1

		<document>:4:26
		3 |    ${'$'}variable2: Boolean = 1.0
		4 |    ${'$'}variable3: Boolean = 1
		  |                          ^
		5 |    ${'$'}variable4: Boolean = []
	""",
	"""
		Boolean cannot represent a non boolean value: []

		<document>:5:26
		4 |    ${'$'}variable3: Boolean = 1
		5 |    ${'$'}variable4: Boolean = []
		  |                          ^
		6 |    ${'$'}variable5: Boolean = {}
	""",
	"""
		Boolean cannot represent a non boolean value: {}

		<document>:6:26
		5 |    ${'$'}variable4: Boolean = []
		6 |    ${'$'}variable5: Boolean = {}
		  |                          ^
		7 |    ${'$'}variable6: Boolean = ""
	""",
	"""
		Boolean cannot represent a non boolean value: ""

		<document>:7:26
		6 |    ${'$'}variable5: Boolean = {}
		7 |    ${'$'}variable6: Boolean = ""
		  |                          ^
		8 |    ${'$'}variable7: Boolean! = null
	""",
	"""
		Type 'Boolean!' does not allow value 'null'.

		<document>:8:27
		7 |    ${'$'}variable6: Boolean = ""
		8 |    ${'$'}variable7: Boolean! = null
		  |                           ^
		9 |    ${'$'}variable8: Boolean! = VALUE
	""",
	"""
		Boolean cannot represent a non boolean value: VALUE

		<document>:9:27
		 8 |    ${'$'}variable7: Boolean! = null
		 9 |    ${'$'}variable8: Boolean! = VALUE
		   |                           ^
		10 |    ${'$'}variable9: Boolean! = 1.0
	""",
	"""
		Boolean cannot represent a non boolean value: 1.0

		<document>:10:27
		 9 |    ${'$'}variable8: Boolean! = VALUE
		10 |    ${'$'}variable9: Boolean! = 1.0
		   |                           ^
		11 |    ${'$'}variable10: Boolean! = 1
	""",
	"""
		Boolean cannot represent a non boolean value: 1

		<document>:11:28
		10 |    ${'$'}variable9: Boolean! = 1.0
		11 |    ${'$'}variable10: Boolean! = 1
		   |                            ^
		12 |    ${'$'}variable11: Boolean! = []
	""",
	"""
		Boolean cannot represent a non boolean value: []

		<document>:12:28
		11 |    ${'$'}variable10: Boolean! = 1
		12 |    ${'$'}variable11: Boolean! = []
		   |                            ^
		13 |    ${'$'}variable12: Boolean! = {}
	""",
	"""
		Boolean cannot represent a non boolean value: {}

		<document>:13:28
		12 |    ${'$'}variable11: Boolean! = []
		13 |    ${'$'}variable12: Boolean! = {}
		   |                            ^
		14 |    ${'$'}variable13: Boolean! = ""
	""",
	"""
		Boolean cannot represent a non boolean value: ""

		<document>:14:28
		13 |    ${'$'}variable12: Boolean! = {}
		14 |    ${'$'}variable13: Boolean! = ""
		   |                            ^
		15 |    ${'$'}variable14: Enum = true
	""",
)

/** Errors reported for field argument values of type `Boolean`, in the order the rule reports them. */
internal val valueValidityRuleDocumentBooleanArgumentErrors = listOf(
	"""
		Boolean cannot represent a non boolean value: VALUE

		<document>:122:18
		121 |    fun(
		122 |       argument1: VALUE
		    |                  ^
		123 |       argument2: 1.0

		<document>:3:18
		2 |    fun(
		3 |       argument1: Boolean = VALUE
		  |                  ^
		4 |       argument2: Boolean = 1.0
	""",
	"""
		Boolean cannot represent a non boolean value: 1.0

		<document>:123:18
		122 |       argument1: VALUE
		123 |       argument2: 1.0
		    |                  ^
		124 |       argument3: 1

		<document>:4:18
		3 |       argument1: Boolean = VALUE
		4 |       argument2: Boolean = 1.0
		  |                  ^
		5 |       argument3: Boolean = 1
	""",
	"""
		Boolean cannot represent a non boolean value: 1

		<document>:124:18
		123 |       argument2: 1.0
		124 |       argument3: 1
		    |                  ^
		125 |       argument4: []

		<document>:5:18
		4 |       argument2: Boolean = 1.0
		5 |       argument3: Boolean = 1
		  |                  ^
		6 |       argument4: Boolean = []
	""",
	"""
		Boolean cannot represent a non boolean value: []

		<document>:125:18
		124 |       argument3: 1
		125 |       argument4: []
		    |                  ^
		126 |       argument5: {}

		<document>:6:18
		5 |       argument3: Boolean = 1
		6 |       argument4: Boolean = []
		  |                  ^
		7 |       argument5: Boolean = {}
	""",
	"""
		Boolean cannot represent a non boolean value: {}

		<document>:126:18
		125 |       argument4: []
		126 |       argument5: {}
		    |                  ^
		127 |       argument6: ""

		<document>:7:18
		6 |       argument4: Boolean = []
		7 |       argument5: Boolean = {}
		  |                  ^
		8 |       argument6: Boolean = ""
	""",
	"""
		Boolean cannot represent a non boolean value: ""

		<document>:127:18
		126 |       argument5: {}
		127 |       argument6: ""
		    |                  ^
		128 |       argument7: null

		<document>:8:18
		7 |       argument5: Boolean = {}
		8 |       argument6: Boolean = ""
		  |                  ^
		9 |       argument7: Boolean! = null
	""",
	"""
		Type 'Boolean' does not allow value 'null'.

		<document>:128:18
		127 |       argument6: ""
		128 |       argument7: null
		    |                  ^
		129 |       argument8: VALUE

		<document>:9:18
		 8 |       argument6: Boolean = ""
		 9 |       argument7: Boolean! = null
		   |                  ^
		10 |       argument8: Boolean! = VALUE
	""",
	"""
		Boolean cannot represent a non boolean value: VALUE

		<document>:129:18
		128 |       argument7: null
		129 |       argument8: VALUE
		    |                  ^
		130 |       argument9: 1.0

		<document>:10:18
		 9 |       argument7: Boolean! = null
		10 |       argument8: Boolean! = VALUE
		   |                  ^
		11 |       argument9: Boolean! = 1.0
	""",
	"""
		Boolean cannot represent a non boolean value: 1.0

		<document>:130:18
		129 |       argument8: VALUE
		130 |       argument9: 1.0
		    |                  ^
		131 |       argument10: 1

		<document>:11:18
		10 |       argument8: Boolean! = VALUE
		11 |       argument9: Boolean! = 1.0
		   |                  ^
		12 |       argument10: Boolean! = 1
	""",
	"""
		Boolean cannot represent a non boolean value: 1

		<document>:131:19
		130 |       argument9: 1.0
		131 |       argument10: 1
		    |                   ^
		132 |       argument11: []

		<document>:12:19
		11 |       argument9: Boolean! = 1.0
		12 |       argument10: Boolean! = 1
		   |                   ^
		13 |       argument11: Boolean! = []
	""",
	"""
		Boolean cannot represent a non boolean value: []

		<document>:132:19
		131 |       argument10: 1
		132 |       argument11: []
		    |                   ^
		133 |       argument12: {}

		<document>:13:19
		12 |       argument10: Boolean! = 1
		13 |       argument11: Boolean! = []
		   |                   ^
		14 |       argument12: Boolean! = {}
	""",
	"""
		Boolean cannot represent a non boolean value: {}

		<document>:133:19
		132 |       argument11: []
		133 |       argument12: {}
		    |                   ^
		134 |       argument13: ""

		<document>:14:19
		13 |       argument11: Boolean! = []
		14 |       argument12: Boolean! = {}
		   |                   ^
		15 |       argument13: Boolean! = ""
	""",
	"""
		Boolean cannot represent a non boolean value: ""

		<document>:134:19
		133 |       argument12: {}
		134 |       argument13: ""
		    |                   ^
		135 |       argument14: true

		<document>:15:19
		14 |       argument12: Boolean! = {}
		15 |       argument13: Boolean! = ""
		   |                   ^
		16 |       argument14: Enum = true
	""",
)

/** Errors reported for input object field values of type `Boolean`, in the order the rule reports them. */
internal val valueValidityRuleDocumentBooleanInputFieldErrors = listOf(
	"""
		Boolean cannot represent a non boolean value: VALUE

		<document>:241:18
		240 |       argument119: {
		241 |          field1: VALUE
		    |                  ^
		242 |          field2: 1.0

		<document>:126:12
		125 | input Input {
		126 |    field1: Boolean = VALUE
		    |            ^
		127 |    field2: Boolean = 1.0
	""",
	"""
		Boolean cannot represent a non boolean value: 1.0

		<document>:242:18
		241 |          field1: VALUE
		242 |          field2: 1.0
		    |                  ^
		243 |          field3: 1

		<document>:127:12
		126 |    field1: Boolean = VALUE
		127 |    field2: Boolean = 1.0
		    |            ^
		128 |    field3: Boolean = 1
	""",
	"""
		Boolean cannot represent a non boolean value: 1

		<document>:243:18
		242 |          field2: 1.0
		243 |          field3: 1
		    |                  ^
		244 |          field4: []

		<document>:128:12
		127 |    field2: Boolean = 1.0
		128 |    field3: Boolean = 1
		    |            ^
		129 |    field4: Boolean = []
	""",
	"""
		Boolean cannot represent a non boolean value: []

		<document>:244:18
		243 |          field3: 1
		244 |          field4: []
		    |                  ^
		245 |          field5: {}

		<document>:129:12
		128 |    field3: Boolean = 1
		129 |    field4: Boolean = []
		    |            ^
		130 |    field5: Boolean = {}
	""",
	"""
		Boolean cannot represent a non boolean value: {}

		<document>:245:18
		244 |          field4: []
		245 |          field5: {}
		    |                  ^
		246 |          field6: ""

		<document>:130:12
		129 |    field4: Boolean = []
		130 |    field5: Boolean = {}
		    |            ^
		131 |    field6: Boolean = ""
	""",
	"""
		Boolean cannot represent a non boolean value: ""

		<document>:246:18
		245 |          field5: {}
		246 |          field6: ""
		    |                  ^
		247 |          field7: null

		<document>:131:12
		130 |    field5: Boolean = {}
		131 |    field6: Boolean = ""
		    |            ^
		132 |    field7: Boolean! = null
	""",
	"""
		Type 'Boolean' does not allow value 'null'.

		<document>:247:18
		246 |          field6: ""
		247 |          field7: null
		    |                  ^
		248 |          field8: VALUE

		<document>:132:12
		131 |    field6: Boolean = ""
		132 |    field7: Boolean! = null
		    |            ^
		133 |    field8: Boolean! = VALUE
	""",
	"""
		Boolean cannot represent a non boolean value: VALUE

		<document>:248:18
		247 |          field7: null
		248 |          field8: VALUE
		    |                  ^
		249 |          field9: 1.0

		<document>:133:12
		132 |    field7: Boolean! = null
		133 |    field8: Boolean! = VALUE
		    |            ^
		134 |    field9: Boolean! = 1.0
	""",
	"""
		Boolean cannot represent a non boolean value: 1.0

		<document>:249:18
		248 |          field8: VALUE
		249 |          field9: 1.0
		    |                  ^
		250 |          field10: 1

		<document>:134:12
		133 |    field8: Boolean! = VALUE
		134 |    field9: Boolean! = 1.0
		    |            ^
		135 |    field10: Boolean! = 1
	""",
	"""
		Boolean cannot represent a non boolean value: 1

		<document>:250:19
		249 |          field9: 1.0
		250 |          field10: 1
		    |                   ^
		251 |          field11: []

		<document>:135:13
		134 |    field9: Boolean! = 1.0
		135 |    field10: Boolean! = 1
		    |             ^
		136 |    field11: Boolean! = []
	""",
	"""
		Boolean cannot represent a non boolean value: []

		<document>:251:19
		250 |          field10: 1
		251 |          field11: []
		    |                   ^
		252 |          field12: {}

		<document>:136:13
		135 |    field10: Boolean! = 1
		136 |    field11: Boolean! = []
		    |             ^
		137 |    field12: Boolean! = {}
	""",
	"""
		Boolean cannot represent a non boolean value: {}

		<document>:252:19
		251 |          field11: []
		252 |          field12: {}
		    |                   ^
		253 |          field13: ""

		<document>:137:13
		136 |    field11: Boolean! = []
		137 |    field12: Boolean! = {}
		    |             ^
		138 |    field13: Boolean! = ""
	""",
	"""
		Boolean cannot represent a non boolean value: ""

		<document>:253:19
		252 |          field12: {}
		253 |          field13: ""
		    |                   ^
		254 |          field14: true

		<document>:138:13
		137 |    field12: Boolean! = {}
		138 |    field13: Boolean! = ""
		    |             ^
		139 |    field14: Enum = true
	""",
)
