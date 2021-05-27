import io.fluidsonic.gradle.*

fluidLibraryModule(description = "FIXME") {
	targets {
		common {
			dependencies {
				api(project(":fluid-graphql-language"))

				implementation(project(":fluid-graphql-dsl"))
			}

			testDependencies {
				implementation(kotlinx("coroutines-core", "1.5.0"))
			}
		}

		darwin {
			withoutWatchosX64() // https://github.com/Kotlin/kotlinx.coroutines/issues/2524
		}
		js()
		jvm {
			testDependencies {
				implementation(kotlinx("coroutines-test", "1.5.0"))
			}
		}
	}
}
