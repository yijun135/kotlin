
apply { plugin("kotlin") }

dependencies {
    compile(project(":kotlin-stdlib"))
    compile(project(":core:util.runtime"))
    compile(project(":compiler:backend"))
    compile(project(":compiler:frontend"))
    compile(project(":compiler:frontend.java"))
    compile(project(":compiler:light-classes"))
    compile(preloadedDeps("uast-common", "uast-java"))
    buildVersion()
    testCompile(project(":compiler.tests-common"))
    testCompile(commonDep("junit:junit"))
    testCompile(project(":compiler:util"))
    testCompile(project(":compiler:cli"))
    testCompile(project(":idea:idea-android"))
    testCompile(preloadedDeps("uast-tests"))
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
