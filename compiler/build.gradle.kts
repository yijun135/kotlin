
import java.io.File
import proguard.gradle.ProGuardTask
import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.gradle.api.file.DuplicatesStrategy
import org.gradle.api.internal.plugins.DslObject
import org.gradle.api.publication.maven.internal.deployer.MavenRemoteRepository

buildscript {
    repositories {
        jcenter()
    }

    dependencies {
        classpath("com.github.jengelman.gradle.plugins:shadow:1.2.3")
        classpath("net.sf.proguard:proguard-gradle:5.3.1")
    }
}

apply { plugin("kotlin") }
apply { plugin("maven") }


// Set to false to disable proguard run on kotlin-compiler.jar. Speeds up the build
val shrink = true
val bootstrapBuild = false

val compilerManifestClassPath =
        if (bootstrapBuild) "kotlin-runtime-internal-bootstrap.jar kotlin-reflect-internal-bootstrap.jar kotlin-script-runtime-internal-bootstrap.jar"
        else "kotlin-runtime.jar kotlin-reflect.jar kotlin-script-runtime.jar"

//val ideaSdkCoreCfg = configurations.create("ideaSdk-core")
//val otherDepsCfg = configurations.create("other-deps")
val packedJars by configurations.creating
val proguardLibraryJarsCfg = configurations.create("library-jars")
val mainCfg = configurations.create("default_")
val packedCfg = configurations.create("packed")
//val withBootstrapRuntimeCfg = configurations.create("withBootstrapRuntime")

val compilerBaseName: String by rootProject.extra
val packedJarClassifier = "before-shrink"

val outputJar = File(buildDir, "libs", "$compilerBaseName.jar")

val javaHome = System.getProperty("java.home")

val buildLocalRepoPath: File by rootProject.extra

val compilerModules: Array<String> by rootProject.extra
val otherCompilerModules = compilerModules.filter { it != path }

dependencies {
    val compile by configurations
    val compileOnly by configurations
    val testCompile by configurations
    val testCompileOnly by configurations
    val testRuntime by configurations
    compileOnly(project(":compiler:cli"))
    compileOnly(project(":compiler:daemon-common"))
    compileOnly(project(":compiler:incremental-compilation-impl"))
    compileOnly(project(":build-common"))
    compileOnly(ideaSdkCoreDeps(*(rootProject.extra["ideaCoreSdkJars"] as Array<String>)))
    compileOnly(commonDep("org.fusesource.jansi", "jansi"))
    compileOnly(commonDep("jline"))

    testCompile(commonDep("junit:junit"))
    testCompile(project(":kotlin-test:kotlin-test-jvm"))
    testCompile(project(":kotlin-test:kotlin-test-junit"))
    testCompile(project(":compiler.tests-common"))
    testCompileOnly(project(":compiler:ir.ir2cfg"))
    testCompileOnly(project(":compiler:ir.tree")) // used for deepCopyWithSymbols call that is removed by proguard from the compiler TODO: make it more straightforward
    testCompile(ideaSdkDeps("openapi", "idea", "util", "asm-all", "commons-httpclient-3.1-patched"))
    // deps below are test runtime deps, but made test compile to split compilation and running to reduce mem req
    testCompile(project(":kotlin-stdlib"))
    testCompile(project(":kotlin-script-runtime"))
    testCompile(project(":kotlin-runtime"))
    testCompile(project(":kotlin-reflect"))
    testCompile(project(":plugins:android-extensions-compiler"))
    testCompile(project(":ant"))
    otherCompilerModules.forEach {
        testCompile(project(it))
    }
    testRuntime(ideaSdkCoreDeps("*.jar"))
    testRuntime(ideaSdkDeps("*.jar"))
    testRuntime(project(":prepare:compiler", configuration = "default"))

    buildVersion()

}

configureKotlinProjectSources(
        "compiler/daemon/src",
        "compiler/conditional-preprocessor/src",
        sourcesBaseDir = rootDir)
configureKotlinProjectResources("idea/src", sourcesBaseDir = rootDir) {
    include("META-INF/extensions/common.xml",
            "META-INF/extensions/kotlin2jvm.xml",
            "META-INF/extensions/kotlin2js.xml")
}
configureKotlinProjectTests("tests")

testsJar {}

tasks.withType<Test> {
    dependsOnTaskIfExistsRec("dist", project = rootProject)
    dependsOn(":prepare:mock-runtime-for-test:dist")
    workingDir = rootDir
    systemProperty("idea.is.unit.test", "true")
    environment("NO_FS_ROOTS_ACCESS_CHECK", "true")
    systemProperty("kotlin.test.script.classpath", the<JavaPluginConvention>().sourceSets.getByName("test").output.classesDirs.joinToString(File.pathSeparator))
    jvmArgs("-ea", "-XX:+HeapDumpOnOutOfMemoryError", "-Xmx1200m", "-XX:+UseCodeCacheFlushing", "-XX:ReservedCodeCacheSize=128m", "-Djna.nosys=true")
    maxHeapSize = "1200m"
    ignoreFailures = true
}

