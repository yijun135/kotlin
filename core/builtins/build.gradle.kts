
import org.gradle.api.tasks.compile.JavaCompile
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jetbrains.kotlin.serialization.builtins.BuiltInsSerializer
import java.io.File

apply { plugin("kotlin") }

val builtinsSrc = File(rootDir, "core/builtins/src")
val builtinsNative = File(rootDir, "core/builtins/native")
val builtinsSerialized = File(rootProject.extra["distDir"].toString(), "builtins")
val builtinsJar = File(buildDir, "builtins.jar")

dependencies {
    compile(protobufLite())
    compile(files(builtinsSerialized))
}

configureKotlinProjectSources("core/builtins/src", "core/runtime.jvm/src", sourcesBaseDir = rootDir)
configureKotlinProjectResources(listOf(builtinsSerialized))
configureKotlinProjectNoTests()

val serialize = task("internal.serialize") {
    val outDir = builtinsSerialized
    val inDirs = arrayOf(builtinsSrc, builtinsNative)
    outputs.file(outDir)
    inputs.files(*inDirs)
    doLast {
        System.setProperty("kotlin.colors.enabled", "false")
        BuiltInsSerializer(dependOnOldBuiltIns = false)
                .serialize(outDir, inDirs.asList(), listOf()) { totalSize, totalFiles ->
                    println("Total bytes written: $totalSize to $totalFiles files")
                }
    }
}


configure<JavaPluginConvention> {
    sourceSets.getByName("main").apply {
        resources.setSrcDirs(listOf(builtinsSerialized))
    }
}

task("sourcesets") {
    doLast {
        the<JavaPluginConvention>().sourceSets.forEach { ss ->
            println("--> ${ss.name}.java: ${ss.java.srcDirs.joinToString()}")
            ss.resources.srcDirs.let {
                if (it.isNotEmpty())
                    println("--> ${ss.name}.resources: ${it.joinToString()}")
            }
        }
    }
}

tasks.withType<JavaCompile> {
    dependsOn(protobufLiteTask)
    dependsOn(serialize)
}

tasks.withType<KotlinCompile> {
    dependsOn(protobufLiteTask)
    dependsOn(serialize)
}

fixKotlinTaskDependencies()
