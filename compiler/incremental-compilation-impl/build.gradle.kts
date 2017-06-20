
apply { plugin("kotlin") }

dependencies {
    compile(project(":core"))
    compile(project(":compiler:util"))
    compile(project(":compiler:frontend"))
    compile(project(":compiler:frontend.java"))
    compile(project(":compiler:cli"))
    compile(project(":build-common"))
}

configureKotlinProjectSourcesDefault()
configureKotlinProjectTestsDefault()

fixKotlinTaskDependencies()
