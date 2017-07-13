
import org.gradle.api.tasks.compile.JavaCompile
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jetbrains.kotlin.serialization.builtins.BuiltInsSerializer
import java.io.File

apply { plugin("kotlin") }

val builtinsSrc = File(rootDir, "core", "builtins", "src")
val builtinsNative = File(rootDir, "core", "builtins", "native")
val builtinsSerialized = File(rootProject.extra["distDir"].toString(), "builtins")
val builtinsJar = File(buildDir, "builtins.jar")

dependencies {
    compile(protobufLite())
    compile(files(builtinsSerialized))
}

configureKotlinProjectSources("core/builtins/src", "core/runtime.jvm/src", sourcesBaseDir = rootDir)
configureKotlinProjectResources(listOf(builtinsSerialized))
configureKotlinProjectNoTests()

val serialize = task("serialize") {
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

tasks.withType<JavaCompile> {
    dependsOn(protobufLiteTask)
    dependsOn(serialize)
}

tasks.withType<KotlinCompile> {
    dependsOn(protobufLiteTask)
    dependsOn(serialize)
}

