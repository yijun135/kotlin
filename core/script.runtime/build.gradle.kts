
import org.gradle.jvm.tasks.Jar

apply { plugin("kotlin") }

dependencies {
    compile(project(":core:builtins"))
    compile(kotlinDep("stdlib"))
    buildVersion()
}

configureKotlinProjectSourcesDefault()
configureKotlinProjectNoTests()

tasks.withType<Jar> {
    setupRuntimeJar("Kotlin Script Runtime")
    archiveName = "kotlin-script-runtime.jar"
}

fixKotlinTaskDependencies()
