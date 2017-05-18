
apply { plugin("kotlin") }

dependencies {
    compile(project(":compiler"))
    compile(ideaSdkCoreDeps(*(rootProject.extra["ideaCoreSdkJars"] as Array<String>)))
    compile(commonDep("org.fusesource.jansi", "jansi"))
    compile(commonDep("jline"))
}

configureKotlinProjectSources("compiler/cli/src",
                              "plugins/annotation-collector/src",
                              "compiler/builtins-serializer/src",
                              sourcesBaseDir = rootDir)
configureKotlinProjectNoTests()

fixKotlinTaskDependencies()
