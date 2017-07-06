import org.gradle.jvm.tasks.Jar

apply { plugin("kotlin") }

//val testsJarCfg = configurations.create("tests-jar").extendsFrom(configurations["testCompile"])

dependencies {
    compile(kotlinDep("reflect"))
    compile(project(":compiler:util"))
    compile(project(":compiler:light-classes"))
    compile(project(":compiler:frontend"))
    compile(project(":compiler:frontend.java"))
    compile(project(":idea"))
    compile(project(":idea:idea-core"))
    compile(project(":idea:ide-common"))
    compile(ideaSdkDeps("openapi", "idea"))
    compile(ideaSdkDeps("gradle-tooling-api", subdir = "plugins/gradle/lib"))
    compile(ideaSdkDeps("android", "common", "sdk-common", "layoutlib-api", subdir = "plugins/android/lib"))
    compile(preloadedDeps("uast-common", "uast-java"))
    testCompile(project(":compiler.tests-common"))
    testCompile(project(":idea:idea-test-framework")) { isTransitive = false }
    testCompile(project(":idea", configuration = "tests-jar")) { isTransitive = false }
    testCompile(project(":plugins:lint")) { isTransitive = false }
    testCompile(ideaSdkDeps("android-common", "sdklib", subdir = "plugins/android/lib"))
    testCompile(ideaSdkDeps("properties", subdir = "plugins/properties/lib"))
    testRuntime(ideaSdkDeps("*.jar", subdir = "plugins/android/lib"))
    testRuntime(ideaSdkDeps("*.jar", subdir = "plugins/gradle/lib"))
    testRuntime(ideaSdkDeps("*.jar"))
    testRuntime(ideaSdkDeps("*.jar", subdir = "plugins/java-i18n/lib"))
    testRuntime(ideaSdkDeps("*.jar", subdir = "plugins/properties/lib"))
    testRuntime(ideaSdkDeps("*.jar", subdir = "plugins/junit/lib"))
    testRuntime(ideaSdkDeps("*.jar", subdir = "plugins/IntelliLang/lib"))
    testRuntime(ideaSdkDeps("*.jar", subdir = "plugins/testng/lib"))
    testRuntime(ideaSdkDeps("*.jar", subdir = "plugins/copyright/lib"))
    testRuntime(ideaSdkDeps("*.jar", subdir = "plugins/java-decompiler/lib"))
    testRuntime(ideaSdkDeps("*.jar", subdir = "plugins/Groovy/lib"))
    testRuntime(ideaSdkDeps("*.jar", subdir = "plugins/maven/lib"))
    testRuntime(ideaSdkDeps("*.jar", subdir = "plugins/coverage/lib"))
    testRuntime(preloadedDeps("uast-common", "uast-java"))
    testRuntime(project(":plugins:android-extensions-idea"))
    testRuntime(project(":plugins:sam-with-receiver-ide"))
    testRuntime(project(":plugins:noarg-ide"))
    testRuntime(project(":plugins:allopen-ide"))
}

configureKotlinProjectSourcesDefault()
configureKotlinProjectTestsDefault()

tasks.withType<Test> {
    workingDir = rootDir
    systemProperty("idea.is.unit.test", "true")
    environment("NO_FS_ROOTS_ACCESS_CHECK", "true")
    ignoreFailures = true
}

//val testsJar by task<Jar> {
//    dependsOn("testClasses")
//    pluginManager.withPlugin("java") {
//        from(project.the<JavaPluginConvention>().sourceSets.getByName("test").output)
//    }
//    classifier = "tests"
//}
//
//artifacts.add(testsJarCfg.name, testsJar)

testsJar {}

fixKotlinTaskDependencies()
