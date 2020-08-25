import io.fluidsonic.gradle.*
import org.jetbrains.kotlin.gradle.plugin.*

fluidLibraryModule(description = "FIXME") {
	targets {
		common {
			dependencies {
				api(project(":fluid-graphql-language"))
			}
		}

		darwin()
		js(compiler = KotlinJsCompilerType.LEGACY) // FIXME https://youtrack.jetbrains.com/issue/KT-39088
		jvm()
	}
}
