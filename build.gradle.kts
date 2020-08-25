import io.fluidsonic.gradle.*
import org.jetbrains.kotlin.gradle.plugin.*

plugins {
	id("io.fluidsonic.gradle") version "1.1.8"
}

fluidLibrary(name = "graphql", version = "0.9.5") {
	allModules {
		language {
			withExperimentalApi("io.fluidsonic.graphql.InternalGraphqlApi")
		}
	}
}

fluidLibraryModule(description = "FIXME") {
	targets {
		common {
			dependencies {
				api(project(":fluid-graphql-dsl"))
				api(project(":fluid-graphql-execution"))
			}
		}

		darwin()
		js(compiler = KotlinJsCompilerType.LEGACY) // FIXME https://youtrack.jetbrains.com/issue/KT-39088
		jvm()
	}
}
