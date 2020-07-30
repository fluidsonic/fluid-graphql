import io.fluidsonic.gradle.*

fluidLibraryModule(description = "FIXME") {
	targets {
		common {
			dependencies {
				api(project(":fluid-graphql-language"))

				implementation(project(":fluid-graphql-dsl"))
			}

			testDependencies {
				implementation(kotlinx("coroutines-core", "1.3.8-1.4.0-rc"))
			}
		}

		js()

		jvm {
			testDependencies {
				implementation(kotlinx("coroutines-test", "1.3.8-1.4.0-rc"))
			}
		}

		nativeDarwin()
	}
}
