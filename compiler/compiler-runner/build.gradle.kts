
apply { plugin("kotlin") }

dependencies {
    compile(project(":build-common"))
    compile(project(":compiler:cli-common"))
    compile(project(":compiler:preloader"))
    compile(project(":compiler:frontend.java"))
    compile(project(":compiler:daemon-common"))
    compile(project(":compiler:daemon-client"))
    compile(project(":compiler:util"))
}

configureKotlinProjectSourcesDefault()
configureKotlinProjectNoTests()

fixKotlinTaskDependencies()
