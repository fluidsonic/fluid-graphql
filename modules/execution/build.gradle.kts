import io.fluidsonic.gradle.*

fluidLibraryModule(description = "FIXME") {
	targets {
		common {
			dependencies {
				api(project(":fluid-graphql-language"))

				implementation(project(":fluid-graphql-dsl"))
			}

			testDependencies {
				implementation(kotlinx("coroutines-core", "1.3.9"))
			}
		}

//		darwin() // FIXME
		js()

		jvm {
			testDependencies {
				implementation(kotlinx("coroutines-test", "1.3.9"))
			}
		}
	}
}
