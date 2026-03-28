import io.fluidsonic.gradle.*

fluidLibraryModule(description = "GraphQL query execution engine with validation") {
	targets {
		common {
			dependencies {
				api(project(":fluid-graphql-language"))

				implementation(kotlinx("coroutines-core", "1.10.2"))
				implementation(project(":fluid-graphql-dsl"))
			}

			testDependencies {
				implementation(kotlinx("coroutines-test", "1.10.2"))
			}
		}

		jvm()
	}
}
