
apply { plugin("kotlin") }

dependencies {
    compile(project(":core"))
    compile(ideaSdkCoreDeps(*(rootProject.extra["ideaCoreSdkJars"] as Array<String>)))
    compile(ideaSdkDeps("jps-model.jar", subdir = "jps"))
}

configureKotlinProjectSourcesDefault()
configureKotlinProjectResources("resources", sourcesBaseDir = rootDir)
configureKotlinProjectNoTests()

fixKotlinTaskDependencies()
