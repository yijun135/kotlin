
apply { plugin("kotlin") }

dependencies {
    compile(project(":compiler:util"))
    compile(project(":jps-plugin"))
    compile(project(":plugins:android-extensions-compiler"))
    compile(ideaSdkDeps("android-jps-plugin", subdir = "plugins/android/lib/jps"))
}

configureKotlinProjectSourcesDefault()
configureKotlinProjectNoTests()

fixKotlinTaskDependencies()
