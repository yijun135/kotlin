
apply { plugin("kotlin") }

dependencies {
    compile(project(":build-common"))
    compile(project(":compiler:cli.cli-common"))
    compile(project(":compiler:preloader"))
    compile(project(":compiler:frontend.java"))
    compile(project(":compiler:daemon.daemon-common"))
    compile(project(":compiler:daemon.daemon-client"))
    compile(project(":compiler:util"))
}

configureKotlinProjectSourcesDefault()
configureKotlinProjectNoTests()

fixKotlinTaskDependencies()
