
import org.gradle.jvm.tasks.Jar

apply {
    plugin("kotlin")
}

//val shadowContentsCfg = configurations.create("shadowContents")

val projectsToShadow = listOf(
        //android-extensions-jps
           ":build-common",
           ":compiler:cli.cli-common",
           ":compiler:compiler-runner",
           ":compiler:daemon.daemon-client",
           ":compiler:daemon.daemon-common",
           ":core",
           ":idea:idea-jps-common",
           ":compiler:preloader",
           ":compiler:util",
           ":core:util.runtime")

dependencies {
    compile(project(":build-common"))
    compile(project(":core"))
    compile(project(":compiler:compiler-runner"))
    compile(project(":compiler:daemon.daemon-common"))
    compile(project(":compiler:daemon.daemon-client"))
    compile(project(":compiler:frontend.java"))
    compile(project(":compiler:preloader"))
    compile(project(":idea:idea-jps-common"))
    compile(ideaSdkDeps("jps-builders", "jps-builders-6", subdir = "jps"))
    buildVersion()
//    shadowContentsCfg(files("$rootDir/dependencies/native-platform-uberjar.jar"))
//    projectsToShadow.forEach {
//        shadowContentsCfg(projectDepIntransitive(it))
//    }
//    testCompile(project(":compiler.tests-common"))
//    testCompile(ideaSdkDeps("idea"))
//    testCompile(ideaSdkDeps("jps-build-test", subdir = "jps/test"))
//    testCompile(commonDep("junit:junit"))
}

configureKotlinProjectSourcesDefault()
configureKotlinProjectNoTests()
//configureKotlinProjectTests("test", sourcesBaseDir = File(projectDir, "jps-tests"))
//configureKotlinProjectTestResources("testData")

tasks.withType<Jar> {
    setupRuntimeJar("Kotlin JPS plugin")
    manifest.attributes.put("Main-Class", "org.jetbrains.kotlin.runner.Main")
    manifest.attributes.put("Class-Path", "kotlin-runtime.jar")
    archiveName = "kotlin-jps-plugin.jar"
    projectsToShadow.forEach {
        dependsOn("$it:classes")
        project(it).let { p ->
            p.pluginManager.withPlugin("java") {
                from(p.the<JavaPluginConvention>().sourceSets.getByName("main").output)
            }
        }
    }
    from(fileTree("$projectDir/src")) { include("META-INF/**") }
    from(files("$rootDir/resources/kotlinManifest.properties"))
    from(zipTree("$rootDir/dependencies/native-platform-uberjar.jar"))
}

fixKotlinTaskDependencies()
