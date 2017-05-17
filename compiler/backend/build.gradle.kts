
apply { plugin("kotlin") }

dependencies {
    compile(project(":compiler:util"))
    compile(project(":compiler:backend-common"))
    compile(project(":compiler:frontend"))
    compile(project(":compiler:frontend.java"))
    compile(project(":compiler:ir.tree"))
    compile(project(":compiler:ir.psi2ir"))
    compile(project(":compiler:serialization"))
}

configureKotlinProjectSources("backend/src", "ir/backend.jvm/src", sourcesBaseDir = File(rootDir, "compiler"))
configureKotlinProjectNoTests()

fixKotlinTaskDependencies()
