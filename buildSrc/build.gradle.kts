buildscript {
    configure(listOf(repositories, project.repositories)) {
        gradleScriptKotlin()
    }

    dependencies {
        classpath(kotlinModule("gradle-plugin"))
    }
}

apply {
    plugin("kotlin")
    plugin("groovy")
}

dependencies {
    compile(gradleScriptKotlinApi())
}
