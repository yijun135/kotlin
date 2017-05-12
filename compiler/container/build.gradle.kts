
apply { plugin("kotlin") }

dependencies {
    compile(project(":core:util.runtime"))
    compile(commonDep("javax.inject"))
    compile(ideaSdkCoreDeps("intellij-core"))
}

configureKotlinProjectSourcesDefault()
configureKotlinProjectTestsDefault()

fixKotlinTaskDependencies()
