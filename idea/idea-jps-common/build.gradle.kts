
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

buildscript {
    repositories {
        mavenLocal()
        maven { setUrl(rootProject.extra["repo"]) }
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
    compile(project(":kotlin-stdlib"))
    compile(project(":core:reflection.jvm"))
    compile(project(":compiler"))
    compile(ideaSdkCoreDeps("intellij-core", "util"))
    buildVersion()
}

//configureKotlinProjectSources("idea/idea-jps-common/src", sourcesBaseDir = rootDir)
configureKotlinProjectNoTests()

tasks.withType<KotlinCompile> {
    kotlinOptions.freeCompilerArgs = listOf("-module-name", "kotlin-idea.jps-common")
}

fixKotlinTaskDependencies()
