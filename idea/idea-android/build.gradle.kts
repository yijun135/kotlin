
apply { plugin("kotlin") }

dependencies {
    compile(kotlinDep("reflect"))
    compile(project(":compiler:util"))
    compile(project(":compiler:light-classes"))
    compile(project(":compiler:frontend"))
    compile(project(":compiler:frontend.java"))
    compile(project(":idea"))
    compile(project(":idea:idea-core"))
    compile(project(":idea:ide-common"))
    compile(ideaSdkDeps("openapi", "idea"))
    compile(ideaSdkDeps("gradle-tooling-api", subdir = "plugins/gradle/lib"))
    compile(ideaSdkDeps("android", "common", "sdk-common", "layoutlib-api", subdir = "plugins/android/lib"))
    compile(preloadedDep("uast-common", "uast-java"))
}

configureKotlinProjectSourcesDefault()
configureKotlinProjectNoTests()

fixKotlinTaskDependencies()
