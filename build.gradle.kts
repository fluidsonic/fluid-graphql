import io.fluidsonic.gradle.*

plugins {
	id("io.fluidsonic.gradle") version "1.1.23"
}

fluidLibrary(name = "graphql", version = "0.9.8") {
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

		darwin {
			withoutWatchosX64() // https://github.com/Kotlin/kotlinx.coroutines/issues/2524
		}
		js()
		jvm()
	}
}
