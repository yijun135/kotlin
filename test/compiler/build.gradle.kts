
apply { plugin("kotlin") }

dependencies {
    testRuntime(ideaSdkCoreDeps("*.jar"))
    testRuntime(ideaSdkDeps("*.jar"))
    testCompile(project(":compiler:tests-common"))
    testCompileOnly(project(":compiler:ir.ir2cfg"))
    testCompileOnly(project(":compiler:ir.tree")) // used for deepCopyWithSymbols call that is removed by proguard from the compiler TODO: make it more straightforward
    testCompile(ideaSdkDeps("openapi", "idea", "util", "asm-all", "commons-httpclient-3.1-patched"))
    testRuntime(project(":prepare:compiler", configuration = "default"))
    testRuntime(project(":plugins:android-extensions-compiler"))
    testRuntime(project(":ant"))
    testRuntime(project(":kotlin-stdlib"))
    testRuntime(project(":kotlin-script-runtime"))
    testRuntime(project(":kotlin-runtime"))
    testRuntime(project(":kotlin-reflect"))
}

configureKotlinProjectSources()
configureKotlinProjectTests("compiler/tests", sourcesBaseDir = rootDir)

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
