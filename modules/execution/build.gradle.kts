import io.fluidsonic.gradle.*
import org.jetbrains.kotlin.gradle.plugin.*

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

		darwin()
		js(compiler = KotlinJsCompilerType.LEGACY) // FIXME https://youtrack.jetbrains.com/issue/KT-39088

		jvm {
			testDependencies {
				implementation(kotlinx("coroutines-test", "1.3.9"))
			}
		}
	}
}
