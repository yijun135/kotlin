
buildscript {
    repositories {
        jcenter()
    }

    dependencies {
        classpath("com.github.jengelman.gradle.plugins:shadow:1.2.3")
    }
}

val mainCfg = configurations.create("default")

val projectsClassesToPack = listOf(
    //android-extensions-jps
    ":build-common",
    //cli-common
    //compiler-runner
    ":compiler:daemon:daemon-client",
    //daemon-common
    //deserialization
    //idea-jps-common
    //jps-plugin
    //preloader
    ":compiler:util",
    ":core:util.runtime"
)

dependencies {

}
