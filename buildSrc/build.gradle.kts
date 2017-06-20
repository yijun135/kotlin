buildscript {

    project.extra["kotlinVersion"] = file("../kotlin-bootstrap-version.txt").readText().trim()

    configure(listOf(repositories, project.repositories)) {
        gradleScriptKotlin()
    }

    dependencies {
        classpath(kotlinModule("gradle-plugin", version = project.extra["kotlinVersion"] as String))
    }
}

apply {
    plugin("kotlin")
}

dependencies {
    compile(kotlinModule("stdlib", version = project.extra["kotlinVersion"] as String))
    compile(gradleApi())
    compile(gradleScriptKotlinApi())
}
