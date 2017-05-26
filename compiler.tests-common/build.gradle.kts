
import java.io.File

apply { plugin("kotlin") }

dependencies {
    compile(project(":core"))
    compile(project(":compiler:util"))
    compile(project(":compiler:backend"))
    compile(project(":compiler:frontend"))
    compile(project(":compiler:frontend.java"))
    compile(project(":compiler:util"))
    compile(project(":compiler:cli-common"))
    compile(project(":compiler:cli"))
    compile(project(":compiler:light-classes"))
    compile(project(":compiler:serialization"))
    compile(project(":compiler:preloader"))
    compile(project(":compiler:daemon-common"))
    compile(project(":compiler:daemon-client"))
    compile(project(":js:js.serializer"))
    compile(project(":js:js.frontend"))
    compile(project(":js:js.translator"))
    compile(project(":plugins:android-extensions-compiler"))
    compile(kotlinDep("test"))
    compile(commonDep("junit"))
    compile(ideaSdkCoreDeps("intellij-core"))
    compile(ideaSdkDeps("openapi", "idea", "idea_rt"))
    compile(preloadedDeps("dx", subdir = "android-5.0/lib"))
}

configure<JavaPluginConvention> {
    sourceSets.getByName("main").java.apply {
        setSrcDirs(listOf(File(rootDir, "compiler/tests-common")))
        exclude("build.gradle.kts")
    }
}
configureKotlinProjectNoTests()

fixKotlinTaskDependencies()
