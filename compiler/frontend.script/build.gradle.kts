
apply { plugin("kotlin") }

dependencies {
    val compile by configurations
    compile(project(":compiler:util"))
    compile(project(":compiler:frontend"))
    compile(kotlinDep("reflect"))
}

configureKotlinProjectSourcesDefault()
configureKotlinProjectNoTests()

