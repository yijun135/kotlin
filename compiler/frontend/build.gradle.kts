
apply { plugin("kotlin") }

dependencies {
    compile(project(":core"))
    compile(project(":compiler:util"))
    compile(project(":compiler:container"))
    compile(project(":compiler:resolution"))
    compile(kotlinDep("script-runtime"))
    compile(commonDep("io.javaslang","javaslang"))
}

configureKotlinProjectSourcesDefault()
configureKotlinProjectNoTests()

fixKotlinTaskDependencies()
