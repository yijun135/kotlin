
import org.gradle.jvm.tasks.Jar

apply { plugin("kotlin") }

dependencies {
    compile(project(":compiler:util"))
    compile(project(":compiler:light-classes"))
    compile(project(":idea:idea-core"))
    compile(project(":plugins:android-extensions-compiler"))
    compile(ideaSdkDeps("android", "sdk-tools", subdir = "plugins/android/lib"))
    compile(ideaSdkDeps("Groovy", subdir = "plugins/Groovy/lib"))
}

configureKotlinProjectSourcesDefault()
configureKotlinProjectNoTests()

fixKotlinTaskDependencies()

val jar: Jar by tasks

ideaPlugin {
    from(jar)
}
