
import org.gradle.jvm.tasks.Jar

apply { plugin("kotlin") }

dependencies {
    compile(project(":compiler:frontend"))
    compile(project(":compiler:frontend.script"))
    compile(project(":compiler:plugin-api"))
}

configureKotlinProjectSourcesDefault()
configureKotlinProjectNoTests()

val jar: Jar by tasks
jar.apply {
    setupRuntimeJar("Kotlin SourceSections Compiler Plugin")
    from(fileTree("$projectDir/src")) { include("META-INF/**") }
    archiveName = "kotlin-source-sections-compiler-plugin.jar"
}

dist {
    from(jar)
}

fixKotlinTaskDependencies()
