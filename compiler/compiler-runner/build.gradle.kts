
import org.gradle.jvm.tasks.Jar

apply { plugin("kotlin") }

dependencies {
    compile(kotlinDep("stdlib"))
    buildVersion()
}

configureKotlinProjectSources("compiler/cli/cli-runner/src", sourcesBaseDir = rootDir)
configureKotlinProjectNoTests()

tasks.withType<Jar> {
    setupRuntimeJar("Kotlin Preloader")
    manifest.attributes.put("Main-Class", "org.jetbrains.kotlin.runner.Main")
    manifest.attributes.put("Class-Path", "kotlin-runtime.jar")
    archiveName = "kotlin-runner.jar"
}

fixKotlinTaskDependencies()
