
apply { plugin("kotlin") }

dependencies {
    testCompile(project(":compiler:util"))
    testCompile(project(":compiler:backend"))
    testCompile(project(":compiler:cli"))
    testCompile(project(":compiler.tests-common"))
    testCompile(project(":plugins:java-model-wrappers"))
    testCompile(project(":plugins:annotation-processing"))
    testCompile(project(":plugins:android-extensions-compiler"))
    testCompile(project(":plugins:android-extensions-idea"))
    testCompile(project(":plugins:allopen-ide")) { isTransitive = false }
    testCompile(project(":plugins:allopen-cli"))
    testCompile(project(":plugins:noarg-ide")) { isTransitive = false }
    testCompile(project(":plugins:noarg-cli"))
    testCompile(project(":plugins:annotation-based-compiler-plugins-ide-support")) { isTransitive = false }
    testCompile(project(":plugins:sam-with-receiver-ide")) { isTransitive = false }
    testCompile(project(":plugins:sam-with-receiver-cli"))
    testCompile(project(":idea:idea-android")) { isTransitive = false }
    testCompile(project(":plugins:lint")) { isTransitive = false }
    testCompile(project(":plugins:uast-kotlin"))
    testCompile(ideaSdkDeps("jps-build-test", subdir = "jps/test"))
    testCompile(ideaSdkDeps("*.jar", subdir = "plugins/android/lib/jps"))
    testCompile(project(":jps-plugin", configuration = "tests-jar"))
    testRuntime(project(":jps-plugin"))
    testRuntime(ideaSdkDeps("*.jar"))
    testRuntime(ideaSdkDeps("*.jar", subdir = "plugins/gradle/lib"))
    testRuntime(ideaSdkDeps("*.jar", subdir = "plugins/junit/lib"))
    testRuntime(ideaSdkDeps("*.jar", subdir = "plugins/android/lib"))
}

configureKotlinProjectSources()
configureKotlinProjectTestsDefault()

val test: Test by tasks
test.apply {
    dependsOnTaskIfExistsRec("dist", project = rootProject)
    workingDir = rootDir
    systemProperty("idea.is.unit.test", "true")
    environment("NO_FS_ROOTS_ACCESS_CHECK", "true")
    ignoreFailures = true
}

fixKotlinTaskDependencies()
