package io.fluidsonic.graphql

import tests.*
import kotlin.test.*


class FragmentSelectionPossibilityRuleTest {

	@Test
	fun `accepts fragment selections on possible types`() {
		assertValidationRule(
			rule = FragmentSelectionPossibilityRule,
			errors = emptyList(),
			document = """
				|{
				|  interface {
				|    ... on Interface {
				|      value
				|    }
				|    ... on Object {
				|      value
				|    }
				|    ... on Union {
				|      value
				|    }
				|    ...interfaceFragment
				|    ...objectFragment
				|    ...unionFragment
				|  }
				|  object {
				|    ... on Interface {
				|      value
				|    }
				|    ... on Object {
				|      value
				|    }
				|    ... on Union {
				|      value
				|    }
				|    ...interfaceFragment
				|    ...objectFragment
				|    ...unionFragment
				|  }
				|  union {
				|    ... on Interface {
				|      value
				|    }
				|    ... on Object {
				|      value
				|    }
				|    ... on Union {
				|      value
				|    }
				|    ...interfaceFragment
				|    ...objectFragment
				|    ...unionFragment
				|  }
				|}
				|
				|fragment interfaceFragment on Interface {
				|  value
				|}
				|
				|fragment objectFragment on Object {
				|  value
				|}
				|
				|fragment unionFragment on Union {
				|  __typename
				|}
			""",
			schema = """
				|type Query {
				|  interface: Interface
				|  object: Object
				|  union: Union
				|}
				|interface Interface { value: String }
				|type Object implements Interface { value: String }
				|union Union = Object
			"""
		)
	}


