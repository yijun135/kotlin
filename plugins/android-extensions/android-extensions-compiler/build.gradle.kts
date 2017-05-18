
import org.gradle.jvm.tasks.Jar

apply { plugin("kotlin") }

dependencies {
    compile(ideaSdkCoreDeps("intellij-core"))
    compile(project(":compiler:util"))
    compile(project(":compiler:plugin-api"))
    compile(project(":compiler:frontend"))
    compile(project(":compiler:frontend.java"))
    compile(project(":compiler:backend"))
}

configureKotlinProjectSourcesDefault()
configureKotlinProjectNoTests()

val jar: Jar by tasks
jar.apply {
    setupRuntimeJar("Kotlin Android Extensions Compiler")
}

dist {
    from(jar)
}

fixKotlinTaskDependencies()
