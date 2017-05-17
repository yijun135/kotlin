
apply { plugin("kotlin") }

dependencies {
    compile(ideaSdkCoreDeps("intellij-core"))
    compile(project(":compiler:util"))
    compile(project(":compiler:plugin-api"))
    compile(project(":compiler:frontend"))
    compile(project(":compiler:frontend.java"))
}

configureKotlinProjectSourcesDefault()
configureKotlinProjectNoTests()

fixKotlinTaskDependencies()
