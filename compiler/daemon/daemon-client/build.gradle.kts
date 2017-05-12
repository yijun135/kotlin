
import org.gradle.jvm.tasks.Jar

apply { plugin("kotlin") }

val nativePlatformUberjar = "$rootDir/dependencies/native-platform-uberjar.jar"

dependencies {
    compile(project(":compiler"))
    compile(files(nativePlatformUberjar))
    buildVersion()
}

configureKotlinProjectSources("compiler/daemon/daemon-client/src", sourcesBaseDir = rootDir)
configureKotlinProjectNoTests()

tasks.withType<Jar> {
    setupRuntimeJar("Kotlin Daemon Client")
    from(zipTree(nativePlatformUberjar))
    archiveName = "kotlin-daemon-client.jar"
}

fixKotlinTaskDependencies()
