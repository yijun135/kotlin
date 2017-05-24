import org.gradle.jvm.tasks.Jar

apply { plugin("kotlin") }

dependencies {
    compile(project(":plugins:sam-with-receiver-cli"))
    compile(project(":compiler:util"))
    compile(project(":compiler:frontend"))
    compile(project(":compiler:frontend.java"))
    compile(project(":idea:idea-core"))
    compile(project(":idea:idea-android"))
    compile(project(":idea"))
}

configureKotlinProjectSourcesDefault()
configureKotlinProjectNoTests()

fixKotlinTaskDependencies()

val jar: Jar by tasks

ideaPlugin {
    from(jar)
}

