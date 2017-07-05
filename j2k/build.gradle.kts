
apply { plugin("kotlin") }

dependencies {
    compile(project(":kotlin-stdlib"))
    compile(project(":compiler:frontend"))
    compile(project(":compiler:frontend.java"))
    compile(project(":compiler:light-classes"))
    compile(project(":compiler:util"))
    compile(ideaSdkCoreDeps("intellij-core", "util"))
    buildVersion()
//    testCompile(project(":compiler.tests-common"))
//    testCompile(project(":idea:idea-test-framework"))
//    testRuntime(project(":idea"))
//    testRuntime(project(":plugins:android-extensions-idea"))
//    testRuntime(project(":plugins:allopen-ide"))
//    testRuntime(project(":plugins:noarg-ide"))
//    testRuntime(project(":plugins:sam-with-receiver-ide"))
//    testRuntime(ideaSdkDeps("*.jar"))
//    testRuntime(ideaSdkDeps("*.jar", subdir = "plugins/java-i18n/lib"))
//    testRuntime(ideaSdkDeps("*.jar", subdir = "plugins/properties/lib"))
//    testRuntime(ideaSdkDeps("*.jar", subdir = "plugins/gradle/lib"))
//    testRuntime(ideaSdkDeps("*.jar", subdir = "plugins/junit/lib"))
//    testRuntime(ideaSdkDeps("*.jar", subdir = "plugins/IntelliLang/lib"))
//    testRuntime(ideaSdkDeps("*.jar", subdir = "plugins/testng/lib"))
//    testRuntime(ideaSdkDeps("*.jar", subdir = "plugins/copyright/lib"))
//    testRuntime(ideaSdkDeps("*.jar", subdir = "plugins/properties/lib"))
//    testRuntime(ideaSdkDeps("*.jar", subdir = "plugins/java-decompiler/lib"))
//    testRuntime(ideaSdkDeps("*.jar", subdir = "plugins/Groovy/lib"))
//    testRuntime(ideaSdkDeps("*.jar", subdir = "plugins/maven/lib"))
//    testRuntime(ideaSdkDeps("*.jar", subdir = "plugins/coverage/lib"))
//    testRuntime(ideaSdkDeps("*.jar", subdir = "plugins/android/lib"))
}

configureKotlinProjectSourcesDefault()
configureKotlinProjectNoTests()
//configureKotlinProjectTestsDefault()
//
//tasks.withType<Test> {
//    workingDir = rootDir
//    systemProperty("idea.is.unit.test", "true")
//    environment("NO_FS_ROOTS_ACCESS_CHECK", "true")
//    ignoreFailures = true
//}

fixKotlinTaskDependencies()
