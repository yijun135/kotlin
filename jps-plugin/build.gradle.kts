
import org.gradle.jvm.tasks.Jar
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.io.File

buildscript {
    repositories {
        mavenLocal()
        maven { setUrl(rootProject.extra["repo"]) }
    }

    dependencies {
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:${rootProject.extra["kotlinVersion"]}")
    }
}

apply { plugin("kotlin") }

repositories {
    mavenLocal()
    maven { setUrl(rootProject.extra["repo"]) }
    mavenCentral()
}

dependencies {
    compile(project(":kotlin-stdlib"))
    compile(project(":build.common"))
    compile(project(":core"))
    compile(project(":compiler"))
    compile(project(":compiler.preloader"))
    compile(project(":daemon.client"))
    buildVersion()
}

configureKotlinProjectSourcesDefault()
configureKotlinProjectNoTests()

tasks.withType<KotlinCompile> {
    kotlinOptions.freeCompilerArgs = listOf("-module-name", "kotlin-idea-jps-plugin")
}

tasks.withType<Jar> {
    setupRuntimeJar("Kotlin Preloader")
    manifest.attributes.put("Main-Class", "org.jetbrains.kotlin.runner.Main")
    manifest.attributes.put("Class-Path", "kotlin-runtime.jar")
    archiveName = "kotlin-runner.jar"
}

fixKotlinTaskDependencies()
