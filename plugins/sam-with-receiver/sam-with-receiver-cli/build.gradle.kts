
import org.gradle.jvm.tasks.Jar

apply { plugin("kotlin") }

dependencies {
    compile(project(":compiler:frontend"))
    compile(project(":compiler:frontend.java"))
    compile(project(":compiler:plugin-api"))
}

configureKotlinProjectSourcesDefault()
configureKotlinProjectNoTests()

val jar: Jar by tasks
jar.apply {
    setupRuntimeJar("Kotlin SamWithReceiver Compiler Plugin")
    from(fileTree("$projectDir/src")) { include("META-INF/**") }
    archiveName = "sam-with-receiver-compiler-plugin.jar"
}

dist {
    from(jar)
}

ideaPlugin {
    from(jar)
}

fixKotlinTaskDependencies()