	@Test
	fun `rejects selections of nonexistent fragments`() {
		assertValidationRule(
			rule = FragmentSelectionPossibilityRule,
			errors = listOf(
				"""
					Inline fragment on 'AnotherInterface' will never match the unrelated type 'Interface'.

					<document>:3:12
					2 |   interface {
					3 |     ... on AnotherInterface {
					  |            ^
					4 |       value

					<document>:2:3
					1 | {
					2 |   interface {
					  |   ^
					3 |     ... on AnotherInterface {

					<document>:2:14
					1 | type Query {
					2 |   interface: Interface
					  |              ^
					3 |   object: Object

					<document>:6:11
					5 | }
					6 | interface Interface { value: String }
					  |           ^
					7 | interface AnotherInterface { value: String }

					<document>:7:11
					6 | interface Interface { value: String }
					7 | interface AnotherInterface { value: String }
					  |           ^
					8 | type Object implements Interface { value: String }
				""",
				"""
					Inline fragment on 'AnotherObject' will never match the unrelated type 'Interface'.

					<document>:6:12
					5 |     }
					6 |     ... on AnotherObject {
					  |            ^
					7 |       value

					<document>:2:3
					1 | {
					2 |   interface {
					  |   ^
					3 |     ... on AnotherInterface {

					<document>:2:14
					1 | type Query {
					2 |   interface: Interface
					  |              ^
					3 |   object: Object

					<document>:6:11
					5 | }
					6 | interface Interface { value: String }
					  |           ^
					7 | interface AnotherInterface { value: String }

					<document>:9:6
					 8 | type Object implements Interface { value: String }
					 9 | type AnotherObject { value: String }
					   |      ^
					10 | union Union = Object
				""",
				"""
					Inline fragment on 'AnotherUnion' will never match the unrelated type 'Interface'.

					<document>:9:12
					 8 |     }
					 9 |     ... on AnotherUnion {
					   |            ^
					10 |       value

					<document>:2:3
					1 | {
					2 |   interface {
					  |   ^
					3 |     ... on AnotherInterface {

					<document>:2:14
					1 | type Query {
					2 |   interface: Interface
					  |              ^
					3 |   object: Object

					<document>:6:11
					5 | }
					6 | interface Interface { value: String }
					  |           ^
					7 | interface AnotherInterface { value: String }

					<document>:11:7
					10 | union Union = Object
					11 | union AnotherUnion = AnotherObject
					   |       ^
				""",
				"""
					Fragment 'interfaceFragment' on 'AnotherInterface' will never match the unrelated type 'Interface'.

					<document>:12:8
					11 |     }
					12 |     ...interfaceFragment
					   |        ^
					13 |     ...objectFragment

					<document>:46:31
					45 | 
					46 | fragment interfaceFragment on AnotherInterface {
					   |                               ^
					47 |   value

					<document>:7:11
					6 | interface Interface { value: String }
					7 | interface AnotherInterface { value: String }
					  |           ^
					8 | type Object implements Interface { value: String }

					<document>:6:11
					5 | }
					6 | interface Interface { value: String }
					  |           ^
					7 | interface AnotherInterface { value: String }
				""",
				"""
					Fragment 'objectFragment' on 'AnotherObject' will never match the unrelated type 'Interface'.

					<document>:13:8
					12 |     ...interfaceFragment
					13 |     ...objectFragment
					   |        ^
					14 |     ...unionFragment

					<document>:50:28
					49 | 
					50 | fragment objectFragment on AnotherObject {
					   |                            ^
					51 |   value

					<document>:9:6
					 8 | type Object implements Interface { value: String }
					 9 | type AnotherObject { value: String }
					   |      ^
					10 | union Union = Object

					<document>:6:11
					5 | }
					6 | interface Interface { value: String }
					  |           ^
					7 | interface AnotherInterface { value: String }
				""",
				"""
					Fragment 'unionFragment' on 'AnotherUnion' will never match the unrelated type 'Interface'.

					<document>:14:8
					13 |     ...objectFragment
					14 |     ...unionFragment
					   |        ^
					15 |   }

					<document>:54:27
					53 | 
					54 | fragment unionFragment on AnotherUnion {
					   |                           ^
					55 |   __typename

					<document>:11:7
					10 | union Union = Object
					11 | union AnotherUnion = AnotherObject
					   |       ^

					<document>:6:11
					5 | }
					6 | interface Interface { value: String }
					  |           ^
					7 | interface AnotherInterface { value: String }
				""",
				"""
					Inline fragment on 'AnotherInterface' will never match the unrelated type 'Object'.

					<document>:17:12
					16 |   object {
					17 |     ... on AnotherInterface {
					   |            ^
					18 |       value

					<document>:16:3
					15 |   }
					16 |   object {
					   |   ^
					17 |     ... on AnotherInterface {

					<document>:3:11
					2 |   interface: Interface
					3 |   object: Object
					  |           ^
					4 |   union: Union

					<document>:8:6
					7 | interface AnotherInterface { value: String }
					8 | type Object implements Interface { value: String }
					  |      ^
					9 | type AnotherObject { value: String }

					<document>:7:11
					6 | interface Interface { value: String }
					7 | interface AnotherInterface { value: String }
					  |           ^
					8 | type Object implements Interface { value: String }
				""",
				"""
					Inline fragment on 'AnotherObject' will never match the unrelated type 'Object'.

					<document>:20:12
					19 |     }
					20 |     ... on AnotherObject {
					   |            ^
					21 |       value

					<document>:16:3
					15 |   }
					16 |   object {
					   |   ^
					17 |     ... on AnotherInterface {

					<document>:3:11
					2 |   interface: Interface
					3 |   object: Object
					  |           ^
					4 |   union: Union

					<document>:8:6
					7 | interface AnotherInterface { value: String }
					8 | type Object implements Interface { value: String }
					  |      ^
					9 | type AnotherObject { value: String }

					<document>:9:6
					 8 | type Object implements Interface { value: String }
					 9 | type AnotherObject { value: String }
					   |      ^
					10 | union Union = Object
				""",
				"""
					Inline fragment on 'AnotherUnion' will never match the unrelated type 'Object'.

					<document>:23:12
					22 |     }
					23 |     ... on AnotherUnion {
					   |            ^
					24 |       value

					<document>:16:3
					15 |   }
					16 |   object {
					   |   ^
					17 |     ... on AnotherInterface {

					<document>:3:11
					2 |   interface: Interface
					3 |   object: Object
					  |           ^
					4 |   union: Union

					<document>:8:6
					7 | interface AnotherInterface { value: String }
					8 | type Object implements Interface { value: String }
					  |      ^
					9 | type AnotherObject { value: String }

					<document>:11:7
					10 | union Union = Object
					11 | union AnotherUnion = AnotherObject
					   |       ^
				""",
				"""
					Fragment 'interfaceFragment' on 'AnotherInterface' will never match the unrelated type 'Object'.

					<document>:26:8
					25 |     }
					26 |     ...interfaceFragment
					   |        ^
					27 |     ...objectFragment

					<document>:46:31
					45 | 
					46 | fragment interfaceFragment on AnotherInterface {
					   |                               ^
					47 |   value

					<document>:7:11
					6 | interface Interface { value: String }
					7 | interface AnotherInterface { value: String }
					  |           ^
					8 | type Object implements Interface { value: String }

					<document>:8:6
					7 | interface AnotherInterface { value: String }
					8 | type Object implements Interface { value: String }
					  |      ^
					9 | type AnotherObject { value: String }
				""",
				"""
					Fragment 'objectFragment' on 'AnotherObject' will never match the unrelated type 'Object'.

					<document>:27:8
					26 |     ...interfaceFragment
					27 |     ...objectFragment
					   |        ^
					28 |     ...unionFragment

					<document>:50:28
					49 | 
					50 | fragment objectFragment on AnotherObject {
					   |                            ^
					51 |   value

					<document>:9:6
					 8 | type Object implements Interface { value: String }
					 9 | type AnotherObject { value: String }
					   |      ^
					10 | union Union = Object

					<document>:8:6
					7 | interface AnotherInterface { value: String }
					8 | type Object implements Interface { value: String }
					  |      ^
					9 | type AnotherObject { value: String }
				""",
				"""
					Fragment 'unionFragment' on 'AnotherUnion' will never match the unrelated type 'Object'.

					<document>:28:8
					27 |     ...objectFragment
					28 |     ...unionFragment
					   |        ^
					29 |   }

					<document>:54:27
					53 | 
					54 | fragment unionFragment on AnotherUnion {
					   |                           ^
					55 |   __typename

					<document>:11:7
					10 | union Union = Object
					11 | union AnotherUnion = AnotherObject
					   |       ^

					<document>:8:6
					7 | interface AnotherInterface { value: String }
					8 | type Object implements Interface { value: String }
					  |      ^
					9 | type AnotherObject { value: String }
				""",
				"""
					Inline fragment on 'AnotherInterface' will never match the unrelated type 'Union'.

					<document>:31:12
					30 |   union {
					31 |     ... on AnotherInterface {
					   |            ^
					32 |       value

					<document>:30:3
					29 |   }
					30 |   union {
					   |   ^
					31 |     ... on AnotherInterface {

					<document>:4:10
					3 |   object: Object
					4 |   union: Union
					  |          ^
					5 | }

					<document>:10:7
					 9 | type AnotherObject { value: String }
					10 | union Union = Object
					   |       ^
					11 | union AnotherUnion = AnotherObject

					<document>:7:11
					6 | interface Interface { value: String }
					7 | interface AnotherInterface { value: String }
					  |           ^
					8 | type Object implements Interface { value: String }
				""",
				"""
					Inline fragment on 'AnotherObject' will never match the unrelated type 'Union'.

					<document>:34:12
					33 |     }
					34 |     ... on AnotherObject {
					   |            ^
					35 |       value

					<document>:30:3
					29 |   }
					30 |   union {
					   |   ^
					31 |     ... on AnotherInterface {

					<document>:4:10
					3 |   object: Object
					4 |   union: Union
					  |          ^
					5 | }

					<document>:10:7
					 9 | type AnotherObject { value: String }
					10 | union Union = Object
					   |       ^
					11 | union AnotherUnion = AnotherObject

					<document>:9:6
					 8 | type Object implements Interface { value: String }
					 9 | type AnotherObject { value: String }
					   |      ^
					10 | union Union = Object
				""",
				"""
					Inline fragment on 'AnotherUnion' will never match the unrelated type 'Union'.

					<document>:37:12
					36 |     }
					37 |     ... on AnotherUnion {
					   |            ^
					38 |       value

					<document>:30:3
					29 |   }
					30 |   union {
					   |   ^
					31 |     ... on AnotherInterface {

					<document>:4:10
					3 |   object: Object
					4 |   union: Union
					  |          ^
					5 | }

					<document>:10:7
					 9 | type AnotherObject { value: String }
					10 | union Union = Object
					   |       ^
					11 | union AnotherUnion = AnotherObject

					<document>:11:7
					10 | union Union = Object
					11 | union AnotherUnion = AnotherObject
					   |       ^
				""",
				"""
					Fragment 'interfaceFragment' on 'AnotherInterface' will never match the unrelated type 'Union'.

					<document>:40:8
					39 |     }
					40 |     ...interfaceFragment
					   |        ^
					41 |     ...objectFragment

					<document>:46:31
					45 | 
					46 | fragment interfaceFragment on AnotherInterface {
					   |                               ^
					47 |   value

					<document>:7:11
					6 | interface Interface { value: String }
					7 | interface AnotherInterface { value: String }
					  |           ^
					8 | type Object implements Interface { value: String }

					<document>:10:7
					 9 | type AnotherObject { value: String }
					10 | union Union = Object
					   |       ^
					11 | union AnotherUnion = AnotherObject
				""",
				"""
					Fragment 'objectFragment' on 'AnotherObject' will never match the unrelated type 'Union'.

					<document>:41:8
					40 |     ...interfaceFragment
					41 |     ...objectFragment
					   |        ^
					42 |     ...unionFragment

					<document>:50:28
					49 | 
					50 | fragment objectFragment on AnotherObject {
					   |                            ^
					51 |   value

					<document>:9:6
					 8 | type Object implements Interface { value: String }
					 9 | type AnotherObject { value: String }
					   |      ^
					10 | union Union = Object

					<document>:10:7
					 9 | type AnotherObject { value: String }
					10 | union Union = Object
					   |       ^
					11 | union AnotherUnion = AnotherObject
				""",
				"""
					Fragment 'unionFragment' on 'AnotherUnion' will never match the unrelated type 'Union'.

					<document>:42:8
					41 |     ...objectFragment
					42 |     ...unionFragment
					   |        ^
					43 |   }

					<document>:54:27
					53 | 
					54 | fragment unionFragment on AnotherUnion {
					   |                           ^
					55 |   __typename

					<document>:11:7
					10 | union Union = Object
					11 | union AnotherUnion = AnotherObject
					   |       ^

					<document>:10:7
					 9 | type AnotherObject { value: String }
					10 | union Union = Object
					   |       ^
					11 | union AnotherUnion = AnotherObject
				"""
			),
			document = """
				|{
				|  interface {
				|    ... on AnotherInterface {
				|      value
				|    }
				|    ... on AnotherObject {
				|      value
				|    }
				|    ... on AnotherUnion {
				|      value
				|    }
				|    ...interfaceFragment
				|    ...objectFragment
				|    ...unionFragment
				|  }
				|  object {
				|    ... on AnotherInterface {
				|      value
				|    }
				|    ... on AnotherObject {
				|      value
				|    }
				|    ... on AnotherUnion {
				|      value
				|    }
				|    ...interfaceFragment
				|    ...objectFragment
				|    ...unionFragment
				|  }
				|  union {
				|    ... on AnotherInterface {
				|      value
				|    }
				|    ... on AnotherObject {
				|      value
				|    }
				|    ... on AnotherUnion {
				|      value
				|    }
				|    ...interfaceFragment
				|    ...objectFragment
				|    ...unionFragment
				|  }
				|}
				|
				|fragment interfaceFragment on AnotherInterface {
				|  value
				|}
				|
				|fragment objectFragment on AnotherObject {
				|  value
				|}
				|
				|fragment unionFragment on AnotherUnion {
				|  __typename
				|}
			""",
			schema = """
				|type Query {
				|  interface: Interface
				|  object: Object
				|  union: Union
				|}
				|interface Interface { value: String }
				|interface AnotherInterface { value: String }
				|type Object implements Interface { value: String }
				|type AnotherObject { value: String }
				|union Union = Object
				|union AnotherUnion = AnotherObject
			"""
		)
	}
}
