
apply { plugin("kotlin") }

dependencies {
    compile(project(":compiler:cli"))
    compile(project(":compiler:daemon-common"))
    compile(project(":build-common"))
    compile(ideaSdkCoreDeps(*(rootProject.extra["ideaCoreSdkJars"] as Array<String>)))
    compile(commonDep("org.fusesource.jansi", "jansi"))
    compile(commonDep("jline"))
    testCompile(project(":compiler.tests-common"))
    testCompile(project(":compiler:ir.ir2cfg"))
    testCompile(ideaSdkDeps("openapi", "idea", "commons-httpclient-3.1-patched"))
}

configureKotlinProjectSources(
        "compiler/daemon/src",
        "compiler/conditional-preprocessor/src",
        "compiler/incremental-compilation-impl/src",
        sourcesBaseDir = rootDir)
configureKotlinProjectNoTests()

fixKotlinTaskDependencies()
