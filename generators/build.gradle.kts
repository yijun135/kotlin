
apply {
    plugin("kotlin")
}

dependencies {
    compile(project(":core"))
    compile(project(":idea"))
    compile(project(":compiler:util"))
    compile(project(":compiler:cli"))
    compile(project(":compiler:backend"))
    compile(project(":compiler:frontend"))
    compile(project(":compiler:frontend.java"))
    compile(project(":compiler:backend"))
    compile(project(":js:js.ast"))
    compile(project(":js:js.frontend"))
    compile(project(":idea:idea-test-framework"))
    compile(project(":build-common", configuration = "tests-jar")) { isTransitive = false }
    compile(project(":compiler", configuration = "tests-jar")) { isTransitive = false }
    compile(project(":compiler:tests-java8", configuration = "tests-jar")) { isTransitive = false }
    compile(project(":compiler:container", configuration = "tests-jar")) { isTransitive = false }
    compile(project(":idea", configuration = "tests-jar")) { isTransitive = false }
    compile(project(":idea:idea-android", configuration = "tests-jar")) { isTransitive = false }
    compile(project(":jps-plugin", configuration = "tests-jar")) { isTransitive = false }
    compile(project(":plugins:plugins-tests", configuration = "tests-jar")) { isTransitive = false }
    compile(project(":plugins:android-extensions-idea", configuration = "tests-jar")) { isTransitive = false }
    compile(project(":plugins:kapt3", configuration = "tests-jar")) { isTransitive = false }
    compile(project(":plugins:uast-kotlin", configuration = "tests-jar")) { isTransitive = false }
    compile(project(":js:js.tests", configuration = "tests-jar")) { isTransitive = false }
    compile(project(":kotlin-test:kotlin-test-jvm"))
    compile(protobufFull())
    compileOnly(ideaSdkDeps("jps-build-test", subdir = "jps/test"))
    testCompile(project(":compiler.tests-common"))
    testCompile(project(":idea:idea-test-framework")) { isTransitive = false }
    testCompile(project(":compiler:incremental-compilation-impl"))
    testCompile(project(":plugins:kapt3"))
    testCompile(commonDep("junit:junit"))
    testCompile(ideaSdkDeps("openapi", "idea"))
    testCompile(preloadedDeps("uast-tests"))
    testRuntime(ideaSdkDeps("*.jar"))
    testRuntime(ideaPluginDeps("idea-junit", "resources_en", plugin = "junit"))
    testRuntime(ideaPluginDeps("IntelliLang", plugin = "IntelliLang"))
    testRuntime(ideaPluginDeps("jcommander", "testng", "testng-plugin", "resources_en", plugin = "testng"))
    testRuntime(ideaPluginDeps("copyright", plugin = "copyright"))
    testRuntime(ideaPluginDeps("properties", "resources_en", plugin = "properties"))
    testRuntime(ideaPluginDeps("java-i18n", plugin = "java-i18n"))
    testRuntime(ideaPluginDeps("*.jar", plugin = "gradle"))
    testRuntime(ideaPluginDeps("*.jar", plugin = "Groovy"))
    testRuntime(ideaPluginDeps("coverage", "jacocoant", plugin = "coverage"))
    testRuntime(ideaPluginDeps("java-decompiler", plugin = "java-decompiler"))
    testRuntime(ideaPluginDeps("*.jar", plugin = "maven"))
    testRuntime(ideaPluginDeps("*.jar", plugin = "android"))
}

configureKotlinProjectSourcesDefault()
configureKotlinProjectTestsDefault()

fixKotlinTaskDependencies()

tasks.withType<Test> {
    workingDir = rootDir
    systemProperty("idea.is.unit.test", "true")
    systemProperty("NO_FS_ROOTS_ACCESS_CHECK", "true")
    ignoreFailures = true
}
