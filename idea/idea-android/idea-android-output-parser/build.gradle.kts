
apply { plugin("kotlin") }

dependencies {
    compile(project(":compiler:util"))
    compile(ideaSdkCoreDeps("intellij-core"))
    compile(ideaSdkDeps("gradle-tooling-api", subdir = "plugins/gradle/lib"))
    compile(ideaSdkDeps("android", "common", "sdk-common", subdir = "plugins/android/lib"))
}

configureKotlinProjectSourcesDefault()
configureKotlinProjectNoTests()

fixKotlinTaskDependencies()
