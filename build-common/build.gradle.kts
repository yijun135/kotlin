
import org.gradle.jvm.tasks.Jar
import java.io.File

apply { plugin("kotlin") }

dependencies {
    compile(project(":compiler"))
    compile(ideaSdkDeps("util"))
    buildVersion()
}

configure<JavaPluginConvention> {
    sourceSets.getByName("main").apply {
        java.setSrcDirs(listOf(File(projectDir, "src")))
    }
    sourceSets.getByName("test").apply {
        java.setSrcDirs(emptyList<File>())
    }
}

tasks.withType<Jar> {
    setupRuntimeJar("Kotlin Build Common")
    archiveName = "kotlin-build-common.jar"
}

fixKotlinTaskDependencies()
