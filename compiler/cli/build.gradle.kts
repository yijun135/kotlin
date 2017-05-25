
apply { plugin("kotlin") }

dependencies {
    compile(project(":compiler:util"))
    compile(project(":compiler:cli-common"))
    compile(project(":compiler:frontend"))
    compile(project(":compiler:frontend.java"))
    compile(project(":compiler:frontend.script"))
    compile(project(":compiler:backend-common"))
    compile(project(":compiler:backend"))
    compile(project(":compiler:light-classes"))
    compile(project(":compiler:serialization"))
    compile(project(":compiler:plugin-api"))
    compile(project(":js:js.translator"))
    compile(project(":js:js.serializer"))
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
