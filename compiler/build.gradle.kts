
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.tasks.compile.JavaCompile
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.io.File

buildscript {
    repositories {
        mavenLocal()
        maven { setUrl(rootProject.extra["repo"]) }
        mavenCentral()
    }

    dependencies {
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:${rootProject.extra["kotlinVersion"]}")
    }
}

apply { plugin("kotlin") }

repositories {
    mavenLocal()
    maven { setUrl(rootProject.extra["repo"]) }
    mavenCentral()
}

dependencies {
//    compile(project(":prepare:runtime", configuration = "default"))
//    compile(project(":kotlin-test:kotlin-test-jvm"))
//    compile(project(":prepare:reflect", configuration = "default"))
//    compile(project(":core:script.runtime"))
    compile(project(":core:util.runtime"))
    compile(project(":compiler:util"))
    compile(fileTree(mapOf("dir" to "$rootDir/ideaSDK/core", "include" to "*.jar")))
    compile(commonDep("com.google.protobuf:protobuf-java"))
//    compile(fileTree(mapOf("dir" to "$rootDir/lib", "include" to "*.jar"))) // direct references below
    compile(commonDep("javax.inject"))
    compile(commonDep("com.google.code.findbugs", "jsr305"))
    compile(commonDep("com.github.spullara.cli-parser", "cli-parser"))
    compile(commonDep("org.fusesource.jansi", "jansi"))
    compile(commonDep("io.javaslang","javaslang"))
    compile(commonDep("jline"))
    compile(files("$rootDir/ideaSDK/jps/jps-model.jar"))
    compile(kotlinDep("stdlib"))
    compile(kotlinDep("script-runtime"))
    compile(kotlinDep("reflect"))
}

configure<JavaPluginConvention> {
    sourceSets.getByName("main").apply {
        listOf( "compiler/backend/src",
                "compiler/backend-common/src",
                "compiler/ir/backend.common/src",
                "compiler/ir/backend.jvm/src",
                "compiler/ir/ir.psi2ir/src",
                "compiler/ir/ir.tree/src",
                "compiler/cli/cli-common/src",
                "compiler/container/src",
                "compiler/frontend/src",
                "compiler/frontend.script/src",
                "compiler/resolution/src",
                "compiler/frontend.java/src",
                "compiler/light-classes/src",
                "compiler/plugin-api/src",
                "compiler/daemon/daemon-common/src",
                "build-common/src",
                "compiler/serialization/src",
                "js/js.ast/src",
                "js/js.translator/src",
                "js/js.frontend/src",
                "js/js.inliner/src",
                "js/js.parser/src",
                "js/js.serializer/src")
        .map { File(rootDir, it) }
        .let { java.setSrcDirs(it) }
//        println(compileClasspath.joinToString("\n    ", prefix = "classpath =\n    ") { it.canonicalFile.relativeTo(rootDir).path })
    }
    sourceSets.getByName("test").apply {
        java.setSrcDirs(emptyList<File>())
    }
}

//tasks.withType<JavaCompile> {
//    // TODO: automatic from deps
//    dependsOn(":prepare:runtime:prepare")
//    dependsOn(":prepare:reflect:prepare")
//}

tasks.withType<KotlinCompile> {
//    dependsOn(":prepare:runtime:prepare")
//    dependsOn(":prepare:reflect:prepare")
    kotlinOptions.freeCompilerArgs = listOf("-Xallow-kotlin-package", "-module-name", "kotlin-compiler")
}

fixKotlinTaskDependencies()

//tasks.withType<Jar> {
//    enabled = false
//}
