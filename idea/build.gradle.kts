
apply { plugin("kotlin") }

dependencies {
    compile(project(":kotlin-stdlib"))
    compile(project(":core"))
    compile(project(":compiler:backend"))
    compile(project(":compiler:cli.cli-common"))
    compile(project(":compiler:daemon.daemon-client"))
    compile(project(":compiler:frontend"))
    compile(project(":compiler:frontend.java"))
    compile(project(":compiler:frontend.script"))
    compile(project(":js:js.frontend"))
    compile(project(":js:js.serializer"))
    compile(project(":compiler:light-classes"))
    compile(project(":compiler:util"))
    compile(project(":eval4j"))
    compile(project(":j2k"))
    compile(project(":idea:formatter"))
    compile(project(":idea:idea-core"))
    compile(project(":idea:ide-common"))
    compile(project(":idea:idea-jps-common"))
    compile(project(":idea:kotlin-gradle-tooling"))
    compile(project(":plugins:uast-kotlin"))
    compile(project(":plugins:uast-kotlin-idea"))
    compile(ideaSdkCoreDeps("intellij-core", "util"))
    compile(ideaSdkDeps("openapi", "idea", "velocity", "boot", "gson", "swingx-core"))
    compile(ideaSdkDeps("gradle-tooling-api", "gradle", subdir = "plugins/gradle/lib"))
    compile(ideaSdkDeps("idea-junit", subdir = "plugins/junit/lib"))
    compile(ideaSdkDeps("IntelliLang", subdir = "plugins/intelliLang/lib"))
    compile(ideaSdkDeps("testng", "testng-plugin", subdir = "plugins/testng/lib"))
    compile(ideaSdkDeps("copyright", subdir = "plugins/copyright/lib"))
    compile(ideaSdkDeps("properties", subdir = "plugins/properties/lib"))
    compile(ideaSdkDeps("java-i18n", subdir = "plugins/java-i18n/lib"))
    compile(ideaSdkDeps("java-decompiler", subdir = "plugins/java-decompiler/lib"))
    compile(ideaSdkDeps("Groovy", subdir = "plugins/Groovy/lib"))
    compile(ideaSdkDeps("maven", subdir = "plugins/maven/lib"))
    compile(ideaSdkDeps("coverage", "coverage-agent", subdir = "plugins/coverage/lib"))
    compile(preloadedDep("markdown", "kotlinx-coroutines-core"))
    buildVersion()
}

configureKotlinProjectSources("idea/src",
                              "idea/idea-maven/src",
                              "idea/idea-completion/src",
                              "idea/idea-live-templates/src",
                              "idea/lint-idea/src",
                              "idea/idea-repl/src",
                              "plugins/lint-checks/src",
                              sourcesBaseDir = rootDir)
configureKotlinProjectNoTests()

fixKotlinTaskDependencies()

