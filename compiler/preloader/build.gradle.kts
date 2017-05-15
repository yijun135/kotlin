
import org.gradle.jvm.tasks.Jar

apply { plugin("kotlin") }

dependencies {
    compile(ideaSdkDeps("asm-all"))
    buildVersion()
}

configureKotlinProjectSources("src", "instrumentation/src")
configureKotlinProjectNoTests()

tasks.withType<Jar> {
    setupRuntimeJar("Kotlin Preloader")
    manifest.attributes.put("Main-Class", "org.jetbrains.kotlin.preloading.Preloader")
    archiveName = "kotlin-preloader.jar"
}

fixKotlinTaskDependencies()
