
import org.gradle.jvm.tasks.Jar

apply { plugin("kotlin") }

dependencies {
    compile(commonDep("org.apache.ant", "ant"))
    compile(project(":compiler:preloader"))
    compile(kotlinDep("stdlib"))
    buildVersion()
}

configureKotlinProjectSourcesDefault()
configureKotlinProjectNoTests()

val jar: Jar by tasks
jar.apply {
    setupRuntimeJar("Kotlin Ant Tools")
    archiveName = "kotlin-ant.jar"
}

dist {
    from(jar)
}

fixKotlinTaskDependencies()
