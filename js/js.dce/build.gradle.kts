
apply { plugin("kotlin") }

dependencies {
    compile(project(":compiler:util"))
    compile(project(":js:js.ast"))
    compile(project(":js:js.translator"))
    compile(ideaSdkCoreDeps("intellij-core"))
}

configureKotlinProjectSourcesDefault()
configureKotlinProjectNoTests()

