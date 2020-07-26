import io.fluidsonic.gradle.*

fluidLibraryVariant {
	description = "FIXME"

	common {
		dependencies {
			api(project(":fluid-graphql-language"))
		}
	}

	jvm(JvmTarget.jdk8)
}
