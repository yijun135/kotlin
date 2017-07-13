
apply { plugin("kotlin") }

dependencies {
    compile(project(":compiler:util"))
    compile(ideaSdkCoreDeps("intellij-core"))
    compile(ideaPluginDeps("gradle-tooling-api", plugin = "gradle"))
    compile(ideaPluginDeps("android", "common", "sdk-common", plugin = "android"))
}

configureKotlinProjectSourcesDefault()
configureKotlinProjectNoTests()

