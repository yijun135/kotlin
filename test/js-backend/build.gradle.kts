
apply { plugin("kotlin") }

dependencies {
    testCompile(project(":compiler.tests-common"))
    testCompile(project(":compiler:frontend"))
    testCompile(project(":compiler:cli"))
    testCompileOnly(project(":compiler:util"))
    testCompile(project(":js:js.translator"))
    testCompile(project(":js:js.serializer"))
    testCompile(ideaSdkDeps("openapi", "idea"))
    testRuntime(project(":prepare:compiler", configuration = "default"))
    testRuntime(project(":kotlin-stdlib"))
    testRuntime(project(":compiler:backend-common"))
    testRuntime(ideaSdkDeps("*.jar"))
}

configureKotlinProjectSources()
configureKotlinProjectTests("js/js.tests/test", sourcesBaseDir = rootDir)

val test: Test by tasks
test.apply {
    dependsOnTaskIfExistsRec("dist", project = rootProject)
    dependsOn(":prepare:mock-runtime-for-test:dist")
    dependsOn(":prepare:compiler:prepare")
    workingDir = rootDir
    systemProperty("kotlin.test.script.classpath", the<JavaPluginConvention>().sourceSets.getByName("test").output.classesDirs.joinToString(File.pathSeparator))
    ignoreFailures = true
}

fixKotlinTaskDependencies()
