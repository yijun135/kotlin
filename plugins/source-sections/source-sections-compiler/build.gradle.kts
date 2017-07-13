
import org.gradle.jvm.tasks.Jar

apply { plugin("kotlin") }

dependencies {
    compile(project(":compiler:frontend"))
    compile(project(":compiler:frontend.script"))
    compile(project(":compiler:plugin-api"))
    testCompile(project(":compiler.tests-common"))
    testCompile(commonDep("junit:junit"))
    testCompile(project(":compiler:util"))
    testCompile(project(":compiler:cli"))
    testCompile(project(":compiler:cli-common"))
    testCompile(project(":compiler:frontend.java"))
    testCompile(project(":compiler:daemon-common"))
    testCompile(project(":compiler:daemon-client"))
}

configureKotlinProjectSourcesDefault()
configureKotlinProjectTestsDefault()

val jar: Jar by tasks
jar.apply {
    setupRuntimeJar("Kotlin SourceSections Compiler Plugin")
    from(fileTree("$projectDir/src")) { include("META-INF/**") }
    archiveName = "kotlin-source-sections-compiler-plugin.jar"
}

dist {
    from(jar)
}

tasks.withType<Test> {
    dependsOnTaskIfExistsRec("dist", project = rootProject)
    workingDir = rootDir
    systemProperty("idea.is.unit.test", "true")
    environment("NO_FS_ROOTS_ACCESS_CHECK", "true")
    ignoreFailures = true
}

