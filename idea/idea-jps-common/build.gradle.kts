
apply { plugin("kotlin") }

dependencies {
    compile(project(":kotlin-stdlib"))
    compile(project(":core:reflection.jvm"))
    compile(project(":compiler"))
    compile(ideaSdkCoreDeps("intellij-core", "util"))
    buildVersion()
}

configureKotlinProjectSourcesDefault()
configureKotlinProjectNoTests()

fixKotlinTaskDependencies()
