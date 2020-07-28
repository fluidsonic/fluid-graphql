import io.fluidsonic.gradle.*

fluidLibraryVariant {
	description = "FIXME"

	common {
		dependencies {
			api(project(":fluid-graphql-language"))
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
