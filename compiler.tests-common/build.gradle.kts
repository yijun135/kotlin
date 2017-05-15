
apply { plugin("kotlin") }

dependencies {
    compile(project(":core"))
    compile(project(":compiler"))
    compile(project(":compiler.standalone"))
    compile(commonDep("junit:junit"))
    compile(kotlinDep("test"))
    compile(ideaSdkDeps("idea", "idea_rt", "openapi"))
    compile(ideaSdkCoreDeps(*(rootProject.extra["ideaCoreSdkJars"] as Array<String>)))
}

configureKotlinProjectSources("tests-common", sourcesBaseDir = File(rootDir, "compiler"))
configureKotlinProjectNoTests()

fixKotlinTaskDependencies()
