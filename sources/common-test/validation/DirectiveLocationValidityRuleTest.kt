package io.fluidsonic.graphql

import tests.*
import kotlin.test.*


class DirectiveLocationValidityRuleTest {

	@Test
	fun `accepts directive in specified locations`() {
		assertValidationRule(
			rule = DirectiveLocationValidityRule,
			errors = emptyList(),
			document = """
				|query someQuery(${'$'}foo: String @VARIABLE_DEFINITION) @QUERY {
				|   ...frag @FRAGMENT_SPREAD
				|   ... on Query @INLINE_FRAGMENT {
				|      field
				|   }
				|}
				|
				|mutation @MUTATION {
				|   field: String
				|}
				|
				|subscription @SUBSCRIPTION {
				|   field: String
				|}
				|
				|fragment frag on Query @FRAGMENT_DEFINITION {
				|   field @FIELD
				|}
				|
				|enum Enum @ENUM {
				|   VALUE @ENUM_VALUE
				|}
				|
				|input Input @INPUT_OBJECT {
				|   field: String @INPUT_FIELD_DEFINITION
				|}
				|
				|interface Interface @INTERFACE {
				|   field(argument: String): String
				|}
				|
				|type Mutation {
				|   field: String
				|}
				|
				|type Query implements Interface @OBJECT {
				|   field(argument: String @ARGUMENT_DEFINITION): String @FIELD_DEFINITION
				|}
				|
				|type Subscription {
				|   field: String
				|}
				|
				|scalar Scalar @SCALAR
				|
				|union Union @UNION = Query
				|
				|schema @SCHEMA {
				|   query: Query
				|}
				|
				|directive @ARGUMENT_DEFINITION on ARGUMENT_DEFINITION
				|directive @ENUM on ENUM
				|directive @ENUM_VALUE on ENUM_VALUE
				|directive @FIELD on FIELD
				|directive @FIELD_DEFINITION on FIELD_DEFINITION
				|directive @FRAGMENT_DEFINITION on FRAGMENT_DEFINITION
				|directive @FRAGMENT_SPREAD on FRAGMENT_SPREAD
				|directive @INLINE_FRAGMENT on INLINE_FRAGMENT
				|directive @INPUT_FIELD_DEFINITION on INPUT_FIELD_DEFINITION
				|directive @INPUT_OBJECT on INPUT_OBJECT
				|directive @INTERFACE on INTERFACE
				|directive @MUTATION on MUTATION
				|directive @OBJECT on OBJECT
				|directive @QUERY on QUERY
				|directive @SCALAR on SCALAR
				|directive @SCHEMA on SCHEMA
				|directive @SUBSCRIPTION on SUBSCRIPTION
				|directive @UNION on UNION
				|directive @VARIABLE_DEFINITION on VARIABLE_DEFINITION
			"""
		)
	}


