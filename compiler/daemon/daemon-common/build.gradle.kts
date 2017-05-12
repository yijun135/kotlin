
apply { plugin("kotlin") }

dependencies {
    compile(project(":core:util.runtime"))
    compile(project(":compiler.util"))
    compile(ideaSdkCoreDeps(*(rootProject.extra["ideaCoreSdkJars"] as Array<String>)))
}

configureKotlinProjectSourcesDefault()
configureKotlinProjectNoTests()

fixKotlinTaskDependencies()
