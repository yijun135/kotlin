
import org.gradle.jvm.tasks.Jar

apply { plugin("kotlin") }

dependencies {
    buildVersion()
}

configureKotlinProjectSources("compiler/preloader/src", sourcesBaseDir = rootDir)
configureKotlinProjectNoTests()

tasks.withType<Jar> {
    setupRuntimeJar("Kotlin Preloader")
    manifest.attributes.put("Main-Class", "org.jetbrains.kotlin.preloading.Preloader")
    archiveName = "kotlin-preloader.jar"
}

fixKotlinTaskDependencies()
