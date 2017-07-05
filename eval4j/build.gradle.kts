import org.jetbrains.kotlin.utils.PathUtil

apply { plugin("kotlin") }

dependencies {
    compile(project(":kotlin-stdlib"))
    compile(project(":kotlin-reflect"))
    compile(project(":compiler:backend"))
    compile(ideaSdkDeps("asm-all"))
//    compile(files(PathUtil.getJdkClassesRootsFromCurrentJre())) // TODO: make this one work instead of the nex one, since it contains more universal logic
    compile(files("${System.getProperty("java.home")}/../lib/tools.jar"))
    buildVersion()
//    testCompile(project(":idea:idea-test-framework"))
//    testCompile(commonDep("junit:junit"))
//    testCompile(project(":kotlin-test:kotlin-test-jvm"))
}

configureKotlinProjectSourcesDefault()
configureKotlinProjectNoTests()
//configureKotlinProjectTestsDefault()
//
//tasks.withType<Test> {
//    workingDir = rootDir
//    systemProperty("idea.is.unit.test", "true")
//    environment("NO_FS_ROOTS_ACCESS_CHECK", "true")
//    ignoreFailures = true
//}

fixKotlinTaskDependencies()
