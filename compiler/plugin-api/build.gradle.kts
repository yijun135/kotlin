
apply { plugin("kotlin") }

dependencies {
    compile(ideaSdkCoreDeps("intellij-core"))
    compile(project(":compiler:util"))
    compile(project(":compiler:frontend"))
}

configureKotlinProjectSourcesDefault()
configureKotlinProjectTestsDefault()

fixKotlinTaskDependencies()
