import io.fluidsonic.gradle.*

fluidLibraryVariant {
	description = "FIXME"

	common {
		dependencies {
			api(project(":fluid-graphql-language"))
			api(kotlinx("coroutines-core-common", "1.3.8"))

			implementation(project(":fluid-graphql-dsl"))
		}
	}

	jvm(JvmTarget.jdk8) {
		dependencies {
			implementation(kotlinx("coroutines-core", "1.3.8"))
		}

		testDependencies {
			implementation(kotlinx("coroutines-test", "1.3.8"))
		}
	}

	// FIXME
//	js {
//		browser()
//		nodejs()
//	}
//	jvm(JvmTarget.jdk8)
//	objc(ObjcTarget.iosArm64)
//	objc(ObjcTarget.iosX64)
//	objc(ObjcTarget.macosX64)
}
