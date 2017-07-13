
apply {
    plugin("java")
    plugin("kotlin")
}

dependencies {
    compile(project(":core:builtins"))
    compile(kotlinDep("stdlib"))
}

configureKotlinProjectSourcesDefault()
configureKotlinProjectNoTests()

