package tests

import kotlin.test.*


// https://github.com/graphql/graphql-js/blob/master/src/language/__tests__/schema-parser-test.js
class AstSchemaParsingTest {

	@Test
	fun `accepts interface`() {
		assertAst("""
			|interface Hello {
			|  world: String
			|}
		""") {
			document(0 .. 35) {
				interfaceTypeDefinition(0 .. 35) {
					name(10 .. 15) { "Hello" }
					field(20 .. 33) {
						name(20 .. 25) { "world" }
						type {
							name(27 .. 33) { "String" }
						}
					}
				}
			}
		}
	}


	@Test
	fun `accepts object`() {
		assertAst("""
			|type Hello {
			|  world: String
			|}
		""") {
			document(0 .. 30) {
				objectTypeDefinition(0 .. 30) {
					name(5 .. 10) { "Hello" }
					field(15 .. 28) {
						name(15 .. 20) { "world" }
						type {
							name(22 .. 28) { "String" }
						}
					}
				}
			}
		}
	}


	@Test
	fun `accepts input object`() {
		assertAst("""
			|input Hello {
			|  world: String
			|}
		""") {
			document(0 .. 31) {
				inputObjectTypeDefinition(0 .. 31) {
					name(6 .. 11) { "Hello" }
					argument(16 .. 29) {
						name(16 .. 21) { "world" }
						type {
							name(23 .. 29) { "String" }
						}
					}
				}
			}
		}
	}


	@Test
	fun `accepts field with argument`() {
		assertAst("""
			|type Hello {
			|  world(flag: Boolean): String
			|}
		""") {
			document(0 .. 45) {
				objectTypeDefinition(0 .. 45) {
					name(5 .. 10) { "Hello" }
					field(15 .. 43) {
						name(15 .. 20) { "world" }
						argument(21 .. 34) {
							name(21 .. 25) { "flag" }
							type {
								name(27 .. 34) { "Boolean" }
							}
						}
						type {
							name(37 .. 43) { "String" }
						}
					}
				}
			}
		}
	}


	@Test
	fun `accepts field with argument and default value`() {
		assertAst("""
			|type Hello {
			|  world(flag: Boolean = true): String
			|}
		""") {
			document(0 .. 52) {
				objectTypeDefinition(0 .. 52) {
					name(5 .. 10) { "Hello" }
					field(15 .. 50) {
						name(15 .. 20) { "world" }
						argument(21 .. 41) {
							name(21 .. 25) { "flag" }
							type {
								name(27 .. 34) { "Boolean" }
							}
							defaultValue {
								boolean(37 .. 41) { true }
							}
						}
						type {
							name(44 .. 50) { "String" }
						}
					}
				}
			}
		}
	}


	@Test
	fun `accepts field with list argument`() {
		assertAst("""
			|type Hello {
			|  world(things: [String]): String
			|}
		""") {
			document(0 .. 48) {
				objectTypeDefinition(0 .. 48) {
					name(5 .. 10) { "Hello" }
					field(15 .. 46) {
						name(15 .. 20) { "world" }
						argument(21 .. 37) {
							name(21 .. 27) { "things" }
							type {
								list(29 .. 37) {
									name(30 .. 36) { "String" }
								}
							}
						}
						type {
							name(40 .. 46) { "String" }
						}
					}
				}
			}
		}
	}


	@Test
	fun `accepts field with two arguments`() {
		assertAst("""
			|type Hello {
			|  world(argOne: Boolean, argTwo: Int): String
			|}
		""") {
			document(0 .. 60) {
				objectTypeDefinition(0 .. 60) {
					name(5 .. 10) { "Hello" }
					field(15 .. 58) {
						name(15 .. 20) { "world" }
						argument(21 .. 36) {
							name(21 .. 27) { "argOne" }
							type {
								name(29 .. 36) { "Boolean" }
							}
						}
						argument(38 .. 49) {
							name(38 .. 44) { "argTwo" }
							type {
								name(46 .. 49) { "Int" }
							}
						}
						type {
							name(52 .. 58) { "String" }
						}
					}
				}
			}
		}
	}


	@Test
	fun `accepts type with description`() {
		assertAst("""
			|"Description"
			|type Hello {
			|  world: String
			|}
		""") {
			document(0 .. 44) {
				objectTypeDefinition(0 .. 44) {
					description(0 .. 13) { "Description" }
					name(19 .. 24) { "Hello" }
					field(29 .. 42) {
						name(29 .. 34) { "world" }
						type {
							name(36 .. 42) { "String" }
						}
					}
				}
			}
		}
	}


	@Test
	fun `accepts type with multi-line description and comment in between`() {
		assertAst("""
			|""${'"'}
			|Description
			|""${'"'}
			|# Even with comments between them
			|type Hello {
			|  world: String
			|}
		""") {
			document(0 .. 84) {
				objectTypeDefinition(0 .. 84) {
					description(0 .. 19, isBlock = true) { "Description" }
					name(59 .. 64) { "Hello" }
					field(69 .. 82) {
						name(69 .. 74) { "world" }
						type {
							name(76 .. 82) { "String" }
						}
					}
				}
			}
		}
	}


	@Test
	fun `accepts non-null type reference`() {
		assertAst("""
			|type Hello {
			|  world: String!
			|}
		""") {
			document(0 .. 31) {
				objectTypeDefinition(0 .. 31) {
					name(5 .. 10) { "Hello" }
					field(15 .. 29) {
						name(15 .. 20) { "world" }
						type {
							nonNull(22 .. 29) {
								name(22 .. 28) { "String" }
							}
						}
					}
				}
			}
		}
	}


