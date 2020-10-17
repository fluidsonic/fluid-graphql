import io.fluidsonic.gradle.*

fluidLibraryModule(description = "FIXME") {
	targets {
		common {
			dependencies {
				api(project(":fluid-graphql-language"))
			}
		}

//		darwin() // FIXME
		js()
		jvm()
	}
}
