
import org.gradle.jvm.tasks.Jar

apply { plugin("kotlin") }

dependencies {
    compile(project(":core:util.runtime"))
    compile(project(":compiler:util"))
    compile(project(":compiler:cli-common"))
    compile(project(":compiler:frontend.java"))
    compile(ideaSdkDeps("util"))
    buildVersion()
    testCompile(commonDep("junit:junit"))
    testCompile(project(":compiler.tests-common"))
    testCompile(protobufFull())
}

val testsJarCfg = configurations.create("tests-jar").extendsFrom(configurations["testCompile"])

configureKotlinProjectSourcesDefault()
configureKotlinProjectTestsDefault()

val jar: Jar by tasks
jar.apply {
    setupRuntimeJar("Kotlin Build Common")
    baseName = "kotlin-build-common"
}

val testsJar by task<Jar> {
    dependsOn("testClasses")
    pluginManager.withPlugin("java") {
        from(project.the<JavaPluginConvention>().sourceSets.getByName("test").output)
    }
    classifier = "tests"
}

artifacts.add(testsJarCfg.name, testsJar)

tasks.withType<Test> {
    workingDir = rootDir
    systemProperty("idea.is.unit.test", "true")
    systemProperty("NO_FS_ROOTS_ACCESS_CHECK", "true")
    ignoreFailures = true
}

fixKotlinTaskDependencies()
