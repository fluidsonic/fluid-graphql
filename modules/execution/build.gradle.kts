import io.fluidsonic.gradle.*

fluidLibraryModule(description = "FIXME") {
	targets {
		common {
			dependencies {
				api(project(":fluid-graphql-language"))

				implementation(kotlinx("coroutines-core", "1.6.0"))
				implementation(project(":fluid-graphql-dsl"))
			}

			testDependencies {
				implementation(kotlinx("coroutines-test", "1.6.0"))
			}
		}

		darwin()
		js()
		jvm()
	}
}
