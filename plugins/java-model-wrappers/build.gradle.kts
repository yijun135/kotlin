
apply { plugin("kotlin") }

dependencies {
    val compile by configurations
    compile(project(":compiler:util"))
    compile(project(":compiler:light-classes"))
    compile(project(":compiler:frontend"))
    compile(ideaSdkCoreDeps("intellij-core"))
}

configureKotlinProjectSourcesDefault()
configureKotlinProjectNoTests()

