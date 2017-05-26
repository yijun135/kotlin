
apply { plugin("kotlin") }

dependencies {
    testCompile(project(":prepare:compiler", configuration = "packed"))
    testCompile(project(":compiler.tests-common"))
    testCompile(project(":compiler:ir.ir2cfg"))
    testCompile(project(":compiler:ir.tree"))
    testCompile(ideaSdkDeps("openapi", "idea", "commons-httpclient-3.1-patched"))
}

configureKotlinProjectSources()
configureKotlinProjectTests("compiler/tests", sourcesBaseDir = rootDir)

val test: Test by tasks
test.apply {
    dependsOnTaskIfExistsRec("dist", project = rootProject)
    dependsOn(":prepare:mock-runtime-for-test:dist")
    workingDir = rootDir
}

fixKotlinTaskDependencies()
