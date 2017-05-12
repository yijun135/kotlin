
import java.io.File

apply { plugin("kotlin") }

dependencies {
    compile(project(":core:builtins"))
    compile(kotlinDep("stdlib"))
}

configureKotlinProjectSourcesDefault()
configureKotlinProjectNoTests()

fixKotlinTaskDependencies()
