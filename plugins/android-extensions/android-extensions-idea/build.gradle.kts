
import org.gradle.jvm.tasks.Jar

apply { plugin("kotlin") }

dependencies {
    compile(project(":compiler:util"))
    compile(project(":compiler:light-classes"))
    compile(project(":idea:idea-core"))
    compile(project(":plugins:android-extensions-compiler"))
    compile(ideaSdkDeps("android", "sdk-tools", subdir = "plugins/android/lib"))
    compile(ideaSdkDeps("Groovy", subdir = "plugins/Groovy/lib"))
    testCompile(project(":compiler.tests-common"))
    testCompile(project(":compiler:cli"))
    testCompile(project(":compiler:frontend.java"))
    testCompile(project(":idea:idea-test-framework")) { isTransitive = false }
    testCompile(project(":idea", configuration = "tests-jar")) { isTransitive = false }
    testCompile(project(":idea:idea-android", configuration = "tests-jar")) { isTransitive = false }
//    testRuntime(project(":idea")) { isTransitive = false }
    testRuntime(project(":plugins:android-extensions-jps"))
    testRuntime(ideaSdkDeps("*.jar"))
    testRuntime(ideaSdkDeps("*.jar", subdir = "plugins/java-i18n/lib"))
    testRuntime(ideaSdkDeps("*.jar", subdir = "plugins/properties/lib"))
    testRuntime(ideaSdkDeps("*.jar", subdir = "plugins/gradle/lib"))
    testRuntime(ideaSdkDeps("*.jar", subdir = "plugins/junit/lib"))
    testRuntime(ideaSdkDeps("*.jar", subdir = "plugins/IntelliLang/lib"))
    testRuntime(ideaSdkDeps("*.jar", subdir = "plugins/testng/lib"))
    testRuntime(ideaSdkDeps("*.jar", subdir = "plugins/copyright/lib"))
    testRuntime(ideaSdkDeps("*.jar", subdir = "plugins/properties/lib"))
    testRuntime(ideaSdkDeps("*.jar", subdir = "plugins/java-decompiler/lib"))
    testRuntime(ideaSdkDeps("*.jar", subdir = "plugins/Groovy/lib"))
    testRuntime(ideaSdkDeps("*.jar", subdir = "plugins/maven/lib"))
    testRuntime(ideaSdkDeps("*.jar", subdir = "plugins/coverage/lib"))
    testRuntime(ideaSdkDeps("*.jar", subdir = "plugins/android/lib"))
    testRuntime(project(":plugins:sam-with-receiver-ide"))
    testRuntime(project(":plugins:noarg-ide"))
    testRuntime(project(":plugins:allopen-ide"))
}

configureKotlinProjectSourcesDefault()
configureKotlinProjectTestsDefault()

testsJar {}

fixKotlinTaskDependencies()

tasks.withType<Test> {
    workingDir = rootDir
    systemProperty("idea.is.unit.test", "true")
    environment("NO_FS_ROOTS_ACCESS_CHECK", "true")
    ignoreFailures = true
}

val jar: Jar by tasks

ideaPlugin {
    from(jar)
}
