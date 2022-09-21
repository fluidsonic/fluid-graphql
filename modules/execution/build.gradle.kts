import io.fluidsonic.gradle.*

fluidLibraryModule(description = "FIXME") {
	targets {
		common {
			dependencies {
				api(project(":fluid-graphql-language"))

				// https://github.com/Kotlin/kotlinx.coroutines/issues/3305#issuecomment-1238705574
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
