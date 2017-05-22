
apply { plugin("kotlin") }

dependencies {
    compile(project(":kotlin-stdlib"))
    compile(project(":compiler:cli.cli-common"))
    compile(ideaSdkDeps("gradle-tooling-api", "gradle", subdir = "plugins/gradle/lib"))
    buildVersion()
}

configureKotlinProjectSourcesDefault()
configureKotlinProjectNoTests()

fixKotlinTaskDependencies()
