import io.fluidsonic.gradle.*

fluidLibraryModule(description = "Kotlin DSL for building GraphQL schemas and documents") {
	targets {
		common {
			dependencies {
				api(project(":fluid-graphql-language"))
			}
		}

		jvm()
	}
}