	@Test
	fun `rejects directive in unspecified location`() {
		assertValidationRule(
			rule = DirectiveLocationValidityRule,
			errors = listOf(
				"""
					Directive '@ARGUMENT_DEFINITION' is not valid on FRAGMENT_SPREAD but only on ARGUMENT_DEFINITION.

					<document>:44:5
					43 |    ...frag
					44 |    @ARGUMENT_DEFINITION
					   |     ^
					45 |    @ENUM

					<document>:429:35
					428 | 
					429 | directive @ARGUMENT_DEFINITION on ARGUMENT_DEFINITION
					    |                                   ^
					430 | directive @ENUM on ENUM
				""",
				"""
					Directive '@ENUM' is not valid on FRAGMENT_SPREAD but only on ENUM.

					<document>:45:5
					44 |    @ARGUMENT_DEFINITION
					45 |    @ENUM
					   |     ^
					46 |    @ENUM_VALUE

					<document>:430:20
					429 | directive @ARGUMENT_DEFINITION on ARGUMENT_DEFINITION
					430 | directive @ENUM on ENUM
					    |                    ^
					431 | directive @ENUM_VALUE on ENUM_VALUE
				""",
				"""
					Directive '@ENUM_VALUE' is not valid on FRAGMENT_SPREAD but only on ENUM_VALUE.

					<document>:46:5
					45 |    @ENUM
					46 |    @ENUM_VALUE
					   |     ^
					47 |    @FIELD

					<document>:431:26
					430 | directive @ENUM on ENUM
					431 | directive @ENUM_VALUE on ENUM_VALUE
					    |                          ^
					432 | directive @FIELD on FIELD
				""",
				"""
					Directive '@FIELD' is not valid on FRAGMENT_SPREAD but only on FIELD.

					<document>:47:5
					46 |    @ENUM_VALUE
					47 |    @FIELD
					   |     ^
					48 |    @FIELD_DEFINITION

					<document>:432:21
					431 | directive @ENUM_VALUE on ENUM_VALUE
					432 | directive @FIELD on FIELD
					    |                     ^
					433 | directive @FIELD_DEFINITION on FIELD_DEFINITION
				""",
				"""
					Directive '@FIELD_DEFINITION' is not valid on FRAGMENT_SPREAD but only on FIELD_DEFINITION.

					<document>:48:5
					47 |    @FIELD
					48 |    @FIELD_DEFINITION
					   |     ^
					49 |    @FRAGMENT_DEFINITION

					<document>:433:32
					432 | directive @FIELD on FIELD
					433 | directive @FIELD_DEFINITION on FIELD_DEFINITION
					    |                                ^
					434 | directive @FRAGMENT_DEFINITION on FRAGMENT_DEFINITION
				""",
				"""
					Directive '@FRAGMENT_DEFINITION' is not valid on FRAGMENT_SPREAD but only on FRAGMENT_DEFINITION.

					<document>:49:5
					48 |    @FIELD_DEFINITION
					49 |    @FRAGMENT_DEFINITION
					   |     ^
					50 |    @FRAGMENT_SPREAD

					<document>:434:35
					433 | directive @FIELD_DEFINITION on FIELD_DEFINITION
					434 | directive @FRAGMENT_DEFINITION on FRAGMENT_DEFINITION
					    |                                   ^
					435 | directive @FRAGMENT_SPREAD on FRAGMENT_SPREAD
				""",
				"""
					Directive '@INLINE_FRAGMENT' is not valid on FRAGMENT_SPREAD but only on INLINE_FRAGMENT.

					<document>:51:5
					50 |    @FRAGMENT_SPREAD
					51 |    @INLINE_FRAGMENT
					   |     ^
					52 |    @INPUT_FIELD_DEFINITION

					<document>:436:31
					435 | directive @FRAGMENT_SPREAD on FRAGMENT_SPREAD
					436 | directive @INLINE_FRAGMENT on INLINE_FRAGMENT
					    |                               ^
					437 | directive @INPUT_FIELD_DEFINITION on INPUT_FIELD_DEFINITION
				""",
				"""
					Directive '@INPUT_FIELD_DEFINITION' is not valid on FRAGMENT_SPREAD but only on INPUT_FIELD_DEFINITION.

					<document>:52:5
					51 |    @INLINE_FRAGMENT
					52 |    @INPUT_FIELD_DEFINITION
					   |     ^
					53 |    @INPUT_OBJECT

					<document>:437:38
					436 | directive @INLINE_FRAGMENT on INLINE_FRAGMENT
					437 | directive @INPUT_FIELD_DEFINITION on INPUT_FIELD_DEFINITION
					    |                                      ^
					438 | directive @INPUT_OBJECT on INPUT_OBJECT
				""",
				"""
					Directive '@INPUT_OBJECT' is not valid on FRAGMENT_SPREAD but only on INPUT_OBJECT.

					<document>:53:5
					52 |    @INPUT_FIELD_DEFINITION
					53 |    @INPUT_OBJECT
					   |     ^
					54 |    @INTERFACE

					<document>:438:28
					437 | directive @INPUT_FIELD_DEFINITION on INPUT_FIELD_DEFINITION
					438 | directive @INPUT_OBJECT on INPUT_OBJECT
					    |                            ^
					439 | directive @INTERFACE on INTERFACE
				""",
				"""
					Directive '@INTERFACE' is not valid on FRAGMENT_SPREAD but only on INTERFACE.

					<document>:54:5
					53 |    @INPUT_OBJECT
					54 |    @INTERFACE
					   |     ^
					55 |    @MUTATION

					<document>:439:25
					438 | directive @INPUT_OBJECT on INPUT_OBJECT
					439 | directive @INTERFACE on INTERFACE
					    |                         ^
					440 | directive @MUTATION on MUTATION
				""",
				"""
					Directive '@MUTATION' is not valid on FRAGMENT_SPREAD but only on MUTATION.

					<document>:55:5
					54 |    @INTERFACE
					55 |    @MUTATION
					   |     ^
					56 |    @OBJECT

					<document>:440:24
					439 | directive @INTERFACE on INTERFACE
					440 | directive @MUTATION on MUTATION
					    |                        ^
					441 | directive @OBJECT on OBJECT
				""",
				"""
					Directive '@OBJECT' is not valid on FRAGMENT_SPREAD but only on OBJECT.

					<document>:56:5
					55 |    @MUTATION
					56 |    @OBJECT
					   |     ^
					57 |    @QUERY

					<document>:441:22
					440 | directive @MUTATION on MUTATION
					441 | directive @OBJECT on OBJECT
					    |                      ^
					442 | directive @QUERY on QUERY
				""",
				"""
					Directive '@QUERY' is not valid on FRAGMENT_SPREAD but only on QUERY.

					<document>:57:5
					56 |    @OBJECT
					57 |    @QUERY
					   |     ^
					58 |    @SCALAR

					<document>:442:21
					441 | directive @OBJECT on OBJECT
					442 | directive @QUERY on QUERY
					    |                     ^
					443 | directive @SCALAR on SCALAR
				""",
				"""
					Directive '@SCALAR' is not valid on FRAGMENT_SPREAD but only on SCALAR.

					<document>:58:5
					57 |    @QUERY
					58 |    @SCALAR
					   |     ^
					59 |    @SCHEMA

					<document>:443:22
					442 | directive @QUERY on QUERY
					443 | directive @SCALAR on SCALAR
					    |                      ^
					444 | directive @SCHEMA on SCHEMA
				""",
				"""
					Directive '@SCHEMA' is not valid on FRAGMENT_SPREAD but only on SCHEMA.

					<document>:59:5
					58 |    @SCALAR
					59 |    @SCHEMA
					   |     ^
					60 |    @SUBSCRIPTION

					<document>:444:22
					443 | directive @SCALAR on SCALAR
					444 | directive @SCHEMA on SCHEMA
					    |                      ^
					445 | directive @SUBSCRIPTION on SUBSCRIPTION
				""",
				"""
					Directive '@SUBSCRIPTION' is not valid on FRAGMENT_SPREAD but only on SUBSCRIPTION.

					<document>:60:5
					59 |    @SCHEMA
					60 |    @SUBSCRIPTION
					   |     ^
					61 |    @UNION

					<document>:445:28
					444 | directive @SCHEMA on SCHEMA
					445 | directive @SUBSCRIPTION on SUBSCRIPTION
					    |                            ^
					446 | directive @UNION on UNION
				""",
				"""
					Directive '@UNION' is not valid on FRAGMENT_SPREAD but only on UNION.

					<document>:61:5
					60 |    @SUBSCRIPTION
					61 |    @UNION
					   |     ^
					62 |    @VARIABLE_DEFINITION

					<document>:446:21
					445 | directive @SUBSCRIPTION on SUBSCRIPTION
					446 | directive @UNION on UNION
					    |                     ^
					447 | directive @VARIABLE_DEFINITION on VARIABLE_DEFINITION
				""",
				"""
					Directive '@VARIABLE_DEFINITION' is not valid on FRAGMENT_SPREAD but only on VARIABLE_DEFINITION.

					<document>:62:5
					61 |    @UNION
					62 |    @VARIABLE_DEFINITION
					   |     ^
					63 | 

					<document>:447:35
					446 | directive @UNION on UNION
					447 | directive @VARIABLE_DEFINITION on VARIABLE_DEFINITION
					    |                                   ^
				""",
				"""
					Directive '@ARGUMENT_DEFINITION' is not valid on INLINE_FRAGMENT but only on ARGUMENT_DEFINITION.

					<document>:65:5
					64 |    ... on Query
					65 |    @ARGUMENT_DEFINITION
					   |     ^
					66 |    @ENUM

					<document>:429:35
					428 | 
					429 | directive @ARGUMENT_DEFINITION on ARGUMENT_DEFINITION
					    |                                   ^
					430 | directive @ENUM on ENUM
				""",
				"""
					Directive '@ENUM' is not valid on INLINE_FRAGMENT but only on ENUM.

					<document>:66:5
					65 |    @ARGUMENT_DEFINITION
					66 |    @ENUM
					   |     ^
					67 |    @ENUM_VALUE

					<document>:430:20
					429 | directive @ARGUMENT_DEFINITION on ARGUMENT_DEFINITION
					430 | directive @ENUM on ENUM
					    |                    ^
					431 | directive @ENUM_VALUE on ENUM_VALUE
				""",
				"""
					Directive '@ENUM_VALUE' is not valid on INLINE_FRAGMENT but only on ENUM_VALUE.

					<document>:67:5
					66 |    @ENUM
					67 |    @ENUM_VALUE
					   |     ^
					68 |    @FIELD

					<document>:431:26
					430 | directive @ENUM on ENUM
					431 | directive @ENUM_VALUE on ENUM_VALUE
					    |                          ^
					432 | directive @FIELD on FIELD
				""",
				"""
					Directive '@FIELD' is not valid on INLINE_FRAGMENT but only on FIELD.

					<document>:68:5
					67 |    @ENUM_VALUE
					68 |    @FIELD
					   |     ^
					69 |    @FIELD_DEFINITION

					<document>:432:21
					431 | directive @ENUM_VALUE on ENUM_VALUE
					432 | directive @FIELD on FIELD
					    |                     ^
					433 | directive @FIELD_DEFINITION on FIELD_DEFINITION
				""",
				"""
					Directive '@FIELD_DEFINITION' is not valid on INLINE_FRAGMENT but only on FIELD_DEFINITION.

					<document>:69:5
					68 |    @FIELD
					69 |    @FIELD_DEFINITION
					   |     ^
					70 |    @FRAGMENT_DEFINITION

					<document>:433:32
					432 | directive @FIELD on FIELD
					433 | directive @FIELD_DEFINITION on FIELD_DEFINITION
					    |                                ^
					434 | directive @FRAGMENT_DEFINITION on FRAGMENT_DEFINITION
				""",
				"""
					Directive '@FRAGMENT_DEFINITION' is not valid on INLINE_FRAGMENT but only on FRAGMENT_DEFINITION.

					<document>:70:5
					69 |    @FIELD_DEFINITION
					70 |    @FRAGMENT_DEFINITION
					   |     ^
					71 |    @FRAGMENT_SPREAD

					<document>:434:35
					433 | directive @FIELD_DEFINITION on FIELD_DEFINITION
					434 | directive @FRAGMENT_DEFINITION on FRAGMENT_DEFINITION
					    |                                   ^
					435 | directive @FRAGMENT_SPREAD on FRAGMENT_SPREAD
				""",
				"""
					Directive '@FRAGMENT_SPREAD' is not valid on INLINE_FRAGMENT but only on FRAGMENT_SPREAD.

					<document>:71:5
					70 |    @FRAGMENT_DEFINITION
					71 |    @FRAGMENT_SPREAD
					   |     ^
					72 |    @INLINE_FRAGMENT

					<document>:435:31
					434 | directive @FRAGMENT_DEFINITION on FRAGMENT_DEFINITION
					435 | directive @FRAGMENT_SPREAD on FRAGMENT_SPREAD
					    |                               ^
					436 | directive @INLINE_FRAGMENT on INLINE_FRAGMENT
				""",
				"""
					Directive '@INPUT_FIELD_DEFINITION' is not valid on INLINE_FRAGMENT but only on INPUT_FIELD_DEFINITION.

					<document>:73:5
					72 |    @INLINE_FRAGMENT
					73 |    @INPUT_FIELD_DEFINITION
					   |     ^
					74 |    @INPUT_OBJECT

					<document>:437:38
					436 | directive @INLINE_FRAGMENT on INLINE_FRAGMENT
					437 | directive @INPUT_FIELD_DEFINITION on INPUT_FIELD_DEFINITION
					    |                                      ^
					438 | directive @INPUT_OBJECT on INPUT_OBJECT
				""",
				"""
					Directive '@INPUT_OBJECT' is not valid on INLINE_FRAGMENT but only on INPUT_OBJECT.

					<document>:74:5
					73 |    @INPUT_FIELD_DEFINITION
					74 |    @INPUT_OBJECT
					   |     ^
					75 |    @INTERFACE

					<document>:438:28
					437 | directive @INPUT_FIELD_DEFINITION on INPUT_FIELD_DEFINITION
					438 | directive @INPUT_OBJECT on INPUT_OBJECT
					    |                            ^
					439 | directive @INTERFACE on INTERFACE
				""",
				"""
					Directive '@INTERFACE' is not valid on INLINE_FRAGMENT but only on INTERFACE.

					<document>:75:5
					74 |    @INPUT_OBJECT
					75 |    @INTERFACE
					   |     ^
					76 |    @MUTATION

					<document>:439:25
					438 | directive @INPUT_OBJECT on INPUT_OBJECT
					439 | directive @INTERFACE on INTERFACE
					    |                         ^
					440 | directive @MUTATION on MUTATION
				""",
				"""
					Directive '@MUTATION' is not valid on INLINE_FRAGMENT but only on MUTATION.

					<document>:76:5
					75 |    @INTERFACE
					76 |    @MUTATION
					   |     ^
					77 |    @OBJECT

					<document>:440:24
					439 | directive @INTERFACE on INTERFACE
					440 | directive @MUTATION on MUTATION
					    |                        ^
					441 | directive @OBJECT on OBJECT
				""",
				"""
					Directive '@OBJECT' is not valid on INLINE_FRAGMENT but only on OBJECT.

					<document>:77:5
					76 |    @MUTATION
					77 |    @OBJECT
					   |     ^
					78 |    @QUERY

					<document>:441:22
					440 | directive @MUTATION on MUTATION
					441 | directive @OBJECT on OBJECT
					    |                      ^
					442 | directive @QUERY on QUERY
				""",
				"""
					Directive '@QUERY' is not valid on INLINE_FRAGMENT but only on QUERY.

					<document>:78:5
					77 |    @OBJECT
					78 |    @QUERY
					   |     ^
					79 |    @SCALAR

					<document>:442:21
					441 | directive @OBJECT on OBJECT
					442 | directive @QUERY on QUERY
					    |                     ^
					443 | directive @SCALAR on SCALAR
				""",
				"""
					Directive '@SCALAR' is not valid on INLINE_FRAGMENT but only on SCALAR.

					<document>:79:5
					78 |    @QUERY
					79 |    @SCALAR
					   |     ^
					80 |    @SCHEMA

					<document>:443:22
					442 | directive @QUERY on QUERY
					443 | directive @SCALAR on SCALAR
					    |                      ^
					444 | directive @SCHEMA on SCHEMA
				""",
				"""
					Directive '@SCHEMA' is not valid on INLINE_FRAGMENT but only on SCHEMA.

					<document>:80:5
					79 |    @SCALAR
					80 |    @SCHEMA
					   |     ^
					81 |    @SUBSCRIPTION

					<document>:444:22
					443 | directive @SCALAR on SCALAR
					444 | directive @SCHEMA on SCHEMA
					    |                      ^
					445 | directive @SUBSCRIPTION on SUBSCRIPTION
				""",
				"""
					Directive '@SUBSCRIPTION' is not valid on INLINE_FRAGMENT but only on SUBSCRIPTION.

					<document>:81:5
					80 |    @SCHEMA
					81 |    @SUBSCRIPTION
					   |     ^
					82 |    @UNION

					<document>:445:28
					444 | directive @SCHEMA on SCHEMA
					445 | directive @SUBSCRIPTION on SUBSCRIPTION
					    |                            ^
					446 | directive @UNION on UNION
				""",
				"""
					Directive '@UNION' is not valid on INLINE_FRAGMENT but only on UNION.

					<document>:82:5
					81 |    @SUBSCRIPTION
					82 |    @UNION
					   |     ^
					83 |    @VARIABLE_DEFINITION

					<document>:446:21
					445 | directive @SUBSCRIPTION on SUBSCRIPTION
					446 | directive @UNION on UNION
					    |                     ^
					447 | directive @VARIABLE_DEFINITION on VARIABLE_DEFINITION
				""",
				"""
					Directive '@VARIABLE_DEFINITION' is not valid on INLINE_FRAGMENT but only on VARIABLE_DEFINITION.

					<document>:83:5
					82 |    @UNION
					83 |    @VARIABLE_DEFINITION
					   |     ^
					84 |    {

					<document>:447:35
					446 | directive @UNION on UNION
					447 | directive @VARIABLE_DEFINITION on VARIABLE_DEFINITION
					    |                                   ^
				""",
				"""
					Directive '@ARGUMENT_DEFINITION' is not valid on VARIABLE_DEFINITION but only on ARGUMENT_DEFINITION.

					<document>:3:5
					2 |    ${'$'}foo: String
					3 |    @ARGUMENT_DEFINITION
					  |     ^
					4 |    @ENUM

					<document>:429:35
					428 | 
					429 | directive @ARGUMENT_DEFINITION on ARGUMENT_DEFINITION
					    |                                   ^
					430 | directive @ENUM on ENUM
				""",
				"""
					Directive '@ENUM' is not valid on VARIABLE_DEFINITION but only on ENUM.

					<document>:4:5
					3 |    @ARGUMENT_DEFINITION
					4 |    @ENUM
					  |     ^
					5 |    @ENUM_VALUE

					<document>:430:20
					429 | directive @ARGUMENT_DEFINITION on ARGUMENT_DEFINITION
					430 | directive @ENUM on ENUM
					    |                    ^
					431 | directive @ENUM_VALUE on ENUM_VALUE
				""",
				"""
					Directive '@ENUM_VALUE' is not valid on VARIABLE_DEFINITION but only on ENUM_VALUE.

					<document>:5:5
					4 |    @ENUM
					5 |    @ENUM_VALUE
					  |     ^
					6 |    @FIELD

					<document>:431:26
					430 | directive @ENUM on ENUM
					431 | directive @ENUM_VALUE on ENUM_VALUE
					    |                          ^
					432 | directive @FIELD on FIELD
				""",
				"""
					Directive '@FIELD' is not valid on VARIABLE_DEFINITION but only on FIELD.

					<document>:6:5
					5 |    @ENUM_VALUE
					6 |    @FIELD
					  |     ^
					7 |    @FIELD_DEFINITION

					<document>:432:21
					431 | directive @ENUM_VALUE on ENUM_VALUE
					432 | directive @FIELD on FIELD
					    |                     ^
					433 | directive @FIELD_DEFINITION on FIELD_DEFINITION
				""",
				"""
					Directive '@FIELD_DEFINITION' is not valid on VARIABLE_DEFINITION but only on FIELD_DEFINITION.

					<document>:7:5
					6 |    @FIELD
					7 |    @FIELD_DEFINITION
					  |     ^
					8 |    @FRAGMENT_DEFINITION

					<document>:433:32
					432 | directive @FIELD on FIELD
					433 | directive @FIELD_DEFINITION on FIELD_DEFINITION
					    |                                ^
					434 | directive @FRAGMENT_DEFINITION on FRAGMENT_DEFINITION
				""",
				"""
					Directive '@FRAGMENT_DEFINITION' is not valid on VARIABLE_DEFINITION but only on FRAGMENT_DEFINITION.

					<document>:8:5
					7 |    @FIELD_DEFINITION
					8 |    @FRAGMENT_DEFINITION
					  |     ^
					9 |    @FRAGMENT_SPREAD

					<document>:434:35
					433 | directive @FIELD_DEFINITION on FIELD_DEFINITION
					434 | directive @FRAGMENT_DEFINITION on FRAGMENT_DEFINITION
					    |                                   ^
					435 | directive @FRAGMENT_SPREAD on FRAGMENT_SPREAD
				""",
				"""
					Directive '@FRAGMENT_SPREAD' is not valid on VARIABLE_DEFINITION but only on FRAGMENT_SPREAD.

					<document>:9:5
					 8 |    @FRAGMENT_DEFINITION
					 9 |    @FRAGMENT_SPREAD
					   |     ^
					10 |    @INLINE_FRAGMENT

					<document>:435:31
					434 | directive @FRAGMENT_DEFINITION on FRAGMENT_DEFINITION
					435 | directive @FRAGMENT_SPREAD on FRAGMENT_SPREAD
					    |                               ^
					436 | directive @INLINE_FRAGMENT on INLINE_FRAGMENT
				""",
				"""
					Directive '@INLINE_FRAGMENT' is not valid on VARIABLE_DEFINITION but only on INLINE_FRAGMENT.

					<document>:10:5
					 9 |    @FRAGMENT_SPREAD
					10 |    @INLINE_FRAGMENT
					   |     ^
					11 |    @INPUT_FIELD_DEFINITION

					<document>:436:31
					435 | directive @FRAGMENT_SPREAD on FRAGMENT_SPREAD
					436 | directive @INLINE_FRAGMENT on INLINE_FRAGMENT
					    |                               ^
					437 | directive @INPUT_FIELD_DEFINITION on INPUT_FIELD_DEFINITION
				""",
				"""
					Directive '@INPUT_FIELD_DEFINITION' is not valid on VARIABLE_DEFINITION but only on INPUT_FIELD_DEFINITION.

					<document>:11:5
					10 |    @INLINE_FRAGMENT
					11 |    @INPUT_FIELD_DEFINITION
					   |     ^
					12 |    @INPUT_OBJECT

					<document>:437:38
					436 | directive @INLINE_FRAGMENT on INLINE_FRAGMENT
					437 | directive @INPUT_FIELD_DEFINITION on INPUT_FIELD_DEFINITION
					    |                                      ^
					438 | directive @INPUT_OBJECT on INPUT_OBJECT
				""",
				"""
					Directive '@INPUT_OBJECT' is not valid on VARIABLE_DEFINITION but only on INPUT_OBJECT.

					<document>:12:5
					11 |    @INPUT_FIELD_DEFINITION
					12 |    @INPUT_OBJECT
					   |     ^
					13 |    @INTERFACE

					<document>:438:28
					437 | directive @INPUT_FIELD_DEFINITION on INPUT_FIELD_DEFINITION
					438 | directive @INPUT_OBJECT on INPUT_OBJECT
					    |                            ^
					439 | directive @INTERFACE on INTERFACE
				""",
				"""
					Directive '@INTERFACE' is not valid on VARIABLE_DEFINITION but only on INTERFACE.

					<document>:13:5
					12 |    @INPUT_OBJECT
					13 |    @INTERFACE
					   |     ^
					14 |    @MUTATION

					<document>:439:25
					438 | directive @INPUT_OBJECT on INPUT_OBJECT
					439 | directive @INTERFACE on INTERFACE
					    |                         ^
					440 | directive @MUTATION on MUTATION
				""",
				"""
					Directive '@MUTATION' is not valid on VARIABLE_DEFINITION but only on MUTATION.

					<document>:14:5
					13 |    @INTERFACE
					14 |    @MUTATION
					   |     ^
					15 |    @OBJECT

					<document>:440:24
					439 | directive @INTERFACE on INTERFACE
					440 | directive @MUTATION on MUTATION
					    |                        ^
					441 | directive @OBJECT on OBJECT
				""",
				"""
					Directive '@OBJECT' is not valid on VARIABLE_DEFINITION but only on OBJECT.

					<document>:15:5
					14 |    @MUTATION
					15 |    @OBJECT
					   |     ^
					16 |    @QUERY

					<document>:441:22
					440 | directive @MUTATION on MUTATION
					441 | directive @OBJECT on OBJECT
					    |                      ^
					442 | directive @QUERY on QUERY
				""",
				"""
					Directive '@QUERY' is not valid on VARIABLE_DEFINITION but only on QUERY.

					<document>:16:5
					15 |    @OBJECT
					16 |    @QUERY
					   |     ^
					17 |    @SCALAR

					<document>:442:21
					441 | directive @OBJECT on OBJECT
					442 | directive @QUERY on QUERY
					    |                     ^
					443 | directive @SCALAR on SCALAR
				""",
				"""
					Directive '@SCALAR' is not valid on VARIABLE_DEFINITION but only on SCALAR.

					<document>:17:5
					16 |    @QUERY
					17 |    @SCALAR
					   |     ^
					18 |    @SCHEMA

					<document>:443:22
					442 | directive @QUERY on QUERY
					443 | directive @SCALAR on SCALAR
					    |                      ^
					444 | directive @SCHEMA on SCHEMA
				""",
				"""
					Directive '@SCHEMA' is not valid on VARIABLE_DEFINITION but only on SCHEMA.

					<document>:18:5
					17 |    @SCALAR
					18 |    @SCHEMA
					   |     ^
					19 |    @SUBSCRIPTION

					<document>:444:22
					443 | directive @SCALAR on SCALAR
					444 | directive @SCHEMA on SCHEMA
					    |                      ^
					445 | directive @SUBSCRIPTION on SUBSCRIPTION
				""",
				"""
					Directive '@SUBSCRIPTION' is not valid on VARIABLE_DEFINITION but only on SUBSCRIPTION.

					<document>:19:5
					18 |    @SCHEMA
					19 |    @SUBSCRIPTION
					   |     ^
					20 |    @UNION

					<document>:445:28
					444 | directive @SCHEMA on SCHEMA
					445 | directive @SUBSCRIPTION on SUBSCRIPTION
					    |                            ^
					446 | directive @UNION on UNION
				""",
				"""
					Directive '@UNION' is not valid on VARIABLE_DEFINITION but only on UNION.

					<document>:20:5
					19 |    @SUBSCRIPTION
					20 |    @UNION
					   |     ^
					21 |    @VARIABLE_DEFINITION

					<document>:446:21
					445 | directive @SUBSCRIPTION on SUBSCRIPTION
					446 | directive @UNION on UNION
					    |                     ^
					447 | directive @VARIABLE_DEFINITION on VARIABLE_DEFINITION
				""",
				"""
					Directive '@ARGUMENT_DEFINITION' is not valid on QUERY but only on ARGUMENT_DEFINITION.

					<document>:23:2
					22 | )
					23 | @ARGUMENT_DEFINITION
					   |  ^
					24 | @ENUM

					<document>:429:35
					428 | 
					429 | directive @ARGUMENT_DEFINITION on ARGUMENT_DEFINITION
					    |                                   ^
					430 | directive @ENUM on ENUM
				""",
				"""
					Directive '@ENUM' is not valid on QUERY but only on ENUM.

					<document>:24:2
					23 | @ARGUMENT_DEFINITION
					24 | @ENUM
					   |  ^
					25 | @ENUM_VALUE

					<document>:430:20
					429 | directive @ARGUMENT_DEFINITION on ARGUMENT_DEFINITION
					430 | directive @ENUM on ENUM
					    |                    ^
					431 | directive @ENUM_VALUE on ENUM_VALUE
				""",
				"""
					Directive '@ENUM_VALUE' is not valid on QUERY but only on ENUM_VALUE.

					<document>:25:2
					24 | @ENUM
					25 | @ENUM_VALUE
					   |  ^
					26 | @FIELD

					<document>:431:26
					430 | directive @ENUM on ENUM
					431 | directive @ENUM_VALUE on ENUM_VALUE
					    |                          ^
					432 | directive @FIELD on FIELD
				""",
				"""
					Directive '@FIELD' is not valid on QUERY but only on FIELD.

					<document>:26:2
					25 | @ENUM_VALUE
					26 | @FIELD
					   |  ^
					27 | @FIELD_DEFINITION

					<document>:432:21
					431 | directive @ENUM_VALUE on ENUM_VALUE
					432 | directive @FIELD on FIELD
					    |                     ^
					433 | directive @FIELD_DEFINITION on FIELD_DEFINITION
				""",
				"""
					Directive '@FIELD_DEFINITION' is not valid on QUERY but only on FIELD_DEFINITION.

					<document>:27:2
					26 | @FIELD
					27 | @FIELD_DEFINITION
					   |  ^
					28 | @FRAGMENT_DEFINITION

					<document>:433:32
					432 | directive @FIELD on FIELD
					433 | directive @FIELD_DEFINITION on FIELD_DEFINITION
					    |                                ^
					434 | directive @FRAGMENT_DEFINITION on FRAGMENT_DEFINITION
				""",
				"""
					Directive '@FRAGMENT_DEFINITION' is not valid on QUERY but only on FRAGMENT_DEFINITION.

					<document>:28:2
					27 | @FIELD_DEFINITION
					28 | @FRAGMENT_DEFINITION
					   |  ^
					29 | @FRAGMENT_SPREAD

					<document>:434:35
					433 | directive @FIELD_DEFINITION on FIELD_DEFINITION
					434 | directive @FRAGMENT_DEFINITION on FRAGMENT_DEFINITION
					    |                                   ^
					435 | directive @FRAGMENT_SPREAD on FRAGMENT_SPREAD
				""",
				"""
					Directive '@FRAGMENT_SPREAD' is not valid on QUERY but only on FRAGMENT_SPREAD.

					<document>:29:2
					28 | @FRAGMENT_DEFINITION
					29 | @FRAGMENT_SPREAD
					   |  ^
					30 | @INLINE_FRAGMENT

					<document>:435:31
					434 | directive @FRAGMENT_DEFINITION on FRAGMENT_DEFINITION
					435 | directive @FRAGMENT_SPREAD on FRAGMENT_SPREAD
					    |                               ^
					436 | directive @INLINE_FRAGMENT on INLINE_FRAGMENT
				""",
				"""
					Directive '@INLINE_FRAGMENT' is not valid on QUERY but only on INLINE_FRAGMENT.

					<document>:30:2
					29 | @FRAGMENT_SPREAD
					30 | @INLINE_FRAGMENT
					   |  ^
					31 | @INPUT_FIELD_DEFINITION

					<document>:436:31
					435 | directive @FRAGMENT_SPREAD on FRAGMENT_SPREAD
					436 | directive @INLINE_FRAGMENT on INLINE_FRAGMENT
					    |                               ^
					437 | directive @INPUT_FIELD_DEFINITION on INPUT_FIELD_DEFINITION
				""",
				"""
					Directive '@INPUT_FIELD_DEFINITION' is not valid on QUERY but only on INPUT_FIELD_DEFINITION.

					<document>:31:2
					30 | @INLINE_FRAGMENT
					31 | @INPUT_FIELD_DEFINITION
					   |  ^
					32 | @INPUT_OBJECT

					<document>:437:38
					436 | directive @INLINE_FRAGMENT on INLINE_FRAGMENT
					437 | directive @INPUT_FIELD_DEFINITION on INPUT_FIELD_DEFINITION
					    |                                      ^
					438 | directive @INPUT_OBJECT on INPUT_OBJECT
				""",
				"""
					Directive '@INPUT_OBJECT' is not valid on QUERY but only on INPUT_OBJECT.

					<document>:32:2
					31 | @INPUT_FIELD_DEFINITION
					32 | @INPUT_OBJECT
					   |  ^
					33 | @INTERFACE

					<document>:438:28
					437 | directive @INPUT_FIELD_DEFINITION on INPUT_FIELD_DEFINITION
					438 | directive @INPUT_OBJECT on INPUT_OBJECT
					    |                            ^
					439 | directive @INTERFACE on INTERFACE
				""",
				"""
					Directive '@INTERFACE' is not valid on QUERY but only on INTERFACE.

					<document>:33:2
					32 | @INPUT_OBJECT
					33 | @INTERFACE
					   |  ^
					34 | @MUTATION

					<document>:439:25
					438 | directive @INPUT_OBJECT on INPUT_OBJECT
					439 | directive @INTERFACE on INTERFACE
					    |                         ^
					440 | directive @MUTATION on MUTATION
				""",
				"""
					Directive '@MUTATION' is not valid on QUERY but only on MUTATION.

					<document>:34:2
					33 | @INTERFACE
					34 | @MUTATION
					   |  ^
					35 | @OBJECT

					<document>:440:24
					439 | directive @INTERFACE on INTERFACE
					440 | directive @MUTATION on MUTATION
					    |                        ^
					441 | directive @OBJECT on OBJECT
				""",
				"""
					Directive '@OBJECT' is not valid on QUERY but only on OBJECT.

					<document>:35:2
					34 | @MUTATION
					35 | @OBJECT
					   |  ^
					36 | @QUERY

					<document>:441:22
					440 | directive @MUTATION on MUTATION
					441 | directive @OBJECT on OBJECT
					    |                      ^
					442 | directive @QUERY on QUERY
				""",
				"""
					Directive '@SCALAR' is not valid on QUERY but only on SCALAR.

					<document>:37:2
					36 | @QUERY
					37 | @SCALAR
					   |  ^
					38 | @SCHEMA

					<document>:443:22
					442 | directive @QUERY on QUERY
					443 | directive @SCALAR on SCALAR
					    |                      ^
					444 | directive @SCHEMA on SCHEMA
				""",
				"""
					Directive '@SCHEMA' is not valid on QUERY but only on SCHEMA.

					<document>:38:2
					37 | @SCALAR
					38 | @SCHEMA
					   |  ^
					39 | @SUBSCRIPTION

					<document>:444:22
					443 | directive @SCALAR on SCALAR
					444 | directive @SCHEMA on SCHEMA
					    |                      ^
					445 | directive @SUBSCRIPTION on SUBSCRIPTION
				""",
				"""
					Directive '@SUBSCRIPTION' is not valid on QUERY but only on SUBSCRIPTION.

					<document>:39:2
					38 | @SCHEMA
					39 | @SUBSCRIPTION
					   |  ^
					40 | @UNION

					<document>:445:28
					444 | directive @SCHEMA on SCHEMA
					445 | directive @SUBSCRIPTION on SUBSCRIPTION
					    |                            ^
					446 | directive @UNION on UNION
				""",
				"""
					Directive '@UNION' is not valid on QUERY but only on UNION.

					<document>:40:2
					39 | @SUBSCRIPTION
					40 | @UNION
					   |  ^
					41 | @VARIABLE_DEFINITION

					<document>:446:21
					445 | directive @SUBSCRIPTION on SUBSCRIPTION
					446 | directive @UNION on UNION
					    |                     ^
					447 | directive @VARIABLE_DEFINITION on VARIABLE_DEFINITION
				""",
				"""
					Directive '@VARIABLE_DEFINITION' is not valid on QUERY but only on VARIABLE_DEFINITION.

					<document>:41:2
					40 | @UNION
					41 | @VARIABLE_DEFINITION
					   |  ^
					42 | {

					<document>:447:35
					446 | directive @UNION on UNION
					447 | directive @VARIABLE_DEFINITION on VARIABLE_DEFINITION
					    |                                   ^
				""",
				"""
					Directive '@ARGUMENT_DEFINITION' is not valid on MUTATION but only on ARGUMENT_DEFINITION.

					<document>:90:2
					89 | mutation
					90 | @ARGUMENT_DEFINITION
					   |  ^
					91 | @ENUM

					<document>:429:35
					428 | 
					429 | directive @ARGUMENT_DEFINITION on ARGUMENT_DEFINITION
					    |                                   ^
					430 | directive @ENUM on ENUM
				""",
				"""
					Directive '@ENUM' is not valid on MUTATION but only on ENUM.

					<document>:91:2
					90 | @ARGUMENT_DEFINITION
					91 | @ENUM
					   |  ^
					92 | @ENUM_VALUE

					<document>:430:20
					429 | directive @ARGUMENT_DEFINITION on ARGUMENT_DEFINITION
					430 | directive @ENUM on ENUM
					    |                    ^
					431 | directive @ENUM_VALUE on ENUM_VALUE
				""",
				"""
					Directive '@ENUM_VALUE' is not valid on MUTATION but only on ENUM_VALUE.

					<document>:92:2
					91 | @ENUM
					92 | @ENUM_VALUE
					   |  ^
					93 | @FIELD

					<document>:431:26
					430 | directive @ENUM on ENUM
					431 | directive @ENUM_VALUE on ENUM_VALUE
					    |                          ^
					432 | directive @FIELD on FIELD
				""",
				"""
					Directive '@FIELD' is not valid on MUTATION but only on FIELD.

					<document>:93:2
					92 | @ENUM_VALUE
					93 | @FIELD
					   |  ^
					94 | @FIELD_DEFINITION

					<document>:432:21
					431 | directive @ENUM_VALUE on ENUM_VALUE
					432 | directive @FIELD on FIELD
					    |                     ^
					433 | directive @FIELD_DEFINITION on FIELD_DEFINITION
				""",
				"""
					Directive '@FIELD_DEFINITION' is not valid on MUTATION but only on FIELD_DEFINITION.

					<document>:94:2
					93 | @FIELD
					94 | @FIELD_DEFINITION
					   |  ^
					95 | @FRAGMENT_DEFINITION

					<document>:433:32
					432 | directive @FIELD on FIELD
					433 | directive @FIELD_DEFINITION on FIELD_DEFINITION
					    |                                ^
					434 | directive @FRAGMENT_DEFINITION on FRAGMENT_DEFINITION
				""",
				"""
					Directive '@FRAGMENT_DEFINITION' is not valid on MUTATION but only on FRAGMENT_DEFINITION.

					<document>:95:2
					94 | @FIELD_DEFINITION
					95 | @FRAGMENT_DEFINITION
					   |  ^
					96 | @FRAGMENT_SPREAD

					<document>:434:35
					433 | directive @FIELD_DEFINITION on FIELD_DEFINITION
					434 | directive @FRAGMENT_DEFINITION on FRAGMENT_DEFINITION
					    |                                   ^
					435 | directive @FRAGMENT_SPREAD on FRAGMENT_SPREAD
				""",
				"""
					Directive '@FRAGMENT_SPREAD' is not valid on MUTATION but only on FRAGMENT_SPREAD.

					<document>:96:2
					95 | @FRAGMENT_DEFINITION
					96 | @FRAGMENT_SPREAD
					   |  ^
					97 | @INLINE_FRAGMENT

					<document>:435:31
					434 | directive @FRAGMENT_DEFINITION on FRAGMENT_DEFINITION
					435 | directive @FRAGMENT_SPREAD on FRAGMENT_SPREAD
					    |                               ^
					436 | directive @INLINE_FRAGMENT on INLINE_FRAGMENT
				""",
				"""
					Directive '@INLINE_FRAGMENT' is not valid on MUTATION but only on INLINE_FRAGMENT.

					<document>:97:2
					96 | @FRAGMENT_SPREAD
					97 | @INLINE_FRAGMENT
					   |  ^
					98 | @INPUT_FIELD_DEFINITION

					<document>:436:31
					435 | directive @FRAGMENT_SPREAD on FRAGMENT_SPREAD
					436 | directive @INLINE_FRAGMENT on INLINE_FRAGMENT
					    |                               ^
					437 | directive @INPUT_FIELD_DEFINITION on INPUT_FIELD_DEFINITION
				""",
				"""
					Directive '@INPUT_FIELD_DEFINITION' is not valid on MUTATION but only on INPUT_FIELD_DEFINITION.

					<document>:98:2
					97 | @INLINE_FRAGMENT
					98 | @INPUT_FIELD_DEFINITION
					   |  ^
					99 | @INPUT_OBJECT

					<document>:437:38
					436 | directive @INLINE_FRAGMENT on INLINE_FRAGMENT
					437 | directive @INPUT_FIELD_DEFINITION on INPUT_FIELD_DEFINITION
					    |                                      ^
					438 | directive @INPUT_OBJECT on INPUT_OBJECT
				""",
				"""
					Directive '@INPUT_OBJECT' is not valid on MUTATION but only on INPUT_OBJECT.

					<document>:99:2
					 98 | @INPUT_FIELD_DEFINITION
					 99 | @INPUT_OBJECT
					    |  ^
					100 | @INTERFACE

					<document>:438:28
					437 | directive @INPUT_FIELD_DEFINITION on INPUT_FIELD_DEFINITION
					438 | directive @INPUT_OBJECT on INPUT_OBJECT
					    |                            ^
					439 | directive @INTERFACE on INTERFACE
				""",
				"""
					Directive '@INTERFACE' is not valid on MUTATION but only on INTERFACE.

					<document>:100:2
					 99 | @INPUT_OBJECT
					100 | @INTERFACE
					    |  ^
					101 | @MUTATION

					<document>:439:25
					438 | directive @INPUT_OBJECT on INPUT_OBJECT
					439 | directive @INTERFACE on INTERFACE
					    |                         ^
					440 | directive @MUTATION on MUTATION
				""",
				"""
					Directive '@OBJECT' is not valid on MUTATION but only on OBJECT.

					<document>:102:2
					101 | @MUTATION
					102 | @OBJECT
					    |  ^
					103 | @QUERY

					<document>:441:22
					440 | directive @MUTATION on MUTATION
					441 | directive @OBJECT on OBJECT
					    |                      ^
					442 | directive @QUERY on QUERY
				""",
				"""
					Directive '@QUERY' is not valid on MUTATION but only on QUERY.

					<document>:103:2
					102 | @OBJECT
					103 | @QUERY
					    |  ^
					104 | @SCALAR

					<document>:442:21
					441 | directive @OBJECT on OBJECT
					442 | directive @QUERY on QUERY
					    |                     ^
					443 | directive @SCALAR on SCALAR
				""",
				"""
					Directive '@SCALAR' is not valid on MUTATION but only on SCALAR.

					<document>:104:2
					103 | @QUERY
					104 | @SCALAR
					    |  ^
					105 | @SCHEMA

					<document>:443:22
					442 | directive @QUERY on QUERY
					443 | directive @SCALAR on SCALAR
					    |                      ^
					444 | directive @SCHEMA on SCHEMA
				""",
				"""
					Directive '@SCHEMA' is not valid on MUTATION but only on SCHEMA.

					<document>:105:2
					104 | @SCALAR
					105 | @SCHEMA
					    |  ^
					106 | @SUBSCRIPTION

					<document>:444:22
					443 | directive @SCALAR on SCALAR
					444 | directive @SCHEMA on SCHEMA
					    |                      ^
					445 | directive @SUBSCRIPTION on SUBSCRIPTION
				""",
				"""
					Directive '@SUBSCRIPTION' is not valid on MUTATION but only on SUBSCRIPTION.

					<document>:106:2
					105 | @SCHEMA
					106 | @SUBSCRIPTION
					    |  ^
					107 | @UNION

					<document>:445:28
					444 | directive @SCHEMA on SCHEMA
					445 | directive @SUBSCRIPTION on SUBSCRIPTION
					    |                            ^
					446 | directive @UNION on UNION
				""",
				"""
					Directive '@UNION' is not valid on MUTATION but only on UNION.

					<document>:107:2
					106 | @SUBSCRIPTION
					107 | @UNION
					    |  ^
					108 | @VARIABLE_DEFINITION

					<document>:446:21
					445 | directive @SUBSCRIPTION on SUBSCRIPTION
					446 | directive @UNION on UNION
					    |                     ^
					447 | directive @VARIABLE_DEFINITION on VARIABLE_DEFINITION
				""",
				"""
					Directive '@VARIABLE_DEFINITION' is not valid on MUTATION but only on VARIABLE_DEFINITION.

					<document>:108:2
					107 | @UNION
					108 | @VARIABLE_DEFINITION
					    |  ^
					109 | {

					<document>:447:35
					446 | directive @UNION on UNION
					447 | directive @VARIABLE_DEFINITION on VARIABLE_DEFINITION
					    |                                   ^
				""",
				"""
					Directive '@ARGUMENT_DEFINITION' is not valid on SUBSCRIPTION but only on ARGUMENT_DEFINITION.

					<document>:114:2
					113 | subscription
					114 | @ARGUMENT_DEFINITION
					    |  ^
					115 | @ENUM

					<document>:429:35
					428 | 
					429 | directive @ARGUMENT_DEFINITION on ARGUMENT_DEFINITION
					    |                                   ^
					430 | directive @ENUM on ENUM
				""",
				"""
					Directive '@ENUM' is not valid on SUBSCRIPTION but only on ENUM.

					<document>:115:2
					114 | @ARGUMENT_DEFINITION
					115 | @ENUM
					    |  ^
					116 | @ENUM_VALUE

					<document>:430:20
					429 | directive @ARGUMENT_DEFINITION on ARGUMENT_DEFINITION
					430 | directive @ENUM on ENUM
					    |                    ^
					431 | directive @ENUM_VALUE on ENUM_VALUE
				""",
				"""
					Directive '@ENUM_VALUE' is not valid on SUBSCRIPTION but only on ENUM_VALUE.

					<document>:116:2
					115 | @ENUM
					116 | @ENUM_VALUE
					    |  ^
					117 | @FIELD

					<document>:431:26
					430 | directive @ENUM on ENUM
					431 | directive @ENUM_VALUE on ENUM_VALUE
					    |                          ^
					432 | directive @FIELD on FIELD
				""",
				"""
					Directive '@FIELD' is not valid on SUBSCRIPTION but only on FIELD.

					<document>:117:2
					116 | @ENUM_VALUE
					117 | @FIELD
					    |  ^
					118 | @FIELD_DEFINITION

					<document>:432:21
					431 | directive @ENUM_VALUE on ENUM_VALUE
					432 | directive @FIELD on FIELD
					    |                     ^
					433 | directive @FIELD_DEFINITION on FIELD_DEFINITION
				""",
				"""
					Directive '@FIELD_DEFINITION' is not valid on SUBSCRIPTION but only on FIELD_DEFINITION.

					<document>:118:2
					117 | @FIELD
					118 | @FIELD_DEFINITION
					    |  ^
					119 | @FRAGMENT_DEFINITION

					<document>:433:32
					432 | directive @FIELD on FIELD
					433 | directive @FIELD_DEFINITION on FIELD_DEFINITION
					    |                                ^
					434 | directive @FRAGMENT_DEFINITION on FRAGMENT_DEFINITION
				""",
				"""
					Directive '@FRAGMENT_DEFINITION' is not valid on SUBSCRIPTION but only on FRAGMENT_DEFINITION.

					<document>:119:2
					118 | @FIELD_DEFINITION
					119 | @FRAGMENT_DEFINITION
					    |  ^
					120 | @FRAGMENT_SPREAD

					<document>:434:35
					433 | directive @FIELD_DEFINITION on FIELD_DEFINITION
					434 | directive @FRAGMENT_DEFINITION on FRAGMENT_DEFINITION
					    |                                   ^
					435 | directive @FRAGMENT_SPREAD on FRAGMENT_SPREAD
				""",
				"""
					Directive '@FRAGMENT_SPREAD' is not valid on SUBSCRIPTION but only on FRAGMENT_SPREAD.

					<document>:120:2
					119 | @FRAGMENT_DEFINITION
					120 | @FRAGMENT_SPREAD
					    |  ^
					121 | @INLINE_FRAGMENT

					<document>:435:31
					434 | directive @FRAGMENT_DEFINITION on FRAGMENT_DEFINITION
					435 | directive @FRAGMENT_SPREAD on FRAGMENT_SPREAD
					    |                               ^
					436 | directive @INLINE_FRAGMENT on INLINE_FRAGMENT
				""",
				"""
					Directive '@INLINE_FRAGMENT' is not valid on SUBSCRIPTION but only on INLINE_FRAGMENT.

					<document>:121:2
					120 | @FRAGMENT_SPREAD
					121 | @INLINE_FRAGMENT
					    |  ^
					122 | @INPUT_FIELD_DEFINITION

					<document>:436:31
					435 | directive @FRAGMENT_SPREAD on FRAGMENT_SPREAD
					436 | directive @INLINE_FRAGMENT on INLINE_FRAGMENT
					    |                               ^
					437 | directive @INPUT_FIELD_DEFINITION on INPUT_FIELD_DEFINITION
				""",
				"""
					Directive '@INPUT_FIELD_DEFINITION' is not valid on SUBSCRIPTION but only on INPUT_FIELD_DEFINITION.

					<document>:122:2
					121 | @INLINE_FRAGMENT
					122 | @INPUT_FIELD_DEFINITION
					    |  ^
					123 | @INPUT_OBJECT

					<document>:437:38
					436 | directive @INLINE_FRAGMENT on INLINE_FRAGMENT
					437 | directive @INPUT_FIELD_DEFINITION on INPUT_FIELD_DEFINITION
					    |                                      ^
					438 | directive @INPUT_OBJECT on INPUT_OBJECT
				""",
				"""
					Directive '@INPUT_OBJECT' is not valid on SUBSCRIPTION but only on INPUT_OBJECT.

					<document>:123:2
					122 | @INPUT_FIELD_DEFINITION
					123 | @INPUT_OBJECT
					    |  ^
					124 | @INTERFACE

					<document>:438:28
					437 | directive @INPUT_FIELD_DEFINITION on INPUT_FIELD_DEFINITION
					438 | directive @INPUT_OBJECT on INPUT_OBJECT
					    |                            ^
					439 | directive @INTERFACE on INTERFACE
				""",
				"""
					Directive '@INTERFACE' is not valid on SUBSCRIPTION but only on INTERFACE.

					<document>:124:2
					123 | @INPUT_OBJECT
					124 | @INTERFACE
					    |  ^
					125 | @MUTATION

					<document>:439:25
					438 | directive @INPUT_OBJECT on INPUT_OBJECT
					439 | directive @INTERFACE on INTERFACE
					    |                         ^
					440 | directive @MUTATION on MUTATION
				""",
				"""
					Directive '@MUTATION' is not valid on SUBSCRIPTION but only on MUTATION.

					<document>:125:2
					124 | @INTERFACE
					125 | @MUTATION
					    |  ^
					126 | @OBJECT

					<document>:440:24
					439 | directive @INTERFACE on INTERFACE
					440 | directive @MUTATION on MUTATION
					    |                        ^
					441 | directive @OBJECT on OBJECT
				""",
				"""
					Directive '@OBJECT' is not valid on SUBSCRIPTION but only on OBJECT.

					<document>:126:2
					125 | @MUTATION
					126 | @OBJECT
					    |  ^
					127 | @QUERY

					<document>:441:22
					440 | directive @MUTATION on MUTATION
					441 | directive @OBJECT on OBJECT
					    |                      ^
					442 | directive @QUERY on QUERY
				""",
				"""
					Directive '@QUERY' is not valid on SUBSCRIPTION but only on QUERY.

					<document>:127:2
					126 | @OBJECT
					127 | @QUERY
					    |  ^
					128 | @SCALAR

					<document>:442:21
					441 | directive @OBJECT on OBJECT
					442 | directive @QUERY on QUERY
					    |                     ^
					443 | directive @SCALAR on SCALAR
				""",
				"""
					Directive '@SCALAR' is not valid on SUBSCRIPTION but only on SCALAR.

					<document>:128:2
					127 | @QUERY
					128 | @SCALAR
					    |  ^
					129 | @SCHEMA

					<document>:443:22
					442 | directive @QUERY on QUERY
					443 | directive @SCALAR on SCALAR
					    |                      ^
					444 | directive @SCHEMA on SCHEMA
				""",
				"""
					Directive '@SCHEMA' is not valid on SUBSCRIPTION but only on SCHEMA.

					<document>:129:2
					128 | @SCALAR
					129 | @SCHEMA
					    |  ^
					130 | @SUBSCRIPTION

					<document>:444:22
					443 | directive @SCALAR on SCALAR
					444 | directive @SCHEMA on SCHEMA
					    |                      ^
					445 | directive @SUBSCRIPTION on SUBSCRIPTION
				""",
				"""
					Directive '@UNION' is not valid on SUBSCRIPTION but only on UNION.

					<document>:131:2
					130 | @SUBSCRIPTION
					131 | @UNION
					    |  ^
					132 | @VARIABLE_DEFINITION

					<document>:446:21
					445 | directive @SUBSCRIPTION on SUBSCRIPTION
					446 | directive @UNION on UNION
					    |                     ^
					447 | directive @VARIABLE_DEFINITION on VARIABLE_DEFINITION
				""",
				"""
					Directive '@VARIABLE_DEFINITION' is not valid on SUBSCRIPTION but only on VARIABLE_DEFINITION.

					<document>:132:2
					131 | @UNION
					132 | @VARIABLE_DEFINITION
					    |  ^
					133 | {

					<document>:447:35
					446 | directive @UNION on UNION
					447 | directive @VARIABLE_DEFINITION on VARIABLE_DEFINITION
					    |                                   ^
				""",
				"""
					Directive '@ARGUMENT_DEFINITION' is not valid on FIELD but only on ARGUMENT_DEFINITION.

					<document>:159:5
					158 |    field
					159 |    @ARGUMENT_DEFINITION
					    |     ^
					160 |    @ENUM

					<document>:429:35
					428 | 
					429 | directive @ARGUMENT_DEFINITION on ARGUMENT_DEFINITION
					    |                                   ^
					430 | directive @ENUM on ENUM
				""",
				"""
					Directive '@ENUM' is not valid on FIELD but only on ENUM.

					<document>:160:5
					159 |    @ARGUMENT_DEFINITION
					160 |    @ENUM
					    |     ^
					161 |    @ENUM_VALUE

					<document>:430:20
					429 | directive @ARGUMENT_DEFINITION on ARGUMENT_DEFINITION
					430 | directive @ENUM on ENUM
					    |                    ^
					431 | directive @ENUM_VALUE on ENUM_VALUE
				""",
				"""
					Directive '@ENUM_VALUE' is not valid on FIELD but only on ENUM_VALUE.

					<document>:161:5
					160 |    @ENUM
					161 |    @ENUM_VALUE
					    |     ^
					162 |    @FIELD

					<document>:431:26
					430 | directive @ENUM on ENUM
					431 | directive @ENUM_VALUE on ENUM_VALUE
					    |                          ^
					432 | directive @FIELD on FIELD
				""",
				"""
					Directive '@FIELD_DEFINITION' is not valid on FIELD but only on FIELD_DEFINITION.

					<document>:163:5
					162 |    @FIELD
					163 |    @FIELD_DEFINITION
					    |     ^
					164 |    @FRAGMENT_DEFINITION

					<document>:433:32
					432 | directive @FIELD on FIELD
					433 | directive @FIELD_DEFINITION on FIELD_DEFINITION
					    |                                ^
					434 | directive @FRAGMENT_DEFINITION on FRAGMENT_DEFINITION
				""",
				"""
					Directive '@FRAGMENT_DEFINITION' is not valid on FIELD but only on FRAGMENT_DEFINITION.

					<document>:164:5
					163 |    @FIELD_DEFINITION
					164 |    @FRAGMENT_DEFINITION
					    |     ^
					165 |    @FRAGMENT_SPREAD

					<document>:434:35
					433 | directive @FIELD_DEFINITION on FIELD_DEFINITION
					434 | directive @FRAGMENT_DEFINITION on FRAGMENT_DEFINITION
					    |                                   ^
					435 | directive @FRAGMENT_SPREAD on FRAGMENT_SPREAD
				""",
				"""
					Directive '@FRAGMENT_SPREAD' is not valid on FIELD but only on FRAGMENT_SPREAD.

					<document>:165:5
					164 |    @FRAGMENT_DEFINITION
					165 |    @FRAGMENT_SPREAD
					    |     ^
					166 |    @INLINE_FRAGMENT

					<document>:435:31
					434 | directive @FRAGMENT_DEFINITION on FRAGMENT_DEFINITION
					435 | directive @FRAGMENT_SPREAD on FRAGMENT_SPREAD
					    |                               ^
					436 | directive @INLINE_FRAGMENT on INLINE_FRAGMENT
				""",
				"""
					Directive '@INLINE_FRAGMENT' is not valid on FIELD but only on INLINE_FRAGMENT.

					<document>:166:5
					165 |    @FRAGMENT_SPREAD
					166 |    @INLINE_FRAGMENT
					    |     ^
					167 |    @INPUT_FIELD_DEFINITION

					<document>:436:31
					435 | directive @FRAGMENT_SPREAD on FRAGMENT_SPREAD
					436 | directive @INLINE_FRAGMENT on INLINE_FRAGMENT
					    |                               ^
					437 | directive @INPUT_FIELD_DEFINITION on INPUT_FIELD_DEFINITION
				""",
				"""
					Directive '@INPUT_FIELD_DEFINITION' is not valid on FIELD but only on INPUT_FIELD_DEFINITION.

					<document>:167:5
					166 |    @INLINE_FRAGMENT
					167 |    @INPUT_FIELD_DEFINITION
					    |     ^
					168 |    @INPUT_OBJECT

					<document>:437:38
					436 | directive @INLINE_FRAGMENT on INLINE_FRAGMENT
					437 | directive @INPUT_FIELD_DEFINITION on INPUT_FIELD_DEFINITION
					    |                                      ^
					438 | directive @INPUT_OBJECT on INPUT_OBJECT
				""",
				"""
					Directive '@INPUT_OBJECT' is not valid on FIELD but only on INPUT_OBJECT.

					<document>:168:5
					167 |    @INPUT_FIELD_DEFINITION
					168 |    @INPUT_OBJECT
					    |     ^
					169 |    @INTERFACE

					<document>:438:28
					437 | directive @INPUT_FIELD_DEFINITION on INPUT_FIELD_DEFINITION
					438 | directive @INPUT_OBJECT on INPUT_OBJECT
					    |                            ^
					439 | directive @INTERFACE on INTERFACE
				""",
				"""
					Directive '@INTERFACE' is not valid on FIELD but only on INTERFACE.

					<document>:169:5
					168 |    @INPUT_OBJECT
					169 |    @INTERFACE
					    |     ^
					170 |    @MUTATION

					<document>:439:25
					438 | directive @INPUT_OBJECT on INPUT_OBJECT
					439 | directive @INTERFACE on INTERFACE
					    |                         ^
					440 | directive @MUTATION on MUTATION
				""",
				"""
					Directive '@MUTATION' is not valid on FIELD but only on MUTATION.

					<document>:170:5
					169 |    @INTERFACE
					170 |    @MUTATION
					    |     ^
					171 |    @OBJECT

					<document>:440:24
					439 | directive @INTERFACE on INTERFACE
					440 | directive @MUTATION on MUTATION
					    |                        ^
					441 | directive @OBJECT on OBJECT
				""",
				"""
					Directive '@OBJECT' is not valid on FIELD but only on OBJECT.

					<document>:171:5
					170 |    @MUTATION
					171 |    @OBJECT
					    |     ^
					172 |    @QUERY

					<document>:441:22
					440 | directive @MUTATION on MUTATION
					441 | directive @OBJECT on OBJECT
					    |                      ^
					442 | directive @QUERY on QUERY
				""",
				"""
					Directive '@QUERY' is not valid on FIELD but only on QUERY.

					<document>:172:5
					171 |    @OBJECT
					172 |    @QUERY
					    |     ^
					173 |    @SCALAR

					<document>:442:21
					441 | directive @OBJECT on OBJECT
					442 | directive @QUERY on QUERY
					    |                     ^
					443 | directive @SCALAR on SCALAR
				""",
				"""
					Directive '@SCALAR' is not valid on FIELD but only on SCALAR.

					<document>:173:5
					172 |    @QUERY
					173 |    @SCALAR
					    |     ^
					174 |    @SCHEMA

					<document>:443:22
					442 | directive @QUERY on QUERY
					443 | directive @SCALAR on SCALAR
					    |                      ^
					444 | directive @SCHEMA on SCHEMA
				""",
				"""
					Directive '@SCHEMA' is not valid on FIELD but only on SCHEMA.

					<document>:174:5
					173 |    @SCALAR
					174 |    @SCHEMA
					    |     ^
					175 |    @SUBSCRIPTION

					<document>:444:22
					443 | directive @SCALAR on SCALAR
					444 | directive @SCHEMA on SCHEMA
					    |                      ^
					445 | directive @SUBSCRIPTION on SUBSCRIPTION
				""",
				"""
					Directive '@SUBSCRIPTION' is not valid on FIELD but only on SUBSCRIPTION.

					<document>:175:5
					174 |    @SCHEMA
					175 |    @SUBSCRIPTION
					    |     ^
					176 |    @UNION

					<document>:445:28
					444 | directive @SCHEMA on SCHEMA
					445 | directive @SUBSCRIPTION on SUBSCRIPTION
					    |                            ^
					446 | directive @UNION on UNION
				""",
				"""
					Directive '@UNION' is not valid on FIELD but only on UNION.

					<document>:176:5
					175 |    @SUBSCRIPTION
					176 |    @UNION
					    |     ^
					177 |    @VARIABLE_DEFINITION

					<document>:446:21
					445 | directive @SUBSCRIPTION on SUBSCRIPTION
					446 | directive @UNION on UNION
					    |                     ^
					447 | directive @VARIABLE_DEFINITION on VARIABLE_DEFINITION
				""",
				"""
					Directive '@VARIABLE_DEFINITION' is not valid on FIELD but only on VARIABLE_DEFINITION.

					<document>:177:5
					176 |    @UNION
					177 |    @VARIABLE_DEFINITION
					    |     ^
					178 | }

					<document>:447:35
					446 | directive @UNION on UNION
					447 | directive @VARIABLE_DEFINITION on VARIABLE_DEFINITION
					    |                                   ^
				""",
				"""
					Directive '@ARGUMENT_DEFINITION' is not valid on FRAGMENT_DEFINITION but only on ARGUMENT_DEFINITION.

					<document>:138:2
					137 | fragment frag on Query
					138 | @ARGUMENT_DEFINITION
					    |  ^
					139 | @ENUM

					<document>:429:35
					428 | 
					429 | directive @ARGUMENT_DEFINITION on ARGUMENT_DEFINITION
					    |                                   ^
					430 | directive @ENUM on ENUM
				""",
				"""
					Directive '@ENUM' is not valid on FRAGMENT_DEFINITION but only on ENUM.

					<document>:139:2
					138 | @ARGUMENT_DEFINITION
					139 | @ENUM
					    |  ^
					140 | @ENUM_VALUE

					<document>:430:20
					429 | directive @ARGUMENT_DEFINITION on ARGUMENT_DEFINITION
					430 | directive @ENUM on ENUM
					    |                    ^
					431 | directive @ENUM_VALUE on ENUM_VALUE
				""",
				"""
					Directive '@ENUM_VALUE' is not valid on FRAGMENT_DEFINITION but only on ENUM_VALUE.

					<document>:140:2
					139 | @ENUM
					140 | @ENUM_VALUE
					    |  ^
					141 | @FIELD

					<document>:431:26
					430 | directive @ENUM on ENUM
					431 | directive @ENUM_VALUE on ENUM_VALUE
					    |                          ^
					432 | directive @FIELD on FIELD
				""",
				"""
					Directive '@FIELD' is not valid on FRAGMENT_DEFINITION but only on FIELD.

					<document>:141:2
					140 | @ENUM_VALUE
					141 | @FIELD
					    |  ^
					142 | @FIELD_DEFINITION

					<document>:432:21
					431 | directive @ENUM_VALUE on ENUM_VALUE
					432 | directive @FIELD on FIELD
					    |                     ^
					433 | directive @FIELD_DEFINITION on FIELD_DEFINITION
				""",
				"""
					Directive '@FIELD_DEFINITION' is not valid on FRAGMENT_DEFINITION but only on FIELD_DEFINITION.

					<document>:142:2
					141 | @FIELD
					142 | @FIELD_DEFINITION
					    |  ^
					143 | @FRAGMENT_DEFINITION

					<document>:433:32
					432 | directive @FIELD on FIELD
					433 | directive @FIELD_DEFINITION on FIELD_DEFINITION
					    |                                ^
					434 | directive @FRAGMENT_DEFINITION on FRAGMENT_DEFINITION
				""",
				"""
					Directive '@FRAGMENT_SPREAD' is not valid on FRAGMENT_DEFINITION but only on FRAGMENT_SPREAD.

					<document>:144:2
					143 | @FRAGMENT_DEFINITION
					144 | @FRAGMENT_SPREAD
					    |  ^
					145 | @INLINE_FRAGMENT

					<document>:435:31
					434 | directive @FRAGMENT_DEFINITION on FRAGMENT_DEFINITION
					435 | directive @FRAGMENT_SPREAD on FRAGMENT_SPREAD
					    |                               ^
					436 | directive @INLINE_FRAGMENT on INLINE_FRAGMENT
				""",
				"""
					Directive '@INLINE_FRAGMENT' is not valid on FRAGMENT_DEFINITION but only on INLINE_FRAGMENT.

					<document>:145:2
					144 | @FRAGMENT_SPREAD
					145 | @INLINE_FRAGMENT
					    |  ^
					146 | @INPUT_FIELD_DEFINITION

					<document>:436:31
					435 | directive @FRAGMENT_SPREAD on FRAGMENT_SPREAD
					436 | directive @INLINE_FRAGMENT on INLINE_FRAGMENT
					    |                               ^
					437 | directive @INPUT_FIELD_DEFINITION on INPUT_FIELD_DEFINITION
				""",
				"""
					Directive '@INPUT_FIELD_DEFINITION' is not valid on FRAGMENT_DEFINITION but only on INPUT_FIELD_DEFINITION.

					<document>:146:2
					145 | @INLINE_FRAGMENT
					146 | @INPUT_FIELD_DEFINITION
					    |  ^
					147 | @INPUT_OBJECT

					<document>:437:38
					436 | directive @INLINE_FRAGMENT on INLINE_FRAGMENT
					437 | directive @INPUT_FIELD_DEFINITION on INPUT_FIELD_DEFINITION
					    |                                      ^
					438 | directive @INPUT_OBJECT on INPUT_OBJECT
				""",
				"""
					Directive '@INPUT_OBJECT' is not valid on FRAGMENT_DEFINITION but only on INPUT_OBJECT.

					<document>:147:2
					146 | @INPUT_FIELD_DEFINITION
					147 | @INPUT_OBJECT
					    |  ^
					148 | @INTERFACE

					<document>:438:28
					437 | directive @INPUT_FIELD_DEFINITION on INPUT_FIELD_DEFINITION
					438 | directive @INPUT_OBJECT on INPUT_OBJECT
					    |                            ^
					439 | directive @INTERFACE on INTERFACE
				""",
				"""
					Directive '@INTERFACE' is not valid on FRAGMENT_DEFINITION but only on INTERFACE.

					<document>:148:2
					147 | @INPUT_OBJECT
					148 | @INTERFACE
					    |  ^
					149 | @MUTATION

					<document>:439:25
					438 | directive @INPUT_OBJECT on INPUT_OBJECT
					439 | directive @INTERFACE on INTERFACE
					    |                         ^
					440 | directive @MUTATION on MUTATION
				""",
				"""
					Directive '@MUTATION' is not valid on FRAGMENT_DEFINITION but only on MUTATION.

					<document>:149:2
					148 | @INTERFACE
					149 | @MUTATION
					    |  ^
					150 | @OBJECT

					<document>:440:24
					439 | directive @INTERFACE on INTERFACE
					440 | directive @MUTATION on MUTATION
					    |                        ^
					441 | directive @OBJECT on OBJECT
				""",
				"""
					Directive '@OBJECT' is not valid on FRAGMENT_DEFINITION but only on OBJECT.

					<document>:150:2
					149 | @MUTATION
					150 | @OBJECT
					    |  ^
					151 | @QUERY

					<document>:441:22
					440 | directive @MUTATION on MUTATION
					441 | directive @OBJECT on OBJECT
					    |                      ^
					442 | directive @QUERY on QUERY
				""",
				"""
					Directive '@QUERY' is not valid on FRAGMENT_DEFINITION but only on QUERY.

					<document>:151:2
					150 | @OBJECT
					151 | @QUERY
					    |  ^
					152 | @SCALAR

					<document>:442:21
					441 | directive @OBJECT on OBJECT
					442 | directive @QUERY on QUERY
					    |                     ^
					443 | directive @SCALAR on SCALAR
				""",
				"""
					Directive '@SCALAR' is not valid on FRAGMENT_DEFINITION but only on SCALAR.

					<document>:152:2
					151 | @QUERY
					152 | @SCALAR
					    |  ^
					153 | @SCHEMA

					<document>:443:22
					442 | directive @QUERY on QUERY
					443 | directive @SCALAR on SCALAR
					    |                      ^
					444 | directive @SCHEMA on SCHEMA
				""",
				"""
					Directive '@SCHEMA' is not valid on FRAGMENT_DEFINITION but only on SCHEMA.

					<document>:153:2
					152 | @SCALAR
					153 | @SCHEMA
					    |  ^
					154 | @SUBSCRIPTION

					<document>:444:22
					443 | directive @SCALAR on SCALAR
					444 | directive @SCHEMA on SCHEMA
					    |                      ^
					445 | directive @SUBSCRIPTION on SUBSCRIPTION
				""",
				"""
					Directive '@SUBSCRIPTION' is not valid on FRAGMENT_DEFINITION but only on SUBSCRIPTION.

					<document>:154:2
					153 | @SCHEMA
					154 | @SUBSCRIPTION
					    |  ^
					155 | @UNION

					<document>:445:28
					444 | directive @SCHEMA on SCHEMA
					445 | directive @SUBSCRIPTION on SUBSCRIPTION
					    |                            ^
					446 | directive @UNION on UNION
				""",
				"""
					Directive '@UNION' is not valid on FRAGMENT_DEFINITION but only on UNION.

					<document>:155:2
					154 | @SUBSCRIPTION
					155 | @UNION
					    |  ^
					156 | @VARIABLE_DEFINITION

					<document>:446:21
					445 | directive @SUBSCRIPTION on SUBSCRIPTION
					446 | directive @UNION on UNION
					    |                     ^
					447 | directive @VARIABLE_DEFINITION on VARIABLE_DEFINITION
				""",
				"""
					Directive '@VARIABLE_DEFINITION' is not valid on FRAGMENT_DEFINITION but only on VARIABLE_DEFINITION.

					<document>:156:2
					155 | @UNION
					156 | @VARIABLE_DEFINITION
					    |  ^
					157 | {

					<document>:447:35
					446 | directive @UNION on UNION
					447 | directive @VARIABLE_DEFINITION on VARIABLE_DEFINITION
					    |                                   ^
				""",
				"""
					Directive '@ARGUMENT_DEFINITION' is not valid on ENUM_VALUE but only on ARGUMENT_DEFINITION.

					<document>:202:5
					201 |    VALUE
					202 |    @ARGUMENT_DEFINITION
					    |     ^
					203 |    @ENUM

					<document>:429:35
					428 | 
					429 | directive @ARGUMENT_DEFINITION on ARGUMENT_DEFINITION
					    |                                   ^
					430 | directive @ENUM on ENUM
				""",
				"""
					Directive '@ENUM' is not valid on ENUM_VALUE but only on ENUM.

					<document>:203:5
					202 |    @ARGUMENT_DEFINITION
					203 |    @ENUM
					    |     ^
					204 |    @ENUM_VALUE

					<document>:430:20
					429 | directive @ARGUMENT_DEFINITION on ARGUMENT_DEFINITION
					430 | directive @ENUM on ENUM
					    |                    ^
					431 | directive @ENUM_VALUE on ENUM_VALUE
				""",
				"""
					Directive '@FIELD' is not valid on ENUM_VALUE but only on FIELD.

					<document>:205:5
					204 |    @ENUM_VALUE
					205 |    @FIELD
					    |     ^
					206 |    @FIELD_DEFINITION

					<document>:432:21
					431 | directive @ENUM_VALUE on ENUM_VALUE
					432 | directive @FIELD on FIELD
					    |                     ^
					433 | directive @FIELD_DEFINITION on FIELD_DEFINITION
				""",
				"""
					Directive '@FIELD_DEFINITION' is not valid on ENUM_VALUE but only on FIELD_DEFINITION.

					<document>:206:5
					205 |    @FIELD
					206 |    @FIELD_DEFINITION
					    |     ^
					207 |    @FRAGMENT_DEFINITION

					<document>:433:32
					432 | directive @FIELD on FIELD
					433 | directive @FIELD_DEFINITION on FIELD_DEFINITION
					    |                                ^
					434 | directive @FRAGMENT_DEFINITION on FRAGMENT_DEFINITION
				""",
				"""
					Directive '@FRAGMENT_DEFINITION' is not valid on ENUM_VALUE but only on FRAGMENT_DEFINITION.

					<document>:207:5
					206 |    @FIELD_DEFINITION
					207 |    @FRAGMENT_DEFINITION
					    |     ^
					208 |    @FRAGMENT_SPREAD

					<document>:434:35
					433 | directive @FIELD_DEFINITION on FIELD_DEFINITION
					434 | directive @FRAGMENT_DEFINITION on FRAGMENT_DEFINITION
					    |                                   ^
					435 | directive @FRAGMENT_SPREAD on FRAGMENT_SPREAD
				""",
				"""
					Directive '@FRAGMENT_SPREAD' is not valid on ENUM_VALUE but only on FRAGMENT_SPREAD.

					<document>:208:5
					207 |    @FRAGMENT_DEFINITION
					208 |    @FRAGMENT_SPREAD
					    |     ^
					209 |    @INLINE_FRAGMENT

					<document>:435:31
					434 | directive @FRAGMENT_DEFINITION on FRAGMENT_DEFINITION
					435 | directive @FRAGMENT_SPREAD on FRAGMENT_SPREAD
					    |                               ^
					436 | directive @INLINE_FRAGMENT on INLINE_FRAGMENT
				""",
				"""
					Directive '@INLINE_FRAGMENT' is not valid on ENUM_VALUE but only on INLINE_FRAGMENT.

					<document>:209:5
					208 |    @FRAGMENT_SPREAD
					209 |    @INLINE_FRAGMENT
					    |     ^
					210 |    @INPUT_FIELD_DEFINITION

					<document>:436:31
					435 | directive @FRAGMENT_SPREAD on FRAGMENT_SPREAD
					436 | directive @INLINE_FRAGMENT on INLINE_FRAGMENT
					    |                               ^
					437 | directive @INPUT_FIELD_DEFINITION on INPUT_FIELD_DEFINITION
				""",
				"""
					Directive '@INPUT_FIELD_DEFINITION' is not valid on ENUM_VALUE but only on INPUT_FIELD_DEFINITION.

					<document>:210:5
					209 |    @INLINE_FRAGMENT
					210 |    @INPUT_FIELD_DEFINITION
					    |     ^
					211 |    @INPUT_OBJECT

					<document>:437:38
					436 | directive @INLINE_FRAGMENT on INLINE_FRAGMENT
					437 | directive @INPUT_FIELD_DEFINITION on INPUT_FIELD_DEFINITION
					    |                                      ^
					438 | directive @INPUT_OBJECT on INPUT_OBJECT
				""",
				"""
					Directive '@INPUT_OBJECT' is not valid on ENUM_VALUE but only on INPUT_OBJECT.

					<document>:211:5
					210 |    @INPUT_FIELD_DEFINITION
					211 |    @INPUT_OBJECT
					    |     ^
					212 |    @INTERFACE

					<document>:438:28
					437 | directive @INPUT_FIELD_DEFINITION on INPUT_FIELD_DEFINITION
					438 | directive @INPUT_OBJECT on INPUT_OBJECT
					    |                            ^
					439 | directive @INTERFACE on INTERFACE
				""",
				"""
					Directive '@INTERFACE' is not valid on ENUM_VALUE but only on INTERFACE.

					<document>:212:5
					211 |    @INPUT_OBJECT
					212 |    @INTERFACE
					    |     ^
					213 |    @MUTATION

					<document>:439:25
					438 | directive @INPUT_OBJECT on INPUT_OBJECT
					439 | directive @INTERFACE on INTERFACE
					    |                         ^
					440 | directive @MUTATION on MUTATION
				""",
				"""
					Directive '@MUTATION' is not valid on ENUM_VALUE but only on MUTATION.

					<document>:213:5
					212 |    @INTERFACE
					213 |    @MUTATION
					    |     ^
					214 |    @OBJECT

					<document>:440:24
					439 | directive @INTERFACE on INTERFACE
					440 | directive @MUTATION on MUTATION
					    |                        ^
					441 | directive @OBJECT on OBJECT
				""",
				"""
					Directive '@OBJECT' is not valid on ENUM_VALUE but only on OBJECT.

					<document>:214:5
					213 |    @MUTATION
					214 |    @OBJECT
					    |     ^
					215 |    @QUERY

					<document>:441:22
					440 | directive @MUTATION on MUTATION
					441 | directive @OBJECT on OBJECT
					    |                      ^
					442 | directive @QUERY on QUERY
				""",
				"""
					Directive '@QUERY' is not valid on ENUM_VALUE but only on QUERY.

					<document>:215:5
					214 |    @OBJECT
					215 |    @QUERY
					    |     ^
					216 |    @SCALAR

					<document>:442:21
					441 | directive @OBJECT on OBJECT
					442 | directive @QUERY on QUERY
					    |                     ^
					443 | directive @SCALAR on SCALAR
				""",
				"""
					Directive '@SCALAR' is not valid on ENUM_VALUE but only on SCALAR.

					<document>:216:5
					215 |    @QUERY
					216 |    @SCALAR
					    |     ^
					217 |    @SCHEMA

					<document>:443:22
					442 | directive @QUERY on QUERY
					443 | directive @SCALAR on SCALAR
					    |                      ^
					444 | directive @SCHEMA on SCHEMA
				""",
				"""
					Directive '@SCHEMA' is not valid on ENUM_VALUE but only on SCHEMA.

					<document>:217:5
					216 |    @SCALAR
					217 |    @SCHEMA
					    |     ^
					218 |    @SUBSCRIPTION

					<document>:444:22
					443 | directive @SCALAR on SCALAR
					444 | directive @SCHEMA on SCHEMA
					    |                      ^
					445 | directive @SUBSCRIPTION on SUBSCRIPTION
				""",
				"""
					Directive '@SUBSCRIPTION' is not valid on ENUM_VALUE but only on SUBSCRIPTION.

					<document>:218:5
					217 |    @SCHEMA
					218 |    @SUBSCRIPTION
					    |     ^
					219 |    @UNION

					<document>:445:28
					444 | directive @SCHEMA on SCHEMA
					445 | directive @SUBSCRIPTION on SUBSCRIPTION
					    |                            ^
					446 | directive @UNION on UNION
				""",
				"""
					Directive '@UNION' is not valid on ENUM_VALUE but only on UNION.

					<document>:219:5
					218 |    @SUBSCRIPTION
					219 |    @UNION
					    |     ^
					220 |    @VARIABLE_DEFINITION

					<document>:446:21
					445 | directive @SUBSCRIPTION on SUBSCRIPTION
					446 | directive @UNION on UNION
					    |                     ^
					447 | directive @VARIABLE_DEFINITION on VARIABLE_DEFINITION
				""",
				"""
					Directive '@VARIABLE_DEFINITION' is not valid on ENUM_VALUE but only on VARIABLE_DEFINITION.

					<document>:220:5
					219 |    @UNION
					220 |    @VARIABLE_DEFINITION
					    |     ^
					221 | }

					<document>:447:35
					446 | directive @UNION on UNION
					447 | directive @VARIABLE_DEFINITION on VARIABLE_DEFINITION
					    |                                   ^
				""",
				"""
					Directive '@ARGUMENT_DEFINITION' is not valid on ENUM but only on ARGUMENT_DEFINITION.

					<document>:181:2
					180 | enum Enum
					181 | @ARGUMENT_DEFINITION
					    |  ^
					182 | @ENUM

					<document>:429:35
					428 | 
					429 | directive @ARGUMENT_DEFINITION on ARGUMENT_DEFINITION
					    |                                   ^
					430 | directive @ENUM on ENUM
				""",
				"""
					Directive '@ENUM_VALUE' is not valid on ENUM but only on ENUM_VALUE.

					<document>:183:2
					182 | @ENUM
					183 | @ENUM_VALUE
					    |  ^
					184 | @FIELD

					<document>:431:26
					430 | directive @ENUM on ENUM
					431 | directive @ENUM_VALUE on ENUM_VALUE
					    |                          ^
					432 | directive @FIELD on FIELD
				""",
				"""
					Directive '@FIELD' is not valid on ENUM but only on FIELD.

					<document>:184:2
					183 | @ENUM_VALUE
					184 | @FIELD
					    |  ^
					185 | @FIELD_DEFINITION

					<document>:432:21
					431 | directive @ENUM_VALUE on ENUM_VALUE
					432 | directive @FIELD on FIELD
					    |                     ^
					433 | directive @FIELD_DEFINITION on FIELD_DEFINITION
				""",
				"""
					Directive '@FIELD_DEFINITION' is not valid on ENUM but only on FIELD_DEFINITION.

					<document>:185:2
					184 | @FIELD
					185 | @FIELD_DEFINITION
					    |  ^
					186 | @FRAGMENT_DEFINITION

					<document>:433:32
					432 | directive @FIELD on FIELD
					433 | directive @FIELD_DEFINITION on FIELD_DEFINITION
					    |                                ^
					434 | directive @FRAGMENT_DEFINITION on FRAGMENT_DEFINITION
				""",
				"""
					Directive '@FRAGMENT_DEFINITION' is not valid on ENUM but only on FRAGMENT_DEFINITION.

					<document>:186:2
					185 | @FIELD_DEFINITION
					186 | @FRAGMENT_DEFINITION
					    |  ^
					187 | @FRAGMENT_SPREAD

					<document>:434:35
					433 | directive @FIELD_DEFINITION on FIELD_DEFINITION
					434 | directive @FRAGMENT_DEFINITION on FRAGMENT_DEFINITION
					    |                                   ^
					435 | directive @FRAGMENT_SPREAD on FRAGMENT_SPREAD
				""",
				"""
					Directive '@FRAGMENT_SPREAD' is not valid on ENUM but only on FRAGMENT_SPREAD.

					<document>:187:2
					186 | @FRAGMENT_DEFINITION
					187 | @FRAGMENT_SPREAD
					    |  ^
					188 | @INLINE_FRAGMENT

					<document>:435:31
					434 | directive @FRAGMENT_DEFINITION on FRAGMENT_DEFINITION
					435 | directive @FRAGMENT_SPREAD on FRAGMENT_SPREAD
					    |                               ^
					436 | directive @INLINE_FRAGMENT on INLINE_FRAGMENT
				""",
				"""
					Directive '@INLINE_FRAGMENT' is not valid on ENUM but only on INLINE_FRAGMENT.

					<document>:188:2
					187 | @FRAGMENT_SPREAD
					188 | @INLINE_FRAGMENT
					    |  ^
					189 | @INPUT_FIELD_DEFINITION

					<document>:436:31
					435 | directive @FRAGMENT_SPREAD on FRAGMENT_SPREAD
					436 | directive @INLINE_FRAGMENT on INLINE_FRAGMENT
					    |                               ^
					437 | directive @INPUT_FIELD_DEFINITION on INPUT_FIELD_DEFINITION
				""",
				"""
					Directive '@INPUT_FIELD_DEFINITION' is not valid on ENUM but only on INPUT_FIELD_DEFINITION.

					<document>:189:2
					188 | @INLINE_FRAGMENT
					189 | @INPUT_FIELD_DEFINITION
					    |  ^
					190 | @INPUT_OBJECT

					<document>:437:38
					436 | directive @INLINE_FRAGMENT on INLINE_FRAGMENT
					437 | directive @INPUT_FIELD_DEFINITION on INPUT_FIELD_DEFINITION
					    |                                      ^
					438 | directive @INPUT_OBJECT on INPUT_OBJECT
				""",
				"""
					Directive '@INPUT_OBJECT' is not valid on ENUM but only on INPUT_OBJECT.

					<document>:190:2
					189 | @INPUT_FIELD_DEFINITION
					190 | @INPUT_OBJECT
					    |  ^
					191 | @INTERFACE

					<document>:438:28
					437 | directive @INPUT_FIELD_DEFINITION on INPUT_FIELD_DEFINITION
					438 | directive @INPUT_OBJECT on INPUT_OBJECT
					    |                            ^
					439 | directive @INTERFACE on INTERFACE
				""",
				"""
					Directive '@INTERFACE' is not valid on ENUM but only on INTERFACE.

					<document>:191:2
					190 | @INPUT_OBJECT
					191 | @INTERFACE
					    |  ^
					192 | @MUTATION

					<document>:439:25
					438 | directive @INPUT_OBJECT on INPUT_OBJECT
					439 | directive @INTERFACE on INTERFACE
					    |                         ^
					440 | directive @MUTATION on MUTATION
				""",
				"""
					Directive '@MUTATION' is not valid on ENUM but only on MUTATION.

					<document>:192:2
					191 | @INTERFACE
					192 | @MUTATION
					    |  ^
					193 | @OBJECT

					<document>:440:24
					439 | directive @INTERFACE on INTERFACE
					440 | directive @MUTATION on MUTATION
					    |                        ^
					441 | directive @OBJECT on OBJECT
				""",
				"""
					Directive '@OBJECT' is not valid on ENUM but only on OBJECT.

					<document>:193:2
					192 | @MUTATION
					193 | @OBJECT
					    |  ^
					194 | @QUERY

					<document>:441:22
					440 | directive @MUTATION on MUTATION
					441 | directive @OBJECT on OBJECT
					    |                      ^
					442 | directive @QUERY on QUERY
				""",
				"""
					Directive '@QUERY' is not valid on ENUM but only on QUERY.

					<document>:194:2
					193 | @OBJECT
					194 | @QUERY
					    |  ^
					195 | @SCALAR

					<document>:442:21
					441 | directive @OBJECT on OBJECT
					442 | directive @QUERY on QUERY
					    |                     ^
					443 | directive @SCALAR on SCALAR
				""",
				"""
					Directive '@SCALAR' is not valid on ENUM but only on SCALAR.

					<document>:195:2
					194 | @QUERY
					195 | @SCALAR
					    |  ^
					196 | @SCHEMA

					<document>:443:22
					442 | directive @QUERY on QUERY
					443 | directive @SCALAR on SCALAR
					    |                      ^
					444 | directive @SCHEMA on SCHEMA
				""",
				"""
					Directive '@SCHEMA' is not valid on ENUM but only on SCHEMA.

					<document>:196:2
					195 | @SCALAR
					196 | @SCHEMA
					    |  ^
					197 | @SUBSCRIPTION

					<document>:444:22
					443 | directive @SCALAR on SCALAR
					444 | directive @SCHEMA on SCHEMA
					    |                      ^
					445 | directive @SUBSCRIPTION on SUBSCRIPTION
				""",
				"""
					Directive '@SUBSCRIPTION' is not valid on ENUM but only on SUBSCRIPTION.

					<document>:197:2
					196 | @SCHEMA
					197 | @SUBSCRIPTION
					    |  ^
					198 | @UNION

					<document>:445:28
					444 | directive @SCHEMA on SCHEMA
					445 | directive @SUBSCRIPTION on SUBSCRIPTION
					    |                            ^
					446 | directive @UNION on UNION
				""",
				"""
					Directive '@UNION' is not valid on ENUM but only on UNION.

					<document>:198:2
					197 | @SUBSCRIPTION
					198 | @UNION
					    |  ^
					199 | @VARIABLE_DEFINITION

					<document>:446:21
					445 | directive @SUBSCRIPTION on SUBSCRIPTION
					446 | directive @UNION on UNION
					    |                     ^
					447 | directive @VARIABLE_DEFINITION on VARIABLE_DEFINITION
				""",
				"""
					Directive '@VARIABLE_DEFINITION' is not valid on ENUM but only on VARIABLE_DEFINITION.

					<document>:199:2
					198 | @UNION
					199 | @VARIABLE_DEFINITION
					    |  ^
					200 | {

					<document>:447:35
					446 | directive @UNION on UNION
					447 | directive @VARIABLE_DEFINITION on VARIABLE_DEFINITION
					    |                                   ^
				""",
				"""
					Directive '@ARGUMENT_DEFINITION' is not valid on INPUT_FIELD_DEFINITION but only on ARGUMENT_DEFINITION.

					<document>:245:2
					244 |    field: String
					245 | @ARGUMENT_DEFINITION
					    |  ^
					246 | @ENUM

					<document>:429:35
					428 | 
					429 | directive @ARGUMENT_DEFINITION on ARGUMENT_DEFINITION
					    |                                   ^
					430 | directive @ENUM on ENUM
				""",
				"""
					Directive '@ENUM' is not valid on INPUT_FIELD_DEFINITION but only on ENUM.

					<document>:246:2
					245 | @ARGUMENT_DEFINITION
					246 | @ENUM
					    |  ^
					247 | @ENUM_VALUE

					<document>:430:20
					429 | directive @ARGUMENT_DEFINITION on ARGUMENT_DEFINITION
					430 | directive @ENUM on ENUM
					    |                    ^
					431 | directive @ENUM_VALUE on ENUM_VALUE
				""",
				"""
					Directive '@ENUM_VALUE' is not valid on INPUT_FIELD_DEFINITION but only on ENUM_VALUE.

					<document>:247:2
					246 | @ENUM
					247 | @ENUM_VALUE
					    |  ^
					248 | @FIELD

					<document>:431:26
					430 | directive @ENUM on ENUM
					431 | directive @ENUM_VALUE on ENUM_VALUE
					    |                          ^
					432 | directive @FIELD on FIELD
				""",
				"""
					Directive '@FIELD' is not valid on INPUT_FIELD_DEFINITION but only on FIELD.

					<document>:248:2
					247 | @ENUM_VALUE
					248 | @FIELD
					    |  ^
					249 | @FIELD_DEFINITION

					<document>:432:21
					431 | directive @ENUM_VALUE on ENUM_VALUE
					432 | directive @FIELD on FIELD
					    |                     ^
					433 | directive @FIELD_DEFINITION on FIELD_DEFINITION
				""",
				"""
					Directive '@FIELD_DEFINITION' is not valid on INPUT_FIELD_DEFINITION but only on FIELD_DEFINITION.

					<document>:249:2
					248 | @FIELD
					249 | @FIELD_DEFINITION
					    |  ^
					250 | @FRAGMENT_DEFINITION

					<document>:433:32
					432 | directive @FIELD on FIELD
					433 | directive @FIELD_DEFINITION on FIELD_DEFINITION
					    |                                ^
					434 | directive @FRAGMENT_DEFINITION on FRAGMENT_DEFINITION
				""",
				"""
					Directive '@FRAGMENT_DEFINITION' is not valid on INPUT_FIELD_DEFINITION but only on FRAGMENT_DEFINITION.

					<document>:250:2
					249 | @FIELD_DEFINITION
					250 | @FRAGMENT_DEFINITION
					    |  ^
					251 | @FRAGMENT_SPREAD

					<document>:434:35
					433 | directive @FIELD_DEFINITION on FIELD_DEFINITION
					434 | directive @FRAGMENT_DEFINITION on FRAGMENT_DEFINITION
					    |                                   ^
					435 | directive @FRAGMENT_SPREAD on FRAGMENT_SPREAD
				""",
				"""
					Directive '@FRAGMENT_SPREAD' is not valid on INPUT_FIELD_DEFINITION but only on FRAGMENT_SPREAD.

					<document>:251:2
					250 | @FRAGMENT_DEFINITION
					251 | @FRAGMENT_SPREAD
					    |  ^
					252 | @INLINE_FRAGMENT

					<document>:435:31
					434 | directive @FRAGMENT_DEFINITION on FRAGMENT_DEFINITION
					435 | directive @FRAGMENT_SPREAD on FRAGMENT_SPREAD
					    |                               ^
					436 | directive @INLINE_FRAGMENT on INLINE_FRAGMENT
				""",
				"""
					Directive '@INLINE_FRAGMENT' is not valid on INPUT_FIELD_DEFINITION but only on INLINE_FRAGMENT.

					<document>:252:2
					251 | @FRAGMENT_SPREAD
					252 | @INLINE_FRAGMENT
					    |  ^
					253 | @INPUT_FIELD_DEFINITION

					<document>:436:31
					435 | directive @FRAGMENT_SPREAD on FRAGMENT_SPREAD
					436 | directive @INLINE_FRAGMENT on INLINE_FRAGMENT
					    |                               ^
					437 | directive @INPUT_FIELD_DEFINITION on INPUT_FIELD_DEFINITION
				""",
				"""
					Directive '@INPUT_OBJECT' is not valid on INPUT_FIELD_DEFINITION but only on INPUT_OBJECT.

					<document>:254:2
					253 | @INPUT_FIELD_DEFINITION
					254 | @INPUT_OBJECT
					    |  ^
					255 | @INTERFACE

					<document>:438:28
					437 | directive @INPUT_FIELD_DEFINITION on INPUT_FIELD_DEFINITION
					438 | directive @INPUT_OBJECT on INPUT_OBJECT
					    |                            ^
					439 | directive @INTERFACE on INTERFACE
				""",
				"""
					Directive '@INTERFACE' is not valid on INPUT_FIELD_DEFINITION but only on INTERFACE.

					<document>:255:2
					254 | @INPUT_OBJECT
					255 | @INTERFACE
					    |  ^
					256 | @MUTATION

					<document>:439:25
					438 | directive @INPUT_OBJECT on INPUT_OBJECT
					439 | directive @INTERFACE on INTERFACE
					    |                         ^
					440 | directive @MUTATION on MUTATION
				""",
				"""
					Directive '@MUTATION' is not valid on INPUT_FIELD_DEFINITION but only on MUTATION.

					<document>:256:2
					255 | @INTERFACE
					256 | @MUTATION
					    |  ^
					257 | @OBJECT

					<document>:440:24
					439 | directive @INTERFACE on INTERFACE
					440 | directive @MUTATION on MUTATION
					    |                        ^
					441 | directive @OBJECT on OBJECT
				""",
				"""
					Directive '@OBJECT' is not valid on INPUT_FIELD_DEFINITION but only on OBJECT.

					<document>:257:2
					256 | @MUTATION
					257 | @OBJECT
					    |  ^
					258 | @QUERY

					<document>:441:22
					440 | directive @MUTATION on MUTATION
					441 | directive @OBJECT on OBJECT
					    |                      ^
					442 | directive @QUERY on QUERY
				""",
				"""
					Directive '@QUERY' is not valid on INPUT_FIELD_DEFINITION but only on QUERY.

					<document>:258:2
					257 | @OBJECT
					258 | @QUERY
					    |  ^
					259 | @SCALAR

					<document>:442:21
					441 | directive @OBJECT on OBJECT
					442 | directive @QUERY on QUERY
					    |                     ^
					443 | directive @SCALAR on SCALAR
				""",
				"""
					Directive '@SCALAR' is not valid on INPUT_FIELD_DEFINITION but only on SCALAR.

					<document>:259:2
					258 | @QUERY
					259 | @SCALAR
					    |  ^
					260 | @SCHEMA

					<document>:443:22
					442 | directive @QUERY on QUERY
					443 | directive @SCALAR on SCALAR
					    |                      ^
					444 | directive @SCHEMA on SCHEMA
				""",
				"""
					Directive '@SCHEMA' is not valid on INPUT_FIELD_DEFINITION but only on SCHEMA.

					<document>:260:2
					259 | @SCALAR
					260 | @SCHEMA
					    |  ^
					261 | @SUBSCRIPTION

					<document>:444:22
					443 | directive @SCALAR on SCALAR
					444 | directive @SCHEMA on SCHEMA
					    |                      ^
					445 | directive @SUBSCRIPTION on SUBSCRIPTION
				""",
				"""
					Directive '@SUBSCRIPTION' is not valid on INPUT_FIELD_DEFINITION but only on SUBSCRIPTION.

					<document>:261:2
					260 | @SCHEMA
					261 | @SUBSCRIPTION
					    |  ^
					262 | @UNION

					<document>:445:28
					444 | directive @SCHEMA on SCHEMA
					445 | directive @SUBSCRIPTION on SUBSCRIPTION
					    |                            ^
					446 | directive @UNION on UNION
				""",
				"""
					Directive '@UNION' is not valid on INPUT_FIELD_DEFINITION but only on UNION.

					<document>:262:2
					261 | @SUBSCRIPTION
					262 | @UNION
					    |  ^
					263 | @VARIABLE_DEFINITION

					<document>:446:21
					445 | directive @SUBSCRIPTION on SUBSCRIPTION
					446 | directive @UNION on UNION
					    |                     ^
					447 | directive @VARIABLE_DEFINITION on VARIABLE_DEFINITION
				""",
				"""
					Directive '@VARIABLE_DEFINITION' is not valid on INPUT_FIELD_DEFINITION but only on VARIABLE_DEFINITION.

					<document>:263:2
					262 | @UNION
					263 | @VARIABLE_DEFINITION
					    |  ^
					264 | }

					<document>:447:35
					446 | directive @UNION on UNION
					447 | directive @VARIABLE_DEFINITION on VARIABLE_DEFINITION
					    |                                   ^
				""",
				"""
					Directive '@ARGUMENT_DEFINITION' is not valid on INPUT_OBJECT but only on ARGUMENT_DEFINITION.

					<document>:224:2
					223 | input Input
					224 | @ARGUMENT_DEFINITION
					    |  ^
					225 | @ENUM

					<document>:429:35
					428 | 
					429 | directive @ARGUMENT_DEFINITION on ARGUMENT_DEFINITION
					    |                                   ^
					430 | directive @ENUM on ENUM
				""",
				"""
					Directive '@ENUM' is not valid on INPUT_OBJECT but only on ENUM.

					<document>:225:2
					224 | @ARGUMENT_DEFINITION
					225 | @ENUM
					    |  ^
					226 | @ENUM_VALUE

					<document>:430:20
					429 | directive @ARGUMENT_DEFINITION on ARGUMENT_DEFINITION
					430 | directive @ENUM on ENUM
					    |                    ^
					431 | directive @ENUM_VALUE on ENUM_VALUE
				""",
				"""
					Directive '@ENUM_VALUE' is not valid on INPUT_OBJECT but only on ENUM_VALUE.

					<document>:226:2
					225 | @ENUM
					226 | @ENUM_VALUE
					    |  ^
					227 | @FIELD

					<document>:431:26
					430 | directive @ENUM on ENUM
					431 | directive @ENUM_VALUE on ENUM_VALUE
					    |                          ^
					432 | directive @FIELD on FIELD
				""",
				"""
					Directive '@FIELD' is not valid on INPUT_OBJECT but only on FIELD.

					<document>:227:2
					226 | @ENUM_VALUE
					227 | @FIELD
					    |  ^
					228 | @FIELD_DEFINITION

					<document>:432:21
					431 | directive @ENUM_VALUE on ENUM_VALUE
					432 | directive @FIELD on FIELD
					    |                     ^
					433 | directive @FIELD_DEFINITION on FIELD_DEFINITION
				""",
				"""
					Directive '@FIELD_DEFINITION' is not valid on INPUT_OBJECT but only on FIELD_DEFINITION.

					<document>:228:2
					227 | @FIELD
					228 | @FIELD_DEFINITION
					    |  ^
					229 | @FRAGMENT_DEFINITION

					<document>:433:32
					432 | directive @FIELD on FIELD
					433 | directive @FIELD_DEFINITION on FIELD_DEFINITION
					    |                                ^
					434 | directive @FRAGMENT_DEFINITION on FRAGMENT_DEFINITION
				""",
				"""
					Directive '@FRAGMENT_DEFINITION' is not valid on INPUT_OBJECT but only on FRAGMENT_DEFINITION.

					<document>:229:2
					228 | @FIELD_DEFINITION
					229 | @FRAGMENT_DEFINITION
					    |  ^
					230 | @FRAGMENT_SPREAD

					<document>:434:35
					433 | directive @FIELD_DEFINITION on FIELD_DEFINITION
					434 | directive @FRAGMENT_DEFINITION on FRAGMENT_DEFINITION
					    |                                   ^
					435 | directive @FRAGMENT_SPREAD on FRAGMENT_SPREAD
				""",
				"""
					Directive '@FRAGMENT_SPREAD' is not valid on INPUT_OBJECT but only on FRAGMENT_SPREAD.

					<document>:230:2
					229 | @FRAGMENT_DEFINITION
					230 | @FRAGMENT_SPREAD
					    |  ^
					231 | @INLINE_FRAGMENT

					<document>:435:31
					434 | directive @FRAGMENT_DEFINITION on FRAGMENT_DEFINITION
					435 | directive @FRAGMENT_SPREAD on FRAGMENT_SPREAD
					    |                               ^
					436 | directive @INLINE_FRAGMENT on INLINE_FRAGMENT
				""",
				"""
					Directive '@INLINE_FRAGMENT' is not valid on INPUT_OBJECT but only on INLINE_FRAGMENT.

					<document>:231:2
					230 | @FRAGMENT_SPREAD
					231 | @INLINE_FRAGMENT
					    |  ^
					232 | @INPUT_FIELD_DEFINITION

					<document>:436:31
					435 | directive @FRAGMENT_SPREAD on FRAGMENT_SPREAD
					436 | directive @INLINE_FRAGMENT on INLINE_FRAGMENT
					    |                               ^
					437 | directive @INPUT_FIELD_DEFINITION on INPUT_FIELD_DEFINITION
				""",
				"""
					Directive '@INPUT_FIELD_DEFINITION' is not valid on INPUT_OBJECT but only on INPUT_FIELD_DEFINITION.

					<document>:232:2
					231 | @INLINE_FRAGMENT
					232 | @INPUT_FIELD_DEFINITION
					    |  ^
					233 | @INPUT_OBJECT

					<document>:437:38
					436 | directive @INLINE_FRAGMENT on INLINE_FRAGMENT
					437 | directive @INPUT_FIELD_DEFINITION on INPUT_FIELD_DEFINITION
					    |                                      ^
					438 | directive @INPUT_OBJECT on INPUT_OBJECT
				""",
				"""
					Directive '@INTERFACE' is not valid on INPUT_OBJECT but only on INTERFACE.

					<document>:234:2
					233 | @INPUT_OBJECT
					234 | @INTERFACE
					    |  ^
					235 | @MUTATION

					<document>:439:25
					438 | directive @INPUT_OBJECT on INPUT_OBJECT
					439 | directive @INTERFACE on INTERFACE
					    |                         ^
					440 | directive @MUTATION on MUTATION
				""",
				"""
					Directive '@MUTATION' is not valid on INPUT_OBJECT but only on MUTATION.

					<document>:235:2
					234 | @INTERFACE
					235 | @MUTATION
					    |  ^
					236 | @OBJECT

					<document>:440:24
					439 | directive @INTERFACE on INTERFACE
					440 | directive @MUTATION on MUTATION
					    |                        ^
					441 | directive @OBJECT on OBJECT
				""",
				"""
					Directive '@OBJECT' is not valid on INPUT_OBJECT but only on OBJECT.

					<document>:236:2
					235 | @MUTATION
					236 | @OBJECT
					    |  ^
					237 | @QUERY

					<document>:441:22
					440 | directive @MUTATION on MUTATION
					441 | directive @OBJECT on OBJECT
					    |                      ^
					442 | directive @QUERY on QUERY
				""",
				"""
					Directive '@QUERY' is not valid on INPUT_OBJECT but only on QUERY.

					<document>:237:2
					236 | @OBJECT
					237 | @QUERY
					    |  ^
					238 | @SCALAR

					<document>:442:21
					441 | directive @OBJECT on OBJECT
					442 | directive @QUERY on QUERY
					    |                     ^
					443 | directive @SCALAR on SCALAR
				""",
				"""
					Directive '@SCALAR' is not valid on INPUT_OBJECT but only on SCALAR.

					<document>:238:2
					237 | @QUERY
					238 | @SCALAR
					    |  ^
					239 | @SCHEMA

					<document>:443:22
					442 | directive @QUERY on QUERY
					443 | directive @SCALAR on SCALAR
					    |                      ^
					444 | directive @SCHEMA on SCHEMA
				""",
				"""
					Directive '@SCHEMA' is not valid on INPUT_OBJECT but only on SCHEMA.

					<document>:239:2
					238 | @SCALAR
					239 | @SCHEMA
					    |  ^
					240 | @SUBSCRIPTION

					<document>:444:22
					443 | directive @SCALAR on SCALAR
					444 | directive @SCHEMA on SCHEMA
					    |                      ^
					445 | directive @SUBSCRIPTION on SUBSCRIPTION
				""",
				"""
					Directive '@SUBSCRIPTION' is not valid on INPUT_OBJECT but only on SUBSCRIPTION.

					<document>:240:2
					239 | @SCHEMA
					240 | @SUBSCRIPTION
					    |  ^
					241 | @UNION

					<document>:445:28
					444 | directive @SCHEMA on SCHEMA
					445 | directive @SUBSCRIPTION on SUBSCRIPTION
					    |                            ^
					446 | directive @UNION on UNION
				""",
				"""
					Directive '@UNION' is not valid on INPUT_OBJECT but only on UNION.

					<document>:241:2
					240 | @SUBSCRIPTION
					241 | @UNION
					    |  ^
					242 | @VARIABLE_DEFINITION

					<document>:446:21
					445 | directive @SUBSCRIPTION on SUBSCRIPTION
					446 | directive @UNION on UNION
					    |                     ^
					447 | directive @VARIABLE_DEFINITION on VARIABLE_DEFINITION
				""",
				"""
					Directive '@VARIABLE_DEFINITION' is not valid on INPUT_OBJECT but only on VARIABLE_DEFINITION.

					<document>:242:2
					241 | @UNION
					242 | @VARIABLE_DEFINITION
					    |  ^
					243 | {

					<document>:447:35
					446 | directive @UNION on UNION
					447 | directive @VARIABLE_DEFINITION on VARIABLE_DEFINITION
					    |                                   ^
				""",
				"""
					Directive '@ARGUMENT_DEFINITION' is not valid on INTERFACE but only on ARGUMENT_DEFINITION.

					<document>:267:2
					266 | interface Interface
					267 | @ARGUMENT_DEFINITION
					    |  ^
					268 | @ENUM

					<document>:429:35
					428 | 
					429 | directive @ARGUMENT_DEFINITION on ARGUMENT_DEFINITION
					    |                                   ^
					430 | directive @ENUM on ENUM
				""",
				"""
					Directive '@ENUM' is not valid on INTERFACE but only on ENUM.

					<document>:268:2
					267 | @ARGUMENT_DEFINITION
					268 | @ENUM
					    |  ^
					269 | @ENUM_VALUE

					<document>:430:20
					429 | directive @ARGUMENT_DEFINITION on ARGUMENT_DEFINITION
					430 | directive @ENUM on ENUM
					    |                    ^
					431 | directive @ENUM_VALUE on ENUM_VALUE
				""",
				"""
					Directive '@ENUM_VALUE' is not valid on INTERFACE but only on ENUM_VALUE.

					<document>:269:2
					268 | @ENUM
					269 | @ENUM_VALUE
					    |  ^
					270 | @FIELD

					<document>:431:26
					430 | directive @ENUM on ENUM
					431 | directive @ENUM_VALUE on ENUM_VALUE
					    |                          ^
					432 | directive @FIELD on FIELD
				""",
				"""
					Directive '@FIELD' is not valid on INTERFACE but only on FIELD.

					<document>:270:2
					269 | @ENUM_VALUE
					270 | @FIELD
					    |  ^
					271 | @FIELD_DEFINITION

					<document>:432:21
					431 | directive @ENUM_VALUE on ENUM_VALUE
					432 | directive @FIELD on FIELD
					    |                     ^
					433 | directive @FIELD_DEFINITION on FIELD_DEFINITION
				""",
				"""
					Directive '@FIELD_DEFINITION' is not valid on INTERFACE but only on FIELD_DEFINITION.

					<document>:271:2
					270 | @FIELD
					271 | @FIELD_DEFINITION
					    |  ^
					272 | @FRAGMENT_DEFINITION

					<document>:433:32
					432 | directive @FIELD on FIELD
					433 | directive @FIELD_DEFINITION on FIELD_DEFINITION
					    |                                ^
					434 | directive @FRAGMENT_DEFINITION on FRAGMENT_DEFINITION
				""",
				"""
					Directive '@FRAGMENT_DEFINITION' is not valid on INTERFACE but only on FRAGMENT_DEFINITION.

					<document>:272:2
					271 | @FIELD_DEFINITION
					272 | @FRAGMENT_DEFINITION
					    |  ^
					273 | @FRAGMENT_SPREAD

					<document>:434:35
					433 | directive @FIELD_DEFINITION on FIELD_DEFINITION
					434 | directive @FRAGMENT_DEFINITION on FRAGMENT_DEFINITION
					    |                                   ^
					435 | directive @FRAGMENT_SPREAD on FRAGMENT_SPREAD
				""",
				"""
					Directive '@FRAGMENT_SPREAD' is not valid on INTERFACE but only on FRAGMENT_SPREAD.

					<document>:273:2
					272 | @FRAGMENT_DEFINITION
					273 | @FRAGMENT_SPREAD
					    |  ^
					274 | @INLINE_FRAGMENT

					<document>:435:31
					434 | directive @FRAGMENT_DEFINITION on FRAGMENT_DEFINITION
					435 | directive @FRAGMENT_SPREAD on FRAGMENT_SPREAD
					    |                               ^
					436 | directive @INLINE_FRAGMENT on INLINE_FRAGMENT
				""",
				"""
					Directive '@INLINE_FRAGMENT' is not valid on INTERFACE but only on INLINE_FRAGMENT.

					<document>:274:2
					273 | @FRAGMENT_SPREAD
					274 | @INLINE_FRAGMENT
					    |  ^
					275 | @INPUT_FIELD_DEFINITION

					<document>:436:31
					435 | directive @FRAGMENT_SPREAD on FRAGMENT_SPREAD
					436 | directive @INLINE_FRAGMENT on INLINE_FRAGMENT
					    |                               ^
					437 | directive @INPUT_FIELD_DEFINITION on INPUT_FIELD_DEFINITION
				""",
				"""
					Directive '@INPUT_FIELD_DEFINITION' is not valid on INTERFACE but only on INPUT_FIELD_DEFINITION.

					<document>:275:2
					274 | @INLINE_FRAGMENT
					275 | @INPUT_FIELD_DEFINITION
					    |  ^
					276 | @INPUT_OBJECT

					<document>:437:38
					436 | directive @INLINE_FRAGMENT on INLINE_FRAGMENT
					437 | directive @INPUT_FIELD_DEFINITION on INPUT_FIELD_DEFINITION
					    |                                      ^
					438 | directive @INPUT_OBJECT on INPUT_OBJECT
				""",
				"""
					Directive '@INPUT_OBJECT' is not valid on INTERFACE but only on INPUT_OBJECT.

					<document>:276:2
					275 | @INPUT_FIELD_DEFINITION
					276 | @INPUT_OBJECT
					    |  ^
					277 | @INTERFACE

					<document>:438:28
					437 | directive @INPUT_FIELD_DEFINITION on INPUT_FIELD_DEFINITION
					438 | directive @INPUT_OBJECT on INPUT_OBJECT
					    |                            ^
					439 | directive @INTERFACE on INTERFACE
				""",
				"""
					Directive '@MUTATION' is not valid on INTERFACE but only on MUTATION.

					<document>:278:2
					277 | @INTERFACE
					278 | @MUTATION
					    |  ^
					279 | @OBJECT

					<document>:440:24
					439 | directive @INTERFACE on INTERFACE
					440 | directive @MUTATION on MUTATION
					    |                        ^
					441 | directive @OBJECT on OBJECT
				""",
				"""
					Directive '@OBJECT' is not valid on INTERFACE but only on OBJECT.

					<document>:279:2
					278 | @MUTATION
					279 | @OBJECT
					    |  ^
					280 | @QUERY

					<document>:441:22
					440 | directive @MUTATION on MUTATION
					441 | directive @OBJECT on OBJECT
					    |                      ^
					442 | directive @QUERY on QUERY
				""",
				"""
					Directive '@QUERY' is not valid on INTERFACE but only on QUERY.

					<document>:280:2
					279 | @OBJECT
					280 | @QUERY
					    |  ^
					281 | @SCALAR

					<document>:442:21
					441 | directive @OBJECT on OBJECT
					442 | directive @QUERY on QUERY
					    |                     ^
					443 | directive @SCALAR on SCALAR
				""",
				"""
					Directive '@SCALAR' is not valid on INTERFACE but only on SCALAR.

					<document>:281:2
					280 | @QUERY
					281 | @SCALAR
					    |  ^
					282 | @SCHEMA

					<document>:443:22
					442 | directive @QUERY on QUERY
					443 | directive @SCALAR on SCALAR
					    |                      ^
					444 | directive @SCHEMA on SCHEMA
				""",
				"""
					Directive '@SCHEMA' is not valid on INTERFACE but only on SCHEMA.

					<document>:282:2
					281 | @SCALAR
					282 | @SCHEMA
					    |  ^
					283 | @SUBSCRIPTION

					<document>:444:22
					443 | directive @SCALAR on SCALAR
					444 | directive @SCHEMA on SCHEMA
					    |                      ^
					445 | directive @SUBSCRIPTION on SUBSCRIPTION
				""",
				"""
					Directive '@SUBSCRIPTION' is not valid on INTERFACE but only on SUBSCRIPTION.

					<document>:283:2
					282 | @SCHEMA
					283 | @SUBSCRIPTION
					    |  ^
					284 | @UNION

					<document>:445:28
					444 | directive @SCHEMA on SCHEMA
					445 | directive @SUBSCRIPTION on SUBSCRIPTION
					    |                            ^
					446 | directive @UNION on UNION
				""",
				"""
					Directive '@UNION' is not valid on INTERFACE but only on UNION.

					<document>:284:2
					283 | @SUBSCRIPTION
					284 | @UNION
					    |  ^
					285 | @VARIABLE_DEFINITION

					<document>:446:21
					445 | directive @SUBSCRIPTION on SUBSCRIPTION
					446 | directive @UNION on UNION
					    |                     ^
					447 | directive @VARIABLE_DEFINITION on VARIABLE_DEFINITION
				""",
				"""
					Directive '@VARIABLE_DEFINITION' is not valid on INTERFACE but only on VARIABLE_DEFINITION.

					<document>:285:2
					284 | @UNION
					285 | @VARIABLE_DEFINITION
					    |  ^
					286 | {

					<document>:447:35
					446 | directive @UNION on UNION
					447 | directive @VARIABLE_DEFINITION on VARIABLE_DEFINITION
					    |                                   ^
				""",
				"""
					Directive '@ENUM' is not valid on ARGUMENT_DEFINITION but only on ENUM.

					<document>:318:8
					317 |       @ARGUMENT_DEFINITION
					318 |       @ENUM
					    |        ^
					319 |       @ENUM_VALUE

					<document>:430:20
					429 | directive @ARGUMENT_DEFINITION on ARGUMENT_DEFINITION
					430 | directive @ENUM on ENUM
					    |                    ^
					431 | directive @ENUM_VALUE on ENUM_VALUE
				""",
				"""
					Directive '@ENUM_VALUE' is not valid on ARGUMENT_DEFINITION but only on ENUM_VALUE.

					<document>:319:8
					318 |       @ENUM
					319 |       @ENUM_VALUE
					    |        ^
					320 |       @FIELD

					<document>:431:26
					430 | directive @ENUM on ENUM
					431 | directive @ENUM_VALUE on ENUM_VALUE
					    |                          ^
					432 | directive @FIELD on FIELD
				""",
				"""
					Directive '@FIELD' is not valid on ARGUMENT_DEFINITION but only on FIELD.

					<document>:320:8
					319 |       @ENUM_VALUE
					320 |       @FIELD
					    |        ^
					321 |       @FIELD_DEFINITION

					<document>:432:21
					431 | directive @ENUM_VALUE on ENUM_VALUE
					432 | directive @FIELD on FIELD
					    |                     ^
					433 | directive @FIELD_DEFINITION on FIELD_DEFINITION
				""",
				"""
					Directive '@FIELD_DEFINITION' is not valid on ARGUMENT_DEFINITION but only on FIELD_DEFINITION.

					<document>:321:8
					320 |       @FIELD
					321 |       @FIELD_DEFINITION
					    |        ^
					322 |       @FRAGMENT_DEFINITION

					<document>:433:32
					432 | directive @FIELD on FIELD
					433 | directive @FIELD_DEFINITION on FIELD_DEFINITION
					    |                                ^
					434 | directive @FRAGMENT_DEFINITION on FRAGMENT_DEFINITION
				""",
				"""
					Directive '@FRAGMENT_DEFINITION' is not valid on ARGUMENT_DEFINITION but only on FRAGMENT_DEFINITION.

					<document>:322:8
					321 |       @FIELD_DEFINITION
					322 |       @FRAGMENT_DEFINITION
					    |        ^
					323 |       @FRAGMENT_SPREAD

					<document>:434:35
					433 | directive @FIELD_DEFINITION on FIELD_DEFINITION
					434 | directive @FRAGMENT_DEFINITION on FRAGMENT_DEFINITION
					    |                                   ^
					435 | directive @FRAGMENT_SPREAD on FRAGMENT_SPREAD
				""",
				"""
					Directive '@FRAGMENT_SPREAD' is not valid on ARGUMENT_DEFINITION but only on FRAGMENT_SPREAD.

					<document>:323:8
					322 |       @FRAGMENT_DEFINITION
					323 |       @FRAGMENT_SPREAD
					    |        ^
					324 |       @INLINE_FRAGMENT

					<document>:435:31
					434 | directive @FRAGMENT_DEFINITION on FRAGMENT_DEFINITION
					435 | directive @FRAGMENT_SPREAD on FRAGMENT_SPREAD
					    |                               ^
					436 | directive @INLINE_FRAGMENT on INLINE_FRAGMENT
				""",
				"""
					Directive '@INLINE_FRAGMENT' is not valid on ARGUMENT_DEFINITION but only on INLINE_FRAGMENT.

					<document>:324:8
					323 |       @FRAGMENT_SPREAD
					324 |       @INLINE_FRAGMENT
					    |        ^
					325 |       @INPUT_FIELD_DEFINITION

					<document>:436:31
					435 | directive @FRAGMENT_SPREAD on FRAGMENT_SPREAD
					436 | directive @INLINE_FRAGMENT on INLINE_FRAGMENT
					    |                               ^
					437 | directive @INPUT_FIELD_DEFINITION on INPUT_FIELD_DEFINITION
				""",
				"""
					Directive '@INPUT_FIELD_DEFINITION' is not valid on ARGUMENT_DEFINITION but only on INPUT_FIELD_DEFINITION.

					<document>:325:8
					324 |       @INLINE_FRAGMENT
					325 |       @INPUT_FIELD_DEFINITION
					    |        ^
					326 |       @INPUT_OBJECT

					<document>:437:38
					436 | directive @INLINE_FRAGMENT on INLINE_FRAGMENT
					437 | directive @INPUT_FIELD_DEFINITION on INPUT_FIELD_DEFINITION
					    |                                      ^
					438 | directive @INPUT_OBJECT on INPUT_OBJECT
				""",
				"""
					Directive '@INPUT_OBJECT' is not valid on ARGUMENT_DEFINITION but only on INPUT_OBJECT.

					<document>:326:8
					325 |       @INPUT_FIELD_DEFINITION
					326 |       @INPUT_OBJECT
					    |        ^
					327 |       @INTERFACE

					<document>:438:28
					437 | directive @INPUT_FIELD_DEFINITION on INPUT_FIELD_DEFINITION
					438 | directive @INPUT_OBJECT on INPUT_OBJECT
					    |                            ^
					439 | directive @INTERFACE on INTERFACE
				""",
				"""
					Directive '@INTERFACE' is not valid on ARGUMENT_DEFINITION but only on INTERFACE.

					<document>:327:8
					326 |       @INPUT_OBJECT
					327 |       @INTERFACE
					    |        ^
					328 |       @MUTATION

					<document>:439:25
					438 | directive @INPUT_OBJECT on INPUT_OBJECT
					439 | directive @INTERFACE on INTERFACE
					    |                         ^
					440 | directive @MUTATION on MUTATION
				""",
				"""
					Directive '@MUTATION' is not valid on ARGUMENT_DEFINITION but only on MUTATION.

					<document>:328:8
					327 |       @INTERFACE
					328 |       @MUTATION
					    |        ^
					329 |       @OBJECT

					<document>:440:24
					439 | directive @INTERFACE on INTERFACE
					440 | directive @MUTATION on MUTATION
					    |                        ^
					441 | directive @OBJECT on OBJECT
				""",
				"""
					Directive '@OBJECT' is not valid on ARGUMENT_DEFINITION but only on OBJECT.

					<document>:329:8
					328 |       @MUTATION
					329 |       @OBJECT
					    |        ^
					330 |       @QUERY

					<document>:441:22
					440 | directive @MUTATION on MUTATION
					441 | directive @OBJECT on OBJECT
					    |                      ^
					442 | directive @QUERY on QUERY
				""",
				"""
					Directive '@QUERY' is not valid on ARGUMENT_DEFINITION but only on QUERY.

					<document>:330:8
					329 |       @OBJECT
					330 |       @QUERY
					    |        ^
					331 |       @SCALAR

					<document>:442:21
					441 | directive @OBJECT on OBJECT
					442 | directive @QUERY on QUERY
					    |                     ^
					443 | directive @SCALAR on SCALAR
				""",
				"""
					Directive '@SCALAR' is not valid on ARGUMENT_DEFINITION but only on SCALAR.

					<document>:331:8
					330 |       @QUERY
					331 |       @SCALAR
					    |        ^
					332 |       @SCHEMA

					<document>:443:22
					442 | directive @QUERY on QUERY
					443 | directive @SCALAR on SCALAR
					    |                      ^
					444 | directive @SCHEMA on SCHEMA
				""",
				"""
					Directive '@SCHEMA' is not valid on ARGUMENT_DEFINITION but only on SCHEMA.

					<document>:332:8
					331 |       @SCALAR
					332 |       @SCHEMA
					    |        ^
					333 |       @SUBSCRIPTION

					<document>:444:22
					443 | directive @SCALAR on SCALAR
					444 | directive @SCHEMA on SCHEMA
					    |                      ^
					445 | directive @SUBSCRIPTION on SUBSCRIPTION
				""",
				"""
					Directive '@SUBSCRIPTION' is not valid on ARGUMENT_DEFINITION but only on SUBSCRIPTION.

					<document>:333:8
					332 |       @SCHEMA
					333 |       @SUBSCRIPTION
					    |        ^
					334 |       @UNION

					<document>:445:28
					444 | directive @SCHEMA on SCHEMA
					445 | directive @SUBSCRIPTION on SUBSCRIPTION
					    |                            ^
					446 | directive @UNION on UNION
				""",
				"""
					Directive '@UNION' is not valid on ARGUMENT_DEFINITION but only on UNION.

					<document>:334:8
					333 |       @SUBSCRIPTION
					334 |       @UNION
					    |        ^
					335 |       @VARIABLE_DEFINITION

					<document>:446:21
					445 | directive @SUBSCRIPTION on SUBSCRIPTION
					446 | directive @UNION on UNION
					    |                     ^
					447 | directive @VARIABLE_DEFINITION on VARIABLE_DEFINITION
				""",
				"""
					Directive '@VARIABLE_DEFINITION' is not valid on ARGUMENT_DEFINITION but only on VARIABLE_DEFINITION.

					<document>:335:8
					334 |       @UNION
					335 |       @VARIABLE_DEFINITION
					    |        ^
					336 |    ): String

					<document>:447:35
					446 | directive @UNION on UNION
					447 | directive @VARIABLE_DEFINITION on VARIABLE_DEFINITION
					    |                                   ^
				""",
				"""
					Directive '@ARGUMENT_DEFINITION' is not valid on FIELD_DEFINITION but only on ARGUMENT_DEFINITION.

					<document>:337:5
					336 |    ): String
					337 |    @ARGUMENT_DEFINITION
					    |     ^
					338 |    @ENUM

					<document>:429:35
					428 | 
					429 | directive @ARGUMENT_DEFINITION on ARGUMENT_DEFINITION
					    |                                   ^
					430 | directive @ENUM on ENUM
				""",
				"""
					Directive '@ENUM' is not valid on FIELD_DEFINITION but only on ENUM.

					<document>:338:5
					337 |    @ARGUMENT_DEFINITION
					338 |    @ENUM
					    |     ^
					339 |    @ENUM_VALUE

					<document>:430:20
					429 | directive @ARGUMENT_DEFINITION on ARGUMENT_DEFINITION
					430 | directive @ENUM on ENUM
					    |                    ^
					431 | directive @ENUM_VALUE on ENUM_VALUE
				""",
				"""
					Directive '@ENUM_VALUE' is not valid on FIELD_DEFINITION but only on ENUM_VALUE.

					<document>:339:5
					338 |    @ENUM
					339 |    @ENUM_VALUE
					    |     ^
					340 |    @FIELD

					<document>:431:26
					430 | directive @ENUM on ENUM
					431 | directive @ENUM_VALUE on ENUM_VALUE
					    |                          ^
					432 | directive @FIELD on FIELD
				""",
				"""
					Directive '@FIELD' is not valid on FIELD_DEFINITION but only on FIELD.

					<document>:340:5
					339 |    @ENUM_VALUE
					340 |    @FIELD
					    |     ^
					341 |    @FIELD_DEFINITION

					<document>:432:21
					431 | directive @ENUM_VALUE on ENUM_VALUE
					432 | directive @FIELD on FIELD
					    |                     ^
					433 | directive @FIELD_DEFINITION on FIELD_DEFINITION
				""",
				"""
					Directive '@FRAGMENT_DEFINITION' is not valid on FIELD_DEFINITION but only on FRAGMENT_DEFINITION.

					<document>:342:5
					341 |    @FIELD_DEFINITION
					342 |    @FRAGMENT_DEFINITION
					    |     ^
					343 |    @FRAGMENT_SPREAD

					<document>:434:35
					433 | directive @FIELD_DEFINITION on FIELD_DEFINITION
					434 | directive @FRAGMENT_DEFINITION on FRAGMENT_DEFINITION
					    |                                   ^
					435 | directive @FRAGMENT_SPREAD on FRAGMENT_SPREAD
				""",
				"""
					Directive '@FRAGMENT_SPREAD' is not valid on FIELD_DEFINITION but only on FRAGMENT_SPREAD.

					<document>:343:5
					342 |    @FRAGMENT_DEFINITION
					343 |    @FRAGMENT_SPREAD
					    |     ^
					344 |    @INLINE_FRAGMENT

					<document>:435:31
					434 | directive @FRAGMENT_DEFINITION on FRAGMENT_DEFINITION
					435 | directive @FRAGMENT_SPREAD on FRAGMENT_SPREAD
					    |                               ^
					436 | directive @INLINE_FRAGMENT on INLINE_FRAGMENT
				""",
				"""
					Directive '@INLINE_FRAGMENT' is not valid on FIELD_DEFINITION but only on INLINE_FRAGMENT.

					<document>:344:5
					343 |    @FRAGMENT_SPREAD
					344 |    @INLINE_FRAGMENT
					    |     ^
					345 |    @INPUT_FIELD_DEFINITION

					<document>:436:31
					435 | directive @FRAGMENT_SPREAD on FRAGMENT_SPREAD
					436 | directive @INLINE_FRAGMENT on INLINE_FRAGMENT
					    |                               ^
					437 | directive @INPUT_FIELD_DEFINITION on INPUT_FIELD_DEFINITION
				""",
				"""
					Directive '@INPUT_FIELD_DEFINITION' is not valid on FIELD_DEFINITION but only on INPUT_FIELD_DEFINITION.

					<document>:345:5
					344 |    @INLINE_FRAGMENT
					345 |    @INPUT_FIELD_DEFINITION
					    |     ^
					346 |    @INPUT_OBJECT

					<document>:437:38
					436 | directive @INLINE_FRAGMENT on INLINE_FRAGMENT
					437 | directive @INPUT_FIELD_DEFINITION on INPUT_FIELD_DEFINITION
					    |                                      ^
					438 | directive @INPUT_OBJECT on INPUT_OBJECT
				""",
				"""
					Directive '@INPUT_OBJECT' is not valid on FIELD_DEFINITION but only on INPUT_OBJECT.

					<document>:346:5
					345 |    @INPUT_FIELD_DEFINITION
					346 |    @INPUT_OBJECT
					    |     ^
					347 |    @INTERFACE

					<document>:438:28
					437 | directive @INPUT_FIELD_DEFINITION on INPUT_FIELD_DEFINITION
					438 | directive @INPUT_OBJECT on INPUT_OBJECT
					    |                            ^
					439 | directive @INTERFACE on INTERFACE
				""",
				"""
					Directive '@INTERFACE' is not valid on FIELD_DEFINITION but only on INTERFACE.

					<document>:347:5
					346 |    @INPUT_OBJECT
					347 |    @INTERFACE
					    |     ^
					348 |    @MUTATION

					<document>:439:25
					438 | directive @INPUT_OBJECT on INPUT_OBJECT
					439 | directive @INTERFACE on INTERFACE
					    |                         ^
					440 | directive @MUTATION on MUTATION
				""",
				"""
					Directive '@MUTATION' is not valid on FIELD_DEFINITION but only on MUTATION.

					<document>:348:5
					347 |    @INTERFACE
					348 |    @MUTATION
					    |     ^
					349 |    @OBJECT

					<document>:440:24
					439 | directive @INTERFACE on INTERFACE
					440 | directive @MUTATION on MUTATION
					    |                        ^
					441 | directive @OBJECT on OBJECT
				""",
				"""
					Directive '@OBJECT' is not valid on FIELD_DEFINITION but only on OBJECT.

					<document>:349:5
					348 |    @MUTATION
					349 |    @OBJECT
					    |     ^
					350 |    @QUERY

					<document>:441:22
					440 | directive @MUTATION on MUTATION
					441 | directive @OBJECT on OBJECT
					    |                      ^
					442 | directive @QUERY on QUERY
				""",
				"""
					Directive '@QUERY' is not valid on FIELD_DEFINITION but only on QUERY.

					<document>:350:5
					349 |    @OBJECT
					350 |    @QUERY
					    |     ^
					351 |    @SCALAR

					<document>:442:21
					441 | directive @OBJECT on OBJECT
					442 | directive @QUERY on QUERY
					    |                     ^
					443 | directive @SCALAR on SCALAR
				""",
				"""
					Directive '@SCALAR' is not valid on FIELD_DEFINITION but only on SCALAR.

					<document>:351:5
					350 |    @QUERY
					351 |    @SCALAR
					    |     ^
					352 |    @SCHEMA

					<document>:443:22
					442 | directive @QUERY on QUERY
					443 | directive @SCALAR on SCALAR
					    |                      ^
					444 | directive @SCHEMA on SCHEMA
				""",
				"""
					Directive '@SCHEMA' is not valid on FIELD_DEFINITION but only on SCHEMA.

					<document>:352:5
					351 |    @SCALAR
					352 |    @SCHEMA
					    |     ^
					353 |    @SUBSCRIPTION

					<document>:444:22
					443 | directive @SCALAR on SCALAR
					444 | directive @SCHEMA on SCHEMA
					    |                      ^
					445 | directive @SUBSCRIPTION on SUBSCRIPTION
				""",
				"""
					Directive '@SUBSCRIPTION' is not valid on FIELD_DEFINITION but only on SUBSCRIPTION.

					<document>:353:5
					352 |    @SCHEMA
					353 |    @SUBSCRIPTION
					    |     ^
					354 |    @UNION

					<document>:445:28
					444 | directive @SCHEMA on SCHEMA
					445 | directive @SUBSCRIPTION on SUBSCRIPTION
					    |                            ^
					446 | directive @UNION on UNION
				""",
				"""
					Directive '@UNION' is not valid on FIELD_DEFINITION but only on UNION.

					<document>:354:5
					353 |    @SUBSCRIPTION
					354 |    @UNION
					    |     ^
					355 |    @VARIABLE_DEFINITION

					<document>:446:21
					445 | directive @SUBSCRIPTION on SUBSCRIPTION
					446 | directive @UNION on UNION
					    |                     ^
					447 | directive @VARIABLE_DEFINITION on VARIABLE_DEFINITION
				""",
				"""
					Directive '@VARIABLE_DEFINITION' is not valid on FIELD_DEFINITION but only on VARIABLE_DEFINITION.

					<document>:355:5
					354 |    @UNION
					355 |    @VARIABLE_DEFINITION
					    |     ^
					356 | }

					<document>:447:35
					446 | directive @UNION on UNION
					447 | directive @VARIABLE_DEFINITION on VARIABLE_DEFINITION
					    |                                   ^
				""",
				"""
					Directive '@ARGUMENT_DEFINITION' is not valid on OBJECT but only on ARGUMENT_DEFINITION.

					<document>:295:2
					294 | type Query implements Interface
					295 | @ARGUMENT_DEFINITION
					    |  ^
					296 | @ENUM

					<document>:429:35
					428 | 
					429 | directive @ARGUMENT_DEFINITION on ARGUMENT_DEFINITION
					    |                                   ^
					430 | directive @ENUM on ENUM
				""",
				"""
					Directive '@ENUM' is not valid on OBJECT but only on ENUM.

					<document>:296:2
					295 | @ARGUMENT_DEFINITION
					296 | @ENUM
					    |  ^
					297 | @ENUM_VALUE

					<document>:430:20
					429 | directive @ARGUMENT_DEFINITION on ARGUMENT_DEFINITION
					430 | directive @ENUM on ENUM
					    |                    ^
					431 | directive @ENUM_VALUE on ENUM_VALUE
				""",
				"""
					Directive '@ENUM_VALUE' is not valid on OBJECT but only on ENUM_VALUE.

					<document>:297:2
					296 | @ENUM
					297 | @ENUM_VALUE
					    |  ^
					298 | @FIELD

					<document>:431:26
					430 | directive @ENUM on ENUM
					431 | directive @ENUM_VALUE on ENUM_VALUE
					    |                          ^
					432 | directive @FIELD on FIELD
				""",
				"""
					Directive '@FIELD' is not valid on OBJECT but only on FIELD.

					<document>:298:2
					297 | @ENUM_VALUE
					298 | @FIELD
					    |  ^
					299 | @FIELD_DEFINITION

					<document>:432:21
					431 | directive @ENUM_VALUE on ENUM_VALUE
					432 | directive @FIELD on FIELD
					    |                     ^
					433 | directive @FIELD_DEFINITION on FIELD_DEFINITION
				""",
				"""
					Directive '@FIELD_DEFINITION' is not valid on OBJECT but only on FIELD_DEFINITION.

					<document>:299:2
					298 | @FIELD
					299 | @FIELD_DEFINITION
					    |  ^
					300 | @FRAGMENT_DEFINITION

					<document>:433:32
					432 | directive @FIELD on FIELD
					433 | directive @FIELD_DEFINITION on FIELD_DEFINITION
					    |                                ^
					434 | directive @FRAGMENT_DEFINITION on FRAGMENT_DEFINITION
				""",
				"""
					Directive '@FRAGMENT_DEFINITION' is not valid on OBJECT but only on FRAGMENT_DEFINITION.

					<document>:300:2
					299 | @FIELD_DEFINITION
					300 | @FRAGMENT_DEFINITION
					    |  ^
					301 | @FRAGMENT_SPREAD

					<document>:434:35
					433 | directive @FIELD_DEFINITION on FIELD_DEFINITION
					434 | directive @FRAGMENT_DEFINITION on FRAGMENT_DEFINITION
					    |                                   ^
					435 | directive @FRAGMENT_SPREAD on FRAGMENT_SPREAD
				""",
				"""
					Directive '@FRAGMENT_SPREAD' is not valid on OBJECT but only on FRAGMENT_SPREAD.

					<document>:301:2
					300 | @FRAGMENT_DEFINITION
					301 | @FRAGMENT_SPREAD
					    |  ^
					302 | @INLINE_FRAGMENT

					<document>:435:31
					434 | directive @FRAGMENT_DEFINITION on FRAGMENT_DEFINITION
					435 | directive @FRAGMENT_SPREAD on FRAGMENT_SPREAD
					    |                               ^
					436 | directive @INLINE_FRAGMENT on INLINE_FRAGMENT
				""",
				"""
					Directive '@INLINE_FRAGMENT' is not valid on OBJECT but only on INLINE_FRAGMENT.

					<document>:302:2
					301 | @FRAGMENT_SPREAD
					302 | @INLINE_FRAGMENT
					    |  ^
					303 | @INPUT_FIELD_DEFINITION

					<document>:436:31
					435 | directive @FRAGMENT_SPREAD on FRAGMENT_SPREAD
					436 | directive @INLINE_FRAGMENT on INLINE_FRAGMENT
					    |                               ^
					437 | directive @INPUT_FIELD_DEFINITION on INPUT_FIELD_DEFINITION
				""",
				"""
					Directive '@INPUT_FIELD_DEFINITION' is not valid on OBJECT but only on INPUT_FIELD_DEFINITION.

					<document>:303:2
					302 | @INLINE_FRAGMENT
					303 | @INPUT_FIELD_DEFINITION
					    |  ^
					304 | @INPUT_OBJECT

					<document>:437:38
					436 | directive @INLINE_FRAGMENT on INLINE_FRAGMENT
					437 | directive @INPUT_FIELD_DEFINITION on INPUT_FIELD_DEFINITION
					    |                                      ^
					438 | directive @INPUT_OBJECT on INPUT_OBJECT
				""",
				"""
					Directive '@INPUT_OBJECT' is not valid on OBJECT but only on INPUT_OBJECT.

					<document>:304:2
					303 | @INPUT_FIELD_DEFINITION
					304 | @INPUT_OBJECT
					    |  ^
					305 | @INTERFACE

					<document>:438:28
					437 | directive @INPUT_FIELD_DEFINITION on INPUT_FIELD_DEFINITION
					438 | directive @INPUT_OBJECT on INPUT_OBJECT
					    |                            ^
					439 | directive @INTERFACE on INTERFACE
				""",
				"""
					Directive '@INTERFACE' is not valid on OBJECT but only on INTERFACE.

					<document>:305:2
					304 | @INPUT_OBJECT
					305 | @INTERFACE
					    |  ^
					306 | @MUTATION

					<document>:439:25
					438 | directive @INPUT_OBJECT on INPUT_OBJECT
					439 | directive @INTERFACE on INTERFACE
					    |                         ^
					440 | directive @MUTATION on MUTATION
				""",
				"""
					Directive '@MUTATION' is not valid on OBJECT but only on MUTATION.

					<document>:306:2
					305 | @INTERFACE
					306 | @MUTATION
					    |  ^
					307 | @OBJECT

					<document>:440:24
					439 | directive @INTERFACE on INTERFACE
					440 | directive @MUTATION on MUTATION
					    |                        ^
					441 | directive @OBJECT on OBJECT
				""",
				"""
					Directive '@QUERY' is not valid on OBJECT but only on QUERY.

					<document>:308:2
					307 | @OBJECT
					308 | @QUERY
					    |  ^
					309 | @SCALAR

					<document>:442:21
					441 | directive @OBJECT on OBJECT
					442 | directive @QUERY on QUERY
					    |                     ^
					443 | directive @SCALAR on SCALAR
				""",
				"""
					Directive '@SCALAR' is not valid on OBJECT but only on SCALAR.

					<document>:309:2
					308 | @QUERY
					309 | @SCALAR
					    |  ^
					310 | @SCHEMA

					<document>:443:22
					442 | directive @QUERY on QUERY
					443 | directive @SCALAR on SCALAR
					    |                      ^
					444 | directive @SCHEMA on SCHEMA
				""",
				"""
					Directive '@SCHEMA' is not valid on OBJECT but only on SCHEMA.

					<document>:310:2
					309 | @SCALAR
					310 | @SCHEMA
					    |  ^
					311 | @SUBSCRIPTION

					<document>:444:22
					443 | directive @SCALAR on SCALAR
					444 | directive @SCHEMA on SCHEMA
					    |                      ^
					445 | directive @SUBSCRIPTION on SUBSCRIPTION
				""",
				"""
					Directive '@SUBSCRIPTION' is not valid on OBJECT but only on SUBSCRIPTION.

					<document>:311:2
					310 | @SCHEMA
					311 | @SUBSCRIPTION
					    |  ^
					312 | @UNION

					<document>:445:28
					444 | directive @SCHEMA on SCHEMA
					445 | directive @SUBSCRIPTION on SUBSCRIPTION
					    |                            ^
					446 | directive @UNION on UNION
				""",
				"""
					Directive '@UNION' is not valid on OBJECT but only on UNION.

					<document>:312:2
					311 | @SUBSCRIPTION
					312 | @UNION
					    |  ^
					313 | @VARIABLE_DEFINITION

					<document>:446:21
					445 | directive @SUBSCRIPTION on SUBSCRIPTION
					446 | directive @UNION on UNION
					    |                     ^
					447 | directive @VARIABLE_DEFINITION on VARIABLE_DEFINITION
				""",
				"""
					Directive '@VARIABLE_DEFINITION' is not valid on OBJECT but only on VARIABLE_DEFINITION.

					<document>:313:2
					312 | @UNION
					313 | @VARIABLE_DEFINITION
					    |  ^
					314 | {

					<document>:447:35
					446 | directive @UNION on UNION
					447 | directive @VARIABLE_DEFINITION on VARIABLE_DEFINITION
					    |                                   ^
				""",
				"""
					Directive '@ARGUMENT_DEFINITION' is not valid on SCALAR but only on ARGUMENT_DEFINITION.

					<document>:363:2
					362 | scalar Scalar
					363 | @ARGUMENT_DEFINITION
					    |  ^
					364 | @ENUM

					<document>:429:35
					428 | 
					429 | directive @ARGUMENT_DEFINITION on ARGUMENT_DEFINITION
					    |                                   ^
					430 | directive @ENUM on ENUM
				""",
				"""
					Directive '@ENUM' is not valid on SCALAR but only on ENUM.

					<document>:364:2
					363 | @ARGUMENT_DEFINITION
					364 | @ENUM
					    |  ^
					365 | @ENUM_VALUE

					<document>:430:20
					429 | directive @ARGUMENT_DEFINITION on ARGUMENT_DEFINITION
					430 | directive @ENUM on ENUM
					    |                    ^
					431 | directive @ENUM_VALUE on ENUM_VALUE
				""",
				"""
					Directive '@ENUM_VALUE' is not valid on SCALAR but only on ENUM_VALUE.

					<document>:365:2
					364 | @ENUM
					365 | @ENUM_VALUE
					    |  ^
					366 | @FIELD

					<document>:431:26
					430 | directive @ENUM on ENUM
					431 | directive @ENUM_VALUE on ENUM_VALUE
					    |                          ^
					432 | directive @FIELD on FIELD
				""",
				"""
					Directive '@FIELD' is not valid on SCALAR but only on FIELD.

					<document>:366:2
					365 | @ENUM_VALUE
					366 | @FIELD
					    |  ^
					367 | @FIELD_DEFINITION

					<document>:432:21
					431 | directive @ENUM_VALUE on ENUM_VALUE
					432 | directive @FIELD on FIELD
					    |                     ^
					433 | directive @FIELD_DEFINITION on FIELD_DEFINITION
				""",
				"""
					Directive '@FIELD_DEFINITION' is not valid on SCALAR but only on FIELD_DEFINITION.

					<document>:367:2
					366 | @FIELD
					367 | @FIELD_DEFINITION
					    |  ^
					368 | @FRAGMENT_DEFINITION

					<document>:433:32
					432 | directive @FIELD on FIELD
					433 | directive @FIELD_DEFINITION on FIELD_DEFINITION
					    |                                ^
					434 | directive @FRAGMENT_DEFINITION on FRAGMENT_DEFINITION
				""",
				"""
					Directive '@FRAGMENT_DEFINITION' is not valid on SCALAR but only on FRAGMENT_DEFINITION.

					<document>:368:2
					367 | @FIELD_DEFINITION
					368 | @FRAGMENT_DEFINITION
					    |  ^
					369 | @FRAGMENT_SPREAD

					<document>:434:35
					433 | directive @FIELD_DEFINITION on FIELD_DEFINITION
					434 | directive @FRAGMENT_DEFINITION on FRAGMENT_DEFINITION
					    |                                   ^
					435 | directive @FRAGMENT_SPREAD on FRAGMENT_SPREAD
				""",
				"""
					Directive '@FRAGMENT_SPREAD' is not valid on SCALAR but only on FRAGMENT_SPREAD.

					<document>:369:2
					368 | @FRAGMENT_DEFINITION
					369 | @FRAGMENT_SPREAD
					    |  ^
					370 | @INLINE_FRAGMENT

					<document>:435:31
					434 | directive @FRAGMENT_DEFINITION on FRAGMENT_DEFINITION
					435 | directive @FRAGMENT_SPREAD on FRAGMENT_SPREAD
					    |                               ^
					436 | directive @INLINE_FRAGMENT on INLINE_FRAGMENT
				""",
				"""
					Directive '@INLINE_FRAGMENT' is not valid on SCALAR but only on INLINE_FRAGMENT.

					<document>:370:2
					369 | @FRAGMENT_SPREAD
					370 | @INLINE_FRAGMENT
					    |  ^
					371 | @INPUT_FIELD_DEFINITION

					<document>:436:31
					435 | directive @FRAGMENT_SPREAD on FRAGMENT_SPREAD
					436 | directive @INLINE_FRAGMENT on INLINE_FRAGMENT
					    |                               ^
					437 | directive @INPUT_FIELD_DEFINITION on INPUT_FIELD_DEFINITION
				""",
				"""
					Directive '@INPUT_FIELD_DEFINITION' is not valid on SCALAR but only on INPUT_FIELD_DEFINITION.

					<document>:371:2
					370 | @INLINE_FRAGMENT
					371 | @INPUT_FIELD_DEFINITION
					    |  ^
					372 | @INPUT_OBJECT

					<document>:437:38
					436 | directive @INLINE_FRAGMENT on INLINE_FRAGMENT
					437 | directive @INPUT_FIELD_DEFINITION on INPUT_FIELD_DEFINITION
					    |                                      ^
					438 | directive @INPUT_OBJECT on INPUT_OBJECT
				""",
				"""
					Directive '@INPUT_OBJECT' is not valid on SCALAR but only on INPUT_OBJECT.

					<document>:372:2
					371 | @INPUT_FIELD_DEFINITION
					372 | @INPUT_OBJECT
					    |  ^
					373 | @INTERFACE

					<document>:438:28
					437 | directive @INPUT_FIELD_DEFINITION on INPUT_FIELD_DEFINITION
					438 | directive @INPUT_OBJECT on INPUT_OBJECT
					    |                            ^
					439 | directive @INTERFACE on INTERFACE
				""",
				"""
					Directive '@INTERFACE' is not valid on SCALAR but only on INTERFACE.

					<document>:373:2
					372 | @INPUT_OBJECT
					373 | @INTERFACE
					    |  ^
					374 | @MUTATION

					<document>:439:25
					438 | directive @INPUT_OBJECT on INPUT_OBJECT
					439 | directive @INTERFACE on INTERFACE
					    |                         ^
					440 | directive @MUTATION on MUTATION
				""",
				"""
					Directive '@MUTATION' is not valid on SCALAR but only on MUTATION.

					<document>:374:2
					373 | @INTERFACE
					374 | @MUTATION
					    |  ^
					375 | @OBJECT

					<document>:440:24
					439 | directive @INTERFACE on INTERFACE
					440 | directive @MUTATION on MUTATION
					    |                        ^
					441 | directive @OBJECT on OBJECT
				""",
				"""
					Directive '@OBJECT' is not valid on SCALAR but only on OBJECT.

					<document>:375:2
					374 | @MUTATION
					375 | @OBJECT
					    |  ^
					376 | @QUERY

					<document>:441:22
					440 | directive @MUTATION on MUTATION
					441 | directive @OBJECT on OBJECT
					    |                      ^
					442 | directive @QUERY on QUERY
				""",
				"""
					Directive '@QUERY' is not valid on SCALAR but only on QUERY.

					<document>:376:2
					375 | @OBJECT
					376 | @QUERY
					    |  ^
					377 | @SCALAR

					<document>:442:21
					441 | directive @OBJECT on OBJECT
					442 | directive @QUERY on QUERY
					    |                     ^
					443 | directive @SCALAR on SCALAR
				""",
				"""
					Directive '@SCHEMA' is not valid on SCALAR but only on SCHEMA.

					<document>:378:2
					377 | @SCALAR
					378 | @SCHEMA
					    |  ^
					379 | @SUBSCRIPTION

					<document>:444:22
					443 | directive @SCALAR on SCALAR
					444 | directive @SCHEMA on SCHEMA
					    |                      ^
					445 | directive @SUBSCRIPTION on SUBSCRIPTION
				""",
				"""
					Directive '@SUBSCRIPTION' is not valid on SCALAR but only on SUBSCRIPTION.

					<document>:379:2
					378 | @SCHEMA
					379 | @SUBSCRIPTION
					    |  ^
					380 | @UNION

					<document>:445:28
					444 | directive @SCHEMA on SCHEMA
					445 | directive @SUBSCRIPTION on SUBSCRIPTION
					    |                            ^
					446 | directive @UNION on UNION
				""",
				"""
					Directive '@UNION' is not valid on SCALAR but only on UNION.

					<document>:380:2
					379 | @SUBSCRIPTION
					380 | @UNION
					    |  ^
					381 | @VARIABLE_DEFINITION

					<document>:446:21
					445 | directive @SUBSCRIPTION on SUBSCRIPTION
					446 | directive @UNION on UNION
					    |                     ^
					447 | directive @VARIABLE_DEFINITION on VARIABLE_DEFINITION
				""",
				"""
					Directive '@VARIABLE_DEFINITION' is not valid on SCALAR but only on VARIABLE_DEFINITION.

					<document>:381:2
					380 | @UNION
					381 | @VARIABLE_DEFINITION
					    |  ^
					382 | 

					<document>:447:35
					446 | directive @UNION on UNION
					447 | directive @VARIABLE_DEFINITION on VARIABLE_DEFINITION
					    |                                   ^
				""",
				"""
					Directive '@ARGUMENT_DEFINITION' is not valid on UNION but only on ARGUMENT_DEFINITION.

					<document>:384:2
					383 | union Union
					384 | @ARGUMENT_DEFINITION
					    |  ^
					385 | @ENUM

					<document>:429:35
					428 | 
					429 | directive @ARGUMENT_DEFINITION on ARGUMENT_DEFINITION
					    |                                   ^
					430 | directive @ENUM on ENUM
				""",
				"""
					Directive '@ENUM' is not valid on UNION but only on ENUM.

					<document>:385:2
					384 | @ARGUMENT_DEFINITION
					385 | @ENUM
					    |  ^
					386 | @ENUM_VALUE

					<document>:430:20
					429 | directive @ARGUMENT_DEFINITION on ARGUMENT_DEFINITION
					430 | directive @ENUM on ENUM
					    |                    ^
					431 | directive @ENUM_VALUE on ENUM_VALUE
				""",
				"""
					Directive '@ENUM_VALUE' is not valid on UNION but only on ENUM_VALUE.

					<document>:386:2
					385 | @ENUM
					386 | @ENUM_VALUE
					    |  ^
					387 | @FIELD

					<document>:431:26
					430 | directive @ENUM on ENUM
					431 | directive @ENUM_VALUE on ENUM_VALUE
					    |                          ^
					432 | directive @FIELD on FIELD
				""",
				"""
					Directive '@FIELD' is not valid on UNION but only on FIELD.

					<document>:387:2
					386 | @ENUM_VALUE
					387 | @FIELD
					    |  ^
					388 | @FIELD_DEFINITION

					<document>:432:21
					431 | directive @ENUM_VALUE on ENUM_VALUE
					432 | directive @FIELD on FIELD
					    |                     ^
					433 | directive @FIELD_DEFINITION on FIELD_DEFINITION
				""",
				"""
					Directive '@FIELD_DEFINITION' is not valid on UNION but only on FIELD_DEFINITION.

					<document>:388:2
					387 | @FIELD
					388 | @FIELD_DEFINITION
					    |  ^
					389 | @FRAGMENT_DEFINITION

					<document>:433:32
					432 | directive @FIELD on FIELD
					433 | directive @FIELD_DEFINITION on FIELD_DEFINITION
					    |                                ^
					434 | directive @FRAGMENT_DEFINITION on FRAGMENT_DEFINITION
				""",
				"""
					Directive '@FRAGMENT_DEFINITION' is not valid on UNION but only on FRAGMENT_DEFINITION.

					<document>:389:2
					388 | @FIELD_DEFINITION
					389 | @FRAGMENT_DEFINITION
					    |  ^
					390 | @FRAGMENT_SPREAD

					<document>:434:35
					433 | directive @FIELD_DEFINITION on FIELD_DEFINITION
					434 | directive @FRAGMENT_DEFINITION on FRAGMENT_DEFINITION
					    |                                   ^
					435 | directive @FRAGMENT_SPREAD on FRAGMENT_SPREAD
				""",
				"""
					Directive '@FRAGMENT_SPREAD' is not valid on UNION but only on FRAGMENT_SPREAD.

					<document>:390:2
					389 | @FRAGMENT_DEFINITION
					390 | @FRAGMENT_SPREAD
					    |  ^
					391 | @INLINE_FRAGMENT

					<document>:435:31
					434 | directive @FRAGMENT_DEFINITION on FRAGMENT_DEFINITION
					435 | directive @FRAGMENT_SPREAD on FRAGMENT_SPREAD
					    |                               ^
					436 | directive @INLINE_FRAGMENT on INLINE_FRAGMENT
				""",
				"""
					Directive '@INLINE_FRAGMENT' is not valid on UNION but only on INLINE_FRAGMENT.

					<document>:391:2
					390 | @FRAGMENT_SPREAD
					391 | @INLINE_FRAGMENT
					    |  ^
					392 | @INPUT_FIELD_DEFINITION

					<document>:436:31
					435 | directive @FRAGMENT_SPREAD on FRAGMENT_SPREAD
					436 | directive @INLINE_FRAGMENT on INLINE_FRAGMENT
					    |                               ^
					437 | directive @INPUT_FIELD_DEFINITION on INPUT_FIELD_DEFINITION
				""",
				"""
					Directive '@INPUT_FIELD_DEFINITION' is not valid on UNION but only on INPUT_FIELD_DEFINITION.

					<document>:392:2
					391 | @INLINE_FRAGMENT
					392 | @INPUT_FIELD_DEFINITION
					    |  ^
					393 | @INPUT_OBJECT

					<document>:437:38
					436 | directive @INLINE_FRAGMENT on INLINE_FRAGMENT
					437 | directive @INPUT_FIELD_DEFINITION on INPUT_FIELD_DEFINITION
					    |                                      ^
					438 | directive @INPUT_OBJECT on INPUT_OBJECT
				""",
				"""
					Directive '@INPUT_OBJECT' is not valid on UNION but only on INPUT_OBJECT.

					<document>:393:2
					392 | @INPUT_FIELD_DEFINITION
					393 | @INPUT_OBJECT
					    |  ^
					394 | @INTERFACE

					<document>:438:28
					437 | directive @INPUT_FIELD_DEFINITION on INPUT_FIELD_DEFINITION
					438 | directive @INPUT_OBJECT on INPUT_OBJECT
					    |                            ^
					439 | directive @INTERFACE on INTERFACE
				""",
				"""
					Directive '@INTERFACE' is not valid on UNION but only on INTERFACE.

					<document>:394:2
					393 | @INPUT_OBJECT
					394 | @INTERFACE
					    |  ^
					395 | @MUTATION

					<document>:439:25
					438 | directive @INPUT_OBJECT on INPUT_OBJECT
					439 | directive @INTERFACE on INTERFACE
					    |                         ^
					440 | directive @MUTATION on MUTATION
				""",
				"""
					Directive '@MUTATION' is not valid on UNION but only on MUTATION.

					<document>:395:2
					394 | @INTERFACE
					395 | @MUTATION
					    |  ^
					396 | @OBJECT

					<document>:440:24
					439 | directive @INTERFACE on INTERFACE
					440 | directive @MUTATION on MUTATION
					    |                        ^
					441 | directive @OBJECT on OBJECT
				""",
				"""
					Directive '@OBJECT' is not valid on UNION but only on OBJECT.

					<document>:396:2
					395 | @MUTATION
					396 | @OBJECT
					    |  ^
					397 | @QUERY

					<document>:441:22
					440 | directive @MUTATION on MUTATION
					441 | directive @OBJECT on OBJECT
					    |                      ^
					442 | directive @QUERY on QUERY
				""",
				"""
					Directive '@QUERY' is not valid on UNION but only on QUERY.

					<document>:397:2
					396 | @OBJECT
					397 | @QUERY
					    |  ^
					398 | @SCALAR

					<document>:442:21
					441 | directive @OBJECT on OBJECT
					442 | directive @QUERY on QUERY
					    |                     ^
					443 | directive @SCALAR on SCALAR
				""",
				"""
					Directive '@SCALAR' is not valid on UNION but only on SCALAR.

					<document>:398:2
					397 | @QUERY
					398 | @SCALAR
					    |  ^
					399 | @SCHEMA

					<document>:443:22
					442 | directive @QUERY on QUERY
					443 | directive @SCALAR on SCALAR
					    |                      ^
					444 | directive @SCHEMA on SCHEMA
				""",
				"""
					Directive '@SCHEMA' is not valid on UNION but only on SCHEMA.

					<document>:399:2
					398 | @SCALAR
					399 | @SCHEMA
					    |  ^
					400 | @SUBSCRIPTION

					<document>:444:22
					443 | directive @SCALAR on SCALAR
					444 | directive @SCHEMA on SCHEMA
					    |                      ^
					445 | directive @SUBSCRIPTION on SUBSCRIPTION
				""",
				"""
					Directive '@SUBSCRIPTION' is not valid on UNION but only on SUBSCRIPTION.

					<document>:400:2
					399 | @SCHEMA
					400 | @SUBSCRIPTION
					    |  ^
					401 | @UNION

					<document>:445:28
					444 | directive @SCHEMA on SCHEMA
					445 | directive @SUBSCRIPTION on SUBSCRIPTION
					    |                            ^
					446 | directive @UNION on UNION
				""",
				"""
					Directive '@VARIABLE_DEFINITION' is not valid on UNION but only on VARIABLE_DEFINITION.

					<document>:402:2
					401 | @UNION
					402 | @VARIABLE_DEFINITION
					    |  ^
					403 |    = Query

					<document>:447:35
					446 | directive @UNION on UNION
					447 | directive @VARIABLE_DEFINITION on VARIABLE_DEFINITION
					    |                                   ^
				""",
				"""
					Directive '@ARGUMENT_DEFINITION' is not valid on SCHEMA but only on ARGUMENT_DEFINITION.

					<document>:406:2
					405 | schema
					406 | @ARGUMENT_DEFINITION
					    |  ^
					407 | @ENUM

					<document>:429:35
					428 | 
					429 | directive @ARGUMENT_DEFINITION on ARGUMENT_DEFINITION
					    |                                   ^
					430 | directive @ENUM on ENUM
				""",
				"""
					Directive '@ENUM' is not valid on SCHEMA but only on ENUM.

					<document>:407:2
					406 | @ARGUMENT_DEFINITION
					407 | @ENUM
					    |  ^
					408 | @ENUM_VALUE

					<document>:430:20
					429 | directive @ARGUMENT_DEFINITION on ARGUMENT_DEFINITION
					430 | directive @ENUM on ENUM
					    |                    ^
					431 | directive @ENUM_VALUE on ENUM_VALUE
				""",
				"""
					Directive '@ENUM_VALUE' is not valid on SCHEMA but only on ENUM_VALUE.

					<document>:408:2
					407 | @ENUM
					408 | @ENUM_VALUE
					    |  ^
					409 | @FIELD

					<document>:431:26
					430 | directive @ENUM on ENUM
					431 | directive @ENUM_VALUE on ENUM_VALUE
					    |                          ^
					432 | directive @FIELD on FIELD
				""",
				"""
					Directive '@FIELD' is not valid on SCHEMA but only on FIELD.

					<document>:409:2
					408 | @ENUM_VALUE
					409 | @FIELD
					    |  ^
					410 | @FIELD_DEFINITION

					<document>:432:21
					431 | directive @ENUM_VALUE on ENUM_VALUE
					432 | directive @FIELD on FIELD
					    |                     ^
					433 | directive @FIELD_DEFINITION on FIELD_DEFINITION
				""",
				"""
					Directive '@FIELD_DEFINITION' is not valid on SCHEMA but only on FIELD_DEFINITION.

					<document>:410:2
					409 | @FIELD
					410 | @FIELD_DEFINITION
					    |  ^
					411 | @FRAGMENT_DEFINITION

					<document>:433:32
					432 | directive @FIELD on FIELD
					433 | directive @FIELD_DEFINITION on FIELD_DEFINITION
					    |                                ^
					434 | directive @FRAGMENT_DEFINITION on FRAGMENT_DEFINITION
				""",
				"""
					Directive '@FRAGMENT_DEFINITION' is not valid on SCHEMA but only on FRAGMENT_DEFINITION.

					<document>:411:2
					410 | @FIELD_DEFINITION
					411 | @FRAGMENT_DEFINITION
					    |  ^
					412 | @FRAGMENT_SPREAD

					<document>:434:35
					433 | directive @FIELD_DEFINITION on FIELD_DEFINITION
					434 | directive @FRAGMENT_DEFINITION on FRAGMENT_DEFINITION
					    |                                   ^
					435 | directive @FRAGMENT_SPREAD on FRAGMENT_SPREAD
				""",
				"""
					Directive '@FRAGMENT_SPREAD' is not valid on SCHEMA but only on FRAGMENT_SPREAD.

					<document>:412:2
					411 | @FRAGMENT_DEFINITION
					412 | @FRAGMENT_SPREAD
					    |  ^
					413 | @INLINE_FRAGMENT

					<document>:435:31
					434 | directive @FRAGMENT_DEFINITION on FRAGMENT_DEFINITION
					435 | directive @FRAGMENT_SPREAD on FRAGMENT_SPREAD
					    |                               ^
					436 | directive @INLINE_FRAGMENT on INLINE_FRAGMENT
				""",
				"""
					Directive '@INLINE_FRAGMENT' is not valid on SCHEMA but only on INLINE_FRAGMENT.

					<document>:413:2
					412 | @FRAGMENT_SPREAD
					413 | @INLINE_FRAGMENT
					    |  ^
					414 | @INPUT_FIELD_DEFINITION

					<document>:436:31
					435 | directive @FRAGMENT_SPREAD on FRAGMENT_SPREAD
					436 | directive @INLINE_FRAGMENT on INLINE_FRAGMENT
					    |                               ^
					437 | directive @INPUT_FIELD_DEFINITION on INPUT_FIELD_DEFINITION
				""",
				"""
					Directive '@INPUT_FIELD_DEFINITION' is not valid on SCHEMA but only on INPUT_FIELD_DEFINITION.

					<document>:414:2
					413 | @INLINE_FRAGMENT
					414 | @INPUT_FIELD_DEFINITION
					    |  ^
					415 | @INPUT_OBJECT

					<document>:437:38
					436 | directive @INLINE_FRAGMENT on INLINE_FRAGMENT
					437 | directive @INPUT_FIELD_DEFINITION on INPUT_FIELD_DEFINITION
					    |                                      ^
					438 | directive @INPUT_OBJECT on INPUT_OBJECT
				""",
				"""
					Directive '@INPUT_OBJECT' is not valid on SCHEMA but only on INPUT_OBJECT.

					<document>:415:2
					414 | @INPUT_FIELD_DEFINITION
					415 | @INPUT_OBJECT
					    |  ^
					416 | @INTERFACE

					<document>:438:28
					437 | directive @INPUT_FIELD_DEFINITION on INPUT_FIELD_DEFINITION
					438 | directive @INPUT_OBJECT on INPUT_OBJECT
					    |                            ^
					439 | directive @INTERFACE on INTERFACE
				""",
				"""
					Directive '@INTERFACE' is not valid on SCHEMA but only on INTERFACE.

					<document>:416:2
					415 | @INPUT_OBJECT
					416 | @INTERFACE
					    |  ^
					417 | @MUTATION

					<document>:439:25
					438 | directive @INPUT_OBJECT on INPUT_OBJECT
					439 | directive @INTERFACE on INTERFACE
					    |                         ^
					440 | directive @MUTATION on MUTATION
				""",
				"""
					Directive '@MUTATION' is not valid on SCHEMA but only on MUTATION.

					<document>:417:2
					416 | @INTERFACE
					417 | @MUTATION
					    |  ^
					418 | @OBJECT

					<document>:440:24
					439 | directive @INTERFACE on INTERFACE
					440 | directive @MUTATION on MUTATION
					    |                        ^
					441 | directive @OBJECT on OBJECT
				""",
				"""
					Directive '@OBJECT' is not valid on SCHEMA but only on OBJECT.

					<document>:418:2
					417 | @MUTATION
					418 | @OBJECT
					    |  ^
					419 | @QUERY

					<document>:441:22
					440 | directive @MUTATION on MUTATION
					441 | directive @OBJECT on OBJECT
					    |                      ^
					442 | directive @QUERY on QUERY
				""",
				"""
					Directive '@QUERY' is not valid on SCHEMA but only on QUERY.

					<document>:419:2
					418 | @OBJECT
					419 | @QUERY
					    |  ^
					420 | @SCALAR

					<document>:442:21
					441 | directive @OBJECT on OBJECT
					442 | directive @QUERY on QUERY
					    |                     ^
					443 | directive @SCALAR on SCALAR
				""",
				"""
					Directive '@SCALAR' is not valid on SCHEMA but only on SCALAR.

					<document>:420:2
					419 | @QUERY
					420 | @SCALAR
					    |  ^
					421 | @SCHEMA

					<document>:443:22
					442 | directive @QUERY on QUERY
					443 | directive @SCALAR on SCALAR
					    |                      ^
					444 | directive @SCHEMA on SCHEMA
				""",
				"""
					Directive '@SUBSCRIPTION' is not valid on SCHEMA but only on SUBSCRIPTION.

					<document>:422:2
					421 | @SCHEMA
					422 | @SUBSCRIPTION
					    |  ^
					423 | @UNION

					<document>:445:28
					444 | directive @SCHEMA on SCHEMA
					445 | directive @SUBSCRIPTION on SUBSCRIPTION
					    |                            ^
					446 | directive @UNION on UNION
				""",
				"""
					Directive '@UNION' is not valid on SCHEMA but only on UNION.

					<document>:423:2
					422 | @SUBSCRIPTION
					423 | @UNION
					    |  ^
					424 | @VARIABLE_DEFINITION

					<document>:446:21
					445 | directive @SUBSCRIPTION on SUBSCRIPTION
					446 | directive @UNION on UNION
					    |                     ^
					447 | directive @VARIABLE_DEFINITION on VARIABLE_DEFINITION
				""",
				"""
					Directive '@VARIABLE_DEFINITION' is not valid on SCHEMA but only on VARIABLE_DEFINITION.

					<document>:424:2
					423 | @UNION
					424 | @VARIABLE_DEFINITION
					    |  ^
					425 | {

					<document>:447:35
					446 | directive @UNION on UNION
					447 | directive @VARIABLE_DEFINITION on VARIABLE_DEFINITION
					    |                                   ^
				"""
			),
			document = """
				|query someQuery(
				|   ${'$'}foo: String
				|   @ARGUMENT_DEFINITION
				|   @ENUM
				|   @ENUM_VALUE
				|   @FIELD
				|   @FIELD_DEFINITION
				|   @FRAGMENT_DEFINITION
				|   @FRAGMENT_SPREAD
				|   @INLINE_FRAGMENT
				|   @INPUT_FIELD_DEFINITION
				|   @INPUT_OBJECT
				|   @INTERFACE
				|   @MUTATION
				|   @OBJECT
				|   @QUERY
				|   @SCALAR
				|   @SCHEMA
				|   @SUBSCRIPTION
				|   @UNION
				|   @VARIABLE_DEFINITION
				|)
				|@ARGUMENT_DEFINITION
				|@ENUM
				|@ENUM_VALUE
				|@FIELD
				|@FIELD_DEFINITION
				|@FRAGMENT_DEFINITION
				|@FRAGMENT_SPREAD
				|@INLINE_FRAGMENT
				|@INPUT_FIELD_DEFINITION
				|@INPUT_OBJECT
				|@INTERFACE
				|@MUTATION
				|@OBJECT
				|@QUERY
				|@SCALAR
				|@SCHEMA
				|@SUBSCRIPTION
				|@UNION
				|@VARIABLE_DEFINITION
				|{
				|   ...frag
				|   @ARGUMENT_DEFINITION
				|   @ENUM
				|   @ENUM_VALUE
				|   @FIELD
				|   @FIELD_DEFINITION
				|   @FRAGMENT_DEFINITION
				|   @FRAGMENT_SPREAD
				|   @INLINE_FRAGMENT
				|   @INPUT_FIELD_DEFINITION
				|   @INPUT_OBJECT
				|   @INTERFACE
				|   @MUTATION
				|   @OBJECT
				|   @QUERY
				|   @SCALAR
				|   @SCHEMA
				|   @SUBSCRIPTION
				|   @UNION
				|   @VARIABLE_DEFINITION
				|
				|   ... on Query
				|   @ARGUMENT_DEFINITION
				|   @ENUM
				|   @ENUM_VALUE
				|   @FIELD
				|   @FIELD_DEFINITION
				|   @FRAGMENT_DEFINITION
				|   @FRAGMENT_SPREAD
				|   @INLINE_FRAGMENT
				|   @INPUT_FIELD_DEFINITION
				|   @INPUT_OBJECT
				|   @INTERFACE
				|   @MUTATION
				|   @OBJECT
				|   @QUERY
				|   @SCALAR
				|   @SCHEMA
				|   @SUBSCRIPTION
				|   @UNION
				|   @VARIABLE_DEFINITION
				|   {
				|      field
				|   }
				|}
				|
				|mutation
				|@ARGUMENT_DEFINITION
				|@ENUM
				|@ENUM_VALUE
				|@FIELD
				|@FIELD_DEFINITION
				|@FRAGMENT_DEFINITION
				|@FRAGMENT_SPREAD
				|@INLINE_FRAGMENT
				|@INPUT_FIELD_DEFINITION
				|@INPUT_OBJECT
				|@INTERFACE
				|@MUTATION
				|@OBJECT
				|@QUERY
				|@SCALAR
				|@SCHEMA
				|@SUBSCRIPTION
				|@UNION
				|@VARIABLE_DEFINITION
				|{
				|   field: String
				|}
				|
				|subscription
				|@ARGUMENT_DEFINITION
				|@ENUM
				|@ENUM_VALUE
				|@FIELD
				|@FIELD_DEFINITION
				|@FRAGMENT_DEFINITION
				|@FRAGMENT_SPREAD
				|@INLINE_FRAGMENT
				|@INPUT_FIELD_DEFINITION
				|@INPUT_OBJECT
				|@INTERFACE
				|@MUTATION
				|@OBJECT
				|@QUERY
				|@SCALAR
				|@SCHEMA
				|@SUBSCRIPTION
				|@UNION
				|@VARIABLE_DEFINITION
				|{
				|   field: String
				|}
				|
				|fragment frag on Query
				|@ARGUMENT_DEFINITION
				|@ENUM
				|@ENUM_VALUE
				|@FIELD
				|@FIELD_DEFINITION
				|@FRAGMENT_DEFINITION
				|@FRAGMENT_SPREAD
				|@INLINE_FRAGMENT
				|@INPUT_FIELD_DEFINITION
				|@INPUT_OBJECT
				|@INTERFACE
				|@MUTATION
				|@OBJECT
				|@QUERY
				|@SCALAR
				|@SCHEMA
				|@SUBSCRIPTION
				|@UNION
				|@VARIABLE_DEFINITION
				|{
				|   field
				|   @ARGUMENT_DEFINITION
				|   @ENUM
				|   @ENUM_VALUE
				|   @FIELD
				|   @FIELD_DEFINITION
				|   @FRAGMENT_DEFINITION
				|   @FRAGMENT_SPREAD
				|   @INLINE_FRAGMENT
				|   @INPUT_FIELD_DEFINITION
				|   @INPUT_OBJECT
				|   @INTERFACE
				|   @MUTATION
				|   @OBJECT
				|   @QUERY
				|   @SCALAR
				|   @SCHEMA
				|   @SUBSCRIPTION
				|   @UNION
				|   @VARIABLE_DEFINITION
				|}
				|
				|enum Enum
				|@ARGUMENT_DEFINITION
				|@ENUM
				|@ENUM_VALUE
				|@FIELD
				|@FIELD_DEFINITION
				|@FRAGMENT_DEFINITION
				|@FRAGMENT_SPREAD
				|@INLINE_FRAGMENT
				|@INPUT_FIELD_DEFINITION
				|@INPUT_OBJECT
				|@INTERFACE
				|@MUTATION
				|@OBJECT
				|@QUERY
				|@SCALAR
				|@SCHEMA
				|@SUBSCRIPTION
				|@UNION
				|@VARIABLE_DEFINITION
				|{
				|   VALUE
				|   @ARGUMENT_DEFINITION
				|   @ENUM
				|   @ENUM_VALUE
				|   @FIELD
				|   @FIELD_DEFINITION
				|   @FRAGMENT_DEFINITION
				|   @FRAGMENT_SPREAD
				|   @INLINE_FRAGMENT
				|   @INPUT_FIELD_DEFINITION
				|   @INPUT_OBJECT
				|   @INTERFACE
				|   @MUTATION
				|   @OBJECT
				|   @QUERY
				|   @SCALAR
				|   @SCHEMA
				|   @SUBSCRIPTION
				|   @UNION
				|   @VARIABLE_DEFINITION
				|}
				|
				|input Input
				|@ARGUMENT_DEFINITION
				|@ENUM
				|@ENUM_VALUE
				|@FIELD
				|@FIELD_DEFINITION
				|@FRAGMENT_DEFINITION
				|@FRAGMENT_SPREAD
				|@INLINE_FRAGMENT
				|@INPUT_FIELD_DEFINITION
				|@INPUT_OBJECT
				|@INTERFACE
				|@MUTATION
				|@OBJECT
				|@QUERY
				|@SCALAR
				|@SCHEMA
				|@SUBSCRIPTION
				|@UNION
				|@VARIABLE_DEFINITION
				|{
				|   field: String
				|@ARGUMENT_DEFINITION
				|@ENUM
				|@ENUM_VALUE
				|@FIELD
				|@FIELD_DEFINITION
				|@FRAGMENT_DEFINITION
				|@FRAGMENT_SPREAD
				|@INLINE_FRAGMENT
				|@INPUT_FIELD_DEFINITION
				|@INPUT_OBJECT
				|@INTERFACE
				|@MUTATION
				|@OBJECT
				|@QUERY
				|@SCALAR
				|@SCHEMA
				|@SUBSCRIPTION
				|@UNION
				|@VARIABLE_DEFINITION
				|}
				|
				|interface Interface
				|@ARGUMENT_DEFINITION
				|@ENUM
				|@ENUM_VALUE
				|@FIELD
				|@FIELD_DEFINITION
				|@FRAGMENT_DEFINITION
				|@FRAGMENT_SPREAD
				|@INLINE_FRAGMENT
				|@INPUT_FIELD_DEFINITION
				|@INPUT_OBJECT
				|@INTERFACE
				|@MUTATION
				|@OBJECT
				|@QUERY
				|@SCALAR
				|@SCHEMA
				|@SUBSCRIPTION
				|@UNION
				|@VARIABLE_DEFINITION
				|{
				|   field(argument: String): String
				|}
				|
				|type Mutation {
				|   field: String
				|}
				|
				|type Query implements Interface
				|@ARGUMENT_DEFINITION
				|@ENUM
				|@ENUM_VALUE
				|@FIELD
				|@FIELD_DEFINITION
				|@FRAGMENT_DEFINITION
				|@FRAGMENT_SPREAD
				|@INLINE_FRAGMENT
				|@INPUT_FIELD_DEFINITION
				|@INPUT_OBJECT
				|@INTERFACE
				|@MUTATION
				|@OBJECT
				|@QUERY
				|@SCALAR
				|@SCHEMA
				|@SUBSCRIPTION
				|@UNION
				|@VARIABLE_DEFINITION
				|{
				|   field(
				|      argument: String
				|      @ARGUMENT_DEFINITION
				|      @ENUM
				|      @ENUM_VALUE
				|      @FIELD
				|      @FIELD_DEFINITION
				|      @FRAGMENT_DEFINITION
				|      @FRAGMENT_SPREAD
				|      @INLINE_FRAGMENT
				|      @INPUT_FIELD_DEFINITION
				|      @INPUT_OBJECT
				|      @INTERFACE
				|      @MUTATION
				|      @OBJECT
				|      @QUERY
				|      @SCALAR
				|      @SCHEMA
				|      @SUBSCRIPTION
				|      @UNION
				|      @VARIABLE_DEFINITION
				|   ): String
				|   @ARGUMENT_DEFINITION
				|   @ENUM
				|   @ENUM_VALUE
				|   @FIELD
				|   @FIELD_DEFINITION
				|   @FRAGMENT_DEFINITION
				|   @FRAGMENT_SPREAD
				|   @INLINE_FRAGMENT
				|   @INPUT_FIELD_DEFINITION
				|   @INPUT_OBJECT
				|   @INTERFACE
				|   @MUTATION
				|   @OBJECT
				|   @QUERY
				|   @SCALAR
				|   @SCHEMA
				|   @SUBSCRIPTION
				|   @UNION
				|   @VARIABLE_DEFINITION
				|}
				|
				|type Subscription {
				|   field: String
				|}
				|
				|scalar Scalar
				|@ARGUMENT_DEFINITION
				|@ENUM
				|@ENUM_VALUE
				|@FIELD
				|@FIELD_DEFINITION
				|@FRAGMENT_DEFINITION
				|@FRAGMENT_SPREAD
				|@INLINE_FRAGMENT
				|@INPUT_FIELD_DEFINITION
				|@INPUT_OBJECT
				|@INTERFACE
				|@MUTATION
				|@OBJECT
				|@QUERY
				|@SCALAR
				|@SCHEMA
				|@SUBSCRIPTION
				|@UNION
				|@VARIABLE_DEFINITION
				|
				|union Union
				|@ARGUMENT_DEFINITION
				|@ENUM
				|@ENUM_VALUE
				|@FIELD
				|@FIELD_DEFINITION
				|@FRAGMENT_DEFINITION
				|@FRAGMENT_SPREAD
				|@INLINE_FRAGMENT
				|@INPUT_FIELD_DEFINITION
				|@INPUT_OBJECT
				|@INTERFACE
				|@MUTATION
				|@OBJECT
				|@QUERY
				|@SCALAR
				|@SCHEMA
				|@SUBSCRIPTION
				|@UNION
				|@VARIABLE_DEFINITION
				|   = Query
				|
				|schema
				|@ARGUMENT_DEFINITION
				|@ENUM
				|@ENUM_VALUE
				|@FIELD
				|@FIELD_DEFINITION
				|@FRAGMENT_DEFINITION
				|@FRAGMENT_SPREAD
				|@INLINE_FRAGMENT
				|@INPUT_FIELD_DEFINITION
				|@INPUT_OBJECT
				|@INTERFACE
				|@MUTATION
				|@OBJECT
				|@QUERY
				|@SCALAR
				|@SCHEMA
				|@SUBSCRIPTION
				|@UNION
				|@VARIABLE_DEFINITION
				|{
				|   query: Query
				|}
				|
				|directive @ARGUMENT_DEFINITION on ARGUMENT_DEFINITION
				|directive @ENUM on ENUM
				|directive @ENUM_VALUE on ENUM_VALUE
				|directive @FIELD on FIELD
				|directive @FIELD_DEFINITION on FIELD_DEFINITION
				|directive @FRAGMENT_DEFINITION on FRAGMENT_DEFINITION
				|directive @FRAGMENT_SPREAD on FRAGMENT_SPREAD
				|directive @INLINE_FRAGMENT on INLINE_FRAGMENT
				|directive @INPUT_FIELD_DEFINITION on INPUT_FIELD_DEFINITION
				|directive @INPUT_OBJECT on INPUT_OBJECT
				|directive @INTERFACE on INTERFACE
				|directive @MUTATION on MUTATION
				|directive @OBJECT on OBJECT
				|directive @QUERY on QUERY
				|directive @SCALAR on SCALAR
				|directive @SCHEMA on SCHEMA
				|directive @SUBSCRIPTION on SUBSCRIPTION
				|directive @UNION on UNION
				|directive @VARIABLE_DEFINITION on VARIABLE_DEFINITION
			"""
		)
	}
}
