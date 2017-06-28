
apply { plugin("kotlin") }

dependencies {
    compile(project(":compiler:cli"))
    compile(project(":compiler:daemon-common"))
    compile(project(":compiler:incremental-compilation-impl"))
    compile(project(":build-common"))
    compile(ideaSdkCoreDeps(*(rootProject.extra["ideaCoreSdkJars"] as Array<String>)))
    compile(commonDep("org.fusesource.jansi", "jansi"))
    compile(commonDep("jline"))
}

configureKotlinProjectSources(
        "compiler/daemon/src",
        "compiler/conditional-preprocessor/src",
        sourcesBaseDir = rootDir)
configureKotlinProjectResources("idea/src", sourcesBaseDir = rootDir) {
    include("META-INF/extensions/common.xml",
            "META-INF/extensions/kotlin2jvm.xml",
            "META-INF/extensions/kotlin2js.xml")
}
configureKotlinProjectNoTests()

fixKotlinTaskDependencies()
