
apply { plugin("kotlin") }

dependencies {
    compile(project(":compiler:util"))
    compile(project(":core"))
}

configureKotlinProjectSourcesDefault()
configureKotlinProjectNoTests()

fixKotlinTaskDependencies()
