
apply { plugin("kotlin") }

dependencies {
    compile(project(":compiler:util"))
    compile(project(":js:js.ast"))
    compile(ideaSdkCoreDeps("intellij-core"))
}

configureKotlinProjectSourcesDefault()
configureKotlinProjectNoTests()

fixKotlinTaskDependencies()
