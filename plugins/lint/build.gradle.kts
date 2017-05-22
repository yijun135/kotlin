
apply {
    plugin("kotlin")
    plugin("java")
}

dependencies {
    compile(project(":compiler:frontend"))
    compile(project(":idea"))
    compile(project(":idea:idea-core"))
    compile(project(":idea:idea-android"))
    compile(project(":plugins:uast-kotlin"))
    compile(preloadedDep("uast-common", "uast-java"))
    compile(ideaSdkDeps("android", "common", "sdk-common", "sdklib", "sdk-tools", "repository", "lombok-ast", "kxml2", subdir = "plugins/android/lib"))
    compile(ideaSdkCoreDeps("intellij-core", "util"))
    compile(ideaSdkDeps("guava"))
}

configureKotlinProjectSources("android-annotations/src",
                              "lint-api/src",
                              "lint-checks/src",
                              "lint-idea/src")
configureKotlinProjectNoTests()

fixKotlinTaskDependencies()
