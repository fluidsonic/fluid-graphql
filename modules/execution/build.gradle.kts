import io.fluidsonic.gradle.*

fluidLibraryModule(description = "FIXME") {
	targets {
		common {
			dependencies {
				api(project(":fluid-graphql-language"))

				implementation(kotlinx("coroutines-core", "1.7.1"))
				implementation(project(":fluid-graphql-dsl"))
			}

			testDependencies {
				implementation(kotlinx("coroutines-test", "1.7.1"))
			}
		}

		darwin()
		js()
		jvm()
	}
}
