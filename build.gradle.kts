import io.fluidsonic.gradle.*

plugins {
	id("io.fluidsonic.gradle") version "1.0.14"
}

fluidLibrary(name = "graphql", version = "0.9.2")

fluidLibraryVariant {
	description = "FIXME"

	common {
		dependencies {
			api(project(":fluid-graphql-client"))
			api(project(":fluid-graphql-dsl"))
			api(project(":fluid-graphql-execution"))
		}
	}
	js {
		browser()
		nodejs()
	}
	jvm(JvmTarget.jdk8)
	objc(ObjcTarget.iosArm64)
	objc(ObjcTarget.iosX64)
	objc(ObjcTarget.macosX64)
}
