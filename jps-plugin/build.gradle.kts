
apply {
    plugin("kotlin")
}

//val shadowContentsCfg = configurations.create("shadowContents")

val projectsToShadow = listOf(
        //android-extensions-jps
           ":build-common",
           ":compiler:cli-common",
           ":compiler:compiler-runner",
           ":compiler:daemon-client",
           ":compiler:daemon-common",
           ":core",
           ":idea:idea-jps-common",
           ":compiler:preloader",
           ":compiler:util",
           ":core:util.runtime")

dependencies {
    compile(project(":build-common"))
    compile(project(":core"))
    compile(project(":compiler:compiler-runner"))
    compile(project(":compiler:daemon-common"))
    compile(project(":compiler:daemon-client"))
    compile(project(":compiler:frontend.java"))
    compile(project(":compiler:preloader"))
    compile(project(":idea:idea-jps-common"))
    compile(ideaSdkDeps("jps-builders", "jps-builders-6", subdir = "jps"))
    buildVersion()
//    shadowContentsCfg(files("$rootDir/dependencies/native-platform-uberjar.jar"))
//    projectsToShadow.forEach {
//        shadowContentsCfg(projectDepIntransitive(it))
//    }
//    testCompile(project(":compiler.tests-common"))
//    testCompile(ideaSdkDeps("idea"))
//    testCompile(ideaSdkDeps("jps-build-test", subdir = "jps/test"))
//    testCompile(commonDep("junit:junit"))
}

configureKotlinProjectSourcesDefault()
configureKotlinProjectNoTests()
//configureKotlinProjectTests("test", sourcesBaseDir = File(projectDir, "jps-tests"))
//configureKotlinProjectTestResources("testData")

ideaPlugin("lib/jps") {
    from("jar")
}

fixKotlinTaskDependencies()
