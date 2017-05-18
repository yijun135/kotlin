import java.util.*
import java.io.File
import org.gradle.api.tasks.bundling.Jar
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.script.lang.kotlin.java
import org.gradle.api.plugins.JavaPluginConvention
import org.gradle.api.internal.HasConvention
import org.gradle.api.tasks.testing.Test
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jetbrains.kotlin.gradle.tasks.Kotlin2JsCompile

buildscript {
    extra["kotlin_version"] = project.properties["deployVersion"] ?: "1.1-SNAPSHOT"
    extra["kotlinVersion"] = extra["kotlin_version"]
    extra["kotlin_language_version"] = "1.1"
    extra["kotlin_gradle_plugin_version"] = "1.1-SNAPSHOT"
    extra["repo"] = "https://repo.gradle.org/gradle/repo"

    repositories {
        mavenLocal()
        maven { setUrl(rootProject.extra["repo"]) }
    }

    dependencies {
        classpath(kotlinDep("gradle-plugin"))
    }
}

plugins {
    java // so we can benefit from the `java()` accessor below
}

extra["build.number"] = "1.1-SNAPSHOT"

extra["kotlin_root"] = rootDir

val bootstrapCfg = configurations.create("bootstrap")
val scriptCompileCfg = configurations.create("scriptCompile")
val scriptRuntimeCfg = configurations.create("scriptRuntime").extendsFrom(scriptCompileCfg)

dependencies {
    bootstrapCfg(kotlinDep("compiler-embeddable"))
    scriptCompileCfg(kotlinDep("compiler-embeddable"))
}

val distDir = "$rootDir/build/dist"
val distLibDir = "$distDir/kotlinc/lib"

extra["distDir"] = distDir
extra["distLibDir"] = project.file(distLibDir)
extra["libsDir"] = project.file(distLibDir)

Properties().apply {
    load(File("$rootDir/resources/kotlinManifest.properties").reader())
    forEach {
        val key = it.key
        if (key != null && key is String)
            extra[key] = it.value
    }
}

extra["JDK_16"] = jdkPath("1.6")
extra["JDK_17"] = jdkPath("1.7")
extra["JDK_18"] = jdkPath("1.8")

extra["compilerJar"] = project.file("$distLibDir/kotlin-compiler.jar")
extra["embeddableCompilerJar"] = project.file("$distLibDir/kotlin-compiler-embeddable.jar")
extra["compilerJarWithBootstrapRuntime"] = project.file("$distDir/kotlin-compiler-with-bootstrap-runtime.jar")
extra["bootstrapCompilerFile"] = bootstrapCfg.files.first().canonicalPath

extra["versions.protobuf-java"] = "2.6.1"
extra["versions.javax.inject"] = "1"
extra["versions.jsr305"] = "1.3.9"
extra["versions.cli-parser"] = "1.1.2"
extra["versions.jansi"] = "1.11"
extra["versions.jline"] = "2.12.1"
extra["versions.junit"] = "4.12"
extra["versions.javaslang"] = "2.0.6"
extra["versions.ant"] = "1.8.2"

extra["ideaCoreSdkJars"] = arrayOf("annotations", "asm-all", "guava", "intellij-core", "jdom", "jna", "log4j", "picocontainer",
                                   "snappy-in-java", "trove4j", "xpp3-1.1.4-min", "xstream")

applyFrom("libraries/commonConfiguration.gradle")

val importedAntTasksPrefix = "imported-ant-update-"

// TODO: check the reasons of import conflict with xerces
//ant.importBuild("$rootDir/update_dependencies.xml") { antTaskName -> importedAntTasksPrefix + antTaskName }

tasks.matching { task ->
    task.name.startsWith(importedAntTasksPrefix)
}.forEach {
    it.group = "Imported ant"
}

//task("update-dependencies") {
//    dependsOn(tasks.getByName(importedAntTasksPrefix + "update"))
//}

val prepareBootstrapTask = task("prepareBootstrap") {
    dependsOn(bootstrapCfg, scriptCompileCfg, scriptRuntimeCfg)
}

fun Project.allprojectsRecursive(body: Project.() -> Unit) {
    this.body()
    this.subprojects { allprojectsRecursive(body) }
}

allprojects {

    setBuildDir("$rootDir/build/${project.name}")

    repositories {
        mavenLocal()
        maven { setUrl(rootProject.extra["repo"]) }
        mavenCentral()
    }

    tasks.withType<KotlinCompile> {
        dependsOn(prepareBootstrapTask)
        compilerJarFile = File(rootProject.extra["bootstrapCompilerFile"].toString())
        kotlinOptions.freeCompilerArgs = listOf("-Xallow-kotlin-package", "-module-name", project.name)
    }

    tasks.withType<Kotlin2JsCompile> {
        dependsOn(prepareBootstrapTask)
        compilerJarFile = File(rootProject.extra["bootstrapCompilerFile"].toString())
        kotlinOptions.freeCompilerArgs = listOf("-Xallow-kotlin-package", "-module-name", project.name)
    }

    task<Jar>("javadocJar") {
        classifier = "javadoc"
    }
}

//task("preparePublication") {
//    val repositoryProviders = mapOf("sonatype-nexus-staging" to "sonatype", "sonatype-nexus-snapshots" to "sonatype")
//
//    val isRelease = !project.version.toString().contains("-SNAPSHOT")
//    project.extra["isRelease"] = isRelease
//
//    val repo = properties["deploy-repo"] as? String
//    val repoProvider = repo?.let { repositoryProviders.getOrDefault(it, it) }
//    project.extra["isSonatypePublish"] = repoProvider == "sonatype"
//    val isSonatypePublish = repoProvider == "sonatype"
//    project.extra["isSonatypePublish"] = isSonatypePublish
//    project.extra["isSonatypeRelease"] = isSonatypePublish && isRelease
//
//    project.extra["signing.keyId"] = properties["kotlin.key.name"]
//    project.extra["signing.password"] = properties["kotlin.key.passphrase"]
//
//    val sonatypeSnapshotsUrl = if (isSonatypePublish && !isRelease) "https://oss.sonatype.org/content/repositories/snapshots/" else null
//
//    extra["repoUrl"] = properties["deployRepoUrl"] ?: sonatypeSnapshotsUrl ?: properties["deploy-url"] ?: "file://${rootProject.buildDir}/repo"
//    extra["username"] = properties["deployRepoUsername"] ?: properties["kotlin.${repoProvider}.user"]
//    extra["password"] = properties["deployRepoPassword"] ?: properties["kotlin.${repoProvider}.password"]
//
//    doLast {
//        println("Deployment respository url: ${extra["repoUrl"]}")
//    }
//}

fun jdkPath(version: String): String {
    val varName = "JDK_${version.replace(".", "")}"
    return System.getenv(varName) ?: throw GradleException ("Please set environment variable $varName to point to JDK $version installation")
}