	@Test
	fun `accepts interface inheriting interface`() {
		assertAst("interface Hello implements World { field: String }") {
			document(0 .. 50) {
				interfaceTypeDefinition(0 .. 50) {
					name(10 .. 15) { "Hello" }
					inherits(27 .. 32) { "World" }
					field(35 .. 48) {
						name(35 .. 40) { "field" }
						type {
							name(42 .. 48) { "String" }
						}
					}
				}
			}
		}
	}


	@Test
	fun `accepts object implementing interface`() {
		assertAst("type Hello implements World { field: String }") {
			document(0 .. 45) {
				objectTypeDefinition(0 .. 45) {
					name(5 .. 10) { "Hello" }
					implements(22 .. 27) { "World" }
					field(30 .. 43) {
						name(30 .. 35) { "field" }
						type {
							name(37 .. 43) { "String" }
						}
					}
				}
			}
		}
	}


	@Test
	fun `accepts interface implementing multiple interfaces`() {
		assertAst("interface Hello implements Wo & rld { field: String }") {
			document(0 .. 53) {
				interfaceTypeDefinition(0 .. 53) {
					name(10 .. 15) { "Hello" }
					inherits(27 .. 29) { "Wo" }
					inherits(32 .. 35) { "rld" }
					field(38 .. 51) {
						name(38 .. 43) { "field" }
						type {
							name(45 .. 51) { "String" }
						}
					}
				}
			}
		}
	}


	@Test
	fun `accepts object implementing multiple interfaces`() {
		assertAst("type Hello implements Wo & rld { field: String }") {
			document(0 .. 48) {
				objectTypeDefinition(0 .. 48) {
					name(5 .. 10) { "Hello" }
					implements(22 .. 24) { "Wo" }
					implements(27 .. 30) { "rld" }
					field(33 .. 46) {
						name(33 .. 38) { "field" }
						type {
							name(40 .. 46) { "String" }
						}
					}
				}
			}
		}
	}


	@Test
	fun `accepts interface implementing multiple interfaces with leading ampersand`() {
		assertAst("interface Hello implements & Wo & rld { field: String }") {
			document(0 .. 55) {
				interfaceTypeDefinition(0 .. 55) {
					name(10 .. 15) { "Hello" }
					inherits(29 .. 31) { "Wo" }
					inherits(34 .. 37) { "rld" }
					field(40 .. 53) {
						name(40 .. 45) { "field" }
						type {
							name(47 .. 53) { "String" }
						}
					}
				}
			}
		}
	}


	@Test
	fun `accepts object implementing multiple interfaces with leading ampersand`() {
		assertAst("type Hello implements & Wo & rld { field: String }") {
			document(0 .. 50) {
				objectTypeDefinition(0 .. 50) {
					name(5 .. 10) { "Hello" }
					implements(24 .. 26) { "Wo" }
					implements(29 .. 32) { "rld" }
					field(35 .. 48) {
						name(35 .. 40) { "field" }
						type {
							name(42 .. 48) { "String" }
						}
					}
				}
			}
		}
	}


	@Test
	fun `accepts enum`() {
		assertAst("enum Hello { WORLD }") {
			document(0 .. 20) {
				enumTypeDefinition(0 .. 20) {
					name(5 .. 10) { "Hello" }
					value(13 .. 18) { "WORLD" }
				}
			}
		}
	}


	@Test
	fun `accepts enum with two value`() {
		assertAst("enum Hello { WO, RLD }") {
			document(0 .. 22) {
				enumTypeDefinition(0 .. 22) {
					name(5 .. 10) { "Hello" }
					value(13 .. 15) { "WO" }
					value(17 .. 20) { "RLD" }
				}
			}
		}
	}


	@Test
	fun `accepts union`() {
		assertAst("union Hello = World") {
			document(0 .. 19) {
				unionTypeDefinition(0 .. 19) {
					name(6 .. 11) { "Hello" }
					possibleType(14 .. 19) { "World" }
				}
			}
		}
	}


	@Test
	fun `accepts union with two types`() {
		assertAst("union Hello = Wo | Rld") {
			document(0 .. 22) {
				unionTypeDefinition(0 .. 22) {
					name(6 .. 11) { "Hello" }
					possibleType(14 .. 16) { "Wo" }
					possibleType(19 .. 22) { "Rld" }
				}
			}
		}
	}


	@Test
	fun `accepts union with two types and leading pipe`() {
		assertAst("union Hello = | Wo | Rld") {
			document(0 .. 24) {
				unionTypeDefinition(0 .. 24) {
					name(6 .. 11) { "Hello" }
					possibleType(16 .. 18) { "Wo" }
					possibleType(21 .. 24) { "Rld" }
				}
			}
		}
	}


	@Test
	fun `accepts scalar`() {
		assertAst("scalar Hello") {
			document(0 .. 12) {
				scalarTypeDefinition(0 .. 12) {
					name(7 .. 12) { "Hello" }
				}
			}
		}
	}


	@Test
	fun `accepts directive`() {
		assertAst("directive @foo on OBJECT | INTERFACE") {
			document(0 .. 36) {
				directiveDefinition(0 .. 36) {
					name(11 .. 14) { "foo" }
					location(18 .. 24) { "OBJECT" }
					location(27 .. 36) { "INTERFACE" }
				}
			}
		}
	}


	@Test
	fun `accepts repeatable directive`() {
		assertAst("directive @foo repeatable on OBJECT | INTERFACE") {
			document(0 .. 47) {
				directiveDefinition(0 .. 47) {
					name(11 .. 14) { "foo" }
					repeatable()
					location(29 .. 35) { "OBJECT" }
					location(38 .. 47) { "INTERFACE" }
				}
			}
		}
	}


	@Test
	fun `accepts kitchen sink schema`() {
		assertAst(kitchenSinkSchema)
	}
}
