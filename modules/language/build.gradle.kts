import io.fluidsonic.gradle.*

fluidLibraryVariant {
	description = "FIXME"

	common()
	js {
		browser()
		nodejs()
	}
	jvm(JvmTarget.jdk8)
	objc(ObjcTarget.iosArm64)
	objc(ObjcTarget.iosX64)
	objc(ObjcTarget.macosX64)
}
