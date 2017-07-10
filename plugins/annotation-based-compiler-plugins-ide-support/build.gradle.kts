
apply { plugin("kotlin") }

dependencies {
    compile(project(":compiler:util"))
    compile(project(":compiler:frontend"))
    compile(project(":compiler:cli-common"))
    compile(project(":idea"))
    compile(project(":idea:idea-jps-common"))
    compile(ideaPluginDeps("maven", plugin = "maven"))
    compile(ideaPluginDeps("gradle-tooling-api", "gradle", plugin = "gradle"))
    compile(ideaSdkDeps("openapi", "idea"))
}

configureKotlinProjectSourcesDefault()
configureKotlinProjectNoTests()

fixKotlinTaskDependencies()
