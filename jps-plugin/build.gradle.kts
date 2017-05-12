
import org.gradle.jvm.tasks.Jar

apply { plugin("kotlin") }

dependencies {
    compile(project(":kotlin-stdlib"))
    compile(project(":build.common"))
    compile(project(":core"))
    compile(project(":compiler"))
    compile(project(":compiler:preloader"))
    compile(project(":daemon.client"))
    buildVersion()
}

configureKotlinProjectSourcesDefault()
configureKotlinProjectNoTests()

tasks.withType<Jar> {
    setupRuntimeJar("Kotlin Preloader")
    manifest.attributes.put("Main-Class", "org.jetbrains.kotlin.runner.Main")
    manifest.attributes.put("Class-Path", "kotlin-runtime.jar")
    archiveName = "kotlin-runner.jar"
}

fixKotlinTaskDependencies()
